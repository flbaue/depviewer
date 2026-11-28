// depviewer extractor for TypeScript and JavaScript (spec/wiki/functional-requirements/FR-005-analyse-typescript-and-javascript-with-the-typescript-compiler.md).
//
// Usage: node extract.mjs <request.json>
//        node extract.mjs --describe [<typescript package folder> <source>]
//   request: { "root": "/abs/project", "module": "name", "typescript": "/abs/node_modules/typescript",
//              "files": [{ "path": "src/a.ts", "test": false, "module": "name", "root": "/abs/project" }] }
//   One run covers several modules: each file names its module and that module's folder.
// Prints a depviewer fragment (spec/wiki/crosscutting-concepts/CC-009-fragment-format.md) to stdout; diagnostics go to stderr.
// --describe prints the extractor's name, version and contract and whether the given TypeScript is
// supported (spec/wiki/functional-requirements/FR-010-choose-the-toolchain-in-a-fixed-order.md).
//
// Uses the TypeScript compiler API and its type checker, so references are resolved exactly:
// imports, re-exports and default exports are followed to the original declaration.

import fs from "node:fs";
import path from "node:path";
import { createRequire } from "node:module";
import { fileURLToPath } from "node:url";

const here = path.dirname(fileURLToPath(import.meta.url));
const manifest = JSON.parse(fs.readFileSync(path.join(here, "package.json"), "utf8"));
/** TypeScript versions whose compiler API this extractor uses: 5.x and 6.x (7 has no JavaScript API). */
const RANGE = { min: [5, 0, 0], below: [7, 0, 0], text: ">=5.0.0 <7.0.0" };

const parseVersion = (v) => (/^(\d+)\.(\d+)\.(\d+)/.exec(v || "") || []).slice(1).map(Number);
const compare = (a, b) => a[0] - b[0] || a[1] - b[1] || a[2] - b[2];
const inRange = (v) => v.length === 3 && compare(v, RANGE.min) >= 0 && compare(v, RANGE.below) < 0;

/** The TypeScript package in `dir` (its folder), or the extractor's pinned copy. */
function loadTypeScript(dir) {
  return createRequire(path.join(here, "extract.mjs"))(dir || "typescript");
}

if (process.argv[2] === "--describe") {
  const dir = process.argv[3] ? path.resolve(process.argv[3]) : path.join(here, "node_modules", "typescript");
  const toolchain = { tool: "typescript", version: null, source: process.argv[4] || "pinned", path: dir, supported: false, range: RANGE.text };
  try {
    toolchain.version = JSON.parse(fs.readFileSync(path.join(dir, "package.json"), "utf8")).version || null;
  } catch {
    toolchain.problem = "no TypeScript package";
  }
  if (toolchain.version && !inRange(parseVersion(toolchain.version))) toolchain.problem = "version not supported";
  else if (toolchain.version) {
    try {
      const ts = loadTypeScript(dir);
      if (typeof ts.createProgram !== "function") toolchain.problem = "no compiler API";
      else toolchain.supported = true;
    } catch (e) {
      toolchain.problem = "could not be loaded";
    }
  }
  const describe = { name: manifest.name, version: manifest.version, contract: 1, languages: ["typescript", "javascript"], exact: true, toolchain };
  process.stdout.write(JSON.stringify(describe) + "\n");
  process.exit(0);
}

const input = JSON.parse(fs.readFileSync(process.argv[2], "utf8"));
const ts = loadTypeScript(input.typescript);

// ------------------------------------------------------------------ programs

// One run covers all modules (package.json folders) of a scan, so an import of a sibling
// workspace package links to its declarations (spec/wiki/functional-requirements/FR-012-link-the-modules-of-a-monorepo.md).
// Each module gets its own program with its own tsconfig.json; declarations are matched across
// programs by file and position, as each program has its own syntax trees.

