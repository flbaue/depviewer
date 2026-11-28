package depviewer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * The architecture file (spec FR-026): views and groups as exported by the viewer, plus the optional
 * {@code match} and {@code rules}. Fields this version does not know are ignored.
 */
final class Architecture {
    static final int VERSION = 1;

    /** A file that cannot be used; the message is meant for the user. */
    static final class Invalid extends Exception {
        private static final long serialVersionUID = 1L;
        Invalid(String msg) { super(msg); }
    }

    final String text;
    final Map<?, ?> root;
    final List<Object> views;

    private Architecture(String text, Map<?, ?> root, List<Object> views) {
        this.text = text;
        this.root = root;
        this.views = views;
    }

    static Architecture load(Path file) throws Invalid {
        String text;
        try {
            text = Files.readString(file);
        } catch (IOException e) {
            throw new Invalid(file + ": cannot read (" + e.getMessage() + ")");
        }
        Object root;
        try {
            root = Json.parse(text);
        } catch (IllegalArgumentException e) {
            throw new Invalid(file + ": " + e.getMessage());
        }
        if (!(root instanceof Map)) throw new Invalid(file + ": expected a JSON object with a \"views\" array");
        Map<?, ?> m = (Map<?, ?>) root;
        Object v = m.get("version");
        if (v != null) {
            if (!(v instanceof Double) || (Double) v != Math.floor((Double) v))
                throw new Invalid(file + ": \"version\" must be a whole number");
            int major = ((Double) v).intValue();
            if (major != VERSION)
                throw new Invalid(file + ": version " + major + " is not supported (this depviewer reads version " + VERSION + ")");
        }
        Object views = m.containsKey("views") ? m.get("views") : List.of();
        if (!(views instanceof List)) throw new Invalid(file + ": \"views\" must be an array");
        @SuppressWarnings("unchecked") List<Object> list = (List<Object>) views;
        for (Object view : list) {
            if (!(view instanceof Map)) throw new Invalid(file + ": every view must be an object");
            Object groups = ((Map<?, ?>) view).get("groups");
            if (!(groups instanceof List)) continue;
            for (Object g : (List<?>) groups) if (g instanceof Map) checkMatch(file, (Map<?, ?>) g);
        }
        for (Object view : list) Rules.validate(file.toString(), (Map<?, ?>) view);
        return new Architecture(text, (Map<?, ?>) root, list);
    }

    /** True when at least one view has a non-empty {@code rules} array. */
    boolean hasRules() {
        for (Object view : views) {
            Object r = ((Map<?, ?>) view).get("rules");
            if (r instanceof List && !((List<?>) r).isEmpty()) return true;
        }
        return false;
    }

    private static void checkMatch(Path file, Map<?, ?> group) throws Invalid {
        if (!group.containsKey("match")) return;
        String where = file + ": group \"" + group.get("id") + "\": ";
        Object match = group.get("match");
        if (!(match instanceof List)) throw new Invalid(where + "\"match\" must be an array of strings");
        for (Object s : (List<?>) match) {
            if (!(s instanceof String)) throw new Invalid(where + "\"match\" must be an array of strings");
            try {
                Selector.parse((String) s);
            } catch (IllegalArgumentException e) {
                throw new Invalid(where + e.getMessage());
            }
        }
    }

    // ---------------------------------------------------------------- selectors (spec FR-027)

    static final Set<String> TYPE_KINDS = Set.of("class", "interface", "enum", "record", "annotation", "object",
            "typealias", "namespace", "mixin", "extension");

    /**
     * {@code [!]<kind>:<glob>[@typeKind]}. {@code *} matches within one name segment, {@code **} across segments
     * (zero or more: {@code a.**} selects {@code a} too). The separator is '/' for paths and folder-based
     * packages, '.' otherwise.
     */
    static final class Selector {
        final String text, kind, typeKind;
        final boolean exclude;
        private final String glob;
        private Pattern dot, slash;

        private Selector(String text, boolean exclude, String kind, String glob, String typeKind) {
            this.text = text; this.exclude = exclude; this.kind = kind; this.glob = glob; this.typeKind = typeKind;
        }

        static Selector parse(String text) {
            boolean exclude = text.startsWith("!");
            String s = exclude ? text.substring(1) : text;
            int colon = s.indexOf(':');
            if (colon < 0) throw new IllegalArgumentException("selector \"" + text + "\" must be <kind>:<pattern>");
            String kind = s.substring(0, colon), glob = s.substring(colon + 1), typeKind = null;
            if (!Set.of("module", "package", "path", "type", "ext").contains(kind))
                throw new IllegalArgumentException("unknown selector kind \"" + kind + "\" in \"" + text
                        + "\" (module, package, path, type or ext)");
            int at = glob.lastIndexOf('@');
            if (at >= 0) {
                if (!kind.equals("type"))
                    throw new IllegalArgumentException("\"@\" is only allowed after type: selectors, in \"" + text + "\"");
                typeKind = glob.substring(at + 1);
                glob = glob.substring(0, at);
                if (!TYPE_KINDS.contains(typeKind))
                    throw new IllegalArgumentException("unknown type kind \"" + typeKind + "\" in \"" + text + "\"");
            }
            if (glob.isEmpty()) throw new IllegalArgumentException("selector \"" + text + "\" has no pattern");
            return new Selector(text, exclude, kind, glob, typeKind);
        }

