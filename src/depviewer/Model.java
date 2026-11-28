package depviewer;

import com.sun.source.tree.*;
import com.sun.source.util.*;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/** The dependency model shared by all language scanners: nodes, edges, packages, externals, JSON output. */
final class Model {

    static final List<String> EDGE_KINDS = List.of("uses", "extends", "implements", "call", "new", "access");
    static final int K_USES = 0, K_EXTENDS = 1, K_IMPLEMENTS = 2, K_CALL = 3, K_NEW = 4, K_ACCESS = 5;
    static final Pattern QUALIFIED = Pattern.compile("(?:[\\w$]+\\.)+([\\w$]+)");

    static final class Node {
        final int idx;
        final String id, kind;
        final int parent;
        String name, q, path, vis, sep;
        int line, loc;
        boolean test, isStatic;
        // For resolving Kotlin references by name: simple member name ("<init>" for constructors),
        // declared type of a field / return type of a method, supertypes (qualified names), Java origin.
        String simple, valueType;
        List<String> supers;
        boolean java, kotlin;

        Node(int idx, String id, String name, String kind, int parent) {
            this.idx = idx; this.id = id; this.name = name; this.kind = kind; this.parent = parent;
        }
    }

    static final class Edge {
        final int from, to, kind;
        int count;
        final List<Integer> lines = new ArrayList<>();

        Edge(int from, int to, int kind) { this.from = from; this.to = to; this.kind = kind; }
    }

    static final List<Node> nodes = new ArrayList<>();
    static final Map<String, Node> byId = new HashMap<>();
    static final Map<String, Edge> edges = new LinkedHashMap<>();
    /** Provenance (spec FR-011): one entry per language and set of modules, in scan order. */
    static final List<Map<String, Object>> analysis = new ArrayList<>();
    static final List<Object[]> javaToKotlin = new ArrayList<>();   // {Raw, typeIdx}: Java member references on Kotlin types

    // ---------------------------------------------------------------- nodes

    static Node node(String id, String name, String kind, int parent) {
        Node n = byId.get(id);
        if (n != null) return n;
        n = new Node(nodes.size(), id, name, kind, parent);
        nodes.add(n);
        byId.put(id, n);
        return n;
    }

    static int packageNode(String module, String pkg) {
        Node mod = node("R:" + module, module, "module", -1);
        int parent = mod.idx;
        if (pkg.isEmpty()) return node("P:" + module + ":", "(default package)", "package", parent).idx;
        StringBuilder sb = new StringBuilder();
        for (String part : pkg.split("\\.")) {
            if (sb.length() > 0) sb.append('.');
            sb.append(part);
            Node n = node("P:" + module + ":" + sb, part, "package", parent);
            n.q = sb.toString();
            parent = n.idx;
        }
        return parent;
    }

    /** Package nodes for a folder path (TypeScript, Dart): one per segment, joined with '/'. "" = the module itself. */
    static int folderPackage(String module, String dir) {
        Node mod = node("R:" + module, module, "module", -1);
        int parent = mod.idx;
        if (dir.isEmpty()) return parent;
        StringBuilder sb = new StringBuilder();
        for (String part : dir.split("/")) {
            if (part.isEmpty()) continue;
            if (sb.length() > 0) sb.append('/');
            sb.append(part);
            Node n = node("P:" + module + ":" + sb, part, "package", parent);
            n.q = sb.toString();
            n.sep = "/";
            parent = n.idx;
        }
        return parent;
    }

    static int external(String pkg, String qname, String simple, String kind) {
        Node rootNode = node("X:", "External", "ext-root", -1);
        Node p = node("XP:" + pkg, pkg, "ext-package", rootNode.idx);
        p.q = pkg;
        Node t = node("XT:" + qname, simple, "ext-type", p.idx);
        t.q = qname;
        if (kind != null) t.vis = kind;
        return t.idx;
    }

    /** Several references at once, as an extractor that aggregates them reports them (spec CC-002). */
    static void addEdges(int from, int to, int kind, int count, List<Integer> lines) {
        if (from < 0 || to < 0 || from == to) return;
        Edge e = edges.computeIfAbsent(from + ">" + to + ">" + kind, x -> new Edge(from, to, kind));
        e.count += count;
        for (int line : lines) if (line > 0 && e.lines.size() < 30 && !e.lines.contains(line)) e.lines.add(line);
    }

    static void addEdge(int from, int to, int kind, int line) {
        if (from < 0 || to < 0 || from == to) return;
        Edge e = edges.computeIfAbsent(from + ">" + to + ">" + kind, x -> new Edge(from, to, kind));
        e.count++;
        if (line > 0 && e.lines.size() < 30 && !e.lines.contains(line)) e.lines.add(line);
    }


