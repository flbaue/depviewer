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

/**
 * Scans Java sources with the JDK compiler (full symbol resolution) and writes a
 * dependency model: module > package > file > type > method/field, plus the
 * dependencies between them. Output is a self-contained HTML viewer (or JSON).
 *
 * Usage: java DepScan.java [options] <project-or-source-dir>...
 */
public class DepScan {

    static final List<String> EDGE_KINDS = List.of("uses", "extends", "implements", "call", "new", "access");
    static final int K_USES = 0, K_EXTENDS = 1, K_IMPLEMENTS = 2, K_CALL = 3, K_NEW = 4, K_ACCESS = 5;
    static final Set<String> SKIP_DIRS = Set.of("target", "build", "out", "bin", "node_modules", ".git", ".mvn",
            ".idea", ".gradle", ".svn", ".settings");
    static final Pattern QUALIFIED = Pattern.compile("(?:[\\w$]+\\.)+([\\w$]+)");

    static final class Node {
        final int idx;
        final String id, kind;
        final int parent;
        String name, q, path, vis;
        int line, loc;
        boolean test, isStatic;

        Node(int idx, String id, String name, String kind, int parent) {
            this.idx = idx; this.id = id; this.name = name; this.kind = kind; this.parent = parent;
        }
    }

    static final class Root {
        final Path dir, projectDir;
        final String module;
        final boolean test;

        Root(Path dir, Path projectDir, String module, boolean test) {
            this.dir = dir; this.projectDir = projectDir; this.module = module; this.test = test;
        }
    }

    /** A reference recorded during the scan, resolved once all declarations are known. */
    static final class Raw {
        final int from, kind, line;
        final String memberKey;
        final TypeElement type;

        Raw(int from, String memberKey, TypeElement type, int kind, int line) {
            this.from = from; this.memberKey = memberKey; this.type = type; this.kind = kind; this.line = line;
        }
    }

    static final class Edge {
        final int from, to, kind;
        int count;
        final List<Integer> lines = new ArrayList<>();

        Edge(int from, int to, int kind) { this.from = from; this.to = to; this.kind = kind; }
    }

    final List<Node> nodes = new ArrayList<>();
    final Map<String, Node> byId = new HashMap<>();
    final List<Raw> raws = new ArrayList<>();
    Trees trees;
    Elements elements;
    Types types;

    public static void main(String[] args) throws Exception {
        new DepScan().run(args);
    }

