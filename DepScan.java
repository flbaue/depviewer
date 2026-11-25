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
        // For resolving Kotlin references by name: simple member name ("<init>" for constructors),
        // declared type of a field / return type of a method, supertypes (qualified names), Java origin.
        String simple, valueType;
        List<String> supers;
        boolean java, kotlin;

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
        List<String> guesses;   // for unresolvable types: qualified names suggested by the file's imports
        List<String> tail;      // ...and the member chain after the type name (NotificationService.INSTANCE.register)

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
    final Map<String, Edge> edges = new LinkedHashMap<>();
    final List<Object[]> javaToKotlin = new ArrayList<>();   // {Raw, typeIdx}: Java member references on Kotlin types
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
        Kotlin kotlin = new Kotlin();
        for (Map.Entry<Path, Root> e : rootOfKt.entrySet()) kotlin.declare(e.getKey(), e.getValue());

        int[] errors = {0};
        List<String> firstErrors = new ArrayList<>();
        if (!rootOfFile.isEmpty()) scanJava(rootOfFile, roots, cp, errors, firstErrors);
        resolve();
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

    void scanJava(Map<Path, Root> rootOfFile, List<Root> roots, String cp, int[] errors, List<String> firstErrors) throws IOException {
        JavaCompiler jc = ToolProvider.getSystemJavaCompiler();
        if (jc == null) die("no Java compiler available - run with a JDK, not a JRE");
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
            n.simple = n.name;
            n.java = true;
            n.supers = new ArrayList<>();
            for (var st : types.directSupertypes(te.asType())) {
                String q = typeName(st);
                if (q != null) n.supers.add(q);
            }
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
            n.simple = e.getSimpleName().toString();
            n.valueType = typeName(((ExecutableElement) e).getReturnType());
            n.java = true;
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
                    n.simple = n.name;
                    n.valueType = typeName(e.asType());
                    n.java = true;
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

        /** Qualified names an unresolvable simple type name may stand for, from this file's imports and package. */
        List<String> guesses(String simple) {
            List<String> out = new ArrayList<>();
            if (simple.isEmpty()) return out;
            for (ImportTree it : cu.getImports()) {
                if (it.isStatic()) continue;
                String q = it.getQualifiedIdentifier().toString();
                if (q.endsWith("." + simple)) out.add(q);
                else if (q.endsWith(".*")) out.add(q.substring(0, q.length() - 1) + simple);
            }
            String pkg = cu.getPackageName() == null ? "" : cu.getPackageName().toString();
            out.add(qual(pkg, simple));
            return out;
        }

        void ref(Tree t) {
            if (ctx.size() <= 1 || generated(t)) return;
            Element e = trees.getElement(getCurrentPath());
            if (e == null) return;
            int from = ctx.peek(), line = line(t);
            ElementKind k = e.getKind();
            if (k.isClass() || k.isInterface()) {
                Raw r = new Raw(from, null, (TypeElement) e, kind, line);
                if (e.asType().getKind() == TypeKind.ERROR) {
                    // javac reports a chain on an unknown type as one error symbol: Foo.INSTANCE.bar
                    String q = ((TypeElement) e).getQualifiedName().toString();
                    if (q.isEmpty()) q = e.getSimpleName().toString();
                    List<String> parts = Arrays.asList(q.split("\\."));
                    int head = 0;   // first segment that looks like a type name (skip a package prefix)
                    while (head < parts.size() - 1 && !parts.get(head).isEmpty() && Character.isLowerCase(parts.get(head).charAt(0))) head++;
                    String type = String.join(".", parts.subList(0, head + 1));
                    r.guesses = head > 0 ? List.of(type) : guesses(type);
                    r.tail = new ArrayList<>(parts.subList(head + 1, parts.size()));
                }
                raws.add(r);
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

    /** Qualified name of a declared (class/interface) type after erasure, else null. */
    String typeName(javax.lang.model.type.TypeMirror t) {
        if (t == null) return null;
        var erased = types.erasure(t);
        if (erased.getKind() != TypeKind.DECLARED) return null;
        return ((TypeElement) ((javax.lang.model.type.DeclaredType) erased).asElement()).getQualifiedName().toString();
    }

    int resolveType(TypeElement te) {
        if (te.asType().getKind() == TypeKind.ERROR) {
            String q = te.getQualifiedName().toString();
            if (q.isEmpty()) q = te.getSimpleName().toString();
            if (q.isEmpty()) return -1;
            // javac does not see Kotlin code: match the name against Kotlin declarations
            // (types, and file facades such as UtilsKt for top-level functions).
            Node kt = byId.get("T:" + q);
            if (kt != null && kt.kotlin) return kt.idx;
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

    void addEdge(int from, int to, int kind, int line) {
        if (from < 0 || to < 0 || from == to) return;
        Edge e = edges.computeIfAbsent(from + ">" + to + ">" + kind, x -> new Edge(from, to, kind));
        e.count++;
        if (line > 0 && e.lines.size() < 30 && !e.lines.contains(line)) e.lines.add(line);
    }

    void resolve() {
        Map<TypeElement, Integer> typeCache = new HashMap<>();
        for (Raw r : raws) {
            int to = -1;
            if (r.memberKey != null) {
                Node m = byId.get(r.memberKey);
                if (m != null) to = m.idx;
            }
            if (to < 0 && r.guesses != null) {
                for (String g : r.guesses) {
                    Node n = byId.get("T:" + g);
                    if (n != null && n.kotlin) { to = n.idx; break; }   // name matching only for Kotlin targets
                }
                if (to >= 0 && !r.tail.isEmpty()) { javaToKotlin.add(new Object[]{r, to}); continue; }   // needs the member index
            }
            if (to < 0) to = typeCache.computeIfAbsent(r.type, this::resolveType);
            addEdge(r.from, to, r.kind, r.line);
        }
    }

    // ---------------------------------------------------------------- Kotlin
    //
    // A lightweight reader for Kotlin sources (no Kotlin compiler needed). It is deliberately
    // simpler than javac:
    //  1. lex the file (comments, strings with ${templates}, `backticked` names),
    //  2. declare()  : read declarations - classes, objects, companions, enums, constructors,
    //                  functions (incl. extensions), properties, type aliases - and remember the
    //                  token spans that belong to each of them,
    //  3. resolveAll(): after all Java and Kotlin declarations are known, resolve the names in
    //                  each span: types via enclosing classes, imports, same package, star imports
    //                  and a list of Kotlin built-ins; members via the receiver's declared type
    //                  (variables, parameters, properties, call results), falling back to a unique
    //                  match among the types the declaration uses.
    // Anything it cannot resolve is ignored rather than guessed.

    static final Set<String> KT_HARD = Set.of("as", "break", "class", "continue", "do", "else", "false", "for", "fun",
            "if", "in", "interface", "is", "null", "object", "package", "return", "super", "this", "throw", "true", "try",
            "typealias", "typeof", "val", "var", "when", "while");
    static final Set<String> KT_MODS = Set.of("public", "private", "protected", "internal", "open", "final", "abstract",
            "sealed", "data", "enum", "annotation", "inner", "value", "inline", "override", "lateinit", "const", "suspend",
            "tailrec", "operator", "infix", "external", "companion", "expect", "actual", "vararg", "noinline",
            "crossinline", "reified");
    static final Set<String> KT_DECL = Set.of("fun", "val", "var", "class", "interface", "object", "typealias",
            "constructor", "init");
    static final Map<String, String> KT_BUILTINS = new HashMap<>();
    static {
        for (String s : ("Any Unit Nothing String Int Long Short Byte Double Float Boolean Char Number Array IntArray LongArray "
                + "DoubleArray FloatArray BooleanArray CharArray ByteArray Pair Triple Comparable Throwable Exception Error "
                + "RuntimeException IllegalArgumentException IllegalStateException UnsupportedOperationException "
                + "NullPointerException IndexOutOfBoundsException NoSuchElementException ArithmeticException Lazy Result "
                + "Enum Annotation Deprecated Suppress Function").split(" ")) KT_BUILTINS.put(s, "kotlin");
        for (String s : ("List MutableList Set MutableSet Map MutableMap Collection MutableCollection Iterable Iterator "
                + "ArrayList HashMap HashSet LinkedHashMap LinkedHashSet").split(" ")) KT_BUILTINS.put(s, "kotlin.collections");
        KT_BUILTINS.put("Sequence", "kotlin.sequences");
        for (String s : "Regex StringBuilder".split(" ")) KT_BUILTINS.put(s, "kotlin.text");
        for (String s : "JvmStatic JvmField JvmOverloads JvmName Throws".split(" ")) KT_BUILTINS.put(s, "kotlin.jvm");
    }

    static final class Tok {
        final String s;
        final int line, start, end;
        final boolean ident;
        boolean first;   // first token on its line
        String str;      // content of a string literal token

        Tok(String s, int line, int start, int end, boolean ident) {
            this.s = s; this.line = line; this.start = start; this.end = end; this.ident = ident;
        }
    }

    static final Set<String> KT_OPS = Set.of("?.", "::", "->", "..", "!!", "==", "!=", "<=", ">=", "&&", "||", "+=",
            "-=", "*=", "/=", "%=", "?:", "++", "--");

    static int ktLex(String s, int from, int to, int line, List<Tok> out) {
        int i = from;
        while (i < to) {
            char c = s.charAt(i);
            if (c == '\n') { line++; i++; continue; }
            if (Character.isWhitespace(c)) { i++; continue; }
            if (c == '/' && i + 1 < to && s.charAt(i + 1) == '/') { while (i < to && s.charAt(i) != '\n') i++; continue; }
            if (c == '/' && i + 1 < to && s.charAt(i + 1) == '*') {
                int depth = 1;
                i += 2;
                while (i < to && depth > 0) {
                    if (s.startsWith("/*", i)) { depth++; i += 2; }
                    else if (s.startsWith("*/", i)) { depth--; i += 2; }
                    else { if (s.charAt(i) == '\n') line++; i++; }
                }
                continue;
            }
            if (c == '"') {
                int startLine = line, start = i;
                boolean raw = s.startsWith("\"\"\"", i);
                i += raw ? 3 : 1;
                StringBuilder text = new StringBuilder();
                while (i < to) {
                    char d = s.charAt(i);
                    if (raw ? s.startsWith("\"\"\"", i) : d == '"') {
                        i += raw ? 3 : 1;
                        if (raw) while (i < to && s.charAt(i) == '"') i++;
                        break;
                    }
                    if (!raw && d == '\\') { if (i + 1 < to) text.append(s.charAt(i + 1)); i += 2; continue; }
                    if (d == '\n') line++;
                    if (d == '$' && i + 1 < to) {
                        char e = s.charAt(i + 1);
                        if (e == '{') {
                            int j = i + 2, depth = 1;
                            while (j < to) {
                                char f = s.charAt(j);
                                if (f == '{') depth++;
                                else if (f == '}' && --depth == 0) break;
                                j++;
                            }
                            line = ktLex(s, i + 2, Math.min(j, to), line, out);
                            i = j + 1;
                            continue;
                        }
                        if (Character.isJavaIdentifierStart(e)) {
                            int j = i + 1;
                            while (j < to && Character.isJavaIdentifierPart(s.charAt(j))) j++;
                            out.add(new Tok(s.substring(i + 1, j), line, i + 1, j, true));
                            i = j;
                            continue;
                        }
                    }
                    text.append(d);
                    i++;
                }
                Tok t = new Tok("\"", startLine, start, i, false);
                t.str = text.toString();
                out.add(t);
                continue;
            }
            if (c == '\'') {
                int start = i;
                i++;
                if (i < to && s.charAt(i) == '\\') i += 2; else i++;
                while (i < to && s.charAt(i) != '\'' && s.charAt(i) != '\n') i++;
                i++;
                out.add(new Tok("'", line, start, i, false));
                continue;
            }
            if (c == '`') {
                int j = s.indexOf('`', i + 1);
                if (j < 0 || j > to) j = to;
                out.add(new Tok(s.substring(i + 1, j), line, i, j + 1, true));
                i = j + 1;
                continue;
            }
            if (Character.isJavaIdentifierStart(c)) {
                int j = i + 1;
                while (j < to && Character.isJavaIdentifierPart(s.charAt(j))) j++;
                out.add(new Tok(s.substring(i, j), line, i, j, true));
                i = j;
                continue;
            }
            if (Character.isDigit(c)) {
                int j = i + 1;
                while (j < to && (Character.isLetterOrDigit(s.charAt(j)) || s.charAt(j) == '_'
                        || (s.charAt(j) == '.' && j + 1 < to && Character.isDigit(s.charAt(j + 1))))) j++;
                out.add(new Tok("0", line, i, j, false));
                i = j;
                continue;
            }
            String op = i + 1 < to ? s.substring(i, i + 2) : "";
            if (KT_OPS.contains(op)) {
                out.add(new Tok(op.equals("?.") ? "." : op, line, i, i + 2, false));
                i += 2;
                continue;
            }
            out.add(new Tok(String.valueOf(c), line, i, i + 1, false));
            i++;
        }
        return line;
    }

    /** One parsed .kt file. */
    final class KtFile {
        final Root root;
        final List<Tok> t;
        final int[] match;
        final Map<String, String> imports = new HashMap<>();   // simple name or alias -> qualified name
        final List<String> starImports = new ArrayList<>();
        String pkg = "", facade;
        Node node;

        KtFile(Root root, List<Tok> t) {
            this.root = root;
            this.t = t;
            match = new int[t.size()];
            Arrays.fill(match, -1);
            Deque<Integer> stack = new ArrayDeque<>();
            for (int i = 0; i < t.size(); i++) {
                String s = t.get(i).s;
                if (s.equals("(") || s.equals("[") || s.equals("{")) stack.push(i);
                else if (s.equals(")") || s.equals("]") || s.equals("}")) {
                    String open = s.equals(")") ? "(" : s.equals("]") ? "[" : "{";
                    if (!stack.isEmpty() && t.get(stack.peek()).s.equals(open)) {
                        int o = stack.pop();
                        match[o] = i;
                        match[i] = o;
                    }
                }
                t.get(i).first = i == 0 || t.get(i - 1).line != t.get(i).line;
            }
        }

        boolean is(int i, String s) { return i >= 0 && i < t.size() && t.get(i).s.equals(s) && !t.get(i).s.isEmpty(); }
        boolean word(int i, String s) { return is(i, s) && t.get(i).ident; }
        boolean ident(int i) { return i >= 0 && i < t.size() && t.get(i).ident && !KT_HARD.contains(t.get(i).s); }
        int close(int open, int limit) { int m = match[open]; return m > open ? m : limit - 1; }
    }

    /** A token range whose names are resolved as dependencies of `from`. */
    static final class Span {
        final KtFile f;
        final int from, s, e, kind;       // kind: forced edge kind for type references, or -1
        final List<Integer> types;        // enclosing types, outermost first

        Span(KtFile f, int from, int s, int e, List<Integer> types, int kind) {
            this.f = f; this.from = from; this.s = s; this.e = e; this.types = types; this.kind = kind;
        }
    }

    final class Kotlin {
        final List<Span> spans = new ArrayList<>();
        final List<Object[]> typeRefs = new ArrayList<>();     // {memberIdx, KtFile, start, end, types, isInit}
        final List<Object[]> superRefs = new ArrayList<>();    // {typeIdx, KtFile, headStart, headEnd, hasArgs, types}
        final List<Object[]> receivers = new ArrayList<>();    // {funIdx, KtFile, start, end, types}
        final Map<Integer, Integer> companion = new HashMap<>();
        final Map<Integer, String> filePkg = new HashMap<>();
        // lookup tables, built in resolveAll()
        final Map<Integer, Map<String, List<Integer>>> members = new HashMap<>();
        final Map<String, Map<String, List<Integer>>> topLevel = new HashMap<>();
        final Map<Integer, List<Integer>> supers = new HashMap<>();
        final Map<Integer, Integer> valueType = new HashMap<>();
        Node last;   // declaration created by the most recent declare* call

        // ------------------------------------------------ declarations

        void declare(Path file, Root root) throws IOException {
            String src = Files.readString(file);
            List<Tok> toks = new ArrayList<>();
            ktLex(src, 0, src.length(), 1, toks);
            KtFile f = new KtFile(root, toks);
            int i = header(f);
            String name = file.getFileName().toString();
            String rel = root.projectDir.relativize(file).toString().replace('\\', '/');
            Node fn = node("F:" + root.module + ":" + rel, name, "file", packageNode(root.module, f.pkg));
            fn.kotlin = true;
            fn.path = rel;
            fn.test = root.test;
            fn.line = 1;
            fn.loc = (int) src.chars().filter(c -> c == '\n').count() + 1;
            f.node = fn;
            filePkg.put(fn.idx, f.pkg);
            if (f.facade == null) {
                String base = name.substring(0, name.length() - 3);
                f.facade = Character.toUpperCase(base.charAt(0)) + base.substring(1) + "Kt";
            }
            byId.putIfAbsent("T:" + qual(f.pkg, f.facade), fn);   // Java sees top-level functions as FileKt.fn()
            decls(f, i, toks.size(), List.of(), fn);
        }

        int header(KtFile f) {
            List<Tok> t = f.t;
            int i = 0;
            while (i < t.size()) {
                if (f.is(i, "@") && f.word(i + 1, "file") && f.is(i + 2, ":")) {
                    if (f.word(i + 3, "JvmName") && f.is(i + 4, "(") && f.is(i + 5, "\"")) f.facade = t.get(i + 5).str;
                    i = annotationEnd(f, i, t.size());
                } else if (f.word(i, "package")) {
                    i = dotted(f, i + 1);
                    f.pkg = lastDotted;
                    if (f.is(i, ";")) i++;
                } else if (f.word(i, "import")) {
                    int line = t.get(i).line, j = i + 1;
                    StringBuilder sb = new StringBuilder();
                    boolean star = false;
                    while (j < t.size() && t.get(j).line == line && (t.get(j).ident || f.is(j, ".") || f.is(j, "*"))
                            && !(f.word(j, "as") && !f.is(j - 1, "."))) {
                        if (f.is(j, "*")) star = true; else sb.append(t.get(j).s);
                        j++;
                    }
                    String fq = sb.toString();
                    if (star) f.starImports.add(fq.endsWith(".") ? fq.substring(0, fq.length() - 1) : fq);
                    else if (!fq.isEmpty()) {
                        String alias = fq.substring(fq.lastIndexOf('.') + 1);
                        if (f.word(j, "as") && j + 1 < t.size()) { alias = t.get(j + 1).s; j += 2; }
                        f.imports.put(alias, fq);
                    }
                    i = f.is(j, ";") ? j + 1 : j;
                } else break;
            }
            return i;
        }

        String lastDotted = "";

        /** Reads a dotted name starting at i (a.b.c); returns the index after it, text in lastDotted. */
        int dotted(KtFile f, int i) {
            StringBuilder sb = new StringBuilder();
            while (i < f.t.size() && f.t.get(i).ident) {
                sb.append(f.t.get(i).s);
                if (f.is(i + 1, ".") && i + 2 < f.t.size() && f.t.get(i + 2).ident) { sb.append('.'); i += 2; }
                else { i++; break; }
            }
            lastDotted = sb.toString();
            return i;
        }

        int annotationEnd(KtFile f, int i, int e) {
            int k = i + 1;
            if (f.is(k, "[")) return f.close(k, e) + 1;
            if (f.ident(k) && f.is(k + 1, ":")) k += 2;   // use-site target, e.g. @field:Ann
            k = dotted(f, k);
            if (f.is(k, "<")) { int g = angleEnd(f, k, e); if (g > 0) k = g + 1; }
            if (f.is(k, "(") && f.t.get(k).start == f.t.get(k - 1).end) k = f.close(k, e) + 1;
            return Math.max(k, i + 1);
        }

        /** Matching '>' for a '<' at i in a type context, or -1. */
        int angleEnd(KtFile f, int i, int e) {
            int depth = 0;
            for (int k = i; k < e && k < i + 200; k++) {
                String s = f.t.get(k).s;
                if (s.equals("<")) depth++;
                else if (s.equals(">") && --depth == 0) return k;
                else if (s.equals("(")) { k = f.close(k, e); continue; }
                else if (!(f.t.get(k).ident || s.equals(".") || s.equals(",") || s.equals("?") || s.equals("*")
                        || s.equals(":") || s.equals("->") || s.equals("@") || s.equals(")"))) return -1;
            }
            return -1;
        }

        boolean declStart(KtFile f, int i, int e) {
            int j = i;
            while (j < e) {
                if (f.is(j, "@")) { j = annotationEnd(f, j, e); continue; }
                if (f.t.get(j).ident && KT_MODS.contains(f.t.get(j).s)) { j++; continue; }
                break;
            }
            if (j >= e || !f.t.get(j).ident) return false;
            String w = f.t.get(j).s;
            if (w.equals("init")) return f.is(j + 1, "{");
            if (w.equals("constructor")) return f.is(j + 1, "(");
            return KT_DECL.contains(w);
        }

        /** End of an expression body / initializer starting at s: next declaration on a new line, ';' or e. */
        int declEnd(KtFile f, int s, int e) {
            int i = s;
            while (i < e) {
                if (i > s && f.t.get(i).first && declStart(f, i, e)) return i;
                if (f.is(i, ";")) return i;
                String c = f.t.get(i).s;
                if ((c.equals("(") || c.equals("[") || c.equals("{")) && f.match[i] > i) { i = f.match[i] + 1; continue; }
                if (c.equals("}") || c.equals(")") || c.equals("]")) return i;   // closes an enclosing scope
                i++;
            }
            return e;
        }

        /** End of a type starting at s (stops at = { , ) by where ; or a new line). */
        int typeEnd(KtFile f, int s, int e) {
            int i = s, angle = 0;
            while (i < e) {
                String c = f.t.get(i).s;
                if (i > s && f.t.get(i).first && angle == 0 && !c.equals(".") && !c.equals("->")) return i;
                if (c.equals("<")) angle++;
                else if (c.equals(">")) { if (angle == 0) return i; angle--; }
                else if (c.equals("(") && f.match[i] > i) { i = f.match[i] + 1; continue; }
                else if (angle == 0 && (c.equals("=") || c.equals("{") || c.equals(",") || c.equals(")") || c.equals(";")
                        || (f.t.get(i).ident && (c.equals("where") || c.equals("by"))))) return i;
                i++;
            }
            return e;
        }

        void span(KtFile f, Node from, int s, int e, List<Integer> types, int kind) {
            if (from != null && s < e) spans.add(new Span(f, from.idx, s, e, types, kind));
        }

        List<Integer> plus(List<Integer> types, int t) {
            List<Integer> l = new ArrayList<>(types);
            l.add(t);
            return l;
        }

        String ownerQ(KtFile f, Node owner) { return owner.kind.equals("file") ? qual(f.pkg, f.facade) : owner.q; }

        void mods(Node n, Set<String> mods, KtFile f) {
            n.vis = mods.contains("private") ? "-" : mods.contains("protected") ? "#" : mods.contains("internal") ? "~" : "+";
            n.test = f.root.test;
        }

        void place(Node n, KtFile f, int s, int e) {
            n.line = f.t.get(s).line;
            n.loc = f.t.get(Math.max(s, Math.min(e, f.t.size()) - 1)).line - n.line + 1;
        }

        void decls(KtFile f, int s, int e, List<Integer> types, Node owner) {
            int i = s;
            while (i < e) {
                int start = i;
                Set<String> mods = new HashSet<>();
                List<int[]> anns = new ArrayList<>();
                while (i < e) {
                    if (f.is(i, "@")) { int a = annotationEnd(f, i, e); anns.add(new int[]{i, a}); i = a; continue; }
                    if (f.t.get(i).ident && KT_MODS.contains(f.t.get(i).s) && i + 1 < e && (f.t.get(i + 1).ident || f.is(i + 1, "@"))) {
                        mods.add(f.t.get(i).s);
                        i++;
                        continue;
                    }
                    break;
                }
                if (i >= e) { for (int[] a : anns) span(f, owner, a[0], a[1], types, -1); break; }
                last = null;
                String w = f.t.get(i).ident ? f.t.get(i).s : "";
                switch (w) {
                    case "class": case "interface": i = declareClass(f, i, e, types, owner, mods, w); break;
                    case "object": i = declareClass(f, i, e, types, owner, mods, w); break;
                    case "fun":
                        i = f.word(i + 1, "interface") ? declareClass(f, i + 1, e, types, owner, mods, "interface")
                                : declareFun(f, i, e, types, owner, mods);
                        break;
                    case "val": case "var": i = declareProp(f, i, e, types, owner, mods); break;
                    case "constructor": i = declareCtor(f, i, e, types, owner, mods); break;
                    case "typealias": i = declareAlias(f, i, e, types, owner, mods); break;
                    case "init":
                        if (f.is(i + 1, "{")) {
                            int c = f.close(i + 1, e);
                            span(f, owner, i + 1, c + 1, types, -1);
                            i = c + 1;
                        } else i++;
                        break;
                    default:
                        if (f.is(i, "{") || f.is(i, "(")) i = f.close(i, e) + 1;   // stray block: skip
                        else i++;
                }
                Node target = last != null ? last : owner;
                for (int[] a : anns) span(f, target, a[0], a[1], types, -1);
                if (i <= start) i = start + 1;
            }
        }

        int declareClass(KtFile f, int i, int e, List<Integer> types, Node owner, Set<String> mods, String kw) {
            int k = i + 1;
            String name;
            if (f.ident(k)) name = f.t.get(k++).s;
            else if (kw.equals("object") && mods.contains("companion")) name = "Companion";
            else return k;
            String q = owner.kind.equals("file") ? qual(f.pkg, name) : owner.q + "." + name;
            String kind = kw.equals("interface") ? "interface" : kw.equals("object") ? "object"
                    : mods.contains("enum") ? "enum" : mods.contains("annotation") ? "annotation" : "class";
            Node n = node("T:" + q, name, kind, owner.idx);
            n.kotlin = true;
            n.q = q;
            n.simple = name;
            mods(n, mods, f);
            n.isStatic = kind.equals("object");
            if (mods.contains("companion") && !owner.kind.equals("file")) companion.put(owner.idx, n.idx);
            List<Integer> inner = plus(types, n.idx);
            if (f.is(k, "<")) { int g = angleEnd(f, k, e); if (g > 0) { span(f, n, k, g + 1, inner, -1); k = g + 1; } }
            // primary constructor
            int c = k;
            while (c < e && (f.is(c, "@") || (f.t.get(c).ident && KT_MODS.contains(f.t.get(c).s)))) c = f.is(c, "@") ? annotationEnd(f, c, e) : c + 1;
            if (f.word(c, "constructor")) c++;
            if (f.is(c, "(")) {
                int close = f.close(c, e);
                primaryCtor(f, n, k, c, close, inner);
                k = close + 1;
            }
            // supertypes
            if (f.is(k, ":")) {
                k++;
                while (k < e) {
                    int head = k;
                    k = dotted(f, k);
                    if (k == head) break;
                    int headEnd = k;
                    int restStart = k;
                    if (f.is(k, "<")) { int g = angleEnd(f, k, e); if (g > 0) k = g + 1; }
                    boolean args = false;
                    if (f.is(k, "(")) { args = true; k = f.close(k, e) + 1; }
                    if (f.word(k, "by")) k = declEndSupers(f, k + 1, e);
                    superRefs.add(new Object[]{n.idx, f, head, headEnd, args, inner});
                    span(f, n, restStart, k, inner, -1);
                    if (f.is(k, ",")) k++; else break;
                }
            }
            if (f.word(k, "where")) { int w = k; while (k < e && !f.is(k, "{") && !(k > w && f.t.get(k).first && declStart(f, k, e))) k++; span(f, n, w, k, inner, -1); }
            if (f.is(k, "{")) {
                int close = f.close(k, e);
                int body = k + 1;
                if (kind.equals("enum")) body = enumEntries(f, n, body, close, inner);
                decls(f, body, close, inner, n);
                place(n, f, i, close + 1);
                last = n;
                return close + 1;
            }
            place(n, f, i, k);
            last = n;
            return k;
        }

        int declEndSupers(KtFile f, int s, int e) {
            int i = s;
            while (i < e && !f.is(i, ",") && !f.is(i, "{") && !(i > s && f.t.get(i).first && declStart(f, i, e))) {
                if (f.is(i, "(") || f.is(i, "[")) i = f.close(i, e);
                i++;
            }
            return i;
        }

        /** Splits a parenthesised parameter list into parameter ranges. */
        List<int[]> params(KtFile f, int open, int close) {
            List<int[]> out = new ArrayList<>();
            int s = open + 1, angle = 0;
            for (int i = open + 1; i < close; i++) {
                String c = f.t.get(i).s;
                if ((c.equals("(") || c.equals("[") || c.equals("{")) && f.match[i] > i) { i = f.match[i]; continue; }
                if (c.equals("<") && i > 0 && f.t.get(i - 1).ident) angle++;
                else if (c.equals(">") && angle > 0) angle--;
                else if (c.equals(",") && angle == 0) { if (i > s) out.add(new int[]{s, i}); s = i + 1; }
            }
            if (close > s) out.add(new int[]{s, close});
            return out;
        }

        /** For a parameter range: {nameIdx, typeStart, typeEnd, defaultStart, isProperty(0/1), modsStart} */
        int[] param(KtFile f, int[] r) {
            int i = r[0];
            boolean prop = false;
            Set<String> mods = new HashSet<>();
            while (i < r[1]) {
                if (f.is(i, "@")) { i = annotationEnd(f, i, r[1]); continue; }
                String w = f.t.get(i).s;
                if (f.t.get(i).ident && (w.equals("val") || w.equals("var"))) { prop = true; i++; continue; }
                if (f.t.get(i).ident && KT_MODS.contains(w) && i + 1 < r[1] && f.t.get(i + 1).ident) { mods.add(w); i++; continue; }
                break;
            }
            int name = i, ts = -1, te = -1, def = -1;
            if (f.is(i + 1, ":")) { ts = i + 2; te = typeEnd(f, ts, r[1]); }
            int k = te > 0 ? te : i + 1;
            if (f.is(k, "=")) def = k + 1;
            return new int[]{name, ts, te, def, prop ? 1 : 0, mods.contains("private") ? 1 : 0};
        }

        String typeText(KtFile f, int s, int e, boolean simple) {
            StringBuilder sb = new StringBuilder();
            for (int i = s; i >= 0 && i < e; i++) sb.append(f.t.get(i).s);
            String t = sb.toString();
            return simple ? QUALIFIED.matcher(t).replaceAll("$1") : t;
        }

        String signature(KtFile f, List<int[]> ps, boolean simple) {
            StringBuilder sb = new StringBuilder();
            for (int[] r : ps) {
                int[] p = param(f, r);
                if (sb.length() > 0) sb.append(simple ? ", " : ",");
                sb.append(p[1] > 0 ? typeText(f, p[1], p[2], simple) : "?");
            }
            return sb.toString();
        }

        void primaryCtor(KtFile f, Node type, int s, int open, int close, List<Integer> inner) {
            List<int[]> ps = params(f, open, close);
            Node ctor = node("M:" + type.q + "#<init>(" + signature(f, ps, false) + ")",
                    type.name + "(" + signature(f, ps, true) + ")", "ctor", type.idx);
            ctor.simple = "<init>";
            ctor.q = type.q + "." + ctor.name;
            ctor.vis = "+";
            ctor.test = f.root.test;
            place(ctor, f, open, close + 1);
            for (int[] r : ps) {
                int[] p = param(f, r);
                if (p[4] == 1 && f.ident(p[0])) {
                    String name = f.t.get(p[0]).s;
                    Node fld = node("M:" + type.q + "#" + name, name, "field", type.idx);
                    fld.simple = name;
                    fld.q = type.q + "." + name;
                    fld.vis = p[5] == 1 ? "-" : "+";
                    fld.test = f.root.test;
                    place(fld, f, r[0], r[1]);
                    if (p[1] > 0) { span(f, fld, p[1], p[2], inner, -1); typeRefs.add(new Object[]{fld.idx, f, p[1], p[2], inner, false}); }
                    if (p[3] > 0) span(f, ctor, p[3], r[1], inner, -1);
                } else span(f, ctor, r[0], r[1], inner, -1);
            }
            span(f, ctor, s, open, inner, -1);   // annotations / visibility of the constructor
        }

        int enumEntries(KtFile f, Node type, int s, int close, List<Integer> inner) {
            int i = s;
            while (i < close) {
                while (f.is(i, "@")) i = annotationEnd(f, i, close);
                if (f.is(i, ";")) return i + 1;
                if (!f.ident(i) || declStart(f, i, close)) return i;
                String name = f.t.get(i).s;
                Node c = node("M:" + type.q + "#" + name, name, "enumconst", type.idx);
                c.simple = name;
                c.q = type.q + "." + name;
                c.vis = "+";
                c.isStatic = true;
                c.test = f.root.test;
                valueType.put(c.idx, type.idx);
                int k = i + 1;
                if (f.is(k, "(")) { int cl = f.close(k, close); span(f, c, k, cl + 1, inner, -1); k = cl + 1; }
                if (f.is(k, "{")) { int cl = f.close(k, close); span(f, c, k, cl + 1, inner, -1); k = cl + 1; }
                place(c, f, i, k);
                if (f.is(k, ",")) i = k + 1;
                else if (f.is(k, ";")) return k + 1;
                else return k;
            }
            return i;
        }

        int declareFun(KtFile f, int i, int e, List<Integer> types, Node owner, Set<String> mods) {
            int k = i + 1;
            if (f.is(k, "<")) { int g = angleEnd(f, k, e); if (g > 0) k = g + 1; }
            int j = k, recvEnd = -1;
            while (j < e && !f.is(j, "(") && j < k + 60) {
                if (f.is(j, "<")) { int g = angleEnd(f, j, e); if (g < 0) break; j = g + 1; continue; }
                if (f.is(j, ".")) recvEnd = j;
                j++;
            }
            if (!f.is(j, "(") || !f.t.get(j - 1).ident) return j;
            String name = f.t.get(j - 1).s;
            int open = j, close = f.close(open, e);
            List<int[]> ps = params(f, open, close);
            int k2 = close + 1, retStart = -1, retEnd = -1;
            if (f.is(k2, ":")) { retStart = k2 + 1; retEnd = typeEnd(f, retStart, e); k2 = retEnd; }
            if (f.word(k2, "where")) { while (k2 < e && !f.is(k2, "{") && !f.is(k2, "=") && !(f.t.get(k2).first && declStart(f, k2, e))) k2++; }
            int end;
            boolean exprBody = false;
            if (f.is(k2, "{")) end = f.close(k2, e) + 1;
            else if (f.is(k2, "=")) { end = declEnd(f, k2 + 1, e); exprBody = true; }
            else end = k2;
            String recv = recvEnd > 0 ? typeText(f, k, recvEnd, false) + "." : "";
            String recvSimple = recvEnd > 0 ? typeText(f, k, recvEnd, true) + "." : "";
            String oq = ownerQ(f, owner);
            Node m = node("M:" + oq + "#" + recv + name + "(" + signature(f, ps, false) + ")",
                    recvSimple + name + "(" + signature(f, ps, true) + ")", "method", owner.idx);
            m.simple = name;
            m.q = oq + "." + m.name;
            mods(m, mods, f);
            m.isStatic = owner.kind.equals("object") || owner.kind.equals("file");
            place(m, f, i, end);
            span(f, m, i, end, types, -1);
            if (retStart > 0) typeRefs.add(new Object[]{m.idx, f, retStart, retEnd, types, false});
            else if (exprBody) typeRefs.add(new Object[]{m.idx, f, k2 + 1, end, types, true});
            if (recvEnd > 0) receivers.add(new Object[]{m.idx, f, k, recvEnd, types});
            last = m;
            return end;
        }

        int declareProp(KtFile f, int i, int e, List<Integer> types, Node owner, Set<String> mods) {
            int k = i + 1;
            if (f.is(k, "<")) { int g = angleEnd(f, k, e); if (g > 0) k = g + 1; }
            if (f.is(k, "(")) return declEnd(f, k, e);   // destructuring declaration
            int recvStart = k, recvEnd = -1;
            while (f.ident(k) && f.is(k + 1, ".") && f.ident(k + 2)) { recvEnd = k + 1; k += 2; }
            if (!f.t.get(Math.min(k, f.t.size() - 1)).ident) return k;
            String name = f.t.get(k).s;
            k++;
            int ts = -1, te = -1;
            if (f.is(k, ":")) { ts = k + 1; te = typeEnd(f, ts, e); k = te; }
            int end = declEnd(f, k, e);
            String recv = recvEnd > 0 ? typeText(f, recvStart, recvEnd, true) + "." : "";
            String oq = ownerQ(f, owner);
            Node p = node("M:" + oq + "#" + recv + name, recv + name, "field", owner.idx);
            p.simple = name;
            p.q = oq + "." + p.name;
            mods(p, mods, f);
            p.isStatic = mods.contains("const") || owner.kind.equals("object") || owner.kind.equals("file");
            place(p, f, i, end);
            span(f, p, i, end, types, -1);
            if (ts > 0) typeRefs.add(new Object[]{p.idx, f, ts, te, types, false});
            else if (f.is(k, "=") || f.word(k, "by")) typeRefs.add(new Object[]{p.idx, f, k + 1, end, types, true});
            if (recvEnd > 0) receivers.add(new Object[]{p.idx, f, recvStart, recvEnd, types});
            last = p;
            return end;
        }

        int declareCtor(KtFile f, int i, int e, List<Integer> types, Node owner, Set<String> mods) {
            int k = i + 1;
            if (!f.is(k, "(") || owner.kind.equals("file")) return k;
            int close = f.close(k, e);
            List<int[]> ps = params(f, k, close);
            int end = close + 1;
            if (f.is(end, ":") && f.t.get(end + 1).ident && f.is(end + 2, "(")) end = f.close(end + 2, e) + 1;
            if (f.is(end, "{")) end = f.close(end, e) + 1;
            Node c = node("M:" + owner.q + "#<init>(" + signature(f, ps, false) + ")",
                    owner.name + "(" + signature(f, ps, true) + ")", "ctor", owner.idx);
            c.simple = "<init>";
            c.q = owner.q + "." + c.name;
            mods(c, mods, f);
            place(c, f, i, end);
            span(f, c, i, end, types, -1);
            last = c;
            return end;
        }

        int declareAlias(KtFile f, int i, int e, List<Integer> types, Node owner, Set<String> mods) {
            if (!f.ident(i + 1)) return i + 1;
            String name = f.t.get(i + 1).s;
            int k = i + 2;
            if (f.is(k, "<")) { int g = angleEnd(f, k, e); if (g > 0) k = g + 1; }
            int end = f.is(k, "=") ? declEnd(f, k + 1, e) : k;
            String q = owner.kind.equals("file") ? qual(f.pkg, name) : owner.q + "." + name;
            Node a = node("T:" + q, name, "typealias", owner.idx);
            a.kotlin = true;
            a.q = q;
            a.simple = name;
            mods(a, mods, f);
            place(a, f, i, end);
            if (f.is(k, "=")) {
                span(f, a, k + 1, end, types, -1);
                typeRefs.add(new Object[]{a.idx, f, k + 1, end, types, false});
            }
            last = a;
            return end;
        }

        // ------------------------------------------------ resolution

        boolean isType(Node n) {
            switch (n.kind) {
                case "class": case "interface": case "enum": case "record": case "annotation": case "object":
                case "typealias": case "ext-type": return true;
                default: return false;
            }
        }

        void index(Map<String, List<Integer>> m, String name, int idx) {
            List<Integer> l = m.computeIfAbsent(name, x -> new ArrayList<>());
            if (!l.contains(idx)) l.add(idx);
        }

        void resolveAll() {
            // Members of all types (Java and Kotlin), top-level Kotlin functions/properties per package.
            for (Node n : List.copyOf(nodes)) {
                if (n.simple == null || !n.id.startsWith("M:") || n.parent < 0) continue;
                Node p = nodes.get(n.parent);
                if (p.kind.equals("file")) {
                    index(topLevel.computeIfAbsent(filePkg.getOrDefault(p.idx, ""), x -> new HashMap<>()), n.simple, n.idx);
                    continue;
                }
                Map<String, List<Integer>> m = members.computeIfAbsent(p.idx, x -> new HashMap<>());
                index(m, n.simple, n.idx);
                // Kotlin sees Java getters as properties: getStatus()/isOpen() -> status/open
                if (n.java && n.kind.equals("method") && n.name.endsWith("()")) {
                    String s = n.simple;
                    String prop = s.startsWith("get") && s.length() > 3 && Character.isUpperCase(s.charAt(3)) ? s.substring(3)
                            : s.startsWith("is") && s.length() > 2 && Character.isUpperCase(s.charAt(2)) ? s.substring(2) : null;
                    if (prop != null) index(m, Character.toLowerCase(prop.charAt(0)) + prop.substring(1), n.idx);
                }
                if (n.java && n.valueType != null) { Node vt = byId.get("T:" + n.valueType); if (vt != null) valueType.put(n.idx, vt.idx); }
            }
            for (Node n : List.copyOf(nodes)) {
                if (n.java && n.supers != null) for (String s : n.supers) {
                    Node st = byId.get("T:" + s);
                    if (st != null) supers.computeIfAbsent(n.idx, x -> new ArrayList<>()).add(st.idx);
                }
            }
            // Supertypes of Kotlin types (+ extends/implements edges).
            for (Object[] r : superRefs) {
                int type = (int) r[0];
                KtFile f = (KtFile) r[1];
                @SuppressWarnings("unchecked") List<Integer> ts = (List<Integer>) r[5];
                Span sp = new Span(f, type, (int) r[2], (int) r[3], ts, -1);
                int st = qualifiedType(sp, (int) r[2], (int) r[3]);
                if (st < 0) continue;
                supers.computeIfAbsent(type, x -> new ArrayList<>()).add(st);
                Node self = nodes.get(type), target = nodes.get(st);
                boolean ext = (boolean) r[4] || self.kind.equals("interface") || target.kind.equals("class") || target.kind.equals("enum");
                addEdge(type, st, ext ? K_EXTENDS : K_IMPLEMENTS, f.t.get((int) r[2]).line);
            }
            // Declared (or simply inferred) types of properties, return types, alias targets.
            for (Object[] r : typeRefs) {
                KtFile f = (KtFile) r[1];
                @SuppressWarnings("unchecked") List<Integer> ts = (List<Integer>) r[4];
                Span sp = new Span(f, (int) r[0], (int) r[2], (int) r[3], ts, -1);
                int t = (boolean) r[5] ? firstProjectType(sp) : declaredType(sp, (int) r[2], (int) r[3]);
                if (t >= 0) valueType.put((int) r[0], t);
            }
            // Extension functions/properties are found as members of their receiver type.
            for (Object[] r : receivers) {
                KtFile f = (KtFile) r[1];
                @SuppressWarnings("unchecked") List<Integer> ts = (List<Integer>) r[4];
                int t = qualifiedType(new Span(f, (int) r[0], (int) r[2], (int) r[3], ts, -1), (int) r[2], (int) r[3]);
                if (t >= 0) index(members.computeIfAbsent(t, x -> new HashMap<>()), nodes.get((int) r[0]).simple, (int) r[0]);
            }
            for (Object[] p : javaToKotlin) javaMemberRef((Raw) p[0], (int) p[1]);
            for (Span sp : spans) refs(sp);
        }

        /** Java reference Foo.INSTANCE.bar / Foo.<init> / FooKt.fn on a Kotlin type (javac sees an error symbol). */
        void javaMemberRef(Raw r, int type) {
            String name = null;
            for (String s : r.tail) if (!s.equals("INSTANCE") && !s.equals("Companion")) { name = s; break; }
            if (name == null) { addEdge(r.from, type, K_USES, r.line); return; }   // the object itself
            if (name.equals("<init>")) {
                List<Integer> ctors = members.getOrDefault(type, Map.of()).getOrDefault("<init>", List.of());
                addEdge(r.from, ctors.size() == 1 ? ctors.get(0) : type, K_NEW, r.line);
                return;
            }
            List<Integer> ms = lookup(type, name);
            Node fileFacade = nodes.get(type);
            if (ms.isEmpty() && fileFacade.kind.equals("file")) ms = topLevel.getOrDefault(filePkg.getOrDefault(type, ""), Map.of()).getOrDefault(name, List.of());
            if (ms.isEmpty()) { addEdge(r.from, type, K_USES, r.line); return; }
            int m = pick(ms, true);
            String k = nodes.get(m).kind;
            addEdge(r.from, m, k.equals("ctor") ? K_NEW : k.equals("method") ? K_CALL : K_ACCESS, r.line);
        }

        /** Type of a declared type expression: the outer type, or for collections/nullable wrappers the first project type argument. */
        int declaredType(Span sp, int s, int e) {
            int t = qualifiedType(sp, s, e);
            if (t >= 0 && !nodes.get(t).kind.startsWith("ext")) return t;
            int inner = firstProjectType(new Span(sp.f, sp.from, s, e, sp.types, -1));
            return inner >= 0 ? inner : t;
        }

        int firstProjectType(Span sp) {
            for (int i = sp.s; i < sp.e; i++) {
                if (!sp.f.ident(i) || (i > sp.s && (sp.f.is(i - 1, ".") || sp.f.is(i - 1, "::")))) continue;
                int t = typeName(sp, sp.f.t.get(i).s);
                if (t >= 0 && !nodes.get(t).kind.startsWith("ext")) return aliasTarget(t);
            }
            return -1;
        }

        int aliasTarget(int t) {
            Node n = nodes.get(t);
            if (n.kind.equals("typealias")) { Integer a = valueType.get(t); if (a != null && a != t) return a; }
            return t;
        }

        /** Resolves a (possibly qualified) type name in tokens [s, e). */
        int qualifiedType(Span sp, int s, int e) {
            KtFile f = sp.f;
            if (!f.ident(s)) return -1;
            int t = typeName(sp, f.t.get(s).s);
            int i = s;
            if (t < 0) {   // package-qualified: a.b.Type
                StringBuilder q = new StringBuilder(f.t.get(s).s);
                while (i + 2 < e && f.is(i + 1, ".") && f.ident(i + 2)) {
                    q.append('.').append(f.t.get(i + 2).s);
                    i += 2;
                    Node n = byId.get("T:" + q);
                    if (n != null && isType(n)) { t = n.idx; break; }
                }
                if (t < 0) return -1;
            }
            while (i + 2 < e && f.is(i + 1, ".") && f.ident(i + 2)) {   // nested: Outer.Inner
                int nt = nested(t, f.t.get(i + 2).s);
                if (nt < 0) break;
                t = nt;
                i += 2;
            }
            return t;
        }

        int nested(int type, String name) {
            Node n = nodes.get(type);
            if (n.q == null) return -1;
            Node c = byId.get("T:" + n.q + "." + name);
            return c != null && isType(c) ? c.idx : -1;
        }

        /** Resolves a simple type name in the context of a span. */
        int typeName(Span sp, String name) {
            KtFile f = sp.f;
            for (int k = sp.types.size() - 1; k >= 0; k--) {
                int t = sp.types.get(k);
                if (nodes.get(t).name.equals(name)) return t;
                int nt = nested(t, name);
                if (nt >= 0) return nt;
                for (int st : supers.getOrDefault(t, List.of())) { nt = nested(st, name); if (nt >= 0) return nt; }
            }
            String fq = f.imports.get(name);
            if (fq != null) {
                Node n = byId.get("T:" + fq);
                if (n != null && isType(n)) return n.idx;
                int dot = fq.lastIndexOf('.');
                String pkg = dot < 0 ? "" : fq.substring(0, dot), simple = fq.substring(dot + 1);
                if (topLevel.getOrDefault(pkg, Map.of()).containsKey(simple)) return -1;   // imported function
                if (!Character.isUpperCase(simple.charAt(0))) return -1;
                // nested class of an imported type, e.g. import a.b.Outer.Inner
                int d2 = pkg.lastIndexOf('.');
                if (d2 > 0 && Character.isUpperCase(pkg.charAt(d2 + 1))) {
                    Node outer = byId.get("T:" + pkg);
                    if (outer == null) return external(pkg.substring(0, d2), pkg, pkg.substring(d2 + 1), null);
                }
                return external(pkg.isEmpty() ? "(default package)" : pkg, fq, simple, null);
            }
            Node n = byId.get("T:" + qual(f.pkg, name));
            if (n != null && isType(n)) return n.idx;
            for (String p : f.starImports) {
                n = byId.get("T:" + qual(p, name));
                if (n != null && isType(n)) return n.idx;
            }
            String bp = KT_BUILTINS.get(name);
            if (bp != null) return external(bp, bp + "." + name, name, null);
            return -1;
        }

        List<Integer> lookup(int type, String name) { return lookup(type, name, new HashSet<>()); }

        List<Integer> lookup(int type, String name, Set<Integer> seen) {
            type = aliasTarget(type);
            if (!seen.add(type)) return List.of();
            List<Integer> m = members.getOrDefault(type, Map.of()).get(name);
            if (m != null && !m.isEmpty()) return m;
            Integer c = companion.get(type);
            if (c != null) { List<Integer> r = lookup(c, name, seen); if (!r.isEmpty()) return r; }
            for (int st : supers.getOrDefault(type, List.of())) {
                List<Integer> r = lookup(st, name, seen);
                if (!r.isEmpty()) return r;
            }
            return List.of();
        }

        int pick(List<Integer> ms, boolean call) {
            for (int m : ms) {
                String k = nodes.get(m).kind;
                if (call == (k.equals("method") || k.equals("ctor"))) return m;
            }
            return ms.isEmpty() ? -1 : ms.get(0);
        }

        /** Index of the '(' if the identifier at i is called (also foo<T>(...)), else -1. */
        int callParen(KtFile f, int i) {
            if (f.is(i + 1, "(") && f.t.get(i + 1).start == f.t.get(i).end) return i + 1;
            if (f.is(i + 1, "(")) return i + 1;
            if (f.is(i + 1, "<")) {
                int g = angleEnd(f, i + 1, f.t.size());
                if (g > 0 && f.is(g + 1, "(")) return g + 1;
            }
            return -1;
        }

        /** A name without receiver: member of an enclosing type, top-level in this package, or imported. */
        int bare(Span sp, String name, boolean call) {
            for (int k = sp.types.size() - 1; k >= 0; k--) {
                List<Integer> ms = lookup(sp.types.get(k), name);
                if (!ms.isEmpty()) return pick(ms, call);
            }
            KtFile f = sp.f;
            List<Integer> l = topLevel.getOrDefault(f.pkg, Map.of()).get(name);
            if (l != null) return pick(l, call);
            String fq = f.imports.get(name);
            if (fq != null) {
                int dot = fq.lastIndexOf('.');
                l = topLevel.getOrDefault(dot < 0 ? "" : fq.substring(0, dot), Map.of()).get(fq.substring(dot + 1));
                if (l != null) return pick(l, call);
                if (dot > 0) {   // import of a member of a type (Java static import style / object member)
                    Node owner = byId.get("T:" + fq.substring(0, dot));
                    if (owner != null) { List<Integer> ms = lookup(owner.idx, fq.substring(dot + 1)); if (!ms.isEmpty()) return pick(ms, call); }
                }
            }
            for (String p : f.starImports) {
                l = topLevel.getOrDefault(p, Map.of()).get(name);
                if (l != null) return pick(l, call);
            }
            return -1;
        }

        /** Member with unknown receiver: unique match among the types this declaration / its class uses. */
        int guess(Span sp, String name, Set<Integer> used, boolean call) {
            int r = unique(used, name, call);
            if (r >= 0 || sp.types.isEmpty()) return r;
            Set<Integer> wider = new LinkedHashSet<>();
            int t = sp.types.get(sp.types.size() - 1);
            for (List<Integer> ms : members.getOrDefault(t, Map.of()).values())
                for (int m : ms) { Integer vt = valueType.get(m); if (vt != null) wider.add(vt); }
            wider.removeAll(used);
            return unique(wider, name, call);
        }

        int unique(Set<Integer> cands, String name, boolean call) {
            int found = -1;
            for (int c : cands) {
                List<Integer> ms = lookup(c, name);
                if (ms.isEmpty()) continue;
                int m = pick(ms, call);
                if (found >= 0 && found != m) return -1;   // ambiguous
                found = m;
            }
            return found;
        }

        void refs(Span sp) {
            KtFile f = sp.f;
            List<Tok> t = f.t;
            Map<String, Integer> vars = new HashMap<>();
            Set<Integer> used = new LinkedHashSet<>();
            Map<Integer, Integer> typeOf = new HashMap<>();
            int self = sp.types.isEmpty() ? -1 : sp.types.get(sp.types.size() - 1);
            for (int i = sp.s; i < sp.e; i++) {
                Tok k = t.get(i);
                if (!k.ident) continue;
                String s = k.s;
                if (KT_HARD.contains(s) && t.get(i).end - t.get(i).start == s.length()) {
                    if ((s.equals("this") || s.equals("super")) && self >= 0) typeOf.put(i, self);
                    continue;
                }
                if (f.is(i - 1, "@") && i >= 2 && t.get(i - 2).end == t.get(i - 1).start) continue;   // label: return@forEach
                if (f.is(i + 1, "=") && (f.is(i - 1, "(") || f.is(i - 1, ","))) continue;                // named argument
                int line = k.line;
                // declarations of local names: (x: T, val x: T, { x: T -> }, val x = T(...)
                if (f.is(i + 1, ":") && (f.is(i - 1, "(") || f.is(i - 1, ",") || f.word(i - 1, "val") || f.word(i - 1, "var") || f.is(i - 1, "{"))) {
                    int ty = declaredType(sp, i + 2, typeEnd(f, i + 2, sp.e));
                    vars.put(s, ty);
                    continue;
                }
                if ((f.word(i - 1, "val") || f.word(i - 1, "var")) && i - 1 > sp.s) {   // local: val x = Foo(...)
                    vars.put(s, f.is(i + 1, "=") && f.ident(i + 2) ? typeName(sp, t.get(i + 2).s) : -1);
                    continue;
                }
                if (f.word(i - 1, "for") || (f.is(i - 1, "(") && f.word(i - 2, "for"))) { vars.put(s, -1); continue; }
                boolean afterDot = i > sp.s && (f.is(i - 1, ".") || f.is(i - 1, "::"));
                int call = callParen(f, i);
                boolean isCall = call >= 0 || f.is(i + 1, "{");
                if (afterDot) {
                    Integer recv = i - 2 >= sp.s ? typeOf.get(i - 2) : null;
                    if (recv == null && i - 2 >= sp.s && t.get(i - 2).ident) recv = vars.get(t.get(i - 2).s);
                    int target = -1;
                    if (recv != null && recv >= 0) {
                        int nt = nested(recv, s);
                        if (nt >= 0) { addEdge(sp.from, nt, K_USES, line); used.add(nt); typeOf.put(i, nt); continue; }
                        List<Integer> ms = lookup(recv, s);
                        if (!ms.isEmpty()) target = pick(ms, isCall);
                        else if (!nodes.get(recv).kind.startsWith("ext")) continue;   // project type without such member
                    }
                    if (target < 0) target = guess(sp, s, used, isCall);
                    if (target >= 0) edgeTo(sp, target, isCall, line, i, call, typeOf, used);
                    continue;
                }
                Integer vt = vars.get(s);
                if (vt != null) { if (vt >= 0) { typeOf.put(i, vt); used.add(vt); } continue; }
                int ty = typeName(sp, s), at = i;
                if (ty < 0 && Character.isLowerCase(s.charAt(0)) && f.is(i + 1, ".")) {   // a.b.Type
                    StringBuilder q = new StringBuilder(s);
                    int j = i;
                    while (j + 2 < sp.e && f.is(j + 1, ".") && f.ident(j + 2)) {
                        q.append('.').append(t.get(j + 2).s);
                        j += 2;
                        Node n = byId.get("T:" + q);
                        if (n != null && isType(n)) { ty = n.idx; at = j; break; }
                    }
                }
                if (ty >= 0) {
                    int c2 = callParen(f, at);
                    int target = ty, kind = sp.kind >= 0 ? sp.kind : K_USES;
                    if (c2 >= 0 && sp.kind < 0) {
                        kind = K_NEW;
                        List<Integer> ctors = members.getOrDefault(aliasTarget(ty), Map.of()).get("<init>");
                        if (ctors != null && !ctors.isEmpty()) target = ctors.get(0);
                    }
                    addEdge(sp.from, target, kind, t.get(at).line);
                    int at2 = aliasTarget(ty);
                    used.add(at2);
                    typeOf.put(at, at2);
                    if (c2 >= 0 && f.match[c2] > c2) typeOf.put(f.match[c2], at2);
                    i = at;
                    continue;
                }
                int target = bare(sp, s, isCall);
                if (target >= 0) edgeTo(sp, target, isCall, line, i, call, typeOf, used);
            }
        }

        void edgeTo(Span sp, int target, boolean isCall, int line, int i, int call, Map<Integer, Integer> typeOf, Set<Integer> used) {
            String k = nodes.get(target).kind;
            boolean fn = k.equals("method") || k.equals("ctor");
            addEdge(sp.from, target, fn ? K_CALL : K_ACCESS, line);
            Integer vt = valueType.get(target);
            if (vt != null) {
                used.add(vt);
                typeOf.put(i, vt);
                if (call >= 0 && sp.f.match[call] > call) typeOf.put(sp.f.match[call], vt);
            }
        }
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
