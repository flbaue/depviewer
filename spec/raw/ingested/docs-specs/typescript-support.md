# Spec: TypeScript and JavaScript support

Status: implemented
Tests: `test/checks/typescript.py` on `test/fixture-ts/`, smoke test in `scripts/check.sh`
Depends on: [multi-language.md](multi-language.md)

## Goal

Scan TypeScript and JavaScript projects with the TypeScript compiler API (type checker), so
references are resolved exactly like javac does for Java.

## Toolchain

- Requires Node.js 18+ on `PATH` (or `DEPVIEWER_NODE` pointing to the `node` binary).
- `extractors/typescript/extract.mjs` uses the `typescript` package: the scanned project's own
  `node_modules/typescript` if present (matches the project's compiler version), otherwise the
  extractor's pinned copy, installed with `npm ci` on first use.
- If Node is missing, TypeScript/JavaScript modules are skipped with a message.

## Sources

- Module root: a folder with `package.json`. Files: `.ts .tsx .mts .cts .js .jsx .mjs .cjs` below it,
  excluding `node_modules`, `dist`, `build`, `out`, `coverage`, `.next`, folders of nested
  `package.json` modules (they are their own modules) and declaration files (`.d.ts`).
- Compiler options come from the module's `tsconfig.json` / `jsconfig.json` if present (so path
  aliases resolve), with `allowJs` and `checkJs: false` forced on; otherwise sensible defaults.
- Test files are flagged: `*.test.*`, `*.spec.*`, anything below `__tests__/`, `test/` or `tests/`.

## Model

| TypeScript / JavaScript | Node kind |
|---|---|
| file | `file`, in the package of its folder (`src/app`) |
| `class`, `abstract class` | `class` |
| `interface` | `interface` |
| `enum`, `const enum` | `enum`; members `enumconst` |
| `type X = …` | `typealias` |
| `namespace` / `module` blocks | `namespace` |
| constructor | `ctor`, named like the class with parameter types |
| method, function, arrow function or function expression assigned to a top-level `const`/`let` or class property | `method` |
| property, accessor pair (`get`/`set`), top-level `const`/`let`/`var` | `field` |

- Top-level functions and variables are direct children of their file. All top-level
  declarations are listed, exported or not (as in Kotlin); local declarations inside functions
  are not.
- Constructor parameter properties (`constructor(private readonly id: string)`) are also fields.
- Member names carry parameter types like Java/Kotlin: `add(Product)`; untyped parameters (plain
  JavaScript) show as `any`.
- Visibility: `private`/`#private` → private, `protected` → protected, everything else public;
  `static` members are static.
- Qualified names: `<path>#Name` and `<path>#Class.member`; externals `<package>.<Name>`
  (`@acme/events.Emitter`, `(builtin).Map`).
- Files directly in the module folder belong to the module itself (no package row).

## Dependencies

Every identifier inside a declaration (signatures, initializers, bodies, decorators) that the
type checker resolves to a declaration produces an edge from the innermost declaring node
(top-level statements that are not declarations, e.g. in scripts and tests, count for the file):

- to a project declaration → that node (member if it is a member, else type/function/variable);
  import aliases and re-exports are followed to the original declaration;
- to a declaration in `node_modules` → external `<package name>` (scoped names kept, `@types/x`
  mapped to `x`), symbol = top-level exported name;
- to the TypeScript standard library (`lib.*.d.ts`) → external package `(builtin)`, e.g.
  `Array`, `Promise`, `Map`.

Edge kinds: heritage `extends` → `extends`; `implements` → `implements`; callee of a call →
`call`; `new X()` → `new` (to the constructor if declared, else the class); property access or
variable read/write → `access`; type references and everything else → `uses`.
Imports themselves are not dependencies; unused imports produce nothing. Unresolvable names are
counted in `errors` and otherwise ignored.

## Fixture expectations (test/fixture-ts)

A small shop front end (`package.json` name `shop-web`), containing: an interface and a class
implementing it, a class with constructor/fields/methods/static and private members, an enum, a
type alias, top-level functions and consts, an arrow function, a path-free relative import, a
re-export via `index.ts`, a default export, a namespace, a `.js` file used from TypeScript, a
deliberate folder cycle, a test file, and a use of a built-in (`Map`, `Promise`). The concrete
assertions live in `test/checks/typescript.py`.

## Out of scope

Vue/Svelte single-file components, Deno-specific module resolution, project references
(`tsconfig` `references`) beyond treating each `package.json` as its own module.
