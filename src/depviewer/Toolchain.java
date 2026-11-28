package depviewer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
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

    /** The toolchain chosen for a set of modules, as the extractor's --describe reported it. */
    static final class Choice {
        final String tool, version, source, path, extractor, extractorVersion;
        final boolean exact;
        final List<String> command;   // the extractor command, without the request file

        Choice(Map<?, ?> describe, List<String> command) {
            Map<?, ?> t = (Map<?, ?>) describe.get("toolchain");
            tool = str(t.get("tool"));
            version = str(t.get("version"));
            source = str(t.get("source"));
            path = str(t.get("path"));
            extractor = str(describe.get("name"));
            extractorVersion = str(describe.get("version"));
            exact = !Boolean.FALSE.equals(describe.get("exact"));
            this.command = command;
        }

        /** Modules whose toolchain is the same folder run together, also through symlinks (pnpm links it into every package). */
        String key() {
            try {
                return Path.of(path).toRealPath().toString();
            } catch (IOException | RuntimeException e) {
                return path;
            }
        }

        @Override public String toString() { return tool + " " + version + " (" + source + ": " + path + ")"; }
    }

    /** A place to look for a toolchain: where it came from and the folder or executable. */
    record Candidate(String source, String location) {}

    final String language, label;
    final Path home;
    int errors;
    private String node;                                   // TypeScript: the Node.js that runs the extractor
    private final Map<String, Choice> described = new HashMap<>();
    private final Set<String> reported = new HashSet<>();
    private Boolean pinnedReady;
    private final Map<String, Boolean> pubGet = new HashMap<>();   // Dart: dart executable -> extractor dependencies installed

    Toolchain(String language, String label, Path home) {
        this.language = language;
        this.label = label;
        this.home = home;
    }

    /** Scans all modules; prints a note and returns quietly when no supported toolchain is found. */
    void scan(List<Module> modules, boolean tests) throws IOException, InterruptedException {
        if (modules.isEmpty()) return;
        if (language.equals("typescript")) {
            node = env("DEPVIEWER_NODE", "node");
            if (!works(node, "--version")) {
                System.err.println(label + ": skipped - Node.js not found (install Node 18+ or set DEPVIEWER_NODE).");
                return;
            }
        }
        // The toolchain is chosen per module (spec FR-010), and each extractor runs once per toolchain
        // with all its modules, so references between them (workspaces, path dependencies) resolve
        // to the real declarations (spec FR-012).
        Map<String, Map<Module, Choice>> runs = new LinkedHashMap<>();
        for (Module m : modules) {
            Choice c = choose(m);
            if (c != null) runs.computeIfAbsent(c.key(), k -> new LinkedHashMap<>()).put(m, c);
        }
        for (Map<Module, Choice> run : runs.values()) run(run, tests);
    }

    /** One extractor run. Its modules share one toolchain, but may have found it in different places. */
    private void run(Map<Module, Choice> chosen, boolean tests) throws IOException, InterruptedException {
        List<Module> run = new ArrayList<>(chosen.keySet());
        Choice choice = chosen.get(run.get(0));
        // One log line and provenance entry per source, so each module's source is recorded (spec FR-011).
        Map<String, List<Module>> bySource = new LinkedHashMap<>();
        for (Map.Entry<Module, Choice> e : chosen.entrySet()) bySource.computeIfAbsent(e.getValue().source, k -> new ArrayList<>()).add(e.getKey());
        for (List<Module> ms : bySource.values()) System.err.printf("%s: %s for %s%n", label, chosen.get(ms.get(0)), names(ms));
        String names = names(run);
        Path input = Files.createTempFile("depviewer-" + language, ".json");
        try {
            Module first = run.get(0);
            StringBuilder sb = new StringBuilder("{\"root\":").append(js(first.dir.toString()))
                    .append(",\"module\":").append(js(first.name));
            if (language.equals("typescript")) sb.append(",\"typescript\":").append(js(choice.path));
            sb.append(",\"files\":[");
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
            List<String> cmd = new ArrayList<>(choice.command);
            cmd.add(input.toString());
            try {
                ProcessBuilder pb = new ProcessBuilder(cmd).redirectError(ProcessBuilder.Redirect.INHERIT);
                Process p = pb.start();
                String out;
                try (InputStream in = p.getInputStream()) { out = new String(in.readAllBytes(), StandardCharsets.UTF_8); }
                int code = p.waitFor();
                if (code != 0) { System.err.printf("%s: extractor failed for %s (exit code %d), skipped%n", label, names, code); return; }
                merge(first.name, Json.parse(out));
                for (List<Module> ms : bySource.values()) {
                    Choice c = chosen.get(ms.get(0));
                    provenance(language, ms.stream().map(m -> m.name).collect(java.util.stream.Collectors.toList()), c.tool,
                            c.version, c.source, c.exact, c.extractor, c.extractorVersion);
                }
            } catch (IOException | IllegalArgumentException | ClassCastException e) {
                System.err.printf("%s: extractor failed for %s (%s), skipped%n", label, names, e.getMessage());
            }
        } finally {
            Files.deleteIfExists(input);
        }
    }

    static String names(List<Module> ms) { return ms.stream().map(m -> m.name).collect(java.util.stream.Collectors.joining(", ")); }

    // ------------------------------------------------ choosing the toolchain (spec FR-010)

    /** The first candidate whose version the extractor supports, or null (the module is skipped). */
    Choice choose(Module m) throws IOException, InterruptedException {
        String tool = language.equals("dart") ? "Dart SDK" : "TypeScript";
        for (Candidate c : candidates(m)) {
            String key = c.source() + " " + c.location();
            Choice known = described.get(key);
            if (known != null) return known;
            if (reported.contains(key)) continue;   // unsupported, already reported
            if (c.source().equals("pinned") && language.equals("typescript") && !pinnedTypeScript()) continue;
            Object d = describe(c);
            Map<?, ?> t = d instanceof Map && ((Map<?, ?>) d).get("toolchain") instanceof Map ? (Map<?, ?>) ((Map<?, ?>) d).get("toolchain") : null;
            if (t != null && Boolean.TRUE.equals(t.get("supported"))) {
                List<String> command = extractorCommand(c);
                if (command == null) continue;   // reported by extractorCommand
                Choice choice = new Choice((Map<?, ?>) d, command);
                described.put(key, choice);
                return choice;
            }
            if (!reported.add(key)) continue;
            String where = c.source() + ": " + c.location();
            if (t == null) System.err.printf("%s: %s (%s) could not be run, trying the next one%n", label, tool, where);
            else if (t.get("version") == null) System.err.printf("%s: %s (%s): %s, trying the next one%n", label, tool, where, t.get("problem"));
            else System.err.printf("%s: %s %s (%s) is not supported (needs %s), trying the next one%n",
                    label, tool, t.get("version"), where, t.get("range"));
        }
        System.err.printf("%s: skipped %s - no supported %s found (%s).%n", label, m.name, tool,
                language.equals("dart") ? "install Dart 3 or set DEPVIEWER_DART" : "add typescript to the project or set DEPVIEWER_TYPESCRIPT");
        return null;
    }

    /** Where to look, in this order: override, the project's own, the PATH, the pinned copy. */
    List<Candidate> candidates(Module m) throws IOException, InterruptedException {
        List<Candidate> list = new ArrayList<>();
        if (language.equals("typescript")) {
            String override = System.getenv("DEPVIEWER_TYPESCRIPT");
            if (override != null && !override.isEmpty()) list.add(new Candidate("override", Path.of(override).toAbsolutePath().normalize().toString()));
            for (Path d = m.dir; d != null; d = d.getParent()) {   // as Node resolves `typescript` from the module
                Path ts = d.resolve("node_modules/typescript");
                if (Files.isRegularFile(ts.resolve("package.json"))) { list.add(new Candidate("project", ts.toString())); break; }
            }
            Path tsc = onPath("tsc");
            if (tsc != null) {
                Path pkg = tsc.toRealPath().getParent().getParent();   // <package>/bin/tsc
                if (pkg != null && Files.isRegularFile(pkg.resolve("package.json"))) list.add(new Candidate("path", pkg.toString()));
            }
            list.add(new Candidate("pinned", home.resolve("extractors/typescript/node_modules/typescript").toString()));   // npm ci when reached
        } else {
            String override = System.getenv("DEPVIEWER_DART");
            if (override != null && !override.isEmpty()) list.add(new Candidate("override", override));
            Path project = projectDart(m.dir);
            if (project != null) list.add(new Candidate("project", project.toString()));
            Path dart = onPath("dart");
            if (dart != null) list.add(new Candidate("path", dart.toString()));
            // No pinned Dart SDK: it is too large to download on demand.
        }
        return list;
    }

    /** Flutter projects name their SDK: FVM's .fvm/flutter_sdk, or flutterRoot in .dart_tool/package_config.json. */
    static Path projectDart(Path dir) throws IOException {
        for (Path d = dir; d != null; d = d.getParent()) {
            Path fvm = d.resolve(".fvm/flutter_sdk/bin/dart");
            if (Files.isExecutable(fvm)) return fvm;
            Path config = d.resolve(".dart_tool/package_config.json");
            if (Files.isRegularFile(config)) {
                try {
                    Object root = ((Map<?, ?>) Json.parse(Files.readString(config))).get("flutterRoot");
                    if (root != null) {
                        Path dart = Path.of(java.net.URI.create(root.toString())).resolve("bin/dart");
                        if (Files.isExecutable(dart)) return dart;
                    }
                } catch (IllegalArgumentException | ClassCastException | java.nio.file.FileSystemNotFoundException ignored) {
                }
                return null;   // the nearest package config decides
            }
        }
        return null;
    }

    /** The first executable `name` on the PATH, or null. */
    static Path onPath(String name) {
        String path = System.getenv("PATH");
        if (path == null) return null;
        for (String dir : path.split(java.io.File.pathSeparator)) {
            if (dir.isEmpty()) continue;
            Path p = Path.of(dir, name);
            if (Files.isRegularFile(p) && Files.isExecutable(p)) return p;
        }
        return null;
    }

    /** The extractor's --describe output for a candidate, or null if it could not run. */
    Object describe(Candidate c) throws IOException, InterruptedException {
        List<String> cmd = new ArrayList<>();
        if (language.equals("typescript")) {
            cmd.addAll(List.of(node, home.resolve("extractors/typescript/extract.mjs").toString(), "--describe", c.location()));
        } else {
            // describe.dart imports nothing, so an SDK too old for the analyzer can still say which version it is.
            if (!works(c.location(), "--version")) return null;
            cmd.addAll(List.of(c.location(), "--packages=" + home.resolve("extractors/dart/describe_packages.json"),
                    home.resolve("extractors/dart/bin/describe.dart").toString()));
        }
        cmd.add(c.source());
        try {
            Process p = new ProcessBuilder(cmd).redirectError(ProcessBuilder.Redirect.DISCARD).start();
            String out;
            try (InputStream in = p.getInputStream()) { out = new String(in.readAllBytes(), StandardCharsets.UTF_8); }
            return p.waitFor() == 0 ? Json.parse(out.trim()) : null;
        } catch (IOException | IllegalArgumentException e) {
            return null;
        }
    }

    /** The command that runs the extractor with a candidate's toolchain; null if it cannot be set up. */
    List<String> extractorCommand(Candidate c) throws IOException, InterruptedException {
        if (language.equals("typescript")) return new ArrayList<>(List.of(node, home.resolve("extractors/typescript/extract.mjs").toString()));
        String dart = c.location();
        Path dir = home.resolve("extractors/dart");
        Path config = dir.resolve(".dart_tool/package_config.json");
        if (!Files.exists(config) && pubGet.get(dart) == null) {
            System.err.println(label + ": installing the extractor's dependencies (dart pub get, one time)...");
            boolean ok = run(dir, List.of(dart, "pub", "get"), null) == 0;
            pubGet.put(dart, ok);
            if (!ok) System.err.println(label + ": 'dart pub get' failed in " + dir + ", trying the next Dart SDK");
        }
        if (Boolean.FALSE.equals(pubGet.get(dart))) return null;
        return new ArrayList<>(List.of(dart, "--packages=" + config, dir.resolve("bin/extract.dart").toString()));
    }

    /** The extractor's pinned TypeScript, installed with npm ci on first use. */
    boolean pinnedTypeScript() throws InterruptedException {
        if (pinnedReady != null) return pinnedReady;
        Path dir = home.resolve("extractors/typescript");
        if (!Files.exists(dir.resolve("node_modules/typescript/package.json"))) {
            System.err.println(label + ": installing the extractor's pinned TypeScript (npm ci, one time)...");
            Path npm = Path.of(node).getParent() != null ? Path.of(node).resolveSibling("npm") : Path.of("npm");
            String npmCmd = Files.isExecutable(npm) ? npm.toString() : "npm";
            if (run(dir, List.of(npmCmd, "ci", "--no-audit", "--no-fund", "--loglevel=error"), node) != 0) {
                System.err.println(label + ": 'npm ci' failed in " + dir);
                return pinnedReady = false;
            }
        }
        return pinnedReady = true;
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