        /** True when this selector, ignoring {@code !}, selects the node (a model JSON node object). */
        boolean selects(Map<?, ?> node) {
            Object k = node.get("k");
            String subject;
            boolean slashSep = false;
            switch (kind) {
                case "module": if (!"module".equals(k)) return false; subject = name(node); break;
                case "package": if (!"package".equals(k)) return false; subject = name(node); slashSep = "/".equals(node.get("sep")); break;
                case "path": if (!"file".equals(k)) return false; subject = (String) node.get("path"); slashSep = true; break;
                case "type":
                    if (!TYPE_KINDS.contains(k) || typeKind != null && !typeKind.equals(k)) return false;
                    subject = name(node);
                    break;
                default: // ext
                    if (!"ext-package".equals(k) && !"ext-type".equals(k)) return false;
                    subject = name(node);
                    if (subject.startsWith("?")) return false;   // unresolved, not an external
            }
            if (subject == null) return false;
            if (slashSep) {
                if (slash == null) slash = compile(glob, '/');
                return slash.matcher(subject).matches();
            }
            if (dot == null) dot = compile(glob, '.');
            return dot.matcher(subject).matches();
        }

        static Pattern compile(String glob, char sep) {
            String q = Pattern.quote(String.valueOf(sep));
            StringBuilder re = new StringBuilder();
            int n = glob.length();
            for (int i = 0; i < n; i++) {
                char c = glob.charAt(i);
                if (c == '*' && i + 1 < n && glob.charAt(i + 1) == '*') {
                    boolean after = i + 2 < n && glob.charAt(i + 2) == sep;
                    boolean before = re.length() > 0 && i > 0 && glob.charAt(i - 1) == sep;
                    if (after) {                       // "**." : zero or more leading segments
                        re.append("(?:.*").append(q).append(")?");
                        i += 2;
                    } else if (before && i + 2 == n) { // ".**" at the end: the name itself or anything below
                        re.setLength(re.length() - q.length());
                        re.append("(?:").append(q).append(".*)?");
                        i += 1;
                    } else {
                        re.append(".*");
                        i += 1;
                    }
                } else if (c == '*') {
                    re.append("[^").append(sep).append("]*");   // '.' and '/' are literal in a class
                } else if (c == sep) {
                    re.append(q);
                } else {
                    re.append(Pattern.quote(String.valueOf(c)));
                }
            }
            return Pattern.compile(re.toString());
        }
    }

    /** A node's full name: its qualified name, or its name when that is the same. */
    static String name(Object node) {
        Map<?, ?> m = (Map<?, ?>) node;
        Object q = m.get("q");
        return (String) (q != null ? q : m.get("n"));
    }

    /** The nodes one group's {@code match} selects, applied in order; a {@code !} selector removes. */
    static BitSet select(List<Selector> match, List<Object> nodes) {
        return select(match, nodes, null);
    }

    /** As {@link #select(List, List)}; {@code excluded}, if given, receives what a {@code !} selector removed or kept out. */
    static BitSet select(List<Selector> match, List<Object> nodes, BitSet excluded) {
        BitSet out = new BitSet(nodes.size());
        for (Selector s : match)
            for (int i = 0; i < nodes.size(); i++)
                if (s.selects((Map<?, ?>) nodes.get(i))) {
                    if (out.get(i) == s.exclude) out.set(i, !s.exclude);
                    if (excluded != null) excluded.set(i, s.exclude);
                }
        return out;
    }

    // ---------------------------------------------------------------- assignment precedence (spec FR-028)

    static final Set<String> MEMBER_KINDS = Set.of("ctor", "method", "field", "enumconst");

    /** Two or more groups' patterns select one element. {@code groups[0]} is the group it goes to. */
    static final class Overlap {
        final int node;
        final String key, via;   // via: the ancestor the other group comes from, or null when all select it directly
        final List<String> groups;

        Overlap(int node, String key, List<String> groups, String via) {
            this.node = node; this.key = key; this.groups = groups; this.via = via;
        }

        String message(String viewId) {
            String others = via == null ? String.join(" and ", groups) : groups.get(0) + " and by " + groups.get(1) + " through " + via;
            return "warning  overlap  " + key + "  selected by " + others + ", goes to " + groups.get(0) + " (view " + viewId + ")";
        }
    }

    /** Every node's group in one view ({@code null}: ungrouped), and the overlaps found on the way. */
    static final class Resolution {
        final String[] group;
        final List<Overlap> overlaps = new ArrayList<>();

        Resolution(int n) { group = new String[n]; }
    }

