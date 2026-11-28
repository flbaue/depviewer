package depviewer;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Architecture rules (spec FR-029 to FR-034): validates the {@code rules} of a view and checks a model against them.
 * Each violation names the innermost elements of the model's edges, as {@link Architecture#resolve} groups them.
 */
final class Rules {
    static final Set<String> KINDS = Set.of("layers", "forbid", "allow-only", "independent", "no-cycles", "no-ungrouped");
    static final Set<String> LEVELS = Set.of("package", "file", "type");

    private Rules() {}

    // ---------------------------------------------------------------- validation

    /** Rejects a view whose rules cannot be checked; the message names the view and the rule. */
    static void validate(String file, Map<?, ?> view) throws Architecture.Invalid {
        Object rs = view.get("rules");
        if (rs == null) return;
        String where = file + ": view \"" + view.get("id") + "\": ";
        if (!(rs instanceof List)) throw new Architecture.Invalid(where + "\"rules\" must be an array");
        Set<String> groups = new HashSet<>();
        if (view.get("groups") instanceof List)
            for (Object g : (List<?>) view.get("groups")) if (g instanceof Map && ((Map<?, ?>) g).get("id") instanceof String) groups.add((String) ((Map<?, ?>) g).get("id"));
        Set<String> ids = new HashSet<>();
        int n = 0;
        for (Object o : (List<?>) rs) {
            n++;
            if (!(o instanceof Map)) throw new Architecture.Invalid(where + "rule " + n + " must be an object");
            Map<?, ?> r = (Map<?, ?>) o;
            if (!(r.get("id") instanceof String) || ((String) r.get("id")).isEmpty()) throw new Architecture.Invalid(where + "rule " + n + ": \"id\" is required");
            String id = (String) r.get("id");
            String at = where + "rule \"" + id + "\": ";
            if (!ids.add(id)) throw new Architecture.Invalid(at + "the id is used twice in this view");
            Object kind = r.get("kind");
            if (!KINDS.contains(kind))
                throw new Architecture.Invalid(at + "unknown kind \"" + kind + "\" (layers, forbid, allow-only, independent, no-cycles or no-ungrouped)");
            Object sev = r.get("severity");
            if (sev != null && !"error".equals(sev) && !"warning".equals(sev)) throw new Architecture.Invalid(at + "\"severity\" must be error or warning");
            for (String b : List.of("tests", "strict"))
                if (r.containsKey(b) && !(r.get(b) instanceof Boolean)) throw new Architecture.Invalid(at + "\"" + b + "\" must be true or false");
            if (r.containsKey("edgeKinds")) {
                if (!(r.get("edgeKinds") instanceof List)) throw new Architecture.Invalid(at + "\"edgeKinds\" must be an array");
                for (Object k : (List<?>) r.get("edgeKinds"))
                    if (!Model.EDGE_KINDS.contains(k)) throw new Architecture.Invalid(at + "unknown dependency kind \"" + k + "\" in \"edgeKinds\" (" + String.join(", ", Model.EDGE_KINDS) + ")");
            }
            switch ((String) kind) {
                case "layers":
                    for (String g : strings(r, "order", at, 1)) group(g, groups, at);
                    break;
                case "forbid": case "allow-only":
                    for (String f : List.of("from", "to")) for (String s : strings(r, f, at, 1)) endpoint(s, groups, at);
                    break;
                case "independent":
                    for (String g : strings(r, "groups", at, 2)) group(g, groups, at);
                    break;
                case "no-cycles":
                    if (!LEVELS.contains(r.get("level"))) throw new Architecture.Invalid(at + "\"level\" must be package, file or type");
                    if (r.containsKey("within")) {
                        if (!(r.get("within") instanceof String)) throw new Architecture.Invalid(at + "\"within\" must be a group id");
                        group((String) r.get("within"), groups, at);
                    }
                    break;
                default: // no-ungrouped
                    if (r.containsKey("scope")) {
                        if (!(r.get("scope") instanceof String)) throw new Architecture.Invalid(at + "\"scope\" must be a selector");
                        selector((String) r.get("scope"), at);
                    }
            }
        }
    }