const realPaths = new Map();
/** The real path of a file: workspace packages are symlinked into node_modules, and TypeScript follows the links. */
function real(file) {
  let r = realPaths.get(file);
  if (r === undefined) {
    try { r = fs.realpathSync.native(file); } catch { r = path.resolve(file); }
    realPaths.set(file, r);
  }
  return r;
}

function compilerOptions(root) {
  for (const name of ["tsconfig.json", "jsconfig.json"]) {
    const file = path.join(root, name);
    if (!fs.existsSync(file)) continue;
    const cfg = ts.readConfigFile(file, ts.sys.readFile);
    if (cfg.error) continue;
    return ts.parseJsonConfigFileContent(cfg.config, ts.sys, root).options;
  }
  return { target: ts.ScriptTarget.ES2022, module: ts.ModuleKind.ESNext, moduleResolution: ts.ModuleResolutionKind.Bundler, jsx: ts.JsxEmit.Preserve };
}

const modules = new Map(); // module root -> { name, root, prefix, files: [absolute path] }
const files = new Map(); // real path -> { path (relative to its module), test, module }
for (const f of input.files) {
  const root = path.resolve(f.root ?? input.root);
  if (!modules.has(root)) modules.set(root, { name: f.module ?? input.module, root, prefix: `${modules.size}|`, files: [] });
  const m = modules.get(root);
  const abs = path.join(root, f.path);
  m.files.push(abs);
  files.set(real(abs), { path: f.path, test: !!f.test, module: m });
}
let program, checker; // of the module being read

// ------------------------------------------------------------------ output

const out = { language: "typescript", errors: 0, nodes: [], externals: [], edges: [] };
const declIds = new Map(); // "real path:pos:end:kind" of a declaration -> fragment node id
const declKey = (node) => `${real(node.getSourceFile().fileName)}:${node.pos}:${node.end}:${node.kind}`;
const declId = { set: (node, id) => declIds.set(declKey(node), id), get: (node) => declIds.get(declKey(node)) };
const nodeKind = new Map(); // fragment id -> kind
const externals = new Map(); // external id -> true

/** Fragment ids start with the module's prefix, so equal paths in two modules stay apart; q leaves it out. */
const qOf = (id) => id.slice(id.indexOf("|") + 1);
const lineOf = (sf, pos) => sf.getLineAndCharacterOfPosition(pos).line + 1;

function addNode(sf, decl, id, name, kind, parent, extra = {}) {
  const start = lineOf(sf, decl.getStart(sf));
  out.nodes.push({ id, name, kind, parent, line: start, loc: lineOf(sf, decl.getEnd()) - start + 1, q: qOf(id), ...extra });
  nodeKind.set(id, kind);
  declId.set(decl, id);
  return id;
}

function modifiersOf(decl) {
  const flags = ts.getCombinedModifierFlags(decl);
  const isPrivate = (flags & ts.ModifierFlags.Private) !== 0 || (decl.name && ts.isPrivateIdentifier(decl.name));
  return {
    vis: isPrivate ? "-" : (flags & ts.ModifierFlags.Protected) !== 0 ? "#" : "+",
    static: (flags & ts.ModifierFlags.Static) !== 0,
  };
}

function nameText(sf, name) {
  if (!name) return "default";
  if (ts.isIdentifier(name) || ts.isPrivateIdentifier(name) || ts.isStringLiteral(name) || ts.isNumericLiteral(name)) return name.text;
  return name.getText(sf);
}

const typeText = (sf, p) => (p.type ? p.type.getText(sf).replace(/\s+/g, " ") : "any");
const signature = (sf, params) => params.map((p) => typeText(sf, p)).join(", ");
const isFunctionInit = (init) => init && (ts.isArrowFunction(init) || ts.isFunctionExpression(init));

// ------------------------------------------------------------------ declarations