    /**
     * Decides each node's group: its own {@code assign} entry, else the first group whose {@code match} selects it,
     * else its nearest ancestor's group, skipping groups whose {@code !} selectors exclude the node. Members have no
     * key and are never selected, so they always take their type's group. Expects a view as {@link #load} accepted it.
     */
    static Resolution resolve(Map<?, ?> view, List<Object> nodes) {
        int n = nodes.size();
        List<String> ids = new ArrayList<>();
        Map<String, String> parentGroup = new HashMap<>();
        List<BitSet> selected = new ArrayList<>(), excluded = new ArrayList<>();
        Object gs = view.get("groups");
        if (gs instanceof List) for (Object g : (List<?>) gs) {
            if (!(g instanceof Map) || !(((Map<?, ?>) g).get("id") instanceof String)) continue;
            Map<?, ?> gm = (Map<?, ?>) g;
            String id = (String) gm.get("id");
            if (ids.contains(id)) continue;
            ids.add(id);
            if (gm.get("parent") instanceof String) parentGroup.put(id, (String) gm.get("parent"));
            List<Selector> match = new ArrayList<>();
            if (gm.get("match") instanceof List) for (Object s : (List<?>) gm.get("match")) match.add(Selector.parse((String) s));
            BitSet ex = new BitSet(n);
            BitSet sel = select(match, nodes, ex);
            ex.andNot(sel);
            selected.add(sel);
            excluded.add(ex);
        }
        Map<?, ?> assign = view.get("assign") instanceof Map ? (Map<?, ?>) view.get("assign") : Map.of();

        Resolution r = new Resolution(n);
        boolean[] byMatch = new boolean[n];   // the group came from a pattern, here or on the ancestor it was inherited from
        int[] parent = new int[n];
        String[] module = new String[n], key = new String[n];
        for (int i = 0; i < n; i++) {
            Map<?, ?> node = (Map<?, ?>) nodes.get(i);
            parent[i] = node.get("p") instanceof Double ? ((Double) node.get("p")).intValue() : -1;
            module[i] = parent[i] >= 0 ? module[parent[i]] : (String) node.get("n");
            key[i] = key(node, module[i]);
            if (key[i] != null && assign.containsKey(key[i])) {
                Object g = assign.get(key[i]);
                r.group[i] = g instanceof String && ids.contains(g) ? (String) g : null;
                continue;
            }
            List<String> direct = new ArrayList<>();
            for (int g = 0; g < ids.size(); g++) if (selected.get(g).get(i)) direct.add(ids.get(g));
            int from = parent[i];   // nearest ancestor whose group this node may inherit
            while (from >= 0 && r.group[from] != null && excluded.get(ids.indexOf(r.group[from])).get(i)) from = parent[from];
            String inherited = from >= 0 ? r.group[from] : null;
            if (direct.isEmpty()) {
                r.group[i] = inherited;
                byMatch[i] = inherited != null && byMatch[from];
                continue;
            }
            String winner = direct.get(0);
            r.group[i] = winner;
            byMatch[i] = true;
            if (direct.size() > 1) {
                r.overlaps.add(new Overlap(i, key[i], direct, null));
            } else if (inherited != null && byMatch[from] && !inherited.equals(winner)
                    && !nested(parentGroup, winner, inherited) && !nested(parentGroup, inherited, winner)) {
                int via = from;
                while (key[via] == null) via = parent[via];   // a member: name the type or file
                r.overlaps.add(new Overlap(i, key[i], List.of(winner, inherited), key[via]));
            }
        }
        return r;
    }

    /** True when group {@code id} is nested, at any depth, in group {@code ancestor}. Guards against parent loops. */
    static boolean nested(Map<String, String> parentGroup, String id, String ancestor) {
        Set<String> seen = new HashSet<>();
        for (String g = parentGroup.get(id); g != null && seen.add(g); g = parentGroup.get(g)) if (g.equals(ancestor)) return true;
        return false;
    }

    /** The persisted view key of a node, as the viewer builds it, plus {@code T:} for types; null for members. */
    static String key(Map<?, ?> node, String module) {
        Object k = node.get("k");
        if ("module".equals(k)) return "R:" + node.get("n");
        if ("package".equals(k)) return "P:" + module + ":" + name(node);
        if ("file".equals(k)) return "F:" + module + ":" + node.get("path");
        if ("ext-root".equals(k)) return "X";
        if ("ext-package".equals(k)) return "XP:" + name(node);
        if ("ext-type".equals(k)) return "XT:" + name(node);
        if (TYPE_KINDS.contains(k)) return "T:" + module + ":" + name(node);
        return null;
    }

    /** The views that have at least one {@code match}: only they need a model to resolve. */
    List<Map<?, ?>> viewsWithPatterns() {
        List<Map<?, ?>> out = new ArrayList<>();
        for (Object view : views) {
            Object gs = ((Map<?, ?>) view).get("groups");
            if (gs instanceof List && ((List<?>) gs).stream().anyMatch(g -> g instanceof Map && ((Map<?, ?>) g).get("match") instanceof List
                    && !((List<?>) ((Map<?, ?>) g).get("match")).isEmpty())) out.add((Map<?, ?>) view);
        }
        return out;
    }
}
