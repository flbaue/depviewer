package depviewer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static depviewer.Model.*;

/**
 * The jvm extractor: Java with javac, Kotlin with the built-in parser, in one run so that Java and
 * Kotlin link to each other (spec BB-011). It runs in its own process and speaks the extractor
 * contract (spec CC-002) like the TypeScript and Dart extractors:
 *
 * <pre>
 *   java -cp &lt;classes&gt; depviewer.JvmExtractor &lt;request.json&gt;
 *   java -cp &lt;classes&gt; depviewer.JvmExtractor --describe &lt;source&gt;
 * </pre>
 *
 * The request's options carry `sourceRoots` ({dir, project, module, test}, every root the core found)
 * and `classpath`; each file names its root by index in `sourceRoot` and its place among all files in
 * `order`. The fragment lists the
 * model this process built in its order, packages included, so the core rebuilds it exactly
 * (spec QR-006): nodes, externals and aggregated edges.
 */
final class JvmExtractor {

    static final String NAME = "depviewer-jvm-extractor", VERSION = "1.0.0";
    static final int MIN_JAVA = 17;

    public static void main(String[] args) throws Exception {
        if (args.length > 0 && args[0].equals("--describe")) {
            System.out.println(Json.write(describe(args.length > 1 ? args[1] : "path")));
            return;
        }
        if (args.length != 1) {
            System.err.println("java: usage: JvmExtractor <request.json> | --describe <source>");
            System.exit(2);
        }
        String fragment;
        try {
            fragment = run((Map<?, ?>) Json.parse(Files.readString(Path.of(args[0]))));
        } catch (DepScan.Failed e) {   // e.g. no Java compiler: a note for the core's log, not a stack trace
            System.err.println("java: " + e.getMessage());
            System.exit(1);
            return;
        }
        System.out.write(fragment.getBytes(StandardCharsets.UTF_8));
        System.out.flush();
    }

    static Map<String, Object> describe(String source) {
        int feature = Runtime.version().feature();
        // A JRE has no javac. It could read Kotlin, but --describe does not know the files, so the
        // next candidate (a JDK) is tried instead.
        boolean compiler = javax.tools.ToolProvider.getSystemJavaCompiler() != null;
        Map<String, Object> t = new LinkedHashMap<>();
        t.put("tool", "java");
        t.put("version", System.getProperty("java.version"));
        t.put("source", source);
        t.put("path", Path.of(System.getProperty("java.home"), "bin", "java").toString());
        t.put("supported", feature >= MIN_JAVA && compiler);
        t.put("range", ">=" + MIN_JAVA + " (a JDK)");
        if (feature < MIN_JAVA) t.put("problem", "version not supported");
        else if (!compiler) t.put("problem", "a JRE without the Java compiler");
        Map<String, Object> d = new LinkedHashMap<>();
        d.put("name", NAME);
        d.put("version", VERSION);
        d.put("contract", Toolchain.CONTRACT);
        d.put("languages", List.of("java", "kotlin"));
        d.put("exact", true);
        d.put("toolchain", t);
        return d;
    }

    /** Scans the request's files as DepScan did in process, and returns the fragment. */
    static String run(Map<?, ?> request) throws IOException {
        Map<?, ?> options = request.get("options") instanceof Map ? (Map<?, ?>) request.get("options") : Map.of();
        List<DepScan.Root> roots = new ArrayList<>();
        if (options.get("sourceRoots") instanceof List) {
            for (Object o : (List<?>) options.get("sourceRoots")) {
                Map<?, ?> r = (Map<?, ?>) o;
                roots.add(new DepScan.Root(Path.of((String) r.get("dir")), Path.of((String) r.get("project")),
                        (String) r.get("module"), Boolean.TRUE.equals(r.get("test"))));
            }
        }
        String cp = options.get("classpath") instanceof String ? (String) options.get("classpath") : null;
        Map<Path, DepScan.Root> rootOfFile = new LinkedHashMap<>(), rootOfKt = new LinkedHashMap<>();
        List<Map<?, ?>> files = new ArrayList<>();
        for (Object o : (List<?>) request.get("files")) files.add((Map<?, ?>) o);
        // The files in the order the core found them (`order`), which the model's order follows.
        files.sort(java.util.Comparator.comparingLong(f -> f.get("order") == null ? Long.MAX_VALUE : Long.parseLong(String.valueOf(f.get("order")))));
        for (Map<?, ?> f : files) {
            DepScan.Root root = roots.get(Integer.parseInt(String.valueOf(f.get("sourceRoot"))));
            Path file = Path.of((String) f.get("root")).resolve((String) f.get("path")).normalize();
            (file.toString().endsWith(".kt") ? rootOfKt : rootOfFile).put(file, root);
        }

        // Kotlin declarations first, so that Java references to Kotlin types (unresolvable for
        // javac) can be matched by name when the Java references are resolved.
        KotlinScanner kotlin = new KotlinScanner();
        for (Map.Entry<Path, DepScan.Root> e : rootOfKt.entrySet()) kotlin.declare(e.getKey(), e.getValue());
        int[] errors = {0};
        List<String> firstErrors = new ArrayList<>();
        JavaScanner java = new JavaScanner();
        if (!rootOfFile.isEmpty()) java.scan(rootOfFile, roots, cp, errors, firstErrors);
        java.resolve();
        kotlin.resolveAll();
        if (errors[0] > 0) {
            System.err.printf("java: %d compile errors; unresolved references are listed under External > (unresolved).%n", errors[0]);
            System.err.println("java: missing libraries? Run 'mvn dependency:copy-dependencies' or pass --cp.");
            if (!rootOfKt.isEmpty()) System.err.println("java: (Java references to Kotlin code are expected here; they are matched by name.)");
            firstErrors.forEach(e -> System.err.println("java:   " + e));
        }

        List<Object> languages = new ArrayList<>();
        Set<String> javaModules = new LinkedHashSet<>(), ktModules = new LinkedHashSet<>();
        for (DepScan.Root r : roots) {
            if (rootOfFile.containsValue(r)) javaModules.add(r.module);
            if (rootOfKt.containsValue(r)) ktModules.add(r.module);
        }
        if (!javaModules.isEmpty()) languages.add(language("java", javaModules, "javac", System.getProperty("java.version"), true, null));
        if (!ktModules.isEmpty()) languages.add(language("kotlin", ktModules, KotlinScanner.EXTRACTOR, KotlinScanner.VERSION, false, "pinned"));
        return fragment(errors[0], languages);
    }

