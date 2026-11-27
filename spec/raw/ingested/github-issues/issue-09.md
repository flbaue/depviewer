# #9 Evaluate the architecture for adding new languages

https://github.com/flbaue/depviewer/issues/9
State on export (2026-11-27): closed, opened 2026-11-26, closed 2026-11-26

depviewer now supports four languages in three different ways:

- **Java** in-process, attributed by javac (`JavaScanner`),
- **Kotlin** in-process, with a built-in name-based parser (`KotlinScanner`),
- **TypeScript/JavaScript** and **Dart** as external extractors (Node, Dart) that print a JSON fragment, merged by `Toolchain` (`docs/specs/multi-language.md`).

Before adding more languages (candidates: Go, Python, C#, Swift, Rust, Kotlin via its compiler #7), we should evaluate whether this architecture is the right base, and write down the decision.

## Questions to answer

- **One model or two paths?** Should Java and Kotlin also become extractors producing the same fragment format, so every language follows one contract? Or is in-process Java (no extra toolchain, the main use case) worth the special case?
- **Fragment contract.** Is the format complete and stable enough for third-party or new extractors: versioning, declaration kinds per language, edge kinds, lines, test flags, external types, errors and diagnostics? Should there be a schema and a shared conformance test that every extractor must pass?
- **Granularity.** Today extractors run once per module. That is wrong for monorepos and workspaces (#4; Dart was already changed to analyse all packages together). Should the contract be "one run per language per scan" with the extractor reporting modules?
- **Cross-language references.** Java ↔ Kotlin works by name; Flutter native code (#3), TypeScript ↔ JavaScript, generated code. Where do cross-language links get resolved: in each extractor, in the core, or not at all?
- **Exactness levels.** Compiler-exact (Java, TypeScript, Dart) vs. name-based (Kotlin) vs. bytecode (#5). Should the model record how an edge was resolved, so the viewer and architecture checks (#6) can say so?
- **Toolchains.** Finding, pinning and installing toolchains (`npm ci`, `dart pub get` on first use), offline use, CI cost, and startup time per language. What does a new language have to provide?
- **User-installed vs. pinned tooling.** Should extractors prefer the tooling the user already has, so the analysis uses the same versions as their project, with a pinned copy only as a fallback?
  - Today: TypeScript already prefers the project's own `typescript` package; Dart uses the user's SDK but a pinned `analyzer` package; Kotlin (#7) was planned as a pinned compiler download, which could instead use the Kotlin version the project declares in Gradle.
  - For: newer syntax is understood, project settings are read the way the user's own build reads them, nothing extra to download. For Swift, Go, C# and Python it is the only realistic option (`swift`, `go`, `dotnet`, `python`).
  - Against: extractors must support a range of versions of APIs that change between major versions (Dart analyzer, Kotlin compiler internals), CI must test the oldest and newest supported version, and results can differ between machines.
  - To decide: the policy (prefer user's, pinned fallback?), the supported version range per language and how it is checked, and whether the model records which toolchain version produced it.
- **Discovery.** Marker files per language (`pom.xml`/source folders, `package.json`, `pubspec.yaml`): how to add markers without conflicts, and how to exclude generated or vendored code consistently.
- **Performance.** Scan time and memory on large projects; process start-up per extractor; large fragments over stdout.
- **Testing.** One fixture plus `test/checks/<language>.py` per language works; is it enough for a growing number of languages, or do we need a shared fixture style and helpers?

## Done when

- An architecture decision record in `docs/` (e.g. `docs/adr/0001-language-support.md`) with the current state, options considered, trade-offs, the decision, and what stays as it is.
- An updated `docs/specs/multi-language.md` (and AGENTS.md "Adding a language") reflecting the decision: the contract a new language must fulfil, step by step.
- Follow-up issues for the changes the decision requires (e.g. fragment schema and versioning, conformance test, per-scan extractor runs), linked here.
- Optionally, a small spike that adds one new language end to end against the proposed contract, to check the guide is complete.

## Comment by flbaue, 2026-11-26

Decided in [ADR 0001](https://github.com/flbaue/depviewer/blob/main/docs/adr/0001-language-support.md): every language (Java and Kotlin included) becomes an extractor behind a versioned contract, run once per scan; toolchains are the user's/project's first with a pinned fallback; the output records tool, version and exactness per language and module.

Migration:
- [ ] #10 Extractor contract v1: spec, `--describe` handshake, conformance test
- [ ] #11 Run extractors once per scan; TypeScript monorepo links (fixes #4)
- [ ] #12 Toolchain selection and analysis provenance
- [ ] #13 Move Java and Kotlin into a `jvm` extractor
- [ ] #7 Exact Kotlin with the compiler (project's Kotlin version)
- [ ] #14 Go · #15 C# · #16 Python · #17 Swift

Closing this evaluation once the ADR is accepted; the work continues in the issues above.
