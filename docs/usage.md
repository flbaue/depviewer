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
