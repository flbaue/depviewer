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
import static depviewer.Model.*;

/**
 * Scans Java, Kotlin, TypeScript/JavaScript and Dart sources and writes a dependency model:
 * module > package > file > type > method/field, plus the dependencies between them. Output is a
 * self-contained HTML viewer (or JSON).
 *
 * Java is parsed and attributed by the JDK compiler (exact symbol resolution). Kotlin has no
 * compiler in the JDK, so Kotlin files are read by a small built-in parser and references are
 * resolved by name (KotlinScanner). TypeScript/JavaScript and Dart are analysed by external
 * extractors on their official toolchains (Toolchain, spec/wiki/crosscutting-concepts/CC-009-fragment-format.md).
 *
 * Usage: java DepScan.java [options] <project-or-source-dir>...
 */
public class DepScan {

    static final Set<String> SKIP_DIRS = Set.of("target", "build", "out", "bin", "node_modules", ".git", ".mvn",
            ".idea", ".gradle", ".svn", ".settings", ".dart_tool", "dist", "coverage", ".next");
    static final Pattern TS_FILE = Pattern.compile(".*\\.(ts|tsx|mts|cts|js|jsx|mjs|cjs)$");
    static final Pattern TS_TEST = Pattern.compile("(^|.*/)(__tests__|tests?)/.*|.*\\.(test|spec)\\.[^/]+$");
    static final List<String> DART_MAIN = List.of("lib", "bin", "web", "tool"), DART_TEST = List.of("test", "integration_test");

    static boolean skipped(Path rel) {
        for (Path seg : rel) if (SKIP_DIRS.contains(seg.toString())) return true;
        return false;
    }

    static final class Root {
        final Path dir, projectDir;
        final String module;
        final boolean test;

        Root(Path dir, Path projectDir, String module, boolean test) {
            this.dir = dir; this.projectDir = projectDir; this.module = module; this.test = test;
        }
    }

    public static void main(String[] args) throws Exception {
        try {
            new DepScan().run(args);
        } catch (Failed e) {
            System.err.println("depviewer: " + e.getMessage());
            System.exit(1);
        }
    }

    /** A scan that cannot go on; the message is meant for the user. */
    static final class Failed extends RuntimeException {
        private static final long serialVersionUID = 1L;
        Failed(String msg) { super(msg); }
    }

    static void usage() {
        System.out.println(String.join("\n",
                "Usage: depviewer [options] <project-or-source-dir>...",
                "       depviewer check [--rules <file>] (<project-dir>... | --model <deps.json>)",
                "",
                "  Finds the modules below each directory: src/{main,test}/{java,kotlin} folders (Java,",
                "  Kotlin), package.json (TypeScript/JavaScript, needs Node.js) and pubspec.yaml (Dart,",
                "  needs the Dart SDK). A directory without any of them is scanned as a plain Java/Kotlin",
                "  source root. Languages whose toolchain is missing are skipped with a note.",
                "",
                "Options:",
                "  -o, --out <file>     output file: .html (viewer, default deps.html) or .json (data only)",
                "  --cp <classpath>     extra classpath for resolving Java/Kotlin libraries (jars in",
                "                       target/dependency, lib/ and libs/ are picked up automatically)",
                "  --no-tests           skip test source folders",
                "  --title <text>       title shown in the viewer",
                "  --views <file>       embed group views exported from the viewer (⋯ > Export views)",
                "  --template <file>    viewer template (default: viewer.html next to the script)",
                "",
                "check: checks the code against the rules of an architecture file (default:",
                "  architecture.json in the first project directory). Exit 0: no failing violations,",
                "  1: failing violations, 2: invalid arguments, architecture file or model.",
                "",
                "Docs: docs/quick-start.md, docs/usage.md, docs/languages.md"));
    }

    static void die(String msg) {
        throw new Failed(msg);
    }