    /** A field that holds one string or an array of strings; at least {@code min} of them. */
    private static List<String> strings(Map<?, ?> r, String field, String at, int min) throws Architecture.Invalid {
        Object v = r.get(field);
        List<String> out = new ArrayList<>();
        if (v instanceof String) out.add((String) v);
        else if (v instanceof List) for (Object o : (List<?>) v) {
            if (!(o instanceof String)) throw new Architecture.Invalid(at + "\"" + field + "\" must hold strings");
            out.add((String) o);
        }
        else if (v != null) throw new Architecture.Invalid(at + "\"" + field + "\" must be a string or an array of strings");
        if (out.size() < min) throw new Architecture.Invalid(at + "\"" + field + "\" needs at least " + min + (min == 1 ? " entry" : " entries"));
        return out;
    }

    private static void group(String g, Set<String> groups, String at) throws Architecture.Invalid {
        if (!groups.contains(g)) throw new Architecture.Invalid(at + "unknown group \"" + g + "\"");
    }

    private static void endpoint(String s, Set<String> groups, String at) throws Architecture.Invalid {
        if (s.startsWith("ext:")) selector(s, at);
        else group(s, groups, at);
    }

    private static void selector(String s, String at) throws Architecture.Invalid {
        try {
            Architecture.Selector.parse(s);
        } catch (IllegalArgumentException e) {
            throw new Architecture.Invalid(at + e.getMessage());
        }
    }

    // ---------------------------------------------------------------- the model

    /** The parts of a model JSON that rules need, indexed by node. */
    static final class Graph {
        final List<Object> nodes;
        final int n;
        final int[] parent, file;   // file: nearest file ancestor-or-self, -1 for externals and containers
        final String[] kind, q, key, id;   // id: unambiguous, for fingerprints (own key, or the type's key + "#" + member)
        final boolean[] test, ext;
        final int[][] edges;                 // {from, to, kind, count}
        final List<List<Integer>> lines = new ArrayList<>();
        final List<String> edgeKinds;

        @SuppressWarnings("unchecked")
        Graph(Map<?, ?> model) {
            nodes = (List<Object>) model.get("nodes");
            n = nodes.size();
            parent = new int[n]; file = new int[n];
            kind = new String[n]; q = new String[n]; key = new String[n]; id = new String[n];
            test = new boolean[n]; ext = new boolean[n];
            String[] module = new String[n];
            for (int i = 0; i < n; i++) {
                Map<?, ?> m = (Map<?, ?>) nodes.get(i);
                parent[i] = m.get("p") instanceof Double ? ((Double) m.get("p")).intValue() : -1;
                int p = parent[i];
                kind[i] = (String) m.get("k");
                q[i] = Architecture.name(m);
                module[i] = p >= 0 ? module[p] : (String) m.get("n");
                String k = Architecture.key(m, module[i]);
                key[i] = k != null ? k : p >= 0 ? key[p] : null;   // members: their type's key
                id[i] = k != null ? k : (p >= 0 ? id[p] : "") + "#" + m.get("n");
                file[i] = "file".equals(kind[i]) ? i : p >= 0 ? file[p] : -1;
                test[i] = Boolean.TRUE.equals(m.get("t")) || m.get("t") instanceof Double && (Double) m.get("t") != 0 || p >= 0 && test[p] && file[p] >= 0;
                ext[i] = kind[i].startsWith("ext");
            }
            edgeKinds = model.get("edgeKinds") instanceof List ? (List<String>) model.get("edgeKinds") : Model.EDGE_KINDS;
            List<?> es = model.get("edges") instanceof List ? (List<?>) model.get("edges") : List.of();
            edges = new int[es.size()][];
            for (int e = 0; e < es.size(); e++) {
                List<?> a = (List<?>) es.get(e);
                edges[e] = new int[]{num(a, 0), num(a, 1), num(a, 2), a.size() > 3 ? num(a, 3) : 1};
                List<Integer> ls = new ArrayList<>();
                if (a.size() > 4 && a.get(4) instanceof List) for (Object l : (List<?>) a.get(4)) ls.add(((Double) l).intValue());
                lines.add(ls);
            }
        }