    static void usage() {
        System.out.println(String.join("\n",
                "Usage: depviewer [options] <project-or-source-dir>...",
                "",
                "  Finds src/main/java (and src/test/java) folders below each directory; a directory",
                "  without them is scanned as a plain source root.",
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

        Map<Path, Root> rootOfFile = new LinkedHashMap<>();
        for (Root r : roots) {
            try (var s = Files.walk(r.dir)) {
                s.filter(p -> p.toString().endsWith(".java"))
                        .filter(p -> !p.getFileName().toString().equals("module-info.java"))
                        .filter(p -> !p.getFileName().toString().equals("package-info.java"))
                        .forEach(p -> rootOfFile.putIfAbsent(p.toAbsolutePath().normalize(), r));
            }
        }
        if (rootOfFile.isEmpty()) die("no .java files found");
        long modules = roots.stream().map(r -> r.module).distinct().count();
        System.err.printf("Scanning %d files in %d module(s)...%n", rootOfFile.size(), modules);

        JavaCompiler jc = ToolProvider.getSystemJavaCompiler();
        if (jc == null) die("no Java compiler available - run with a JDK, not a JRE");
        int[] errors = {0};
        List<String> firstErrors = new ArrayList<>();
        DiagnosticListener<JavaFileObject> listener = d -> {
            if (d.getKind() == Diagnostic.Kind.ERROR) {
                errors[0]++;
                if (firstErrors.size() < 5) {
                    String src = d.getSource() == null ? "" : Paths.get(d.getSource().toUri()).getFileName() + ":" + d.getLineNumber() + ": ";
                    firstErrors.add(src + d.getMessage(Locale.ENGLISH).lines().findFirst().orElse(""));
                }
            }
        };
        StandardJavaFileManager fm = jc.getStandardFileManager(listener, Locale.ENGLISH, StandardCharsets.UTF_8);
        List<String> opts = new ArrayList<>(List.of("-proc:none", "-encoding", "UTF-8", "-nowarn", "-Xlint:none",
                "-Xmaxerrs", "1000000", "-XDshould-stop.ifError=FLOW", "-XDshould-stop.ifNoError=FLOW"));
        String classpath = buildClasspath(cp, roots);
        if (!classpath.isEmpty()) { opts.add("-classpath"); opts.add(classpath); }

        Iterable<? extends JavaFileObject> fos = fm.getJavaFileObjectsFromPaths(rootOfFile.keySet());
        JavacTask task = (JavacTask) jc.getTask(null, fm, listener, opts, null, fos);
        List<CompilationUnitTree> units = new ArrayList<>();
        for (CompilationUnitTree u : task.parse()) units.add(u);
        try {
            task.analyze();
        } catch (Throwable t) {
            System.err.println("warning: analysis stopped early: " + t);
        }
        trees = Trees.instance(task);
        elements = task.getElements();
        types = task.getTypes();

        for (CompilationUnitTree cu : units) {
            Path file = Paths.get(cu.getSourceFile().toUri()).toAbsolutePath().normalize();
            Root r = rootOfFile.get(file);
            if (r == null) continue;
            new Scan(cu, r, file).scan(new TreePath(cu), null);
        }

        Collection<Edge> edges = resolve();
        String json = toJson(title, edges, errors[0]);

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

        long typeCount = nodes.stream().filter(n -> n.id.startsWith("T:")).count();
        long memberCount = nodes.stream().filter(n -> n.id.startsWith("M:")).count();
        int depCount = edges.stream().mapToInt(e -> e.count).sum();
        System.err.printf("%d types, %d members, %d dependencies (%d distinct) -> %s%n",
                typeCount, memberCount, depCount, edges.size(), out.toAbsolutePath().normalize());
        if (errors[0] > 0) {
            System.err.printf("note: %d compile errors; unresolved references are listed under External > (unresolved).%n", errors[0]);
            System.err.println("      Missing libraries? Run 'mvn dependency:copy-dependencies' or pass --cp.");
            firstErrors.forEach(e -> System.err.println("      " + e));
        }
    }

    // ---------------------------------------------------------------- roots

    List<Root> findRoots(Path in, boolean tests) throws IOException {
        List<Root> found = new ArrayList<>();
        Path main = Paths.get("src", "main", "java"), test = Paths.get("src", "test", "java");
        Files.walkFileTree(in, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                if (!dir.equals(in) && SKIP_DIRS.contains(dir.getFileName().toString())) return FileVisitResult.SKIP_SUBTREE;
                boolean isMain = dir.endsWith(main), isTest = dir.endsWith(test);
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

    // ---------------------------------------------------------------- nodes

    Node node(String id, String name, String kind, int parent) {
        Node n = byId.get(id);
        if (n != null) return n;
        n = new Node(nodes.size(), id, name, kind, parent);
        nodes.add(n);
        byId.put(id, n);
        return n;
    }

    int packageNode(String module, String pkg) {
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

    static boolean isNamed(TypeElement te) {
        NestingKind nk = te.getNestingKind();
        if (nk == NestingKind.TOP_LEVEL) return true;
        return nk == NestingKind.MEMBER && te.getEnclosingElement() instanceof TypeElement
                && isNamed((TypeElement) te.getEnclosingElement());
    }

    static String typeId(TypeElement te) {
        return "T:" + te.getQualifiedName();
    }

    static String typeKind(TypeElement te) {
        switch (te.getKind().name()) {
            case "INTERFACE": return "interface";
            case "ENUM": return "enum";
            case "ANNOTATION_TYPE": return "annotation";
            case "RECORD": return "record";
            default: return "class";
        }
    }

    String memberId(Element e) {
        StringBuilder sb = new StringBuilder("M:").append(((TypeElement) e.getEnclosingElement()).getQualifiedName()).append('#');
        sb.append(e.getSimpleName());
        if (e instanceof ExecutableElement) {
            sb.append('(').append(((ExecutableElement) e).getParameters().stream()
                    .map(p -> types.erasure(p.asType()).toString()).collect(Collectors.joining(","))).append(')');
        }
        return sb.toString();
    }

    String memberName(Element e) {
        if (!(e instanceof ExecutableElement)) return e.getSimpleName().toString();
        ExecutableElement ee = (ExecutableElement) e;
        String name = ee.getKind() == ElementKind.CONSTRUCTOR
                ? ee.getEnclosingElement().getSimpleName().toString() : ee.getSimpleName().toString();
        String params = ee.getParameters().stream()
                .map(p -> QUALIFIED.matcher(p.asType().toString()).replaceAll("$1"))
                .collect(Collectors.joining(", "));
        return name + "(" + params + ")";
    }

    static void modifiers(Node n, Element e) {
        Set<Modifier> m = e.getModifiers();
        n.vis = m.contains(Modifier.PUBLIC) ? "+" : m.contains(Modifier.PROTECTED) ? "#" : m.contains(Modifier.PRIVATE) ? "-" : "~";
        n.isStatic = m.contains(Modifier.STATIC);
    }

    // ---------------------------------------------------------------- scanning

    final class Scan extends TreePathScanner<Void, Void> {
        final CompilationUnitTree cu;
        final Root root;
        final Path file;
        final LineMap lines;
        final SourcePositions pos;
        final Deque<Integer> ctx = new ArrayDeque<>();
        int kind = K_USES;

        Scan(CompilationUnitTree cu, Root root, Path file) {
            this.cu = cu; this.root = root; this.file = file;
            this.lines = cu.getLineMap();
            this.pos = trees.getSourcePositions();
        }

        int line(Tree t) {
            long p = pos.getStartPosition(cu, t);
            return p < 0 ? 0 : (int) lines.getLineNumber(p);
        }

        boolean generated(Tree t) {
            return pos.getEndPosition(cu, t) == Diagnostic.NOPOS;
        }

        void place(Node n, Tree t) {
            long s = pos.getStartPosition(cu, t), e = pos.getEndPosition(cu, t);
            if (s >= 0) n.line = (int) lines.getLineNumber(s);
            if (s >= 0 && e >= 0) n.loc = (int) (lines.getLineNumber(e) - n.line + 1);
            n.test = root.test;
        }

        @Override
        public Void visitCompilationUnit(CompilationUnitTree unit, Void v) {
            String pkg = unit.getPackageName() == null ? "" : unit.getPackageName().toString();
            int pkgIdx = packageNode(root.module, pkg);
            String rel = root.projectDir.relativize(file).toString().replace('\\', '/');
            Node f = node("F:" + root.module + ":" + rel, file.getFileName().toString(), "file", pkgIdx);
            f.path = rel;
            f.test = root.test;
            f.line = 1;
            try {
                f.loc = (int) unit.getSourceFile().getCharContent(true).chars().filter(c -> c == '\n').count() + 1;
            } catch (IOException ignored) {
            }
            ctx.push(f.idx);
            for (Tree t : unit.getTypeDecls()) scan(t, v);
            ctx.pop();
            return null;
        }

        @Override
        public Void visitClass(ClassTree ct, Void v) {
            Element e = trees.getElement(getCurrentPath());
            if (!(e instanceof TypeElement) || !isNamed((TypeElement) e)) return super.visitClass(ct, v);
            TypeElement te = (TypeElement) e;
            Node n = node(typeId(te), te.getSimpleName().toString(), typeKind(te), ctx.peek());
            n.q = te.getQualifiedName().toString();
            modifiers(n, te);
            place(n, ct);
            ctx.push(n.idx);
            scan(ct.getModifiers(), v);
            scan(ct.getTypeParameters(), v);
            kind = K_EXTENDS;
            scan(ct.getExtendsClause(), v);
            kind = te.getKind() == ElementKind.INTERFACE ? K_EXTENDS : K_IMPLEMENTS;
            scan(ct.getImplementsClause(), v);
            kind = K_USES;
            scan(ct.getMembers(), v);
            ctx.pop();
            return null;
        }

        @Override
        public Void visitMethod(MethodTree mt, Void v) {
            if (generated(mt)) return null; // default constructors, record accessors...
            Element e = trees.getElement(getCurrentPath());
            if (!(e instanceof ExecutableElement) || !(e.getEnclosingElement() instanceof TypeElement)
                    || !isNamed((TypeElement) e.getEnclosingElement())) return super.visitMethod(mt, v);
            Node n = node(memberId(e), memberName(e), e.getKind() == ElementKind.CONSTRUCTOR ? "ctor" : "method", ctx.peek());
            n.q = ((TypeElement) e.getEnclosingElement()).getQualifiedName() + "." + n.name;
            modifiers(n, e);
            place(n, mt);
            ctx.push(n.idx);
            super.visitMethod(mt, v);
            ctx.pop();
            return null;
        }

        @Override
        public Void visitVariable(VariableTree vt, Void v) {
            if (getCurrentPath().getParentPath().getLeaf() instanceof ClassTree) {
                Element e = trees.getElement(getCurrentPath());
                if (e != null && e.getEnclosingElement() instanceof TypeElement && isNamed((TypeElement) e.getEnclosingElement())) {
                    if (generated(vt)) return null;
                    Node n = node(memberId(e), e.getSimpleName().toString(),
                            e.getKind() == ElementKind.ENUM_CONSTANT ? "enumconst" : "field", ctx.peek());
                    n.q = ((TypeElement) e.getEnclosingElement()).getQualifiedName() + "." + n.name;
                    modifiers(n, e);
                    place(n, vt);
                    ctx.push(n.idx);
                    super.visitVariable(vt, v);
                    ctx.pop();
                    return null;
                }
            }
            return super.visitVariable(vt, v);
        }

        @Override
        public Void visitIdentifier(IdentifierTree t, Void v) {
            ref(t);
            return super.visitIdentifier(t, v);
        }

        @Override
        public Void visitMemberSelect(MemberSelectTree t, Void v) {
            ref(t);
            return super.visitMemberSelect(t, v);
        }

        @Override
        public Void visitMemberReference(MemberReferenceTree t, Void v) {
            ref(t);
            return super.visitMemberReference(t, v);
        }

        @Override
        public Void visitNewClass(NewClassTree t, Void v) {
            ref(t);
            return super.visitNewClass(t, v);
        }

        void ref(Tree t) {
            if (ctx.size() <= 1 || generated(t)) return;
            Element e = trees.getElement(getCurrentPath());
            if (e == null) return;
            int from = ctx.peek(), line = line(t);
            ElementKind k = e.getKind();
            if (k.isClass() || k.isInterface()) {
                raws.add(new Raw(from, null, (TypeElement) e, kind, line));
            } else if (k == ElementKind.METHOD || k == ElementKind.CONSTRUCTOR) {
                if (!(e.getEnclosingElement() instanceof TypeElement)) return;
                if (k == ElementKind.CONSTRUCTOR && ((TypeElement) e.getEnclosingElement()).getQualifiedName()
                        .contentEquals("java.lang.Object")) return; // implicit super()
                int rk =k == ElementKind.CONSTRUCTOR ? K_NEW : K_CALL;
                raws.add(new Raw(from, memberId(e), (TypeElement) e.getEnclosingElement(), rk, line));
            } else if (k == ElementKind.FIELD || k == ElementKind.ENUM_CONSTANT) {
                String name = e.getSimpleName().toString();
                if (name.equals("this") || name.equals("super") || name.equals("class")) return;
                if (!(e.getEnclosingElement() instanceof TypeElement)) return;
                TypeElement owner = (TypeElement) e.getEnclosingElement();
                if (owner.getQualifiedName().toString().equals("Array")) return; // array.length
                raws.add(new Raw(from, memberId(e), owner, K_ACCESS, line));
            }
        }
    }

    // ---------------------------------------------------------------- resolution

    int external(String pkg, String qname, String simple, String kind) {
        Node rootNode = node("X:", "External", "ext-root", -1);
        Node p = node("XP:" + pkg, pkg, "ext-package", rootNode.idx);
        p.q = pkg;
        Node t = node("XT:" + qname, simple, "ext-type", p.idx);
        t.q = qname;
        if (kind != null) t.vis = kind;
        return t.idx;
    }

    int resolveType(TypeElement te) {
        if (te.asType().getKind() == TypeKind.ERROR) {
            String q = te.getQualifiedName().toString();
            if (q.isEmpty()) q = te.getSimpleName().toString();
            if (q.isEmpty()) return -1;
            return external("(unresolved)", "?" + q, q.substring(q.lastIndexOf('.') + 1), null);
        }
        TypeElement cur = te;
        while (!isNamed(cur)) {
            Element enc = cur.getEnclosingElement();
            while (enc != null && !(enc instanceof TypeElement)) enc = enc.getEnclosingElement();
            if (enc == null) return -1;
            cur = (TypeElement) enc;
        }
        Node n = byId.get(typeId(cur));
        if (n != null) return n.idx;
        TypeElement top = cur;
        while (top.getEnclosingElement() instanceof TypeElement) top = (TypeElement) top.getEnclosingElement();
        String pkg = elements.getPackageOf(top).getQualifiedName().toString();
        return external(pkg.isEmpty() ? "(default package)" : pkg, top.getQualifiedName().toString(),
                top.getSimpleName().toString(), typeKind(top));
    }

    Collection<Edge> resolve() {
        Map<String, Edge> edges = new LinkedHashMap<>();
        Map<TypeElement, Integer> typeCache = new HashMap<>();
        for (Raw r : raws) {
            int to = -1;
            if (r.memberKey != null) {
                Node m = byId.get(r.memberKey);
                if (m != null) to = m.idx;
            }
            if (to < 0) to = typeCache.computeIfAbsent(r.type, this::resolveType);
            if (to < 0 || to == r.from) continue;
            int target = to;
            Edge e = edges.computeIfAbsent(r.from + ">" + to + ">" + r.kind, x -> new Edge(r.from, target, r.kind));
            e.count++;
            if (r.line > 0 && e.lines.size() < 30 && !e.lines.contains(r.line)) e.lines.add(r.line);
        }
        return edges.values();
    }

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

    String toJson(String title, Collection<Edge> edges, int errors) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"title\":").append(js(title))
                .append(",\"generated\":").append(js(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))))
                .append(",\"errors\":").append(errors)
                .append(",\"edgeKinds\":[").append(EDGE_KINDS.stream().map(DepScan::js).collect(Collectors.joining(","))).append("]")
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
