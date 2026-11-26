# ADR 0001: Architecture for language support

- Status: accepted
- Date: 2026-11-26
- Issue: [#9](https://github.com/flbaue/depviewer/issues/9)
- Related: [#3](https://github.com/flbaue/depviewer/issues/3), [#4](https://github.com/flbaue/depviewer/issues/4),
  [#5](https://github.com/flbaue/depviewer/issues/5), [#6](https://github.com/flbaue/depviewer/issues/6),
  [#7](https://github.com/flbaue/depviewer/issues/7)

## Context

depviewer supports four languages in three different ways:

| Language | How | Where |
|---|---|---|
| Java | javac Trees API, in the scanner process | `JavaScanner` |
| Kotlin | built-in name-based parser, in the scanner process | `KotlinScanner` |
| TypeScript / JavaScript | Node extractor printing a JSON fragment, one run per module | `extractors/typescript/`, `Toolchain` |
| Dart | Dart extractor printing a JSON fragment, one run for all packages of a scan | `extractors/dart/`, `Toolchain` |

Java and Kotlin are coupled inside the process: Kotlin declarations are collected first, javac's
errors for references to Kotlin code are matched to them by name, and the Kotlin parser resolves
references to Java declarations (`docs/specs/kotlin-support.md`).

The next languages wanted are **Swift, Python, Go and C#**, plus exact Kotlin with its compiler
(#7). Before adding them we need one way to add a language, so that each new one does not invent
its own integration. Problems with the current state:

- Two integration paths (in-process and extractor) with different capabilities.
- TypeScript runs once per module, so references between packages of a monorepo point to
  `node_modules` or external packages instead of the real declarations (#4). Dart already had to
  be changed to run once per scan.
- Toolchain versions: TypeScript prefers the project's own compiler, Dart uses the user's SDK with
  a pinned analyzer, and #7 planned a pinned Kotlin download. There is no common policy, and
  results can differ between machines without the output saying why.
- The model does not say how exact it is: Java, TypeScript and Dart are compiler-exact, Kotlin is
  name-based. Architecture checks (#6) and the viewer cannot tell the user.
- The fragment format has no version and no shared test, so an extractor can drift from what the
  core expects without anyone noticing.

## Decision

### 1. Every language is an extractor

All languages, **Java and Kotlin included**, are analysed by extractors: separate processes that
receive a file list and print one JSON fragment. The core (`src/depviewer/`) is reduced to
discovery, running extractors, merging fragments, cross-language linking and writing output. It
has no language-specific analysis left.

Extractors are grouped by **ecosystem**, not by file type, where languages compile together:

| Extractor | Languages | Toolchain |
|---|---|---|
| `jvm` | Java, Kotlin | JDK (javac) for Java; Kotlin compiler for exact Kotlin (#7), built-in parser as fallback |
| `typescript` | TypeScript, JavaScript | Node + TypeScript compiler |
| `dart` | Dart | Dart SDK + analyzer |
| `swift`, `python`, `go`, `dotnet` | Swift; Python; Go; C# | each language's own toolchain (to be specified per language) |

The `jvm` extractor stays written in Java and needs only the JDK, so a Java-only user still needs
nothing else. It keeps the Java ↔ Kotlin linking inside one run, where both sides' declarations are
known.

### 2. One run per extractor per scan

The core starts each extractor **once per scan** with all modules of its language(s). The extractor
reports the module of every file (as the Dart extractor already does). References between modules
of the same ecosystem (monorepos, workspaces, multi-module Gradle builds) therefore resolve to the
real declarations. This replaces per-module runs and fixes #4 as a consequence.

### 3. Toolchains: the user's first, pinned as fallback

For each extractor the toolchain is chosen in this order:

1. An explicit override (`DEPVIEWER_<TOOL>`, e.g. `DEPVIEWER_NODE`, `DEPVIEWER_DART`).
2. The **project's own** version, where the project declares one: `node_modules/typescript`, the
   Kotlin version from the Gradle build, a project-local SDK.
3. The **user's installed** tool on the `PATH`.
4. A **pinned download**, where the toolchain can be downloaded as a library or archive
   (TypeScript, Kotlin compiler). Announced on stderr; can be done ahead of time for offline use.
5. Otherwise the language is **skipped with a note**, never a scan failure.

A candidate is only used if its version is in the extractor's **supported range**. Each extractor
declares that range and checks it itself at start-up (see the contract below); an unsupported
version is reported and the next candidate is tried. The range is tested in CI with its oldest and
newest version.

Rationale: the same versions as the user's own build understand the same syntax and read the same
project settings (`tsconfig.json`, `analysis_options.yaml`, Gradle), and for Swift, Go, C# and
Python an installed toolchain is the only realistic option anyway. The pinned fallback keeps
"works with nothing but a JDK" for Java and makes TypeScript and Kotlin work on machines without a
project install.

### 4. Provenance per language and module

The output records, per language and module, what produced it:

```jsonc
"analysis": [
  { "language": "kotlin", "modules": ["app"], "tool": "kotlinc", "version": "2.1.0",
    "source": "project",      // override | project | path | pinned
    "exact": true },
  { "language": "java", "modules": ["app"], "tool": "javac", "version": "21.0.5", "source": "path", "exact": true }
]
```

`exact: false` means name-based resolution (today's Kotlin parser). The viewer shows this in the
header/details, and architecture checks (#6) report it with their violations. It is recorded per
language and module, **not per dependency**, to keep the JSON small; per-dependency precision can
be added later if mixed cases need it.

### 5. A versioned contract with a conformance test

The extractor contract is written down as a versioned spec (`docs/specs/extractor-contract.md`,
replacing the fragment section of `multi-language.md`) and checked by a shared test:

- **Input:** one invocation per scan with a JSON request on stdin or in a file: modules (name,
  folder), files per module with test flags, options (tests on/off, classpath for JVM), and the
  contract version the core speaks.
- **Handshake:** `--describe` prints the extractor's name, contract version, languages, and the
  detected toolchain with version, source and whether it is supported. The core uses it for
  toolchain selection (section 3) and for `analysis` (section 4).
- **Output:** the fragment as today (nodes, externals, edges, errors, per-file module), plus a
  `contract` version. The core rejects an unknown major version with a clear message and ignores
  unknown fields.
- **Diagnostics** go to stderr with a common prefix and an error count in the fragment, so notes
  like "13 compile errors" are reported per language consistently.
- **Conformance test:** a small language-neutral checker run on every extractor's output for its
  fixture: valid JSON shape, parents before children, every edge endpoint exists, ids unique, kinds
  and edge kinds from the allowed lists, line numbers inside the file, module of every file known.
  It runs in `scripts/check.sh` next to each language's own model checks.

### 6. Cross-ecosystem references

References between ecosystems (a Flutter app calling native Kotlin or Swift code through platform
channels, a TypeScript frontend calling a Java backend) are **not** linked. They do not exist as
language-level references, and guessing them would make the model less trustworthy. Within an
ecosystem, linking is the extractor's job (section 1). The core only links an external reference
to a declaration of another fragment when both carry the same fully qualified name and language
family; this is a fallback, not the main mechanism.

## Consequences

**Positive**

- One way to add a language: implement an extractor against the contract, pass the conformance
  test, add a fixture and model checks. AGENTS.md "Adding a language" becomes a checklist.
- Monorepos and workspaces link correctly for every language (#4).
- Results say which tool and version produced them, and whether they are exact.
- The core gets smaller and language-neutral.

**Negative / costs**

- **Refactor of Java and Kotlin** out of the core into the `jvm` extractor. Risky because it is the
  most-used path. Mitigation: Java- and Kotlin-only scans must produce **identical JSON** (except
  `generated` and the new `analysis` field) before and after, on the fixtures and on a large real
  project, before the old path is removed.
- **One more process start** per scan for JVM projects (about a JVM start-up, well under a second),
  and fragments of large projects go through stdout (tens of MB): acceptable against a scan time of
  up to a minute, but measured in the refactor.
- **Supporting version ranges** instead of one pinned version: extractors must cope with API
  changes across versions (Dart analyzer, Kotlin compiler), and CI tests the oldest and newest
  supported version of each toolchain. Results can differ between machines; provenance makes that
  visible.
- Toolchain detection (project-declared versions, Gradle) is per ecosystem work.

## Alternatives considered

- **Keep Java (and Kotlin) in-process, extractors for the rest.** Less work and no extra process,
  but keeps two integration paths and the Java/Kotlin special case. Rejected by the maintainer in
  favour of one contract for everything.
- **Pinned toolchains only.** Reproducible, but analyses code with a different compiler version than
  the user's build (new syntax, different config semantics), and impossible for Swift, Go, C# and
  Python without large downloads. Rejected.
- **User's toolchains only.** Never downloads anything, but Java-only and TypeScript users would lose
  the zero-setup experience. Rejected; pinned stays as a fallback.
- **Language servers (LSP)** as the common integration. One protocol for many languages, but needs
  one request per identifier, project import and long start-up; too slow and fragile for batch
  scans of large projects. Not chosen as the common mechanism; an extractor may still use one
  internally if a language has nothing better.
- **Reading compiled output** (`--classes`, #5) as the JVM mechanism. Exact without a Kotlin
  compiler, but needs a build and loses source-level detail. Stays a possible extra mode of the
  `jvm` extractor, not the architecture.
- **Provenance per dependency.** More precise for mixed Java ↔ Kotlin cases, but enlarges a JSON
  that already has hundreds of thousands of edges. Deferred.

## Migration plan

Each step follows the spec-first, test-driven workflow (AGENTS.md) and is its own issue:

1. **Contract spec and conformance test**: write `extractor-contract.md` (version 1 = today's
   fragment plus request, handshake, `contract`, per-file module), the conformance checker, and run
   it on the TypeScript and Dart extractors.
2. **Once-per-scan runs** for TypeScript (fixes #4), with a monorepo fixture.
3. **Toolchain selection and provenance**: `--describe`, the selection order and supported ranges,
   the `analysis` field, and showing it in the viewer.
4. **`jvm` extractor**: move Java and Kotlin behind the contract with identical output; then remove
   the in-process path.
5. **Exact Kotlin** (#7) inside the `jvm` extractor, using the project's Kotlin version.
6. **New languages**, each with its own spec: Go, C#, Python, Swift (order to be decided; Go and C#
   have the most exact official tooling).

## Open questions

- Request format: JSON on stdin or a request file (Windows command-line limits favour a file).
- How the project's Kotlin version is read without running Gradle (version catalogs, plugins block).
- Whether `analysis` should also record the extractor's own version.
