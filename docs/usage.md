# Usage

New to depviewer? Start with the [quick start](quick-start.md). This page is the reference for the
command line and the files it writes.

## Command line

```
./depviewer [options] <project-or-source-dir>...
```

The `depviewer` script compiles the scanner (`src/depviewer/`) on first use and whenever its
sources change. It needs a JDK 17+; TypeScript and Dart additionally need their toolchain (see
[languages](languages.md)).

| Option | Meaning |
|---|---|
| `-o`, `--out <file>` | Output file. `.html` (default `deps.html`): the viewer with the data embedded. `.json`: the data only. |
| `--no-tests` | Leave out test sources. |
| `--title <text>` | Title shown in the viewer. Saved views are stored per title in the browser. |
| `--views <file>` | Embed group views exported from the viewer (*⋯ › Export views*) or an architecture file. The scanner resolves its patterns and rules, so the viewer shows the same groups and violations as `check`. |
| `--cp <classpath>` | Extra classpath for resolving Java/Kotlin library types. |
| `--template <file>` | Viewer template (default: `viewer.html` next to the script). |
| `-h`, `--help` | Show the options. |

Without the wrapper: `javac -d out src/depviewer/*.java && java -cp out depviewer.DepScan …`.

## Checking the architecture

```
./depviewer check [--rules <file>] [--view <id>] (<project-dir>... | --model deps.json)
                  [--format text|json] [--out <file>] [--baseline <file>] [--update-baseline]
                  [--fail-on error|warning|never] [--cp <classpath>] [--no-tests] [--title <text>]
```

`check` reads an architecture file: the views file the viewer exports, with `match` patterns and
`rules`. Without `--rules` it reads `architecture.json` from the first project directory. With
`--model` you must pass `--rules`, and no language toolchain is needed. The file is plain JSON,
without comments. `version` is optional (missing means 1). Fields depviewer does not know are
ignored.

Without `--model`, `check` scans the project directories first, with the scan options `--cp`,
`--no-tests` and `--title`. Without `--view` it checks every view that has rules. A file without
rules prints `no rules` and exits 0.

| Option | Meaning |
|---|---|
| `--view <id>` | Check only this view. |
| `--format json` | Print the JSON report instead of text. Overlap warnings then go to stderr. |
| `--out <file>` | Also write the viewer, with the view's pattern assignments and violations embedded. |
| `--baseline <file>` | Accept the violations listed in this file (see below). |
| `--update-baseline` | Write all current violations to the `--baseline` file and exit 0. |
| `--fail-on <level>` | Which violations fail the check: `error` (default), `warning` (errors and warnings) or `never`. |

| Exit code | Meaning |
|---|---|
| 0 | No failing violations. |
| 1 | Failing violations. |
| 2 | The scan failed, e.g. the jvm extractor (Java and Kotlin) could not run; or invalid arguments, architecture file or model, for example a missing file, an unknown `version`, an unknown rule kind, a rule naming a group that does not exist, two views with one id, a view with rules but no id, a `|` in the id of a rule or of a view with rules, or a `--model` that is not a depviewer model. |

### Selectors

A group can select its elements by pattern instead of by hand. `match` lists selectors
`<kind>:<pattern>`:

```json
{ "id": "dom", "name": "Domain",
  "match": ["package:com.example.domain.**", "type:**Repository@interface", "!type:**Test"] }
```

| Kind | Selects | Separator |
|---|---|---|
| `module:` | modules by name | — |
| `package:` | packages by full name | `.`, or `/` for folder-based packages (TypeScript, Dart) |
| `path:` | files by path inside their module | `/` |
| `type:` | types by qualified name; `@<kind>` (e.g. `@interface`) also checks the kind | `.` |
| `ext:` | external packages and types | `.` |

`*` matches within one segment, `**` across any number of segments, including none:
`package:com.example.domain.**` selects `com.example.domain` and every package below it. A leading
`!` removes what an earlier selector of the same group selected; selectors apply in order. A
TypeScript or Dart type is named `<path>#<Name>`, so select it with `type:**Name`. An unknown kind,
an unknown `@<kind>` or `@` on any other kind than `type:` is rejected with exit code 2.