        private static int num(List<?> a, int i) { return ((Double) a.get(i)).intValue(); }

        /** How reports name an element: a file by its path, everything else by its qualified name. */
        String name(int i) {
            return "file".equals(kind[i]) && path(i) != null ? path(i) : q[i];
        }

        String path(int i) {
            int f = file[i];
            return f >= 0 ? (String) ((Map<?, ?>) nodes.get(f)).get("path") : null;
        }

        int line(int i) {
            Object l = ((Map<?, ?>) nodes.get(i)).get("l");
            return l instanceof Double ? ((Double) l).intValue() : 0;
        }

        String edgeKind(int e) {
            int k = edges[e][2];
            return k >= 0 && k < edgeKinds.size() ? edgeKinds.get(k) : "uses";
        }

        /** Name-based: the source is Kotlin, which is resolved by name until FR-052. */
        boolean exact(int from) {
            String p = path(from);
            return p == null || !p.endsWith(".kt");
        }

        boolean isAncestor(int a, int i) {
            for (int x = parent[i]; x >= 0; x = parent[x]) if (x == a) return true;
            return false;
        }
    }

    // ---------------------------------------------------------------- violations

    /** One reference: an edge of the model, with its kind. */
    static final class Ref {
        final int from, to;
        final String kind;
        final int count;
        final List<Integer> lines;

        Ref(int from, int to, String kind, int count, List<Integer> lines) {
            this.from = from; this.to = to; this.kind = kind; this.count = count; this.lines = lines;
        }
    }

    static final class Violation {
        final String view, rule, kind, severity;
        int from = -1, to = -1;           // innermost elements; for no-ungrouped only `from`, for no-cycles neither
        List<Integer> members;            // no-cycles: the cycle's packages, files or types, sorted by name
        final List<Ref> refs = new ArrayList<>();
        boolean exact = true, baselined;
        String fingerprint;

        Violation(String view, Map<?, ?> rule) {
            this.view = view;
            this.rule = (String) rule.get("id");
            this.kind = (String) rule.get("kind");
            this.severity = rule.get("severity") instanceof String ? (String) rule.get("severity") : "error";
        }

        List<Integer> lines() {
            List<Integer> out = new ArrayList<>();
            for (Ref r : refs) for (int l : r.lines) if (!out.contains(l)) out.add(l);
            out.sort(null);
            return out;
        }

        int count() { return refs.stream().mapToInt(r -> r.count).sum(); }
    }

    /** Checks one view against its rules. The view must have passed {@link #validate}. */
    static List<Violation> check(Map<?, ?> view, Graph g, Architecture.Resolution res) {
        String viewId = String.valueOf(view.get("id"));
        Map<String, String> parentGroup = new HashMap<>();
        if (view.get("groups") instanceof List) for (Object o : (List<?>) view.get("groups")) {
            if (!(o instanceof Map)) continue;
            Map<?, ?> gm = (Map<?, ?>) o;
            if (gm.get("id") instanceof String && gm.get("parent") instanceof String) parentGroup.putIfAbsent((String) gm.get("id"), (String) gm.get("parent"));
        }
        List<Violation> out = new ArrayList<>();
        Object rs = view.get("rules");
        if (!(rs instanceof List)) return out;
        for (Object o : (List<?>) rs) {
            Map<?, ?> rule = (Map<?, ?>) o;
            Check c = new Check(viewId, rule, g, res.group, parentGroup);
            switch ((String) rule.get("kind")) {
                case "no-cycles": c.cycles(out); break;
                case "no-ungrouped": c.ungrouped(out); break;
                default: c.edges(out);
            }
        }
        for (Violation v : out) v.fingerprint = fingerprint(v, g);
        return out;
    }

