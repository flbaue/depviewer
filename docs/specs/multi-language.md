# Spec: multi-language architecture

Status: implemented
Tests: `scripts/check.sh` (all fixtures), regression comparison against the previous version

## Goal

Make it straightforward to add languages, with each language analysed by the **official
toolchain** of that language wherever possible, so dependencies are resolved exactly. The first
additions are TypeScript/JavaScript ([typescript-support.md](typescript-support.md)) and Dart
([dart-support.md](dart-support.md)). Java (javac) and Kotlin (built-in parser,
[kotlin-support.md](kotlin-support.md)) keep working unchanged.

## Constraints

- The core stays Java with the JDK only. A language whose toolchain is not installed is
  **skipped with a clear message**, never a crash; the remaining languages are still scanned.
- Toolchain helpers are written in the language of the toolchain (Node for TypeScript, Dart for
  Dart), live in `extractors/<language>/`, and pin their dependencies (lock files committed).
- No network access during a scan except a one-time dependency install of an extractor
  (`npm ci` / `dart pub get`), which is announced on stderr. It can be done ahead of time.
- On Java/Kotlin-only projects the output stays identical to the previous version.

## Code layout

```
depviewer                          wrapper: compiles src/ with javac on first run (or when
                                   sources changed) into build/depviewer-classes, then runs it
src/depviewer/DepScan.java         CLI, source discovery, orchestration, output
src/depviewer/Model.java           nodes, edges, packages, externals, JSON writer
src/depviewer/Json.java            minimal JSON reader (for extractor output)
src/depviewer/JavaScanner.java     javac-based Java scanner
src/depviewer/KotlinScanner.java   built-in Kotlin scanner
src/depviewer/Toolchain.java       runs an extractor and merges its fragment into the model
extractors/typescript/             extract.mjs + package.json/package-lock.json (typescript)
extractors/dart/                   bin/extract.dart + pubspec.yaml/pubspec.lock (analyzer)
viewer.html                        unchanged role
```

Running without the wrapper: `javac -d out src/depviewer/*.java && java -cp out depviewer.DepScan …`.

## Discovery

A scan input directory is searched (skipping `node_modules`, `.dart_tool`, build output and VCS
folders) for:

| Marker | Language | Module name |
|---|---|---|
| `src/{main,test}/{java,kotlin}` | Java / Kotlin | `artifactId` from `pom.xml`, else folder name |
| `package.json` | TypeScript / JavaScript | `name` from `package.json`, else folder name |
| `pubspec.yaml` | Dart | `name` from `pubspec.yaml`, else folder name |

A directory without any marker is still scanned for `.java`/`.kt` files as before.
Several markers in one tree (monorepos, a Flutter app next to a backend) give several modules.

## Fragment format (extractor → core)

An extractor is started as a separate process with the project directory and module name and
writes one JSON document to stdout (diagnostics to stderr):

```jsonc
{
  "language": "typescript",
  "errors": 0,                       // diagnostics count, reported like javac's
  "nodes": [
    { "id": "src/app/cart.ts", "kind": "file", "path": "src/app/cart.ts",
      "package": "src/app", "test": false, "loc": 42 },
    { "id": "src/app/cart.ts#Cart", "kind": "class", "name": "Cart",
      "parent": "src/app/cart.ts", "line": 3, "loc": 20, "vis": "+", "static": false,
      "q": "src/app/cart.ts#Cart" }
  ],
  "externals": [ { "id": "ext:react#useState", "package": "react", "name": "useState" } ],
  "edges": [ { "from": "src/app/cart.ts#Cart", "to": "ext:react#useState", "kind": "call", "line": 7 } ]
}
```

- `id`s are only meaningful inside one fragment; the core prefixes them with language and module.
- Parents come before children; every non-file node has a parent from the same fragment.
- `kind` uses the existing node kinds plus `namespace`, `mixin`, `extension` (see viewer below).
- `package` is a folder path with `/` separators (`""` for the project root). The core builds
  package nodes per folder segment and marks them so the viewer joins collapsed chains with `/`
  (`src/app`), not `.`.
- Edge kinds are the existing ones: `uses`, `extends`, `implements`, `call`, `new`, `access`.
- Unknown fields are ignored; a missing parent or edge endpoint drops that node/edge with a
  warning instead of failing the scan.

## Viewer

- Package rows built from folders show `/`-joined names when merged (`src/app/services`).
- New kinds: `namespace` (icon "N"), `mixin` ("m"), `extension` ("+"); file icons by extension:
  "J" Java, "K" Kotlin, "TS" TypeScript, "JS" JavaScript, "D" Dart.

## Out of scope (for now)

Python, Go and other languages; language servers; reading compiled output (`--classes`).