function declareFile(sf) {
  const info = files.get(real(sf.fileName));
  const id = info.module.prefix + info.path;
  const dir = info.path.includes("/") ? info.path.slice(0, info.path.lastIndexOf("/")) : "";
  out.nodes.push({ id, kind: "file", path: info.path, module: info.module.name, package: dir, test: info.test,
    loc: sf.getLineAndCharacterOfPosition(sf.getEnd()).line + 1 });
  nodeKind.set(id, "file");
  declId.set(sf, id);
  declareStatements(sf, sf.statements, id, id + "#");
}

function declareStatements(sf, statements, parent, prefix) {
  for (const st of statements) {
    if (ts.isClassDeclaration(st)) declareClass(sf, st, parent, prefix);
    else if (ts.isInterfaceDeclaration(st)) {
      const id = addNode(sf, st, prefix + st.name.text, st.name.text, "interface", parent);
      for (const m of st.members) declareMember(sf, m, id, prefix + st.name.text + ".");
    } else if (ts.isEnumDeclaration(st)) {
      const id = addNode(sf, st, prefix + st.name.text, st.name.text, "enum", parent);
      for (const m of st.members) {
        const n = nameText(sf, m.name);
        addNode(sf, m, prefix + st.name.text + "." + n, n, "enumconst", id, { static: true });
      }
    } else if (ts.isTypeAliasDeclaration(st)) {
      addNode(sf, st, prefix + st.name.text, st.name.text, "typealias", parent);
    } else if (ts.isModuleDeclaration(st) && st.name && ts.isIdentifier(st.name)) {
      declareNamespace(sf, st, parent, prefix, st.name.text);
    } else if (ts.isFunctionDeclaration(st)) {
      const n = nameText(sf, st.name);
      addNode(sf, st, `${prefix}${n}(${signature(sf, st.parameters)})`, `${n}(${signature(sf, st.parameters)})`, "method", parent, { static: true });
    } else if (ts.isVariableStatement(st)) {
      for (const d of st.declarationList.declarations) {
        if (!ts.isIdentifier(d.name)) continue; // destructuring: references count for the parent
        const n = d.name.text;
        if (isFunctionInit(d.initializer)) {
          const sig = signature(sf, d.initializer.parameters);
          addNode(sf, d, `${prefix}${n}(${sig})`, `${n}(${sig})`, "method", parent, { static: true });
        } else addNode(sf, d, prefix + n, n, "field", parent, { static: true });
      }
    }
  }
}

function declareNamespace(sf, decl, parent, prefix, name) {
  const id = addNode(sf, decl, prefix + name, name, "namespace", parent);
  let body = decl.body;
  while (body && ts.isModuleDeclaration(body)) body = body.body; // namespace A.B.C
  if (body && ts.isModuleBlock(body)) declareStatements(sf, body.statements, id, prefix + name + ".");
}

function declareClass(sf, decl, parent, prefix) {
  const cname = nameText(sf, decl.name);
  const id = addNode(sf, decl, prefix + cname, cname, "class", parent, modifiersOf(decl));
  const mp = prefix + cname + ".";
  for (const m of decl.members) {
    if (ts.isConstructorDeclaration(m)) {
      const sig = signature(sf, m.parameters);
      const ctor = addNode(sf, m, `${mp}${cname}(${sig})`, `${cname}(${sig})`, "ctor", id, modifiersOf(m));
      for (const p of m.parameters) {
        if (ts.isParameterPropertyDeclaration(p, m) && ts.isIdentifier(p.name)) {
          addNode(sf, p, mp + p.name.text, p.name.text, "field", id, modifiersOf(p));
        }
      }
      void ctor;
    } else declareMember(sf, m, id, mp);
  }
}

