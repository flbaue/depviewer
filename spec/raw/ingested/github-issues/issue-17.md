# #17 Language: Swift

https://github.com/flbaue/depviewer/issues/17
State on export (2026-11-27): open, opened 2026-11-26

Part of step 6 in [ADR 0001](https://github.com/flbaue/depviewer/blob/main/docs/adr/0001-language-support.md) (#9): add Swift as a new language extractor (`extractors/swift/`).

**Likely toolchain:** the user's Swift toolchain (swift-syntax, or the index store after a build for exact results); decide what is exact without a build, chosen per the ADR's policy (user's tool first, pinned fallback only where a download is practical).

## Done when (AGENTS.md "Adding a language")

- Spec `docs/specs/swift-support.md`: toolchain and supported version range, discovery markers and module names, mapping to the model (modules, packages, files, types, members), dependency rules, exactness, fixture expectations, out of scope.
- Fixture `test/fixture-swift/` with a deliberate cycle, an external and an unresolved reference; `test/checks/swift.py` failing first.
- Extractor implementing the contract from step 1 (once per scan, `--describe`), passing the conformance test.
- CI installs the oldest and newest supported toolchain; nothing skipped.
- Tried on a real Swift code base; `docs/languages.md` updated.

Depends on steps 1–3 (contract, once-per-scan runs, toolchain selection).