    static Map<String, Object> language(String language, Set<String> modules, String tool, String version, boolean exact, String source) {
        Map<String, Object> a = new LinkedHashMap<>();
        a.put("language", language);
        a.put("modules", new ArrayList<Object>(modules));
        a.put("tool", tool);
        a.put("version", version);
        a.put("exact", exact);
        if (source != null) a.put("source", source);
        return a;
    }

    /** The model of this process as a fragment, in an order the core's merge rebuilds exactly. */
    static String fragment(int errors, List<Object> languages) {
        Map<String, Object> f = new LinkedHashMap<>();
        f.put("contract", Toolchain.CONTRACT);
        f.put("language", "java");
        f.put("errors", errors);
        f.put("packageSep", ".");
        f.put("languages", languages);
        List<Object> ns = new ArrayList<>(), xs = new ArrayList<>(), es = new ArrayList<>();
        int skipped = 0;
        for (Node n : nodes) {
            String kind = n.kind;
            if (kind.equals("module") || kind.equals("ext-root") || kind.equals("ext-package")) continue;
            if (kind.equals("package")) {   // created here, as the scanners created it (before its files)
                Map<String, Object> p = new LinkedHashMap<>();
                String module = moduleOf(n);
                p.put("kind", "package");
                p.put("module", module);
                p.put("package", n.id.substring(("P:" + module + ":").length()));
                ns.add(p);
                continue;
            }
            if (kind.equals("ext-type")) {
                Map<String, Object> x = new LinkedHashMap<>();
                x.put("id", "x" + n.idx);
                Node pkg = nodes.get(n.parent);
                x.put("package", pkg.q != null ? pkg.q : pkg.name);
                x.put("name", n.name);
                x.put("q", n.q);
                if (n.vis != null) x.put("kind", n.vis);
                xs.add(x);
                continue;
            }
            Node parent = n.parent >= 0 ? nodes.get(n.parent) : null;
            Map<String, Object> o = new LinkedHashMap<>();
            o.put("id", "n" + n.idx);
            o.put("kind", kind);
            if (kind.equals("file")) {
                if (parent == null || !parent.kind.equals("package")) { skipped++; continue; }
                String module = moduleOf(n);
                o.put("path", n.path);
                o.put("module", module);
                o.put("package", parent.id.substring(("P:" + module + ":").length()));
                o.put("test", n.test);
                o.put("loc", n.loc);
            } else {
                if (parent == null || parent.kind.equals("module") || parent.kind.equals("package")) { skipped++; continue; }
                o.put("name", n.name);
                o.put("parent", "n" + parent.idx);
                o.put("line", n.line);
                o.put("loc", n.loc);
                if (n.vis != null) o.put("vis", n.vis);
                if (n.isStatic) o.put("static", true);
                o.put("q", n.q);
            }
            ns.add(o);
        }
        for (Edge e : edges.values()) {
            String from = ref(e.from), to = ref(e.to);
            if (from == null || to == null) { skipped++; continue; }
            Map<String, Object> o = new LinkedHashMap<>();
            o.put("from", from);
            o.put("to", to);
            o.put("kind", EDGE_KINDS.get(e.kind));
            o.put("count", e.count);
            o.put("lines", new ArrayList<Object>(e.lines));
            es.add(o);
        }
        if (skipped > 0) System.err.printf("java: %d nodes or dependencies have no place in a fragment and were left out%n", skipped);
        f.put("nodes", ns);
        f.put("externals", xs);
        f.put("edges", es);
        return Json.write(f);
    }

    /** The fragment id of a node an edge may point to: declarations, files and external types. */
    static String ref(int i) {
        Node n = nodes.get(i);
        if (n.kind.equals("ext-type")) return "x" + i;
        if (n.kind.equals("module") || n.kind.equals("package") || n.kind.startsWith("ext")) return null;
        return "n" + i;
    }

    static String moduleOf(Node n) {
        while (n.parent >= 0) n = nodes.get(n.parent);
        return n.name;
    }
}