    /** Records which tool analysed a language in some modules, and whether it resolves references exactly. */
    static void provenance(String language, List<String> modules, String tool, String version, String source, boolean exact,
                           String extractor, String extractorVersion) {
        Map<String, Object> a = new LinkedHashMap<>();
        a.put("language", language);
        a.put("modules", new ArrayList<Object>(modules));
        a.put("tool", tool);
        a.put("version", version);
        a.put("source", source);
        a.put("exact", exact);
        Map<String, Object> x = new LinkedHashMap<>();
        x.put("name", extractor);
        x.put("version", extractorVersion);
        a.put("extractor", x);
        analysis.add(a);
    }

    static String qual(String pkg, String name) { return pkg == null || pkg.isEmpty() ? name : pkg + "." + name; }

    // ---------------------------------------------------------------- output

    static String js(String s) {
        StringBuilder sb = new StringBuilder("\"");
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                case '<': sb.append("\\u003c"); break;
                case '>': sb.append("\\u003e"); break;
                case '&': sb.append("\\u0026"); break;
                default:
                    if (c < 0x20 || c == ' ' || c == ' ') sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
            }
        }
        return sb.append('"').toString();
    }

    /** The model as {@link Json#parse} would read {@link #toJson}'s output, for {@code check} without a round trip. */
    static Map<String, Object> toMap(String title, String generated, Collection<Edge> edges, int errors) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("title", title);
        m.put("generated", generated);
        m.put("errors", (double) errors);
        m.put("analysis", Json.parse(Json.write(analysis)));
        m.put("edgeKinds", new ArrayList<Object>(EDGE_KINDS));
        List<Object> ns = new ArrayList<>(nodes.size());
        for (Node n : nodes) {
            Map<String, Object> o = new LinkedHashMap<>();
            o.put("n", n.name);
            o.put("k", n.kind);
            if (n.parent >= 0) o.put("p", (double) n.parent);
            if (n.line > 0) o.put("l", (double) n.line);
            if (n.loc > 0) o.put("loc", (double) n.loc);
            if (n.test) o.put("t", 1.0);
            if (n.isStatic) o.put("s", 1.0);
            if (n.vis != null) o.put("v", n.vis);
            if (n.q != null && !n.q.equals(n.name)) o.put("q", n.q);
            if (n.path != null) o.put("path", n.path);
            if (n.sep != null) o.put("sep", n.sep);
            ns.add(o);
        }
        m.put("nodes", ns);
        List<Object> es = new ArrayList<>(edges.size());
        for (Edge e : edges) {
            List<Object> ls = new ArrayList<>(e.lines.size());
            for (int l : e.lines) ls.add((double) l);
            es.add(new ArrayList<Object>(List.of((double) e.from, (double) e.to, (double) e.kind, (double) e.count, ls)));
        }
        m.put("edges", es);
        return m;
    }

    static String toJson(String title, Collection<Edge> edges, int errors) {
        return toJson(title, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")), edges, errors);
    }

    static String toJson(String title, String generated, Collection<Edge> edges, int errors) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"title\":").append(js(title))
                .append(",\"generated\":").append(js(generated))
                .append(",\"errors\":").append(errors)
                .append(",\"analysis\":").append(Json.write(analysis))
                .append(",\"edgeKinds\":[").append(EDGE_KINDS.stream().map(Model::js).collect(Collectors.joining(","))).append("]")
                .append(",\"nodes\":[");
        for (int i = 0; i < nodes.size(); i++) {
            Node n = nodes.get(i);
            if (i > 0) sb.append(",\n");
            sb.append("{\"n\":").append(js(n.name)).append(",\"k\":").append(js(n.kind));
            if (n.parent >= 0) sb.append(",\"p\":").append(n.parent);
            if (n.line > 0) sb.append(",\"l\":").append(n.line);
            if (n.loc > 0) sb.append(",\"loc\":").append(n.loc);
            if (n.test) sb.append(",\"t\":1");
            if (n.isStatic) sb.append(",\"s\":1");
            if (n.vis != null) sb.append(",\"v\":").append(js(n.vis));
            if (n.q != null && !n.q.equals(n.name)) sb.append(",\"q\":").append(js(n.q));
            if (n.path != null) sb.append(",\"path\":").append(js(n.path));
            if (n.sep != null) sb.append(",\"sep\":").append(js(n.sep));
            sb.append('}');
        }
        sb.append("],\n\"edges\":[");
        boolean first = true;
        for (Edge e : edges) {
            if (!first) sb.append(",\n");
            first = false;
            sb.append('[').append(e.from).append(',').append(e.to).append(',').append(e.kind).append(',').append(e.count)
                    .append(",[").append(e.lines.stream().map(String::valueOf).collect(Collectors.joining(","))).append("]]");
        }
        return sb.append("]}").toString();
    }
}
