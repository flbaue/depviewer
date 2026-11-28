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
| `--views <file>` | Embed group views exported from the viewer (*⋯ › Export views*). |
| `--cp <classpath>` | Extra classpath for resolving Java/Kotlin library types. |
| `--template <file>` | Viewer template (default: `viewer.html` next to the script). |
| `-h`, `--help` | Show the options. |

Without the wrapper: `javac -d out src/depviewer/*.java && java -cp out depviewer.DepScan …`.

## Checking the architecture

```
./depviewer check [--rules <file>] (<project-dir>... | --model deps.json)
```

`check` reads an architecture file: the views file the viewer exports, optionally with rules. Without
`--rules` it reads `architecture.json` from the first project directory; with `--model` you must
pass `--rules`. The file is plain JSON, without comments. `version` is optional (missing means 1).
Fields depviewer does not know are ignored.

Rule checking itself is not built yet. Today `check` validates the file and, if a group has `match`
patterns, scans the project (or reads `--model`) and reports overlapping patterns. A file without
rules prints `no rules` and exits 0, and a file with rules exits 2 so that a CI job never passes
without checking.

| Exit code | Meaning |
|---|---|
| 0 | No failing violations. |
| 1 | Failing violations. |
| 2 | Invalid arguments, architecture file or model, for example a missing file or an unknown `version`. |

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
assigned on its own with a `type:` selector or the key `T:<module>:<qualified name>` (the viewer does
not show such assignments yet).

`check` warns when two groups' patterns select the same element, and when an element's pattern
overrides another group that the element would get from an ancestor's pattern. A subgroup refining
the group it is nested in is not an overlap. Warnings do not change the exit code:

```
warning  overlap  T:fixture:com.example.infra.InMemoryOrderRepository  selected by dom and by infra through F:fixture:src/main/java/com/example/infra/InMemoryOrderRepository.java, goes to dom (view layers)
```

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
language whose toolchain is missing is skipped with a note; the others are still scanned.

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
