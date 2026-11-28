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
    private int kept;
    /** Whether a failure stops the scan instead of skipping the language (the jvm extractor). */
    boolean required;
    private boolean incompatible;
    private String why;                                     // why the last --describe could not run                           // the extractor speaks another contract                                       // runs kept for DEPVIEWER_FRAGMENTS
    /** Language-specific request options, e.g. the classpath of the jvm extractor. */
    final Map<String, Object> options = new LinkedHashMap<>();
    /** The version of the extractor contract this core speaks (spec CC-002). */
    static final int CONTRACT = 1;
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
        Map<String, List<Module>> byPlace = new LinkedHashMap<>();   // the log names each place the toolchain was found
        for (Map.Entry<Module, Choice> e : chosen.entrySet()) byPlace.computeIfAbsent(e.getValue().toString(), k -> new ArrayList<>()).add(e.getKey());
        for (Map.Entry<String, List<Module>> e : byPlace.entrySet()) System.err.printf("%s: %s for %s%n", label, e.getKey(), names(e.getValue()));
        String names = names(run);
        Path input = Files.createTempFile("depviewer-" + language, ".json");
        try {
            Module first = run.get(0);
            Files.writeString(input, Json.write(request(run, choice, tests)));
            List<String> cmd = new ArrayList<>(choice.command);
            cmd.add(input.toString());
            try {
                ProcessBuilder pb = new ProcessBuilder(cmd).redirectError(ProcessBuilder.Redirect.INHERIT);
                Process p = pb.start();
                String out;
                try (InputStream in = p.getInputStream()) { out = new String(in.readAllBytes(), StandardCharsets.UTF_8); }
                int code = p.waitFor();
                keep(input, out);
                if (code != 0) { skip(String.format("extractor failed for %s (exit code %d)", names, code)); return; }
                Object parsed = Json.parse(out);
                if (!(parsed instanceof Map)) { skip("the extractor printed no fragment for " + names); return; }
                Map<?, ?> frag = (Map<?, ?>) parsed;
                Object contract = frag.get("contract");
                if (!(contract instanceof Number) || ((Number) contract).intValue() != CONTRACT) {
                    skip(String.format("the extractor printed contract %s, this depviewer reads contract %d; %s",
                            contract == null ? "(none)" : Json.write(contract), CONTRACT, names));
                    return;
                }
                String bad = invalid(frag);   // checked first, so nothing of a broken fragment enters the model
                if (bad != null) { skip("the extractor printed a broken fragment for " + names + " (" + bad + ")"); return; }
                merge(first.name, frag);
                for (List<Module> ms : bySource.values()) record(frag, ms, chosen.get(ms.get(0)));
            } catch (IOException | RuntimeException e) {
                if (e instanceof DepScan.Failed) throw e;
                skip(String.format("extractor failed for %s (%s)", names, e));
            }
        } finally {
            Files.deleteIfExists(input);
        }
    }

    /**
     * A run or a module that cannot be analysed: skipped with a note (spec FR-007), except for a
     * required language. Java and Kotlin are the core's own (spec QR-005): a scan or check without
     * them would look clean, so their failure stops it.
     */
    private void skip(String what) {
        if (required) DepScan.die(label + ": " + what + "; no model written");
        System.err.printf("%s: %s, skipped%n", label, what);
    }

    /** Why a fragment cannot be merged as `merge` reads it, or null: its lists must hold objects, lines numbers. */
    static String invalid(Map<?, ?> frag) {
        for (String list : List.of("packages", "nodes", "externals", "edges")) {
            Object v = frag.get(list);
            if (v == null) continue;
            if (!(v instanceof List)) return "\"" + list + "\" is not a list";
            for (Object o : (List<?>) v) {
                if (!(o instanceof Map)) return "\"" + list + "\" holds " + (o == null ? "null" : Json.write(o));
                if (list.equals("edges") && ((Map<?, ?>) o).get("lines") != null) {
                    Object ls = ((Map<?, ?>) o).get("lines");
                    if (!(ls instanceof List) || ((List<?>) ls).stream().anyMatch(l -> !(l instanceof Number))) return "an edge's \"lines\" are not numbers";
                }
            }
        }
        return null;
    }

    static String names(List<Module> ms) { return ms.stream().map(m -> m.name).collect(java.util.stream.Collectors.joining(", ")); }

    /** The request of one run (spec CC-002): modules, files with their module and test flag, options, the toolchain. */
    private Map<String, Object> request(List<Module> run, Choice choice, boolean tests) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("contract", CONTRACT);
        r.put("language", language);
        r.put("root", run.get(0).dir.toString());   // the first module, for extractors that read one module
        r.put("module", run.get(0).name);
        Map<String, Object> tc = new LinkedHashMap<>();
        tc.put("tool", choice.tool);
        tc.put("version", choice.version);
        tc.put("path", choice.path);
        r.put("toolchain", tc);
        Map<String, Object> opts = new LinkedHashMap<>();
        opts.put("tests", tests);
        opts.putAll(options);
        r.put("options", opts);
        List<Object> ms = new ArrayList<>(), fs = new ArrayList<>();
        for (Module m : run) {
            ms.add(new LinkedHashMap<>(Map.of("name", m.name, "root", m.dir.toString())));
            for (String[] f : m.files) {
                if (!tests && f[1].equals("1")) continue;
                Map<String, Object> o = new LinkedHashMap<>();
                o.put("path", f[0]);
                o.put("test", f[1].equals("1"));
                o.put("module", m.name);
                o.put("root", m.dir.toString());
                for (int i = 2; i + 1 < f.length; i += 2) o.put(f[i], f[i + 1]);   // language-specific fields
                fs.add(o);
            }
        }
        r.put("modules", ms);
        r.put("files", fs);
        return r;
    }

    /** Provenance of a run's modules from one source: the fragment's `languages`, else the run's language. */
    private void record(Map<?, ?> frag, List<Module> ms, Choice c) {
        List<String> names = ms.stream().map(m -> m.name).distinct().collect(java.util.stream.Collectors.toList());
        if (!(frag.get("languages") instanceof List)) {
            provenance(language, names, c.tool, c.version, c.source, c.exact, c.extractor, c.extractorVersion);
            return;
        }
        for (Object o : (List<?>) frag.get("languages")) {
            if (!(o instanceof Map)) continue;
            Map<?, ?> a = (Map<?, ?>) o;
            List<String> mods = new ArrayList<>(names);
            if (a.get("modules") instanceof List) mods.retainAll((List<?>) a.get("modules"));
            if (mods.isEmpty()) continue;
            provenance(str(a.get("language")), mods, a.get("tool") != null ? str(a.get("tool")) : c.tool,
                    a.get("version") != null ? str(a.get("version")) : c.version, a.get("source") != null ? str(a.get("source")) : c.source,
                    !Boolean.FALSE.equals(a.get("exact")), c.extractor, c.extractorVersion);
        }
    }

    /** DEPVIEWER_FRAGMENTS names a folder that keeps every run's request and raw fragment, for test/checks/contract.py. */
    private void keep(Path request, String fragment) throws IOException {
        String dir = System.getenv("DEPVIEWER_FRAGMENTS");
        if (dir == null || dir.isEmpty()) return;
        Files.createDirectories(Path.of(dir));
        String name = language + "-" + (++kept);
        Files.copy(request, Path.of(dir, name + ".request.json"), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        Files.writeString(Path.of(dir, name + ".fragment.json"), fragment);
    }

    // ------------------------------------------------ choosing the toolchain (spec FR-010)

    /** The first candidate whose version the extractor supports, or null (the module is skipped). */
    Choice choose(Module m) throws IOException, InterruptedException {
        if (incompatible) return null;
        String tool = language.equals("dart") ? "Dart SDK" : language.equals("jvm") ? "JDK" : "TypeScript";
        for (Candidate c : candidates(m)) {
            String key = c.source() + " " + c.location();
            Choice known = described.get(key);
            if (known != null) return known;
            if (reported.contains(key)) continue;   // unsupported, already reported
            if (c.source().equals("pinned") && language.equals("typescript") && !pinnedTypeScript()) continue;
            Object d = describe(c);
            Map<?, ?> t = d instanceof Map && ((Map<?, ?>) d).get("toolchain") instanceof Map ? (Map<?, ?>) ((Map<?, ?>) d).get("toolchain") : null;
            Object contract = d instanceof Map ? ((Map<?, ?>) d).get("contract") : null;
            if (t != null && (!(contract instanceof Number) || ((Number) contract).intValue() != CONTRACT)) {
                // The extractor itself does not fit, whatever the toolchain: the language is skipped.
                System.err.printf("%s: extractor %s %s speaks contract %s, this depviewer speaks contract %d; skipped%n",
                        label, ((Map<?, ?>) d).get("name"), ((Map<?, ?>) d).get("version"), contract == null ? "(none)" : Json.write(contract), CONTRACT);
                incompatible = true;
                if (required) DepScan.die(label + ": the extractor speaks another contract; no model written");
                return null;
            }
            if (t != null && Boolean.TRUE.equals(t.get("supported"))) {
                List<String> command = extractorCommand(c);
                if (command == null) continue;   // reported by extractorCommand
                Choice choice = new Choice((Map<?, ?>) d, command);
                described.put(key, choice);
                return choice;
            }
            if (!reported.add(key)) continue;
            String where = c.source() + ": " + c.location();
            if (t == null) System.err.printf("%s: %s (%s) could not be run%s, trying the next one%n", label, tool, where,
                    why == null ? "" : ": " + (why.length() > 160 ? why.substring(0, 160) + "..." : why));
            else if (t.get("version") == null) System.err.printf("%s: %s (%s): %s, trying the next one%n", label, tool, where, t.get("problem"));
            else System.err.printf("%s: %s %s (%s) is not supported (needs %s), trying the next one%n",
                    label, tool, t.get("version"), where, t.get("range"));
        }
        String hint = (
                language.equals("dart") ? "install Dart 3.11 or later or set DEPVIEWER_DART"
                        : language.equals("jvm") ? "install a JDK 17 or later or set DEPVIEWER_JAVA" : "add typescript to the project or set DEPVIEWER_TYPESCRIPT");
        if (required) DepScan.die(String.format("%s: no supported %s found for %s (%s); no model written", label, tool, m.name, hint));
        System.err.printf("%s: skipped %s - no supported %s found (%s).%n", label, m.name, tool, hint);
        return null;
    }

    /** Where to look, in this order: override, the project's own, the PATH, the pinned copy. */
    List<Candidate> candidates(Module m) throws IOException, InterruptedException {
        List<Candidate> list = new ArrayList<>();
        if (language.equals("jvm")) {
            String override = System.getenv("DEPVIEWER_JAVA");
            if (override != null && !override.isEmpty()) list.add(new Candidate("override", override));
            Path java = onPath("java");
            if (java != null) list.add(new Candidate("path", java.toString()));
            // No project JDK yet. Last, the JDK that runs depviewer: the wrapper found it on the PATH too.
            Path running = Path.of(System.getProperty("java.home"), "bin", "java");
            try {
                if (java == null || !java.toRealPath().equals(running.toRealPath())) list.add(new Candidate("path", running.toString()));
            } catch (IOException e) {
                list.add(new Candidate("path", running.toString()));
            }
            return list;
        }
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
        why = null;
        List<String> cmd = new ArrayList<>();
        if (language.equals("jvm")) {
            cmd.addAll(List.of(c.location(), "-cp", System.getProperty("java.class.path"), "depviewer.JvmExtractor", "--describe"));
        } else if (language.equals("typescript")) {
            cmd.addAll(List.of(node, home.resolve("extractors/typescript/extract.mjs").toString(), "--describe", c.location()));
        } else {
            // describe.dart imports nothing, so an SDK too old for the analyzer can still say which version it is.
            if (!works(c.location(), "--version")) return null;
            cmd.addAll(List.of(c.location(), "--packages=" + home.resolve("extractors/dart/describe_packages.json"),
                    home.resolve("extractors/dart/bin/describe.dart").toString()));
        }
        cmd.add(c.source());
        try {
            Path err = Files.createTempFile("depviewer-describe", ".err");
            try {
                Process p = new ProcessBuilder(cmd).redirectError(err.toFile()).start();
                String out;
                try (InputStream in = p.getInputStream()) { out = new String(in.readAllBytes(), StandardCharsets.UTF_8); }
                if (p.waitFor() == 0) return Json.parse(out.trim());
                // Why it could not run, e.g. a JDK too old for the classes: the first line that is not JVM chatter.
                List<String> lines = Files.readAllLines(err, StandardCharsets.ISO_8859_1).stream().map(String::trim)
                        .filter(l -> !l.isEmpty() && !l.startsWith("Picked up ")).collect(java.util.stream.Collectors.toList());
                why = lines.stream().filter(l -> l.contains("UnsupportedClassVersionError")).findFirst()
                        .orElse(lines.isEmpty() ? null : lines.get(0));
                return null;
            } finally {
                Files.deleteIfExists(err);
            }
        } catch (IOException | IllegalArgumentException e) {
            why = e.getMessage();
            return null;
        }
    }

    /** The command that runs the extractor with a candidate's toolchain; null if it cannot be set up. */
    List<String> extractorCommand(Candidate c) throws IOException, InterruptedException {
        if (language.equals("jvm")) {
            List<String> cmd = new ArrayList<>(List.of(c.location()));
            // The heap and stack options depviewer was started with apply to the analysis, which runs there.
            for (String a : java.lang.management.ManagementFactory.getRuntimeMXBean().getInputArguments())
                if (a.startsWith("-Xmx") || a.startsWith("-Xms") || a.startsWith("-Xss")) cmd.add(a);
            cmd.addAll(List.of("-cp", System.getProperty("java.class.path"), "depviewer.JvmExtractor"));
            return cmd;
        }
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
    void merge(String module, Map<?, ?> parsed) {
        Map<String, Object> frag = (Map<String, Object>) parsed;
        errors += intOf(frag.get("errors"));
        // Packages are '/' paths (folders) unless the fragment says '.', as for Java and Kotlin.
        boolean dotted = ".".equals(frag.get("packageSep"));
        for (Object o : (List<Object>) frag.getOrDefault("packages", List.of())) {   // optional: their order in the model
            Map<String, Object> p = (Map<String, Object>) o;
            String m = str(p.get("module")), pkg = str(p.get("package"));
            if (m != null && pkg != null) { if (dotted) packageNode(m, pkg); else folderPackage(m, pkg); }
        }
        String prefix = "L:" + language + ":" + module + ":";
        Map<String, Integer> ids = new HashMap<>();
        Set<String> dropped = new HashSet<>();
        for (Object o : (List<Object>) frag.getOrDefault("nodes", List.of())) {
            Map<String, Object> n = (Map<String, Object>) o;
            String id = str(n.get("id")), kind = str(n.get("kind"));
            if ("package".equals(kind)) {   // optional: creates a package at this place in the model's order
                String m = str(n.get("module")), pkg = str(n.get("package"));
                if (m != null && pkg != null) { if (dotted) packageNode(m, pkg); else folderPackage(m, pkg); }
                continue;
            }
            if (id == null || kind == null) continue;
            if (kind.equals("file")) {
                String path = str(n.get("path"));
                if (path == null || path.isEmpty()) { dropped.add(id); continue; }
                String pkg = str(n.getOrDefault("package", ""));
                String fileModule = n.get("module") != null ? str(n.get("module")) : module;   // multi-module runs
                pkg = pkg == null ? "" : pkg;
                Node f = node("F:" + fileModule + ":" + path, path.substring(path.lastIndexOf('/') + 1), "file",
                        dotted ? packageNode(fileModule, pkg) : folderPackage(fileModule, pkg));
                f.path = path;
                f.test = Boolean.TRUE.equals(n.get("test"));
                f.line = 1;
                f.loc = intOf(n.get("loc"));
                ids.put(id, f.idx);
                continue;
            }
            Integer parent = ids.get(str(n.get("parent")));
            if (parent == null || n.get("name") == null) { dropped.add(id); continue; }
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
            String q = x.get("q") != null ? str(x.get("q")) : pkg + "." + name;
            ids.put(str(x.get("id")), external(pkg, q, name, str(x.get("kind"))));
        }
        int missing = 0;
        for (Object o : (List<Object>) frag.getOrDefault("edges", List.of())) {
            Map<String, Object> e = (Map<String, Object>) o;
            Integer from = ids.get(str(e.get("from"))), to = ids.get(str(e.get("to")));
            int kind = EDGE_KINDS.indexOf(str(e.get("kind")));
            if (from == null || to == null || kind < 0) { missing++; continue; }
            if (e.get("lines") instanceof List) {   // aggregated: count and lines of several references
                List<Integer> ls = new ArrayList<>();
                for (Object l : (List<?>) e.get("lines")) ls.add(intOf(l));
                addEdges(from, to, kind, Math.max(1, intOf(e.getOrDefault("count", ls.size()))), ls);
            } else {
                addEdge(from, to, kind, intOf(e.get("line")));
            }
        }
        if (!dropped.isEmpty() || missing > 0) {
            System.err.printf("%s: warning: %d node(s) and %d dependencies of %s had unknown references and were dropped%n",
                    label, dropped.size(), missing, module);
        }
    }

    static String str(Object o) { return o == null ? null : o.toString(); }

    static int intOf(Object o) { return o instanceof Number ? ((Number) o).intValue() : 0; }
}