function declareMember(sf, m, parent, mp) {
  if (!m.name) return; // index signatures, static blocks: references count for the class
  const n = nameText(sf, m.name);
  const mods = modifiersOf(m);
  if (ts.isMethodDeclaration(m) || ts.isMethodSignature(m)) {
    addNode(sf, m, `${mp}${n}(${signature(sf, m.parameters)})`, `${n}(${signature(sf, m.parameters)})`, "method", parent, mods);
  } else if (ts.isPropertyDeclaration(m) && isFunctionInit(m.initializer)) {
    const sig = signature(sf, m.initializer.parameters);
    addNode(sf, m, `${mp}${n}(${sig})`, `${n}(${sig})`, "method", parent, mods);
  } else if (ts.isGetAccessorDeclaration(m) || ts.isSetAccessorDeclaration(m)) {
    const id = mp + n;
    if (nodeKind.has(id)) declId.set(m, id); // get/set pair: one field
    else addNode(sf, m, id, n, "field", parent, mods);
  } else if (ts.isPropertyDeclaration(m) || ts.isPropertySignature(m)) {
    addNode(sf, m, mp + n, n, "field", parent, mods);
  }
}

// ------------------------------------------------------------------ references

/** The fragment node a declaration belongs to: itself if declared, else the nearest declared ancestor. */
function ownerOf(decl) {
  for (let d = decl; d; d = d.parent) {
    const id = declId.get(d);
    if (id) return id;
  }
  return null;
}

function packageOf(fileName) {
  const norm = fileName.split(path.sep).join("/");
  if (program.isSourceFileDefaultLibrary(program.getSourceFile(fileName)) || /\/typescript\/lib\/lib\.[^/]*\.d\.ts$/.test(norm)) return "(builtin)";
  const i = norm.lastIndexOf("/node_modules/");
  if (i < 0) return "(other)";
  const parts = norm.slice(i + "/node_modules/".length).split("/");
  let pkg = parts[0].startsWith("@") ? parts[0] + "/" + parts[1] : parts[0];
  if (pkg.startsWith("@types/")) {
    const t = pkg.slice("@types/".length);
    pkg = t.includes("__") ? "@" + t.replace("__", "/") : t;
  }
  return pkg;
}

/** Top-level name of an external declaration (class for a member, the variable for `console`, ...). */
function externalTarget(decl) {
  let top = decl;
  while (top.parent && !ts.isSourceFile(top.parent) && !(ts.isModuleBlock(top.parent) && ts.isModuleDeclaration(top.parent.parent) && ts.isStringLiteral(top.parent.parent.name))) {
    top = top.parent;
  }
  let name = top.name ? nameText(top.getSourceFile(), top.name) : undefined;
  if (!name && ts.isVariableStatement(top)) name = nameText(top.getSourceFile(), top.declarationList.declarations[0].name);
  if (!name) return null;
  let pkg = packageOf(decl.getSourceFile().fileName);
  const ambient = top.parent && ts.isModuleBlock(top.parent) ? top.parent.parent : null;
  if (ambient && ts.isStringLiteral(ambient.name)) pkg = ambient.name.text;
  const id = `ext:${pkg}#${name}`;
  if (!externals.has(id)) {
    externals.set(id, true);
    out.externals.push({ id, package: pkg, name });
  }
  return id;
}

function heritageKind(node) {
  for (let n = node; n; n = n.parent) {
    if (ts.isHeritageClause(n)) return n.token === ts.SyntaxKind.ImplementsKeyword ? "implements" : "extends";
    if (ts.isClassLike(n) || ts.isInterfaceDeclaration(n) || ts.isSourceFile(n)) return null;
  }
  return null;
}

/** The expression a name is the "head" of: a.b.c -> for c, the property access a.b.c. */
const accessOf = (id) => (id.parent && ts.isPropertyAccessExpression(id.parent) && id.parent.name === id ? id.parent : id);

function edgeKind(id, symbol, target) {
  const h = heritageKind(id);
  if (h) return h;
  const expr = accessOf(id);
  const p = expr.parent;
  if (p && ts.isNewExpression(p) && p.expression === expr) return "new";
  if (p && (ts.isCallExpression(p) || ts.isTaggedTemplateExpression(p) || ts.isDecorator(p)) && (p.expression ?? p.tag) === expr) return "call";
  if (symbol.flags & (ts.SymbolFlags.Class | ts.SymbolFlags.Interface | ts.SymbolFlags.TypeAlias | ts.SymbolFlags.Enum | ts.SymbolFlags.Module | ts.SymbolFlags.TypeParameter)) {
    return nodeKind.get(target) === "enumconst" ? "access" : "uses";
  }
  return "access";
}

