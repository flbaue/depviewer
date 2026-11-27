# #12 Toolchain selection (user's first, pinned fallback) and analysis provenance

https://github.com/flbaue/depviewer/issues/12
State on export (2026-11-27): open, opened 2026-11-26

Step 3 of the migration in [ADR 0001](https://github.com/flbaue/depviewer/blob/main/docs/adr/0001-language-support.md) (#9).

Implement the toolchain policy and record which tool produced each part of the model.

## Scope

- **Selection order** per extractor: explicit override (`DEPVIEWER_<TOOL>`), the project's own version (e.g. `node_modules/typescript`, the Kotlin version from the Gradle build, a project-local SDK), the user's tool on the `PATH`, a pinned download where possible (TypeScript, Kotlin compiler), else skip with a note.
- A candidate is used only if its version is in the extractor's **supported range**; the extractor reports this via `--describe` (contract from step 1). Unsupported versions are reported and the next candidate is tried.
- **`analysis`** in the output JSON, per language and module: language, modules, tool, version, source (`override | project | path | pinned`), exact (true/false).
- **Viewer**: show it (header summary and details), and mark name-based languages so users know a dependency may be missing.
- **CI**: test the oldest and newest supported version of each toolchain.

## Done when

- Spec first (contract and viewer parts), failing checks for selection order and the `analysis` field.
- On the fixtures, `analysis` lists every language with the tool that was really used; a forced unsupported version falls back with a note.
- README/docs (`languages.md`, `usage.md`) explain which tool is used and how to override it.
