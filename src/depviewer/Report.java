package depviewer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Output of {@code depviewer check}: the text report (FR-037), the versioned JSON report (FR-038), and the views
 * with resolved pattern assignments and violations that are embedded in the viewer (FR-041, FR-042).
 */
final class Report {
    static final int FORMAT = 1;

    private Report() {}

    /** Sorted by file and line, as the text report lists them; elements without a file last. */
    static List<Rules.Violation> sorted(List<Rules.Violation> vs, Rules.Graph g) {
        List<Rules.Violation> out = new ArrayList<>(vs);
        out.sort(Comparator.comparing((Rules.Violation v) -> path(v, g) == null ? 1 : 0)
                .thenComparing(v -> Objects.toString(path(v, g), ""))
                .thenComparingInt(v -> line(v, g))
                .thenComparing(v -> v.view).thenComparing(v -> v.rule).thenComparing(v -> v.fingerprint));
        return out;
    }

    /** The file a violation is reported at: the source of its first reference, or the ungrouped element's first file. */
    static String path(Rules.Violation v, Rules.Graph g) {
        if (v.members != null) return v.refs.isEmpty() ? null : g.path(first(v, g).from);
        if (v.to < 0) return Rules.firstPath(g, v.from);
        return g.path(v.from);
    }

    static int line(Rules.Violation v, Rules.Graph g) {
        if (v.members != null) {
            Rules.Ref r = v.refs.isEmpty() ? null : first(v, g);
            return r == null || r.lines.isEmpty() ? 0 : r.lines.stream().min(Integer::compare).get();
        }
        if (v.to < 0) return g.line(v.from);
        List<Integer> ls = v.lines();
        return ls.isEmpty() ? 0 : ls.get(0);
    }

    /** A cycle's first reference by file and line. */
    static Rules.Ref first(Rules.Violation v, Rules.Graph g) {
        return v.refs.stream().min(Comparator.comparing((Rules.Ref r) -> Objects.toString(g.path(r.from), "￿"))
                .thenComparingInt(r -> r.lines.isEmpty() ? 0 : r.lines.stream().min(Integer::compare).get())).get();
    }

    static String label(int i, Rules.Graph g, String[] group) {
        return group[i] != null ? group[i] : g.ext[i] ? "(external)" : "(ungrouped)";
    }

    static String lines(List<Integer> ls) {
        List<Integer> l = new ArrayList<>(ls);
        l.sort(null);
        StringBuilder sb = new StringBuilder();
        for (int x : l) sb.append(sb.length() > 0 ? "," : "").append(x);
        return sb.toString();
    }

    /** One header line per violation, then one indented line per reference with its kind. */
    static String text(List<Rules.Violation> vs, Rules.Graph g, Map<String, String[]> groups) {
        StringBuilder sb = new StringBuilder();
        for (Rules.Violation v : sorted(vs, g)) {
            String[] group = groups.get(v.view);
            String p = path(v, g);
            int l = line(v, g);
            String at = v.members == null && v.to >= 0 && !v.lines().isEmpty() ? ":" + lines(v.lines()) : l > 0 ? ":" + l : "";
            sb.append(p == null ? "-" : p).append(at).append("  ").append(v.severity).append("  ").append(v.rule).append("  ");
            if (v.members != null) {
                sb.append("cycle ");
                for (int i = 0; i < v.members.size(); i++) sb.append(i > 0 ? ", " : "").append(g.name(v.members.get(i)));
            } else if (v.to < 0) {
                int i = v.from;
                sb.append(Architecture.TYPE_KINDS.contains(g.kind[i]) ? "type" : g.kind[i]).append(' ')
                        .append(g.name(i)).append(" is in no group");
            } else {
                sb.append(label(v.from, g, group)).append(" -> ").append(label(v.to, g, group));
            }
            if (v.baselined) sb.append("  baselined");
            sb.append('\n');
            List<Rules.Ref> refs = new ArrayList<>(v.refs);
            if (v.members != null) refs.sort(Comparator.comparing((Rules.Ref r) -> Objects.toString(g.path(r.from), "")).thenComparing(r -> g.name(r.from)).thenComparing(r -> g.name(r.to)));
            for (Rules.Ref r : refs) {
                sb.append("    ").append(g.name(r.from)).append(" -> ").append(g.name(r.to)).append(" (").append(r.kind).append(')');
                if (v.members != null && g.path(r.from) != null) sb.append("  ").append(g.path(r.from)).append(r.lines.isEmpty() ? "" : ":" + lines(r.lines));
                sb.append('\n');
            }
        }
        return sb.toString();
    }