    /** {@code depviewer check}: exit 0 no failing violations, 1 failing violations, 2 invalid input (spec FR-036). */
    static int check(String[] args, Path template) {
        Path rules = null, model = null, out = null, baseline = null;
        String viewId = null, format = "text", failOn = "error";
        boolean updateBaseline = false;
        List<Path> inputs = new ArrayList<>();
        List<String> scanOptions = new ArrayList<>();
        for (int i = 1; i < args.length; i++) {
            String a = args[i];
            boolean hasValue = i + 1 < args.length;
            switch (a) {
                case "--rules": if (!hasValue) return invalid("--rules needs a file"); rules = Paths.get(args[++i]); break;
                case "--model": if (!hasValue) return invalid("--model needs a file"); model = Paths.get(args[++i]); break;
                case "--view": if (!hasValue) return invalid("--view needs a view id"); viewId = args[++i]; break;
                case "--format": if (!hasValue) return invalid("--format needs text or json"); format = args[++i]; break;
                case "--out": case "-o": if (!hasValue) return invalid(a + " needs a file"); out = Paths.get(args[++i]); break;
                case "--baseline": if (!hasValue) return invalid("--baseline needs a file"); baseline = Paths.get(args[++i]); break;
                case "--update-baseline": updateBaseline = true; break;
                case "--fail-on": if (!hasValue) return invalid("--fail-on needs error, warning or never"); failOn = args[++i]; break;
                case "--cp": case "-cp": case "--classpath": case "--title":
                    if (!hasValue) return invalid(a + " needs a value");
                    scanOptions.add(a); scanOptions.add(args[++i]);
                    break;
                case "--no-tests": scanOptions.add(a); break;
                case "-h": case "--help": usage(); return 0;
                default:
                    if (a.startsWith("-")) return invalid("unknown option " + a);
                    inputs.add(Paths.get(a));
            }
        }
        if (!format.equals("text") && !format.equals("json")) return invalid("--format must be text or json");
        if (!Set.of("error", "warning", "never").contains(failOn)) return invalid("--fail-on must be error, warning or never");
        if (updateBaseline && baseline == null) return invalid("--update-baseline needs --baseline <file>");
        if (model != null && !inputs.isEmpty()) return invalid("check takes either project directories or --model, not both");
        if (model != null && !scanOptions.isEmpty()) return invalid(scanOptions.get(0) + " only applies when check scans project directories");
        if (rules == null) {
            if (model != null) return invalid("check --model needs --rules");
            Path dir = inputs.isEmpty() ? Paths.get(".") : inputs.get(0);
            rules = dir.resolve("architecture.json");
            if (!Files.exists(rules)) return invalid("no architecture.json in " + dir + " (pass --rules <file>)");
        }
        Architecture arch;
        try {
            arch = Architecture.load(rules);
        } catch (Architecture.Invalid e) {
            return invalid(e.getMessage());
        }
        List<Map<?, ?>> views = new ArrayList<>();
        for (Object v : arch.views) if (viewId == null || viewId.equals(((Map<?, ?>) v).get("id"))) views.add((Map<?, ?>) v);
        if (viewId != null && views.isEmpty()) return invalid("no view \"" + viewId + "\" in " + rules);
        List<Map<?, ?>> checked = new ArrayList<>();
        for (Map<?, ?> v : views) if (v.get("rules") instanceof List && !((List<?>) v.get("rules")).isEmpty()) checked.add(v);
        List<Map<?, ?>> patterned = new ArrayList<>(arch.viewsWithPatterns());
        patterned.retainAll(views);
        Set<String> baselineIn = new LinkedHashSet<>();
        if (baseline != null && (!updateBaseline || Files.exists(baseline))) {
            try {
                Object b = Json.parse(Files.readString(baseline));
                if (!(b instanceof List)) return invalid(baseline + ": a baseline is a JSON array of fingerprints");
                for (Object f : (List<?>) b) {
                    if (!(f instanceof String)) return invalid(baseline + ": a baseline is a JSON array of fingerprints");
                    baselineIn.add((String) f);
                }
            } catch (IOException e) {
                return invalid(baseline + ": cannot read (" + e.getMessage() + ")");
            } catch (IllegalArgumentException e) {
                return invalid(baseline + ": " + e.getMessage());
            }
        }
        if (patterned.isEmpty() && checked.isEmpty() && out == null && !updateBaseline) {
            if (format.equals("json")) {
                System.out.println(Json.write(Report.json(Map.of(), List.of(), null, Map.of(), List.of(), 0)));
            } else {
                System.out.println("no rules in " + rules);
            }
            return 0;
        }

        Path modelFile;
        Map<?, ?> modelData;
        try {
            modelFile = model != null ? model : scanToJson(inputs, template, scanOptions);
            modelData = readModel(modelFile);
        } catch (Failed e) {
            return invalid(e.getMessage());
        }
        @SuppressWarnings("unchecked") List<Object> nodes = (List<Object>) modelData.get("nodes");
        Rules.Graph g = new Rules.Graph(modelData);
        java.io.PrintStream info = format.equals("json") ? System.err : System.out;
        int overlaps = 0;
        Map<String, String[]> groups = new LinkedHashMap<>();
        Map<String, List<Rules.Violation>> byView = new LinkedHashMap<>();
        List<Rules.Violation> all = new ArrayList<>();
        for (Map<?, ?> view : views) {
            boolean p = patterned.contains(view), c = checked.contains(view);
            if (!p && !c) continue;
            Architecture.Resolution res = Architecture.resolve(view, nodes);
            for (Architecture.Overlap o : res.overlaps) info.println(o.message(String.valueOf(view.get("id"))));
            overlaps += res.overlaps.size();
            groups.put(String.valueOf(view.get("id")), res.group);
            if (!c) continue;
            List<Rules.Violation> vs = Rules.check(view, g, res);
            byView.put(String.valueOf(view.get("id")), vs);
            all.addAll(vs);
        }
        // A fingerprint starts with its view: entries of views this run does not check stay as they are.
        Set<String> checkedIds = new HashSet<>();
        for (Map<?, ?> v : checked) checkedIds.add(String.valueOf(v.get("id")));
        java.util.function.Predicate<String> ours = f -> checkedIds.contains(f.substring(0, Math.max(0, f.indexOf('|'))));
        if (updateBaseline) {
            Set<String> fps = new java.util.TreeSet<>();
            for (String f : baselineIn) if (!ours.test(f)) fps.add(f);
            int own = 0;
            for (Rules.Violation v : all) if (fps.add(v.fingerprint)) own++;
            StringBuilder sb = new StringBuilder("[");
            int k = 0;
            for (String f : fps) sb.append(k++ > 0 ? "," : "").append("\n  ").append(Model.js(f));
            sb.append(fps.isEmpty() ? "]\n" : "\n]\n");
            try {
                Files.writeString(baseline, sb.toString());
            } catch (IOException e) {
                return invalid(baseline + ": cannot write (" + e.getMessage() + ")");
            }
            info.println("wrote " + own + " fingerprints to " + baseline + (fps.size() > own ? " (kept " + (fps.size() - own) + " of other views)" : ""));
            return 0;
        }
        Set<String> current = new LinkedHashSet<>();
        for (Rules.Violation v : all) {
            v.baselined = baselineIn.contains(v.fingerprint);
            current.add(v.fingerprint);
        }
        List<String> resolved = new ArrayList<>();
        for (String f : baselineIn) if (!current.contains(f) && ours.test(f)) resolved.add(f);
        resolved.sort(null);

        if (out != null) {
            try {
                String html = viewerHtml(template, Files.readString(modelFile), Report.embed(arch.root, nodes, byView, g));
                Files.writeString(out, html);
            } catch (IOException e) {
                return invalid(out + ": cannot write (" + e.getMessage() + ")");
            } catch (Failed e) {
                return invalid(e.getMessage());
            }
        }
        if (format.equals("json")) {
            System.out.println(Json.write(Report.json(modelData, all, g, groups, resolved, overlaps)));
        } else {
            if (checked.isEmpty()) System.out.println("no rules in " + rules);
            System.out.print(Report.text(all, g, groups));
            for (String f : resolved) System.out.println("resolved  " + f);
            long errors = all.stream().filter(v -> !v.baselined && v.severity.equals("error")).count();
            long warnings = all.stream().filter(v -> !v.baselined && v.severity.equals("warning")).count();
            long baselined = all.stream().filter(v -> v.baselined).count();
            System.out.println("errors: " + errors + ", warnings: " + warnings + ", baselined: " + baselined
                    + ", resolved: " + resolved.size() + ", overlaps: " + overlaps);
        }
        for (Rules.Violation v : all)
            if (!v.baselined && (failOn.equals("warning") || failOn.equals("error") && v.severity.equals("error"))) return 1;
        return 0;
    }