    /** View, rule, source and target: no line numbers and no dependency kinds (DT-018). */
    static String fingerprint(Violation v, Graph g) {
        StringBuilder sb = new StringBuilder(v.view).append('|').append(v.rule).append('|');
        if (v.members != null) {
            sb.append("cycle");
            for (int i = 0; i < v.members.size(); i++) sb.append(i == 0 ? ":" : ",").append(g.id[v.members.get(i)]);
        } else if (v.to < 0) {
            sb.append(g.id[v.from]);
        } else {
            sb.append(g.id[v.from]).append('|').append(g.id[v.to]);
        }
        return sb.toString();
    }

    /** One rule applied to the model. */
    private static final class Check {
        final String viewId;
        final Map<?, ?> rule;
        final Graph g;
        final String[] group;
        final Map<String, String> parentGroup;
        final boolean tests;
        final Set<Integer> kinds;

        Check(String viewId, Map<?, ?> rule, Graph g, String[] group, Map<String, String> parentGroup) {
            this.viewId = viewId; this.rule = rule; this.g = g; this.group = group; this.parentGroup = parentGroup;
            this.tests = Boolean.TRUE.equals(rule.get("tests"));
            if (rule.get("edgeKinds") instanceof List) {
                kinds = new HashSet<>();
                for (Object k : (List<?>) rule.get("edgeKinds")) kinds.add(g.edgeKinds.indexOf(k));
            } else {
                kinds = null;
            }
        }

        /** The edges this rule looks at: its dependency kinds, and test code only with {@code tests: true}. */
        boolean applies(int e) {
            int[] a = g.edges[e];
            if (kinds != null && !kinds.contains(a[2])) return false;
            return tests || !g.test[a[0]] && !g.test[a[1]];
        }

        /** Group {@code id} is {@code ancestor} or nested in it. */
        boolean in(String id, String ancestor) {
            return id != null && (id.equals(ancestor) || Architecture.nested(parentGroup, id, ancestor));
        }

        /** An endpoint of a rule: a group (with its subgroups) or an {@code ext:} selector on the node or its ancestors. */
        boolean matches(String endpoint, int i) {
            if (!endpoint.startsWith("ext:")) return in(group[i], endpoint);
            Architecture.Selector s = Architecture.Selector.parse(endpoint);
            for (int a = i; a >= 0; a = g.parent[a]) if (s.selects((Map<?, ?>) g.nodes.get(a))) return true;
            return false;
        }

        List<String> list(String field) {
            Object v = rule.get(field);
            List<String> out = new ArrayList<>();
            if (v instanceof String) out.add((String) v);
            else if (v instanceof List) for (Object o : (List<?>) v) out.add((String) o);
            return out;
        }

        int layer(List<String> order, String gid) {
            for (int i = 0; i < order.size(); i++) if (in(gid, order.get(i))) return i;
            return -1;
        }

        /** layers, forbid, allow-only, independent: one violation per pair of innermost elements. */
        void edges(List<Violation> out) {
            String kind = (String) rule.get("kind");
            List<String> order = list("order"), from = list("from"), to = list("to"), groups = list("groups");
            boolean strict = Boolean.TRUE.equals(rule.get("strict"));
            Map<Long, Violation> byPair = new LinkedHashMap<>();
            Map<String, Boolean> selCache = new HashMap<>();
            for (int e = 0; e < g.edges.length; e++) {
                if (!applies(e)) continue;
                int f = g.edges[e][0], t = g.edges[e][1];
                String gf = group[f], gt = group[t];
                if (gf != null && gf.equals(gt)) continue;   // inside one group
                boolean bad;
                switch (kind) {
                    case "layers": {
                        int lf = layer(order, gf), lt = layer(order, gt);
                        bad = lf >= 0 && lt >= 0 && (lf > lt || strict && lt > lf + 1);
                        break;
                    }
                    case "forbid":
                        bad = any(from, f, selCache) && any(to, t, selCache);
                        break;
                    case "allow-only":
                        bad = any(from, f, selCache) && !any(to, t, selCache);
                        break;
                    default: { // independent
                        int a = -1, b = -1;
                        for (int i = 0; i < groups.size(); i++) {
                            if (a < 0 && in(gf, groups.get(i))) a = i;
                            if (b < 0 && in(gt, groups.get(i))) b = i;
                        }
                        bad = a >= 0 && b >= 0 && a != b;
                    }
                }
                if (!bad) continue;
                Violation v = byPair.computeIfAbsent(((long) f << 32) | t, x -> {
                    Violation nv = new Violation(viewId, rule);
                    nv.from = f; nv.to = t; nv.exact = g.exact(f);
                    return nv;
                });
                v.refs.add(new Ref(f, t, g.edgeKind(e), g.edges[e][3], g.lines.get(e)));
            }
            out.addAll(fold(byPair.values()));
        }

