package depviewer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static depviewer.Model.*;

/**
 * Languages analysed by their official toolchain: an extractor in extractors/&lt;language&gt;/ runs as a
 * separate process and prints a JSON fragment (spec/wiki/crosscutting-concepts/CC-009-fragment-format.md), which is merged here.
 */
final class Toolchain {

    /** One module (package.json / pubspec.yaml folder) and the files to analyse. */
    static final class Module {
        final Path dir;
        final String name;
        final List<String[]> files = new ArrayList<>();   // {relative path, "1" if test}

        Module(Path dir, String name) { this.dir = dir; this.name = name; }
    }

    final String language, label;
    final Path home;
    int errors;

    Toolchain(String language, String label, Path home) {
        this.language = language;
        this.label = label;
        this.home = home;
    }

    /** Scans all modules; prints a note and returns quietly when the toolchain is not installed. */
    void scan(List<Module> modules, boolean tests) throws IOException, InterruptedException {
        if (modules.isEmpty()) return;
        List<String> base = command();
        if (base == null) return;
        // Dart: one run over all packages, so references between them (workspaces, path
        // dependencies) resolve to the real declarations. TypeScript: one run per module.
        List<List<Module>> runs = new ArrayList<>();
        if (language.equals("dart")) runs.add(modules);
        else for (Module m : modules) runs.add(List.of(m));
        for (List<Module> run : runs) {
            String names = run.stream().map(m -> m.name).collect(java.util.stream.Collectors.joining(", "));
            Path input = Files.createTempFile("depviewer-" + language, ".json");
            try {
                Module first = run.get(0);
                StringBuilder sb = new StringBuilder("{\"root\":").append(js(first.dir.toString()))
                        .append(",\"module\":").append(js(first.name)).append(",\"files\":[");
                boolean firstFile = true;
                for (Module m : run) {
                    for (String[] f : m.files) {
                        if (!tests && f[1].equals("1")) continue;
                        if (!firstFile) sb.append(',');
                        firstFile = false;
                        sb.append("{\"path\":").append(js(f[0])).append(",\"test\":").append(f[1].equals("1"))
                                .append(",\"module\":").append(js(m.name)).append(",\"root\":").append(js(m.dir.toString())).append('}');
                    }
                }
                Files.writeString(input, sb.append("]}").toString());
                List<String> cmd = new ArrayList<>(base);
                cmd.add(input.toString());
                try {
                    ProcessBuilder pb = new ProcessBuilder(cmd).redirectError(ProcessBuilder.Redirect.INHERIT);
                    Process p = pb.start();
                    String out;
                    try (InputStream in = p.getInputStream()) { out = new String(in.readAllBytes(), StandardCharsets.UTF_8); }
                    int code = p.waitFor();
                    if (code != 0) { System.err.printf("%s: extractor failed for %s (exit code %d), skipped%n", label, names, code); continue; }
                    merge(first.name, Json.parse(out));
                } catch (IOException | IllegalArgumentException | ClassCastException e) {
                    System.err.printf("%s: extractor failed for %s (%s), skipped%n", label, names, e.getMessage());
                }
            } finally {
                Files.deleteIfExists(input);
            }
        }
    }

    // ------------------------------------------------ toolchain commands

    List<String> command() throws IOException, InterruptedException {
        switch (language) {
            case "typescript": return nodeJs();
            case "dart": return dart();
            default: throw new IllegalStateException(language);
        }
    }

    List<String> nodeJs() throws IOException, InterruptedException {
        String node = env("DEPVIEWER_NODE", "node");
        if (!works(node, "--version")) {
            System.err.println(label + ": skipped - Node.js not found (install Node 18+ or set DEPVIEWER_NODE).");
            return null;
        }
        Path dir = home.resolve("extractors/typescript");
        if (!Files.exists(dir.resolve("node_modules/typescript/package.json"))) {
            System.err.println(label + ": installing the extractor's dependencies (npm ci, one time)...");
            Path npm = Path.of(node).getParent() != null ? Path.of(node).resolveSibling("npm") : Path.of("npm");
            String npmCmd = Files.isExecutable(npm) ? npm.toString() : "npm";
            if (run(dir, List.of(npmCmd, "ci", "--no-audit", "--no-fund", "--loglevel=error"), node) != 0) {
                System.err.println(label + ": skipped - 'npm ci' failed in " + dir);
                return null;
            }
        }
        return new ArrayList<>(List.of(node, dir.resolve("extract.mjs").toString()));
    }