### Which group an element is in

For each element, in this order:

1. its own entry in `assign`,
2. the first group, in the order of `groups`, whose `match` selects it,
3. the group of its nearest ancestor, decided the same way.

An element that a group removes with `!` never gets that group from an ancestor either. It takes the
group of the next ancestor above, or stays ungrouped. Members always go with their type. A type can be
assigned on its own with a `type:` selector or the key `T:<module>:<qualified name>`. `check` uses
these, but the viewer places types with their file.

`check` warns when two groups' patterns select the same element, and when an element's pattern
overrides another group that the element would get from an ancestor's pattern. A subgroup refining
the group it is nested in is not an overlap. Warnings do not change the exit code:

```
warning  overlap  T:fixture:com.example.infra.InMemoryOrderRepository  selected by dom and by infra through F:fixture:src/main/java/com/example/infra/InMemoryOrderRepository.java, goes to dom (view layers)
```

### Rules

Each view may list rules. Every rule has an `id`, unique in its view, and a `kind`:

```json
"rules": [
  { "id": "layering", "kind": "layers", "order": ["app", "infra", "svc", "dom"] },
  { "id": "domain-pure", "kind": "allow-only", "from": "dom", "to": ["dom", "ext:java.**"],
    "description": "The domain only uses itself and the JDK." },
  { "id": "no-pkg-cycles", "kind": "no-cycles", "level": "package" }
]
```

| Kind | Fields | Violation |
|---|---|---|
| `layers` | `order` (groups, top to bottom), `strict` (default `false`) | a dependency from a lower layer to a higher one; with `strict`, also one that skips a layer |
| `forbid` | `from`, `to` | any dependency from `from` to `to` |
| `allow-only` | `from`, `to` | a dependency from `from` to anything not in `to`, ungrouped code and externals included |
| `independent` | `groups` | any dependency between two of the listed groups |
| `no-cycles` | `level` (`package`, `file` or `type`), `within` (a group, optional) | each cycle at that level, with its members and the references between them |
| `no-ungrouped` | `scope` (a selector, optional) | each topmost element in scope with code below it and nothing in a group |

`from` and `to` take a group id or an `ext:` selector, or an array of them. A group stands for
itself and its subgroups, and a dependency inside one group never breaks a rule between groups.
Ungrouped code is only checked by `allow-only`, `no-cycles` and `no-ungrouped`. Packages at
`no-cycles` level are the package a file is in, and at `type` level a nested type and the type
around it are not a cycle.

All rules also take `severity` (`error`, the default, or `warning`), `description`, `tests`
(default `false`: references from or to test sources are not checked) and `edgeKinds` (the
dependency kinds the rule looks at, default all: `uses`, `extends`, `implements`, `call`, `new`,
`access`).

### Reports

The text report has one violation per rule and pair of innermost elements, sorted by file and line,
with all its lines after the path, then one line per reference with its kind. Files are named by
their path. A use of a type and a call of one of its members from
the same element are one violation, at the member:

```
src/main/java/com/example/domain/Order.java:25  error  layering  dom -> svc
    com.example.domain.Order.total() -> com.example.service.PriceCalculator.gross(double) (call)
    com.example.domain.Order.total() -> com.example.service.PriceCalculator (uses)
errors: 3, warnings: 0, baselined: 0, resolved: 0, overlaps: 0
```

