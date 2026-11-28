// depviewer extractor for Dart (spec/wiki/functional-requirements/FR-006-analyse-dart-with-the-dart-analyzer.md).
//
// Usage: dart --packages=<extractor>/.dart_tool/package_config.json extract.dart <request.json>
//        dart --packages=... extract.dart --describe [<source>]
//   request: { "root": "/abs/package", "module": "name", "files": [{ "path": "lib/a.dart", "test": false }] }
// Prints a depviewer fragment (spec/wiki/crosscutting-concepts/CC-009-fragment-format.md) to stdout; diagnostics go to stderr.
//
// --describe prints the extractor's name, version and contract and whether the Dart SDK running it
// is supported (describe.dart, which the core runs on its own: it also works on SDKs too old for this file).
//
// Uses the Dart analyzer (resolved ASTs and the element model), so references are resolved exactly.

import 'dart:convert';
import 'dart:io';

import 'package:analyzer/dart/analysis/analysis_context_collection.dart';
import 'package:analyzer/dart/analysis/results.dart';
import 'package:analyzer/dart/ast/ast.dart';
import 'package:analyzer/dart/ast/visitor.dart';
import 'package:analyzer/dart/element/element.dart';
import 'package:analyzer/diagnostic/diagnostic.dart';
import 'package:analyzer/source/line_info.dart';
import 'package:path/path.dart' as p;

import 'describe.dart';

final out = <String, Object>{'contract': 1, 'language': 'dart', 'errors': 0, 'nodes': <Object>[], 'externals': <Object>[], 'edges': <Object>[]};
final nodes = out['nodes'] as List<Object>;
final edges = out['edges'] as List<Object>;
final externals = out['externals'] as List<Object>;

final declOfAst = <AstNode, String>{};    // declaration AST node -> fragment id
final declOfElement = <Element, String>{}; // declared element -> fragment id
final kindOf = <String, String>{};
final externalIds = <String>{};
late Set<String> ourFiles;                 // absolute paths

