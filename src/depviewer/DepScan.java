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
 * Scans Java and Kotlin sources and writes a dependency model: module > package > file >
 * type > method/field, plus the dependencies between them. Output is a self-contained HTML
 * viewer (or JSON).
 *
 * Java is parsed and attributed by the JDK compiler (exact symbol resolution). Kotlin has no
 * compiler in the JDK, so Kotlin files are read by a small built-in parser and references are
 * resolved by name (imports, same package, declared/inferred types) - see the Kotlin section.
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
        new DepScan().run(args);
    }

    static void usage() {
        System.out.println(String.join("\n",
                "Usage: depviewer [options] <project-or-source-dir>...",
                "",
                "  Finds src/main/{java,kotlin} (and src/test/{java,kotlin}) folders below each directory;",
                "  a directory without them is scanned as a plain source root. Java and Kotlin files",
                "  (.java, .kt) are supported.",
                "",
                "Options:",
                "  -o, --out <file>     output file: .html (viewer, default deps.html) or .json (data only)",
                "  --cp <classpath>     extra classpath for resolving libraries (jars from",
                "                       target/dependency are picked up automatically)",
                "  --no-tests           skip test source folders",
                "  --title <text>       title shown in the viewer",
                "  --views <file>       embed group views exported from the viewer (⋯ > Export views)",
                "  --template <file>    viewer template (default: viewer.html next to the script)"));
    }

    static void die(String msg) {
        System.err.println("depviewer: " + msg);
        System.exit(1);
    }

    void run(String[] args) throws Exception {
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
        } else {
            if (template == null) template = Paths.get("viewer.html");
            if (!Files.exists(template)) die("viewer template not found: " + template + " (use --template, or -o file.json)");
            String html = Files.readString(template);
            String marker = "/*DEPS_DATA*/null";
            if (!html.contains(marker)) die("template has no data marker: " + template);
            html = html.replace(marker, json);
            if (views != null) {
                String v = Files.readString(views).trim();
                if (!v.startsWith("{") && !v.startsWith("[")) die("--views file is not JSON: " + views);
                html = html.replace("/*DEPS_VIEWS*/null", v.replace("</", "<\\/"));
            }
            Files.writeString(out, html);
        }
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
