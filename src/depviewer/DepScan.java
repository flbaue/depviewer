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
            ".idea", ".gradle", ".svn", ".settings");

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

        Map<Path, Root> rootOfFile = new LinkedHashMap<>(), rootOfKt = new LinkedHashMap<>();
        for (Root r : roots) {
            try (var s = Files.walk(r.dir)) {
                s.filter(p -> !p.getFileName().toString().equals("module-info.java"))
                        .filter(p -> !p.getFileName().toString().equals("package-info.java"))
                        .filter(p -> SKIP_DIRS.stream().noneMatch(d -> r.dir.relativize(p).toString().startsWith(d + "/")))
                        .forEach(p -> {
                            String f = p.getFileName().toString();
                            if (f.endsWith(".java")) rootOfFile.putIfAbsent(p.toAbsolutePath().normalize(), r);
                            else if (f.endsWith(".kt")) rootOfKt.putIfAbsent(p.toAbsolutePath().normalize(), r);
                        });
            }
        }
        if (rootOfFile.isEmpty() && rootOfKt.isEmpty()) die("no .java or .kt files found");
        long modules = roots.stream().map(r -> r.module).distinct().count();
        System.err.printf("Scanning %d Java and %d Kotlin files in %d module(s)...%n", rootOfFile.size(), rootOfKt.size(), modules);

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
        String json = toJson(title, edges.values(), errors[0]);
        writeOutput(out, template, views, json);

        long typeCount = nodes.stream().filter(n -> n.id.startsWith("T:")).count();
        long memberCount = nodes.stream().filter(n -> n.id.startsWith("M:")).count();
        int depCount = edges.values().stream().mapToInt(e -> e.count).sum();
        System.err.printf("%d types, %d members, %d dependencies (%d distinct) -> %s%n",
                typeCount, memberCount, depCount, edges.size(), out.toAbsolutePath().normalize());
        if (errors[0] > 0) {
            System.err.printf("note: %d Java compile errors; unresolved references are listed under External > (unresolved).%n", errors[0]);
            System.err.println("      Missing libraries? Run 'mvn dependency:copy-dependencies' or pass --cp.");
            if (!rootOfKt.isEmpty()) System.err.println("      (Java references to Kotlin code are expected here; they are matched by name.)");
            firstErrors.forEach(e -> System.err.println("      " + e));
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