    /** Scans the project directories with the given scan options into a temporary JSON model. */
    static Path scanToJson(List<Path> inputs, Path template, List<String> options) {
        try {
            Path tmp = Files.createTempFile("depviewer-check", ".json");
            tmp.toFile().deleteOnExit();
            List<String> a = new ArrayList<>(options);
            if (template != null) { a.add("--template"); a.add(template.toString()); }
            a.add("-o"); a.add(tmp.toString());
            for (Path in : inputs) a.add(in.toString());
            new DepScan().run(a.toArray(new String[0]));
            return tmp;
        } catch (Failed e) {
            throw e;
        } catch (Exception e) {
            throw new Failed("scan failed: " + e);
        }
    }

    static Map<?, ?> readModel(Path model) {
        Object root;
        try {
            root = Json.parse(Files.readString(model));
        } catch (IOException e) {
            throw new Failed(model + ": cannot read (" + e.getMessage() + ")");
        } catch (IllegalArgumentException e) {
            throw new Failed(model + ": " + e.getMessage());
        }
        String why = modelProblem(root);
        if (why != null) throw new Failed(model + ": not a depviewer model (" + why + ")");
        return (Map<?, ?>) root;
    }

    /** What makes a parsed model unusable, or null: nodes with a name and kind, parents first, edges between nodes. */
    static String modelProblem(Object root) {
        if (!(root instanceof Map)) return "expected a JSON object";
        Object ns = ((Map<?, ?>) root).get("nodes"), es = ((Map<?, ?>) root).get("edges");
        if (!(ns instanceof List) || !(es instanceof List)) return "\"nodes\" and \"edges\" must be arrays";
        List<?> nodes = (List<?>) ns;
        for (int i = 0; i < nodes.size(); i++) {
            Object o = nodes.get(i);
            if (!(o instanceof Map)) return "node " + i + " is not an object";
            Map<?, ?> m = (Map<?, ?>) o;
            if (!(m.get("n") instanceof String) || !(m.get("k") instanceof String)) return "node " + i + " needs \"n\" and \"k\"";
            Object p = m.get("p");
            if (p != null && !(p instanceof Double && (Double) p >= 0 && (Double) p < i && (Double) p == Math.floor((Double) p)))
                return "node " + i + " has no earlier parent " + p;
        }
        int n = 0;
        for (Object o : (List<?>) es) {
            if (!(o instanceof List) || ((List<?>) o).size() < 3) return "edge " + n + " is not [from, to, kind, ...]";
            List<?> e = (List<?>) o;
            for (int k = 0; k < Math.min(e.size(), 4); k++)
                if (!(e.get(k) instanceof Double) || (Double) e.get(k) < 0 || (Double) e.get(k) != Math.floor((Double) e.get(k))) return "edge " + n + " has a bad number";
            if ((Double) e.get(0) >= nodes.size() || (Double) e.get(1) >= nodes.size()) return "edge " + n + " names a node that does not exist";
            if (e.size() > 4 && e.get(4) instanceof List) for (Object l : (List<?>) e.get(4)) if (!(l instanceof Double)) return "edge " + n + " has a bad line";
            n++;
        }
        return null;
    }