        /**
         * A reference to a type and one to its member from the same element are one violation, at the member:
         * {@code PriceCalculator.gross(net)} gives a {@code uses} edge to the type and a {@code call} edge to the method.
         */
        List<Violation> fold(java.util.Collection<Violation> vs) {
            Map<Integer, List<Violation>> bySource = new LinkedHashMap<>();
            for (Violation v : vs) bySource.computeIfAbsent(v.from, x -> new ArrayList<>()).add(v);
            List<Violation> out = new ArrayList<>();
            for (List<Violation> same : bySource.values()) {
                for (Violation v : same) {
                    Violation into = null;
                    for (Violation w : same)
                        if (w != v && g.isAncestor(v.to, w.to) && (into == null || g.q[w.to].compareTo(g.q[into.to]) < 0)) into = w;
                    if (into != null) into.refs.addAll(v.refs);
                    else out.add(v);
                }
            }
            for (Violation v : out) v.refs.sort(Comparator.comparingInt((Ref r) -> r.to == v.to ? 0 : 1));
            return out;
        }

        private boolean any(List<String> endpoints, int i, Map<String, Boolean> cache) {
            for (String s : endpoints) {
                Boolean hit = cache.get(s + "\u0000" + i);
                if (hit == null) cache.put(s + "\u0000" + i, hit = matches(s, i));
                if (hit) return true;
            }
            return false;
        }

        /** The package, file or type an element belongs to at the rule's level; -1 if none. */
        int unit(int i, String level) {
            for (int a = i; a >= 0; a = g.parent[a]) {
                String k = g.kind[a];
                if (level.equals("package") ? "package".equals(k) : level.equals("file") ? "file".equals(k) : Architecture.TYPE_KINDS.contains(k)) return a;
            }
            return -1;
        }

        /** no-cycles: one violation per strongly connected component of more than one unit. */
        void cycles(List<Violation> out) {
            String level = (String) rule.get("level");
            String within = rule.get("within") instanceof String ? (String) rule.get("within") : null;
            Map<Integer, Map<Integer, List<Integer>>> adj = new TreeMap<>();   // unit -> unit -> edges
            for (int e = 0; e < g.edges.length; e++) {
                if (!applies(e)) continue;
                int f = g.edges[e][0], t = g.edges[e][1];
                if (g.ext[f] || g.ext[t]) continue;
                if (within != null && !(in(group[f], within) && in(group[t], within))) continue;
                int uf = unit(f, level), ut = unit(t, level);
                if (uf < 0 || ut < 0 || uf == ut) continue;
                if (level.equals("type") && (g.isAncestor(uf, ut) || g.isAncestor(ut, uf))) continue;   // nested types
                adj.computeIfAbsent(uf, x -> new TreeMap<>()).computeIfAbsent(ut, x -> new ArrayList<>()).add(e);
                adj.computeIfAbsent(ut, x -> new TreeMap<>());
            }
            for (List<Integer> comp : scc(adj)) {
                if (comp.size() < 2) continue;
                Set<Integer> members = new HashSet<>(comp);
                Violation v = new Violation(viewId, rule);
                v.members = new ArrayList<>(comp);
                v.members.sort(Comparator.comparing((Integer i) -> g.name(i)).thenComparing(i -> g.id[i]));
                for (int u : v.members)
                    for (Map.Entry<Integer, List<Integer>> to : adj.get(u).entrySet())
                        if (members.contains(to.getKey()))
                            for (int e : to.getValue()) {
                                int f = g.edges[e][0];
                                v.refs.add(new Ref(f, g.edges[e][1], g.edgeKind(e), g.edges[e][3], g.lines.get(e)));
                                if (!g.exact(f)) v.exact = false;
                            }
                out.add(v);
            }
        }

