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
        String name, q, path, vis;
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

    static int external(String pkg, String qname, String simple, String kind) {
        Node rootNode = node("X:", "External", "ext-root", -1);
        Node p = node("XP:" + pkg, pkg, "ext-package", rootNode.idx);
        p.q = pkg;
        Node t = node("XT:" + qname, simple, "ext-type", p.idx);
        t.q = qname;
        if (kind != null) t.vis = kind;
        return t.idx;
    }

    static void addEdge(int from, int to, int kind, int line) {
        if (from < 0 || to < 0 || from == to) return;
        Edge e = edges.computeIfAbsent(from + ">" + to + ">" + kind, x -> new Edge(from, to, kind));
        e.count++;
        if (line > 0 && e.lines.size() < 30 && !e.lines.contains(line)) e.lines.add(line);
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

    static String toJson(String title, Collection<Edge> edges, int errors) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"title\":").append(js(title))
                .append(",\"generated\":").append(js(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))))
                .append(",\"errors\":").append(errors)
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