`--format json` prints `format: 1`, the model's `title`, `generated` and `analysis` (which tool
analysed each language, see [What gets scanned](#what-gets-scanned)), a `summary` with the counts `error`, `warning`, `baselined`, `resolved` and
`overlaps`, the `violations` and the `resolved` fingerprints. A violation has `rule`, `view`,
`severity`, `kind` (the rule's kind), `from` and `to` (`key`, `q`, `path`, `group`), `lines`, `count`,
`exact`, `baselined`, `fingerprint` and `refs` (one per reference: `kind`, `lines`, `count`, and `to`
when it is not the violation's `to`). A `no-cycles` violation has `members` instead of `from` and
`to`, and its `refs` name `from`, `to` and `path`. A `no-ungrouped` violation has only `from`.

`exact` comes from the `analysis` entry of the source's language and module. It is `false` for
violations from Kotlin code, which is resolved by name: such a violation can be wrong. It still fails the check. Accept a wrong one into the baseline, or give the rule
`"severity": "warning"`.

### Baseline

A fingerprint is the view, the rule and the two elements (or the cycle's members, or the ungrouped
element), without lines or dependency kinds, so it stays the same when code moves within a file.
An element is named by its view key, which includes the module, and a member by its type's key and
its name:

```
layers|layering|T:fixture:com.example.domain.Order#total()|T:fixture:com.example.service.PriceCalculator#gross(double)
```

`--update-baseline --baseline baseline.json` writes the fingerprints of all current violations,
sorted, as one JSON array to commit. With `--baseline baseline.json`, the violations in it are
reported as `baselined` and never fail the check. Entries that no longer occur are reported as
`resolved`: run `--update-baseline` again to drop them, so the baseline only shrinks. One baseline
file can hold all views: with `--view`, entries of the other views are kept and never reported as
`resolved`.

## What gets scanned

Each directory is searched for language markers, skipping `node_modules`, `.dart_tool`, build
output and VCS folders:

| Marker | Language | Module name |
|---|---|---|
| `src/{main,test}/{java,kotlin}` | Java / Kotlin | `artifactId` from `pom.xml`, else the folder name |
| `package.json` | TypeScript / JavaScript | `name` from `package.json`, else the folder name |
| `pubspec.yaml` | Dart | `name` from `pubspec.yaml`, else the folder name |

A directory without any marker is scanned as a plain Java/Kotlin source root. Several markers in
one tree (a monorepo, a Flutter app next to a backend) give several modules in one model. A
language whose toolchain is missing is skipped with a note; the others are still scanned. Java and
Kotlin are the exception: if their extractor cannot run, the scan stops with an error. Which
toolchain each language uses is described in [languages](languages.md).

The model records it in `analysis`, one entry per language and set of modules
([FR-011](../spec/wiki/functional-requirements/FR-011-record-the-provenance-of-every-language-and-module.md)):

```json
{ "language": "typescript", "modules": ["shop-web"], "tool": "typescript", "version": "5.9.3",
  "source": "project", "exact": true,
  "extractor": { "name": "depviewer-typescript-extractor", "version": "1.0.0" } }
```

`source` is `override` (a `DEPVIEWER_<TOOL>` variable), `project` (the project's own), `path` or
`pinned` (shipped with depviewer, like the Kotlin parser). Java is analysed by the JDK of the jvm
extractor: `DEPVIEWER_JAVA` (`source: override`), else the `java` on the `PATH` or, failing that, the
JDK that runs depviewer (both `source: path`). `exact` is `false` for a language resolved by name (Kotlin).

## Libraries and the classpath (Java, Kotlin)

Java references to library types resolve when the jars are on the classpath. `target/dependency/*.jar`,
`lib/` and `libs/` are picked up automatically, so for Maven projects running
`mvn dependency:copy-dependencies` first is enough. Otherwise pass `--cp`.

Without the jars the scan still works: references that cannot be resolved are listed under
*External › (unresolved)* in the viewer, and the scan prints a note with the number of compile
errors. Dependencies between your own classes are still found.

## Output

- **`.html`**: one self-contained file with the viewer, the data and (with `--views`) your views.
  It works offline from `file://` and can be shared, attached to tickets or published as a CI
  artifact. It contains your class and member names, so share it like source code.
- **`.json`**: the data only. Open it in `viewer.html` with *Open…*, by dragging the file onto the
  page, or with `viewer.html?data=deps.json` when the viewer is served over HTTP. The format is
  described in [AGENTS.md](../AGENTS.md#data-model-scanner--viewer-contract).

## URL parameters

| Parameter | Meaning |
|---|---|
| `?level=1` … `4` | Open the tree at Packages, Files, Types or Members (default: Files). |
| `?data=<url>` | Load a `.json` file into `viewer.html` (needs HTTP; browsers block it on `file://`). |