    static int invalid(String msg) {
        System.err.println("depviewer check: " + msg);
        return 2;
    }

    void run(String[] args) throws Exception {
        // The wrapper puts --template before the command.
        int cmd = args.length > 1 && args[0].equals("--template") ? 2 : 0;
        if (args.length > cmd && args[cmd].equals("check")) {
            System.exit(check(Arrays.copyOfRange(args, cmd, args.length), cmd == 2 ? Paths.get(args[1]) : null));
            return;
        }
        List<Path> inputs = new ArrayList<>();
        Path out = null, template = null, views = null;
        String cp = null, title = null;
        boolean tests = true;
        for (int i = 0; i < args.length; i++) {
            String a = args[i];
            switch (a) {
                case "-o": case "--out": out = Paths.get(args[++i]); break;
                case "-cp": case "--cp": case "--classpath": cp = args[++i]; break;
                case "--no-tests": tests = false; break;
                case "--title": title = args[++i]; break;
                case "--template": template = Paths.get(args[++i]); break;
                case "--views": views = Paths.get(args[++i]); break;
                case "-h": case "--help": usage(); return;
                default:
                    if (a.startsWith("-")) die("unknown option " + a);
                    inputs.add(Paths.get(a));
            }
        }
        if (inputs.isEmpty()) inputs.add(Paths.get("."));
        if (out == null) out = Paths.get("deps.html");

        List<Root> roots = new ArrayList<>();
        for (Path in : inputs) {
            Path abs = in.toAbsolutePath().normalize();
            if (!Files.isDirectory(abs)) die("not a directory: " + in);
            roots.addAll(findRoots(abs, tests));
        }
        if (title == null) title = inputs.get(0).toAbsolutePath().normalize().getFileName().toString();
        List<Toolchain.Module> tsModules = new ArrayList<>(), dartModules = new ArrayList<>();
        for (Path in : inputs) findToolchainModules(in.toAbsolutePath().normalize(), tsModules, dartModules);

        Map<Path, Root> rootOfFile = new LinkedHashMap<>(), rootOfKt = new LinkedHashMap<>();
        for (Root r : roots) {
            try (var s = Files.walk(r.dir)) {
                s.filter(p -> !p.getFileName().toString().equals("module-info.java"))
                        .filter(p -> !p.getFileName().toString().equals("package-info.java"))
                        .filter(p -> !skipped(r.dir.relativize(p)))
                        .forEach(p -> {
                            String f = p.getFileName().toString();
                            if (f.endsWith(".java")) rootOfFile.putIfAbsent(p.toAbsolutePath().normalize(), r);
                            else if (f.endsWith(".kt")) rootOfKt.putIfAbsent(p.toAbsolutePath().normalize(), r);
                        });
            }
        }
        int tsFiles = tsModules.stream().mapToInt(m -> m.files.size()).sum(), dartFiles = dartModules.stream().mapToInt(m -> m.files.size()).sum();
        if (rootOfFile.isEmpty() && rootOfKt.isEmpty() && tsFiles == 0 && dartFiles == 0) die("no source files found (.java, .kt, .ts/.js, .dart)");
        StringBuilder what = new StringBuilder();
        if (!rootOfFile.isEmpty() || !rootOfKt.isEmpty()) {
            long modules = roots.stream().filter(r -> rootOfFile.containsValue(r) || rootOfKt.containsValue(r)).map(r -> r.module).distinct().count();
            what.append(String.format("%d Java and %d Kotlin files in %d module(s)", rootOfFile.size(), rootOfKt.size(), modules));
        }
        if (tsFiles > 0) what.append(what.length() > 0 ? ", " : "").append(String.format("%d TypeScript/JavaScript files in %d module(s)", tsFiles, tsModules.size()));
        if (dartFiles > 0) what.append(what.length() > 0 ? ", " : "").append(String.format("%d Dart files in %d module(s)", dartFiles, dartModules.size()));
        System.err.println("Scanning " + what + "...");

        // Kotlin declarations first, so that Java references to Kotlin types (unresolvable for
        // javac) can be matched by name when the Java references are resolved.
        KotlinScanner kotlin = new KotlinScanner();
        for (Map.Entry<Path, Root> e : rootOfKt.entrySet()) kotlin.declare(e.getKey(), e.getValue());

        int[] errors = {0};
        List<String> firstErrors = new ArrayList<>();
        JavaScanner java = new JavaScanner();
        if (!rootOfFile.isEmpty()) java.scan(rootOfFile, roots, cp, errors, firstErrors);
        java.resolve();
        kotlin.resolveAll();
        Path home = Paths.get(Toolchain.env("DEPVIEWER_HOME", template != null && template.toAbsolutePath().getParent() != null
                ? template.toAbsolutePath().getParent().toString() : ".")).toAbsolutePath().normalize();
        List<Toolchain> toolchains = List.of(new Toolchain("typescript", "TypeScript/JavaScript", home), new Toolchain("dart", "Dart", home));
        int totalErrors = errors[0];
        for (Toolchain tc : toolchains) {
            tc.scan(tc.language.equals("dart") ? dartModules : tsModules.stream().filter(m -> !m.files.isEmpty()).collect(Collectors.toList()), tests);
            totalErrors += tc.errors;
        }
        String json = toJson(title, edges.values(), totalErrors);
        writeOutput(out, template, views, json);

        Set<String> typeKinds = Set.of("class", "interface", "enum", "record", "annotation", "object", "typealias", "namespace", "mixin", "extension");
        Set<String> memberKinds = Set.of("ctor", "method", "field", "enumconst");
        long typeCount = nodes.stream().filter(n -> typeKinds.contains(n.kind)).count();
        long memberCount = nodes.stream().filter(n -> memberKinds.contains(n.kind)).count();
        int depCount = edges.values().stream().mapToInt(e -> e.count).sum();
        System.err.printf("%d types, %d members, %d dependencies (%d distinct) -> %s%n",
                typeCount, memberCount, depCount, edges.size(), out.toAbsolutePath().normalize());
        if (errors[0] > 0) {
            System.err.printf("note: %d Java compile errors; unresolved references are listed under External > (unresolved).%n", errors[0]);
            System.err.println("      Missing libraries? Run 'mvn dependency:copy-dependencies' or pass --cp.");
            if (!rootOfKt.isEmpty()) System.err.println("      (Java references to Kotlin code are expected here; they are matched by name.)");
            firstErrors.forEach(e -> System.err.println("      " + e));
        }
        for (Toolchain tc : toolchains) {
            if (tc.errors > 0) System.err.printf("note: %d %s errors reported by the %s toolchain%s%n", tc.errors, tc.label, tc.label,
                    tc.language.equals("dart") ? " (unresolved imports? run 'dart pub get' / 'flutter pub get' in the project)" : "");
        }
    }

