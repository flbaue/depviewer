# #10 Extractor contract v1: versioned spec, --describe handshake and conformance test

https://github.com/flbaue/depviewer/issues/10
State on export (2026-11-27): open, opened 2026-11-26

Step 1 of the migration in [ADR 0001](https://github.com/flbaue/depviewer/blob/main/docs/adr/0001-language-support.md) (#9).

Write the extractor contract as a versioned spec and add a language-neutral conformance test, so every extractor (existing and new) is checked against the same rules.

## Scope

- `docs/specs/extractor-contract.md`, replacing the fragment section of `docs/specs/multi-language.md`. Version 1 = today's fragment plus:
  - **request**: modules (name, folder), files per module with test flags, options (tests on/off, JVM classpath), contract version; decide stdin vs. request file (open question in the ADR: Windows command-line limits favour a file),
  - **handshake** `--describe`: extractor name, contract version, languages, detected toolchain (tool, version, source, supported or not),
  - **output**: `contract` version, per-file `module`, nodes/externals/edges/errors as today,
  - **diagnostics**: stderr format and error count, reported per language.
- The core rejects an unknown major contract version with a clear message; unknown fields stay ignored.
- **Conformance checker** (`test/checks/contract.py`): valid shape, parents before children, unique ids, every edge endpoint exists, kinds and edge kinds from the allowed lists, line numbers within the file, every file's module known.
- Run it in `scripts/check.sh` on the TypeScript and Dart extractors' raw output for their fixtures.

## Done when

- Spec written first; the checker fails on a deliberately broken fragment and passes on both extractors (after small fixes to them if needed).
- TypeScript and Dart extractors speak contract version 1, including `--describe`.
- Scan output on all fixtures is unchanged apart from intended additions.