function isDeclarationName(id) {
  const p = id.parent;
  if (!p) return false;
  if (ts.isShorthandPropertyAssignment(p)) return false;
  if (ts.isPropertyAssignment(p) && p.name === id) return true;
  if (ts.isBindingElement(p) && p.propertyName === id) return true;
  return ts.isDeclaration(p) && p.name === id;
}

// One edge per reference (the core aggregates and counts them, like for Java).
function addEdge(from, to, kind, line) {
  if (!from || !to || from === to) return;
  out.edges.push({ from, to, kind, line });
}

function visitRefs(sf, node, owner) {
  if (ts.isImportDeclaration(node) || ts.isImportEqualsDeclaration(node) || (ts.isExportDeclaration(node) && node.moduleSpecifier)) return;
  const own = declId.get(node);
  if (own) owner = own;
  if ((ts.isIdentifier(node) || ts.isPrivateIdentifier(node)) && !isDeclarationName(node)) reference(sf, node, owner);
  ts.forEachChild(node, (c) => visitRefs(sf, c, owner));
}

function reference(sf, id, owner) {
  let symbol = ts.isShorthandPropertyAssignment(id.parent) && id.parent.name === id
    ? checker.getShorthandAssignmentValueSymbol(id.parent)
    : checker.getSymbolAtLocation(id);
  if (!symbol) return;
  if (symbol.flags & ts.SymbolFlags.Alias) {
    try { symbol = checker.getAliasedSymbol(symbol); } catch { return; }
  }
  const decl = symbol.valueDeclaration ?? symbol.declarations?.[0];
  if (!decl) return;
  const dsf = decl.getSourceFile();
  let target;
  if (files.has(real(dsf.fileName))) {
    target = ownerOf(decl);
    // `new X()`: the constructor when X declares one
    if (target && nodeKind.get(target) === "class") {
      const expr = accessOf(id);
      if (expr.parent && ts.isNewExpression(expr.parent) && expr.parent.expression === expr) {
        const ctor = ts.isClassLike(decl) ? decl.members.find(ts.isConstructorDeclaration) : undefined;
        if (ctor && declId.get(ctor)) target = declId.get(ctor);
      }
    }
  } else {
    target = externalTarget(decl);
  }
  if (!target) return;
  addEdge(owner, target, edgeKind(id, symbol, target), lineOf(sf, id.getStart(sf)));
}

// ------------------------------------------------------------------ run

// Each module's own files, read with its program: first all declarations, then the references,
// which may point into any module. Only one program is alive at a time, so a monorepo with many
// packages needs no more memory than its largest package; each program is built twice for that.
let kept = null; // a single module keeps its program for both passes
function load(m) {
  if (kept?.m === m) return kept.sources;
  program = ts.createProgram(m.files, { ...compilerOptions(m.root), allowJs: true, checkJs: false, noEmit: true, skipLibCheck: true });
  checker = program.getTypeChecker();
  const sources = program.getSourceFiles().filter((sf) => files.get(real(sf.fileName))?.module === m);
  kept = modules.size === 1 ? { m, sources } : null;
  return sources;
}
for (const m of modules.values()) for (const sf of load(m)) declareFile(sf);
let read = 0;
for (const m of modules.values()) {
  const sources = load(m);
  for (const sf of sources) {
    visitRefs(sf, sf, declId.get(sf));
    out.errors += program.getSyntacticDiagnostics(sf).length + program.getSemanticDiagnostics(sf).length;
  }
  read += sources.length;
}
program = checker = null;
const missing = input.files.length - read;
if (missing > 0) console.error(`typescript: ${missing} file(s) could not be loaded`);
process.stdout.write(JSON.stringify(out));