    void writeOutput(Path out, Path template, Path views, String json) throws IOException {
        if (out.toString().endsWith(".json")) {
            Files.writeString(out, json);
            return;
        }
        String v = null;
        if (views != null) {
            Architecture arch;
            try {
                arch = Architecture.load(views);
            } catch (Architecture.Invalid e) {
                die("--views " + e.getMessage());
                return;
            }
            v = arch.text.trim();
            if (!arch.viewsWithPatterns().isEmpty() || arch.hasRules()) v = resolvedViews(arch, json);
        }
        Files.writeString(out, viewerHtml(template, json, v));
    }

    /** The views with their pattern assignments and violations resolved against the model (spec FR-041, FR-042). */
    static String resolvedViews(Architecture arch, String json) {
        Map<?, ?> model = (Map<?, ?>) Json.parse(json);
        @SuppressWarnings("unchecked") List<Object> nodes = (List<Object>) model.get("nodes");
        Rules.Graph g = new Rules.Graph(model);
        Map<String, List<Rules.Violation>> byView = new LinkedHashMap<>();
        for (Object o : arch.views) {
            Map<?, ?> view = (Map<?, ?>) o;
            if (view.get("rules") instanceof List && !((List<?>) view.get("rules")).isEmpty())
                byView.put(String.valueOf(view.get("id")), Rules.check(view, g, Architecture.resolve(view, nodes)));
        }
        return Report.embed(arch.root, nodes, byView, g);
    }