    static Map<String, Object> element(int i, Rules.Graph g, String[] group) {
        Map<String, Object> m = new LinkedHashMap<>();
        if (g.key[i] != null) m.put("key", g.key[i]);
        m.put("q", g.name(i));
        if (g.path(i) != null) m.put("path", g.path(i));
        if (group != null && group[i] != null) m.put("group", group[i]);
        return m;
    }

    /** The JSON report, format 1. */
    static Map<String, Object> json(Map<?, ?> model, List<Rules.Violation> vs, Rules.Graph g, Map<String, String[]> groups,
                                    List<String> resolved, int overlaps) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("format", FORMAT);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("title", model.get("title"));
        m.put("generated", model.get("generated"));
        if (model.containsKey("analysis")) m.put("analysis", model.get("analysis"));
        r.put("model", m);
        Map<String, Object> sum = new LinkedHashMap<>();
        sum.put("error", vs.stream().filter(v -> !v.baselined && v.severity.equals("error")).count());
        sum.put("warning", vs.stream().filter(v -> !v.baselined && v.severity.equals("warning")).count());
        sum.put("baselined", vs.stream().filter(v -> v.baselined).count());
        sum.put("resolved", resolved.size());
        sum.put("overlaps", overlaps);
        r.put("summary", sum);
        List<Object> list = new ArrayList<>();
        for (Rules.Violation v : sorted(vs, g)) {
            String[] group = groups.get(v.view);
            Map<String, Object> o = new LinkedHashMap<>();
            o.put("rule", v.rule);
            o.put("view", v.view);
            o.put("severity", v.severity);
            o.put("kind", v.kind);
            if (v.members != null) {
                List<Object> ms = new ArrayList<>();
                for (int i : v.members) ms.add(element(i, g, group));
                o.put("members", ms);
            } else {
                o.put("from", element(v.from, g, group));
                if (v.to >= 0) o.put("to", element(v.to, g, group));
            }
            if (v.members == null && v.to >= 0) o.put("lines", v.lines());
            if (!v.refs.isEmpty()) o.put("count", v.count());
            o.put("exact", v.exact);
            o.put("baselined", v.baselined);
            o.put("fingerprint", v.fingerprint);
            if (!v.refs.isEmpty()) {
                List<Object> refs = new ArrayList<>();
                for (Rules.Ref ref : v.refs) {
                    Map<String, Object> x = new LinkedHashMap<>();
                    x.put("kind", ref.kind);
                    if (v.members == null && ref.to != v.to) x.put("to", g.name(ref.to));
                    if (v.members != null) {
                        x.put("from", g.name(ref.from));
                        x.put("to", g.name(ref.to));
                        if (g.path(ref.from) != null) x.put("path", g.path(ref.from));
                    }
                    List<Integer> ls = new ArrayList<>(ref.lines);
                    ls.sort(null);
                    x.put("lines", ls);
                    x.put("count", ref.count);
                    refs.add(x);
                }
                o.put("refs", refs);
            }
            list.add(o);
        }
        r.put("violations", list);
        r.put("resolved", resolved);
        return r;
    }

    // ---------------------------------------------------------------- embedded in the viewer

    /**
     * The architecture file as the viewer gets it: each view with patterns gains {@code matched} (the persisted view
     * keys whose group comes from a pattern, {@code ""} for an element a {@code !} selector keeps ungrouped), and each
     * checked view gains {@code violations} (rule, severity, exact, kind and the edges as node index pairs).
     * {@code T:} keys are left out: the viewer assigns files, not types.
     */
    static String embed(Map<?, ?> root, List<Object> nodes, Map<String, List<Rules.Violation>> violations, Rules.Graph g) {
        Map<String, Object> out = new LinkedHashMap<>();
        for (Map.Entry<?, ?> e : root.entrySet()) out.put(String.valueOf(e.getKey()), e.getValue());
        List<Object> views = new ArrayList<>();
        Object vs = root.get("views");
        if (vs instanceof List) for (Object o : (List<?>) vs) {
            Map<String, Object> view = new LinkedHashMap<>();
            for (Map.Entry<?, ?> e : ((Map<?, ?>) o).entrySet()) view.put(String.valueOf(e.getKey()), e.getValue());
            view.remove("matched");
            view.remove("violations");
            view.remove("excluded");
            if (hasPatterns(view)) {
                view.put("matched", matched(view, nodes, g));
                Map<String, Object> ex = excluded(view, nodes, g);
                if (!ex.isEmpty()) view.put("excluded", ex);
            }
            List<Rules.Violation> list = violations.get(String.valueOf(view.get("id")));
            if (list != null) {
                List<Object> vl = new ArrayList<>();
                for (Rules.Violation v : list) {
                    if (v.refs.isEmpty()) continue;
                    Map<String, Object> x = new LinkedHashMap<>();
                    x.put("rule", v.rule);
                    x.put("severity", v.severity);
                    x.put("exact", v.exact);
                    x.put("kind", v.kind);
                    Set<List<Integer>> pairs = new LinkedHashSet<>();
                    for (Rules.Ref r : v.refs) pairs.add(List.of(r.from, r.to));
                    x.put("edges", new ArrayList<>(pairs));
                    vl.add(x);
                }
                view.put("violations", vl);
            }
            views.add(view);
        }
        out.put("views", views);
        return Json.write(out);
    }

    static boolean hasPatterns(Map<?, ?> view) {
        Object gs = view.get("groups");
        if (!(gs instanceof List)) return false;
        for (Object gr : (List<?>) gs)
            if (gr instanceof Map && ((Map<?, ?>) gr).get("match") instanceof List && !((List<?>) ((Map<?, ?>) gr).get("match")).isEmpty()) return true;
        return false;
    }

    /** Per persisted view key: the groups whose {@code !} selectors keep the element out, so it never inherits them. */
    static Map<String, Object> excluded(Map<?, ?> view, List<Object> nodes, Rules.Graph g) {
        Architecture.Resolution r = Architecture.resolve(view, nodes);
        Map<String, Object> out = new LinkedHashMap<>();
        for (int i = 0; i < g.n; i++) {
            if (Architecture.MEMBER_KINDS.contains(g.kind[i]) || Architecture.TYPE_KINDS.contains(g.kind[i]) || g.key[i] == null) continue;
            List<String> gs = new ArrayList<>();
            for (Map.Entry<String, java.util.BitSet> e : r.excluded.entrySet()) if (e.getValue().get(i)) gs.add(e.getKey());
            gs.sort(null);
            if (!gs.isEmpty()) out.put(g.key[i], gs);
        }
        return out;
    }

    /** Pattern assignments for the viewer: where the group from patterns alone differs from the nearest keyed ancestor's. */
    static Map<String, Object> matched(Map<?, ?> view, List<Object> nodes, Rules.Graph g) {
        Map<String, Object> patternsOnly = new LinkedHashMap<>();
        for (Map.Entry<?, ?> e : view.entrySet()) if (!"assign".equals(e.getKey())) patternsOnly.put(String.valueOf(e.getKey()), e.getValue());
        String[] group = Architecture.resolve(patternsOnly, nodes).group;
        String[] vk = new String[g.n];
        int[] keyed = new int[g.n];   // nearest ancestor with a viewer key
        Map<String, Object> out = new LinkedHashMap<>();
        for (int i = 0; i < g.n; i++) {
            int p = g.parent[i];
            keyed[i] = p < 0 ? -1 : vk[p] != null ? p : keyed[p];
            String k = Architecture.MEMBER_KINDS.contains(g.kind[i]) || Architecture.TYPE_KINDS.contains(g.kind[i]) ? null : g.key[i];
            vk[i] = k;
            if (k == null) continue;
            String inherited = keyed[i] >= 0 ? group[keyed[i]] : null;
            if (!Objects.equals(group[i], inherited)) out.put(k, group[i] == null ? "" : group[i]);
        }
        return out;
    }
}