Future<void> main(List<String> args) async {
  if (args.isNotEmpty && args.first == '--describe') {
    stdout.writeln(jsonEncode(describe(args.length > 1 ? args[1] : 'path')));
    return;
  }
  final input = jsonDecode(File(args.first).readAsStringSync()) as Map<String, dynamic>;
  final defaultRoot = p.normalize(p.absolute(input['root'] as String));
  // One run may cover several packages (all Dart packages of a scan): each file names its module and root.
  final files = <String, ({bool test, String module, String root})>{};
  for (final f in (input['files'] as List).cast<Map<String, dynamic>>()) {
    final root = p.normalize(p.absolute((f['root'] ?? defaultRoot) as String));
    files[p.normalize(p.join(root, f['path'] as String))] =
        (test: f['test'] == true, module: (f['module'] ?? input['module']) as String, root: root);
  }
  ourFiles = files.keys.toSet();
  final roots = {for (final f in files.values) f.root: f.module};
  for (final e in roots.entries) {
    if (!hasPackageConfig(e.key)) {
      stderr.writeln('dart: ${e.value}: no .dart_tool/package_config.json here or in a parent folder - run `dart pub get` '
          '(or `flutter pub get`) so imports of other packages resolve');
    }
  }
  // Included paths must not overlap (a workspace member lies inside the workspace root).
  final included = roots.keys.where((r) => !roots.keys.any((o) => o != r && p.isWithin(o, r))).toList();
  final collection = AnalysisContextCollection(includedPaths: included);
  final root = included.length == 1 ? included.first : defaultRoot;
  final units = <String, ResolvedUnitResult>{};
  final excluded = <String, int>{}; // module -> files analysis_options.yaml excludes
  for (final path in files.keys) {
    try {
      final result = await collection.contextFor(path).currentSession.getResolvedUnit(path);
      if (result is ResolvedUnitResult) {
        units[path] = result;
        out['errors'] = (out['errors'] as int) + result.diagnostics.where((d) => d.severity == Severity.error).length;
      } else {
        stderr.writeln('dart: could not resolve ${p.relative(path, from: root)}, skipped');
      }
    } on StateError {
      // excluded by analysis_options.yaml: the analyzer has no context for it
      excluded.update(files[path]!.module, (n) => n + 1, ifAbsent: () => 1);
    } catch (e) {
      stderr.writeln('dart: ${p.relative(path, from: root)} failed to analyse ($e), skipped');
    }
  }
  for (final e in excluded.entries) {
    stderr.writeln('dart: ${e.key}: ${e.value} file(s) skipped - excluded from analysis by analysis_options.yaml');
  }
  // Declarations of all files first (references may point to any of them), then references.
  for (final e in units.entries) {
    final f = files[e.key]!;
    Declarations(f.module, p.relative(e.key, from: f.root).replaceAll(r'\', '/'), f.test, e.value).run();
  }
  for (final e in units.entries) {
    e.value.unit.accept(References(e.value.lineInfo, declOfAst[e.value.unit]!));
  }
  stdout.write(jsonEncode(out));
}

/// A package uses the package_config.json of its own folder or of a parent (pub workspaces).
bool hasPackageConfig(String dir) {
  for (var d = dir;; d = p.dirname(d)) {
    if (File(p.join(d, '.dart_tool', 'package_config.json')).existsSync()) return true;
    if (p.dirname(d) == d) return false;
  }
}

// ---------------------------------------------------------------- declarations

class Declarations {
  final String module, path, rel; // rel = id base "module|path", unique across the packages of one run
  final bool test;
  final ResolvedUnitResult result;
  Declarations(this.module, this.path, this.test, this.result) : rel = '$module|$path';

  LineInfo get lines => result.lineInfo;
  int lineOf(int offset) => lines.getLocation(offset).lineNumber;

  String add(AstNode decl, Element? element, String id, String name, String kind, String parent, {String? vis, bool isStatic = false}) {
    final start = lineOf(decl.offset);
    nodes.add({
      'id': id, 'name': name, 'kind': kind, 'parent': parent, 'line': start,
      'loc': lineOf(decl.end) - start + 1, 'q': id.substring(module.length + 1), 'vis': vis ?? (name.startsWith('_') ? '-' : '+'), 'static': isStatic,
    });
    kindOf[id] = kind;
    declOfAst[decl] = id;
    if (element != null) declOfElement[element] = id;
    return id;
  }

  void run() {
    final dir = path.contains('/') ? path.substring(0, path.lastIndexOf('/')) : '';
    nodes.add({'id': rel, 'kind': 'file', 'path': path, 'package': dir, 'module': module, 'test': test, 'loc': lines.lineCount});
    kindOf[rel] = 'file';
    declOfAst[result.unit] = rel;
    final prefix = '$rel#';
    for (final d in result.unit.declarations) {
      if (d is ClassDeclaration) {
        final name = d.namePart.typeName.lexeme;
        final kind = d.mixinKeyword != null ? 'mixin' : d.interfaceKeyword != null ? 'interface' : 'class';
        final id = add(d, d.declaredFragment?.element, '$prefix$name', name, kind, rel);
        members(d.body.members, id, '$prefix$name.', name);
      } else if (d is MixinDeclaration) {
        final id = add(d, d.declaredFragment?.element, '$prefix${d.name.lexeme}', d.name.lexeme, 'mixin', rel);
        members(d.body.members, id, '$prefix${d.name.lexeme}.', d.name.lexeme);
      } else if (d is EnumDeclaration) {
        final name = d.namePart.typeName.lexeme;
        final id = add(d, d.declaredFragment?.element, '$prefix$name', name, 'enum', rel);
        for (final c in d.body.constants) {
          add(c, c.declaredFragment?.element, '$prefix$name.${c.name.lexeme}', c.name.lexeme, 'enumconst', id, isStatic: true);
        }
        members(d.body.members, id, '$prefix$name.', name);
      } else if (d is ExtensionDeclaration) {
        final name = d.name?.lexeme ?? 'extension on ${d.onClause?.extendedType.toSource() ?? '?'}';
        final id = add(d, d.declaredFragment?.element, '$prefix$name', name, 'extension', rel);
        members(d.body.members, id, '$prefix$name.', name);
      } else if (d is ExtensionTypeDeclaration) {
        final name = d.namePart.typeName.lexeme;
        final id = add(d, d.declaredFragment?.element, '$prefix$name', name, 'class', rel);
        members(d.body.members, id, '$prefix$name.', name);
      } else if (d is TypeAlias) {
        add(d, d.declaredFragment?.element, '$prefix${d.name.lexeme}', d.name.lexeme, 'typealias', rel);
      } else if (d is FunctionDeclaration) {
        final name = d.name.lexeme;
        if (d.isGetter || d.isSetter) {
          property(d, d.declaredFragment?.element, '$prefix$name', name, rel, true);
        } else {
          final sig = '$name(${signature(d.functionExpression.parameters, const {})})';
          add(d, d.declaredFragment?.element, '$prefix$sig', sig, 'method', rel, isStatic: true);
        }
      } else if (d is TopLevelVariableDeclaration) {
        variables(d, d.variables, rel, prefix, true);
      }
    }
  }

  /// get/set pairs and fields with explicit accessors become one field node.
  void property(AstNode decl, Element? element, String id, String name, String parent, bool isStatic) {
    if (kindOf.containsKey(id)) {
      declOfAst[decl] = id;
      if (element != null) declOfElement[element] = id;
    } else {
      add(decl, element, id, name, 'field', parent, isStatic: isStatic);
    }
  }

  void variables(AstNode decl, VariableDeclarationList list, String parent, String prefix, bool isStatic) {
    String? first;
    for (final v in list.variables) {
      final id = add(v, v.declaredFragment?.element, '$prefix${v.name.lexeme}', v.name.lexeme, 'field', parent, isStatic: isStatic);
      first ??= id;
    }
    if (first != null) declOfAst[decl] = first; // the declared type counts for the (first) variable
  }

  void members(List<ClassMember> members, String parent, String mp, String typeName) {
    // Field types first: constructor parameters `this.x` take the field's declared type.
    final fieldTypes = <String, String>{};
    for (final m in members) {
      if (m is FieldDeclaration) {
        variables(m, m.fields, parent, mp, m.isStatic);
        final t = m.fields.type?.toSource();
        if (t != null) for (final v in m.fields.variables) fieldTypes[v.name.lexeme] = t;
      }
    }
    for (final m in members) {
      if (m is ConstructorDeclaration) {
        final name = m.name == null ? typeName : '$typeName.${m.name!.lexeme}';
        final sig = '$name(${signature(m.parameters, fieldTypes)})';
        add(m, m.declaredFragment?.element, '$mp$sig', sig, 'ctor', parent);
      } else if (m is MethodDeclaration) {
        final name = m.name.lexeme;
        if (m.isGetter || m.isSetter) {
          property(m, m.declaredFragment?.element, '$mp$name', name, parent, m.isStatic);
        } else {
          final label = m.isOperator ? 'operator $name' : name;
          final sig = '$label(${signature(m.parameters, fieldTypes)})';
          add(m, m.declaredFragment?.element, '$mp$sig', sig, 'method', parent, isStatic: m.isStatic);
        }
      }
    }
  }

  String signature(FormalParameterList? params, Map<String, String> fieldTypes) {
    if (params == null) return '';
    return params.parameters.map((p) {
      final t = p.type?.toSource();
      if (t != null) return t;
      if (p is FieldFormalParameter) return fieldTypes[p.name.lexeme] ?? p.declaredFragment?.element.type.getDisplayString() ?? 'dynamic';
      if (p.functionTypedSuffix != null) return 'Function';
      return p.declaredFragment?.element.type.getDisplayString() ?? 'dynamic';
    }).join(', ');
  }
}

// ---------------------------------------------------------------- references

String? sourcePath(Element e) => e.firstFragment.libraryFragment?.source.fullName;

/// The fragment node an element refers to: a declared node, the declared node enclosing it
/// (locals, parameters, implicit constructors -> their class), or an external top-level element.
String? targetOf(Element element) {
  var e = element.baseElement;
  if (e is PropertyAccessorElement && !declOfElement.containsKey(e)) e = e.variable.baseElement;
  if (e is FieldFormalParameterElement && e.field != null) e = e.field!;
  final path = sourcePath(e);
  if (path != null && ourFiles.contains(p.normalize(path))) {
    for (Element? x = e; x != null; x = x.enclosingElement) {
      final id = declOfElement[x];
      if (id != null) return id;
      if (x is LibraryElement) break;
    }
    return null;
  }
  return external(e);
}

String? external(Element e) {
  var top = e;
  while (top.enclosingElement != null && top.enclosingElement is! LibraryElement) {
    top = top.enclosingElement!;
  }
  if (top is PropertyAccessorElement) top = top.variable;
  final name = top.name;
  final uri = top.library?.uri;
  if (name == null || name.isEmpty || uri == null) return null;
  final pkg = uri.scheme == 'dart'
      ? 'dart:${uri.pathSegments.first}'
      : uri.scheme == 'package' ? uri.pathSegments.first : '(other)';
  final id = 'ext:$pkg#$name';
  if (externalIds.add(id)) externals.add({'id': id, 'package': pkg, 'name': name});
  return id;
}

class References extends RecursiveAstVisitor<void> {
  final LineInfo lines;
  String owner;
  References(this.lines, this.owner);

  void edge(Element? element, String kind, AstNode at) {
    if (element == null || element is PrefixElement || element is LibraryElement) return;
    final to = targetOf(element);
    if (to == null || to == owner) return;
    edges.add({'from': owner, 'to': to, 'kind': kind, 'line': lines.getLocation(at.offset).lineNumber});
  }

  void within(AstNode node, void Function() visit) {
    final id = declOfAst[node];
    if (id == null) return visit();
    final saved = owner;
    owner = id;
    visit();
    owner = saved;
  }

  @override
  void visitImportDirective(ImportDirective node) {}
  @override
  void visitExportDirective(ExportDirective node) {}
  @override
  void visitPartDirective(PartDirective node) {}
  @override
  void visitPartOfDirective(PartOfDirective node) {}

  @override
  void visitClassDeclaration(ClassDeclaration node) => within(node, () => super.visitClassDeclaration(node));
  @override
  void visitMixinDeclaration(MixinDeclaration node) => within(node, () => super.visitMixinDeclaration(node));
  @override
  void visitEnumDeclaration(EnumDeclaration node) => within(node, () => super.visitEnumDeclaration(node));
  @override
  void visitEnumConstantDeclaration(EnumConstantDeclaration node) => within(node, () => super.visitEnumConstantDeclaration(node));
  @override
  void visitExtensionDeclaration(ExtensionDeclaration node) => within(node, () => super.visitExtensionDeclaration(node));
  @override
  void visitExtensionTypeDeclaration(ExtensionTypeDeclaration node) => within(node, () => super.visitExtensionTypeDeclaration(node));
  @override
  void visitGenericTypeAlias(GenericTypeAlias node) => within(node, () => super.visitGenericTypeAlias(node));
  @override
  void visitFunctionTypeAlias(FunctionTypeAlias node) => within(node, () => super.visitFunctionTypeAlias(node));
  @override
  void visitClassTypeAlias(ClassTypeAlias node) => within(node, () => super.visitClassTypeAlias(node));
  @override
  void visitFunctionDeclaration(FunctionDeclaration node) => within(node, () => super.visitFunctionDeclaration(node));
  @override
  void visitTopLevelVariableDeclaration(TopLevelVariableDeclaration node) =>
      within(node, () => super.visitTopLevelVariableDeclaration(node));
  @override
  void visitFieldDeclaration(FieldDeclaration node) => within(node, () => super.visitFieldDeclaration(node));
  @override
  void visitVariableDeclaration(VariableDeclaration node) => within(node, () => super.visitVariableDeclaration(node));
  @override
  void visitConstructorDeclaration(ConstructorDeclaration node) => within(node, () => super.visitConstructorDeclaration(node));
  @override
  void visitMethodDeclaration(MethodDeclaration node) => within(node, () => super.visitMethodDeclaration(node));

  @override
  void visitNamedType(NamedType node) {
    final parent = node.parent;
    if (parent is! ConstructorName) {
      final kind = parent is ExtendsClause
          ? 'extends'
          : parent is ImplementsClause || parent is WithClause || parent is MixinOnClause ? 'implements' : 'uses';
      edge(node.element, kind, node);
    }
    super.visitNamedType(node);
  }

  @override
  void visitInstanceCreationExpression(InstanceCreationExpression node) {
    edge(node.constructorName.element, 'new', node.constructorName);
    node.constructorName.type.typeArguments?.accept(this);
    node.argumentList.accept(this);
  }

  @override
  void visitRedirectingConstructorInvocation(RedirectingConstructorInvocation node) {
    edge(node.element, 'new', node);
    super.visitRedirectingConstructorInvocation(node);
  }

  @override
  void visitSuperConstructorInvocation(SuperConstructorInvocation node) {
    edge(node.element, 'new', node);
    super.visitSuperConstructorInvocation(node);
  }

  @override
  void visitSimpleIdentifier(SimpleIdentifier node) {
    final parent = node.parent;
    if (parent is ConstructorName || parent is Annotation && parent.constructorName == node) return;
    if (parent is ConstructorDeclaration && parent.typeName == node) return; // `Product(...)` declares a constructor
    var element = node.element;
    if (element == null && parent is CompoundAssignmentExpression) {
      element = parent.readElement ?? parent.writeElement;
    }
    if (element == null) return;
    final String kind;
    if (parent is MethodInvocation && parent.methodName == node) {
      kind = 'call';
    } else if (element is InterfaceElement || element is TypeAliasElement || element is ExtensionElement || parent is Annotation) {
      kind = 'uses';
    } else {
      kind = 'access';
    }
    edge(element, kind, node);
  }
}