    /** The viewer template with the model and, if given, the views embedded. */
    static String viewerHtml(Path template, String json, String views) throws IOException {
        if (template == null) template = Paths.get("viewer.html");
        if (!Files.exists(template)) die("viewer template not found: " + template + " (use --template, or -o file.json)");
        String html = Files.readString(template);
        String marker = "/*DEPS_DATA*/null";
        if (!html.contains(marker)) die("template has no data marker: " + template);
        html = html.replace(marker, json);
        if (views != null) html = html.replace("/*DEPS_VIEWS*/null", views.replace("</", "<\\/"));
        return html;
    }

    // ---------------------------------------------------------------- roots

    List<Root> findRoots(Path in, boolean tests) throws IOException {
        List<Root> found = new ArrayList<>();
        List<Path> mains = List.of(Paths.get("src", "main", "java"), Paths.get("src", "main", "kotlin"));
        List<Path> tests_ = List.of(Paths.get("src", "test", "java"), Paths.get("src", "test", "kotlin"));
        Files.walkFileTree(in, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                if (!dir.equals(in) && SKIP_DIRS.contains(dir.getFileName().toString())) return FileVisitResult.SKIP_SUBTREE;
                boolean isMain = mains.stream().anyMatch(dir::endsWith), isTest = tests_.stream().anyMatch(dir::endsWith);
                if (isMain || isTest) {
                    if (isMain || tests) {
                        Path project = dir.getParent().getParent().getParent();
                        found.add(new Root(dir, project, moduleName(project), isTest));
                    }
                    return FileVisitResult.SKIP_SUBTREE;
                }
                return FileVisitResult.CONTINUE;
            }
        });
        if (found.isEmpty()) found.add(new Root(in, in, in.getFileName().toString(), false));
        return found;
    }

    /** Folders with package.json (TypeScript/JavaScript) or pubspec.yaml (Dart) below `in`, with their files. */
    static void findToolchainModules(Path in, List<Toolchain.Module> ts, List<Toolchain.Module> dart) throws IOException {
        Files.walkFileTree(in, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                if (!dir.equals(in) && SKIP_DIRS.contains(dir.getFileName().toString())) return FileVisitResult.SKIP_SUBTREE;
                if (Files.isRegularFile(dir.resolve("package.json"))) ts.add(tsModule(dir));
                if (Files.isRegularFile(dir.resolve("pubspec.yaml"))) dart.add(dartModule(dir));
                return FileVisitResult.CONTINUE;
            }
        });
    }

    static Toolchain.Module tsModule(Path dir) throws IOException {
        String name = null;
        try {
            Object pj = Json.parse(Files.readString(dir.resolve("package.json")));
            if (pj instanceof Map) name = Toolchain.str(((Map<?, ?>) pj).get("name"));
        } catch (IllegalArgumentException ignored) {
        }
        Toolchain.Module m = new Toolchain.Module(dir, name != null ? name : dir.getFileName().toString());
        Files.walkFileTree(dir, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path d, BasicFileAttributes attrs) {
                if (d.equals(dir)) return FileVisitResult.CONTINUE;
                if (SKIP_DIRS.contains(d.getFileName().toString()) || Files.exists(d.resolve("package.json"))) return FileVisitResult.SKIP_SUBTREE;
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path f, BasicFileAttributes attrs) {
                String rel = dir.relativize(f).toString().replace('\\', '/');
                if (TS_FILE.matcher(rel).matches() && !rel.endsWith(".d.ts") && !rel.matches(".*\\.d\\.[cm]ts$"))
                    m.files.add(new String[]{rel, TS_TEST.matcher(rel).matches() ? "1" : "0"});
                return FileVisitResult.CONTINUE;
            }
        });
        m.files.sort(Comparator.comparing(f -> f[0]));
        return m;
    }

    static Toolchain.Module dartModule(Path dir) throws IOException {
        Matcher nm = Pattern.compile("(?m)^name:\\s*['\"]?([\\w.-]+)").matcher(Files.readString(dir.resolve("pubspec.yaml")));
        Toolchain.Module m = new Toolchain.Module(dir, nm.find() ? nm.group(1) : dir.getFileName().toString());
        for (String sub : concat(DART_MAIN, DART_TEST)) {
            Path root = dir.resolve(sub);
            if (!Files.isDirectory(root)) continue;
            String test = DART_TEST.contains(sub) ? "1" : "0";
            try (var s = Files.walk(root)) {
                s.filter(p -> p.toString().endsWith(".dart") && !skipped(root.relativize(p)))   // `bin/` is a Dart source folder
                        .forEach(p -> m.files.add(new String[]{dir.relativize(p).toString().replace('\\', '/'), test}));
            }
        }
        m.files.sort(Comparator.comparing(f -> f[0]));
        return m;
    }

    static List<String> concat(List<String> a, List<String> b) {
        List<String> l = new ArrayList<>(a);
        l.addAll(b);
        return l;
    }

    static String moduleName(Path project) {
        Path pom = project.resolve("pom.xml");
        if (Files.exists(pom)) {
            try {
                String xml = Files.readString(pom).replaceAll("(?s)<!--.*?-->", "").replaceAll("(?s)<parent>.*?</parent>", "");
                Matcher m = Pattern.compile("<artifactId>\\s*([^<\\s]+)\\s*</artifactId>").matcher(xml);
                if (m.find()) return m.group(1);
            } catch (IOException ignored) {
            }
        }
        return project.getFileName() == null ? "project" : project.getFileName().toString();
    }

    static String buildClasspath(String cp, List<Root> roots) throws IOException {
        List<String> parts = new ArrayList<>();
        if (cp != null && !cp.isEmpty()) parts.add(cp);
        Set<Path> projects = roots.stream().map(r -> r.projectDir).collect(Collectors.toCollection(LinkedHashSet::new));
        for (Path p : projects) {
            for (Path libDir : List.of(p.resolve("target/dependency"), p.resolve("lib"), p.resolve("libs"))) {
                if (!Files.isDirectory(libDir)) continue;
                try (var s = Files.list(libDir)) {
                    s.filter(f -> f.toString().endsWith(".jar")).sorted().forEach(f -> parts.add(f.toString()));
                }
            }
        }
        return String.join(java.io.File.pathSeparator, parts);
    }

}