    List<String> dart() throws IOException, InterruptedException {
        String dart = env("DEPVIEWER_DART", "dart");
        if (!works(dart, "--version")) {
            System.err.println(label + ": skipped - Dart SDK not found (install Dart 3 or set DEPVIEWER_DART).");
            return null;
        }
        Path dir = home.resolve("extractors/dart");
        Path config = dir.resolve(".dart_tool/package_config.json");
        if (!Files.exists(config)) {
            System.err.println(label + ": installing the extractor's dependencies (dart pub get, one time)...");
            if (run(dir, List.of(dart, "pub", "get"), null) != 0) {
                System.err.println(label + ": skipped - 'dart pub get' failed in " + dir);
                return null;
            }
        }
        return new ArrayList<>(List.of(dart, "--packages=" + config, dir.resolve("bin/extract.dart").toString()));
    }

    static String env(String name, String def) {
        String v = System.getenv(name);
        return v == null || v.isEmpty() ? def : v;
    }

    static boolean works(String exe, String arg) {
        try {
            Process p = new ProcessBuilder(exe, arg).redirectErrorStream(true).start();
            p.getInputStream().readAllBytes();
            return p.waitFor() == 0;
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }

    /** Runs a setup command; when nodeBin is given, its folder is put first on PATH (npm needs node). */
    static int run(Path dir, List<String> cmd, String nodeBin) throws InterruptedException {
        if (!Files.isDirectory(dir)) {
            System.err.println("extractor folder missing: " + dir);
            return -1;
        }
        ProcessBuilder pb = new ProcessBuilder(cmd).directory(dir.toFile()).redirectOutput(ProcessBuilder.Redirect.DISCARD)
                .redirectError(ProcessBuilder.Redirect.INHERIT);
        if (nodeBin != null && Path.of(nodeBin).getParent() != null) {
            pb.environment().put("PATH", Path.of(nodeBin).getParent() + java.io.File.pathSeparator + System.getenv("PATH"));
        }
        try {
            return pb.start().waitFor();
        } catch (IOException e) {
            System.err.println("could not run " + cmd.get(0) + ": " + e.getMessage());
            return -1;
        }
    }

    // ------------------------------------------------ merging

    @SuppressWarnings("unchecked")
    void merge(String module, Object parsed) {
        Map<String, Object> frag = (Map<String, Object>) parsed;
        errors += intOf(frag.get("errors"));
        String prefix = "L:" + language + ":" + module + ":";
        Map<String, Integer> ids = new HashMap<>();
        Set<String> dropped = new HashSet<>();
        for (Object o : (List<Object>) frag.getOrDefault("nodes", List.of())) {
            Map<String, Object> n = (Map<String, Object>) o;
            String id = str(n.get("id")), kind = str(n.get("kind"));
            if (id == null || kind == null) continue;
            if (kind.equals("file")) {
                String path = str(n.get("path"));
                String pkg = str(n.getOrDefault("package", ""));
                String fileModule = n.get("module") != null ? str(n.get("module")) : module;   // multi-module runs
                Node f = node("F:" + fileModule + ":" + path, path.substring(path.lastIndexOf('/') + 1), "file", folderPackage(fileModule, pkg == null ? "" : pkg));
                f.path = path;
                f.test = Boolean.TRUE.equals(n.get("test"));
                f.line = 1;
                f.loc = intOf(n.get("loc"));
                ids.put(id, f.idx);
                continue;
            }
            Integer parent = ids.get(str(n.get("parent")));
            if (parent == null) { dropped.add(id); continue; }
            Node x = node(prefix + id, str(n.get("name")), kind, parent);
            x.q = str(n.getOrDefault("q", id));
            x.line = intOf(n.get("line"));
            x.loc = intOf(n.get("loc"));
            x.vis = str(n.get("vis"));
            x.isStatic = Boolean.TRUE.equals(n.get("static"));
            x.test = nodes.get(parent).test;
            ids.put(id, x.idx);
        }
        for (Object o : (List<Object>) frag.getOrDefault("externals", List.of())) {
            Map<String, Object> x = (Map<String, Object>) o;
            String pkg = str(x.get("package")), name = str(x.get("name"));
            if (pkg == null || name == null) continue;
            ids.put(str(x.get("id")), external(pkg, pkg + "." + name, name, null));
        }
        int missing = 0;
        for (Object o : (List<Object>) frag.getOrDefault("edges", List.of())) {
            Map<String, Object> e = (Map<String, Object>) o;
            Integer from = ids.get(str(e.get("from"))), to = ids.get(str(e.get("to")));
            int kind = EDGE_KINDS.indexOf(str(e.get("kind")));
            if (from == null || to == null || kind < 0) { missing++; continue; }
            addEdge(from, to, kind, intOf(e.get("line")));
        }
        if (!dropped.isEmpty() || missing > 0) {
            System.err.printf("%s: warning: %d node(s) and %d dependencies of %s had unknown references and were dropped%n",
                    label, dropped.size(), missing, module);
        }
    }

    static String str(Object o) { return o == null ? null : o.toString(); }

    static int intOf(Object o) { return o instanceof Number ? ((Number) o).intValue() : 0; }
}