        /** Tarjan's algorithm without recursion, so deep graphs cannot overflow the stack. */
        List<List<Integer>> scc(Map<Integer, Map<Integer, List<Integer>>> adj) {
            Map<Integer, Integer> index = new HashMap<>(), low = new HashMap<>();
            Set<Integer> on = new HashSet<>();
            Deque<Integer> stack = new ArrayDeque<>();
            List<List<Integer>> comps = new ArrayList<>();
            int next = 0;
            for (int start : adj.keySet()) {
                if (index.containsKey(start)) continue;
                Deque<int[]> work = new ArrayDeque<>();   // {node, position in its successor list}
                Map<Integer, List<Integer>> succ = new HashMap<>();
                work.push(new int[]{start, 0});
                while (!work.isEmpty()) {
                    int[] top = work.peek();
                    int v = top[0];
                    if (top[1] == 0 && !index.containsKey(v)) {
                        index.put(v, next); low.put(v, next); next++;
                        stack.push(v); on.add(v);
                        succ.put(v, new ArrayList<>(adj.get(v).keySet()));
                    }
                    List<Integer> s = succ.get(v);
                    if (top[1] < s.size()) {
                        int w = s.get(top[1]++);
                        if (!index.containsKey(w)) work.push(new int[]{w, 0});
                        else if (on.contains(w)) low.put(v, Math.min(low.get(v), index.get(w)));
                        continue;
                    }
                    work.pop();
                    if (!work.isEmpty()) { int u = work.peek()[0]; low.put(u, Math.min(low.get(u), low.get(v))); }
                    if (low.get(v).equals(index.get(v))) {
                        List<Integer> comp = new ArrayList<>();
                        int w;
                        do { w = stack.pop(); on.remove(w); comp.add(w); } while (w != v);
                        comps.add(comp);
                    }
                }
            }
            return comps;
        }

        /**
         * no-ungrouped: each topmost element in scope that has checked code below it and nothing grouped at or below it.
         * Members go with their type and are not reported on their own.
         */
        void ungrouped(List<Violation> out) {
            Architecture.Selector scope = rule.get("scope") instanceof String ? Architecture.Selector.parse((String) rule.get("scope")) : null;
            int n = g.n;
            boolean[] grouped = new boolean[n], code = new boolean[n], inScope = new boolean[n];
            for (int i = 0; i < n; i++) {
                int p = g.parent[i];
                inScope[i] = scope == null ? !g.ext[i] : p >= 0 && inScope[p] || scope.selects((Map<?, ?>) g.nodes.get(i));
            }
            for (int i = n - 1; i >= 0; i--) {   // children come after their parents
                boolean member = Architecture.MEMBER_KINDS.contains(g.kind[i]);
                if (group[i] != null && !member) grouped[i] = true;
                if (!member && (tests || !g.test[i]) && ("file".equals(g.kind[i]) || "ext-type".equals(g.kind[i]))) code[i] = true;
                int p = g.parent[i];
                if (p >= 0) { grouped[p] |= grouped[i]; code[p] |= code[i]; }
            }
            for (int i = 0; i < n; i++) {
                if (Architecture.MEMBER_KINDS.contains(g.kind[i]) || !inScope[i] || !code[i] || grouped[i]) continue;
                int p = g.parent[i];
                if (p >= 0 && inScope[p] && !grouped[p]) continue;   // reported with its parent
                Violation v = new Violation(viewId, rule);
                v.from = i;
                out.add(v);
            }
        }
    }

    /** The first file below an element, for reporting a package or module by a path. */
    static String firstPath(Graph g, int i) {
        String best = g.path(i);
        if (best != null) return best;
        for (int j = i + 1; j < g.n; j++) {
            if (!g.isAncestor(i, j)) continue;
            String p = g.path(j);
            if (p != null && (best == null || p.compareTo(best) < 0)) best = p;
        }
        return best;
    }
}
