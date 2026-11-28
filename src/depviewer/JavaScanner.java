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
import static depviewer.DepScan.*;
import static depviewer.Model.*;

/** Java: parsed and attributed by the JDK compiler (javac Trees API), so references are resolved exactly. */
final class JavaScanner {

    /** Name and version of this built-in extractor in the provenance (spec FR-011). */
    static final String EXTRACTOR = "depviewer-java", VERSION = "1.0.0";

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

    final List<Raw> raws = new ArrayList<>();
    Trees trees;
    Elements elements;
    Types types;

    void scan(Map<Path, Root> rootOfFile, List<Root> roots, String cp, int[] errors, List<String> firstErrors) throws IOException {
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
            System.err.println("java: warning: analysis stopped early: " + t);
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

}
