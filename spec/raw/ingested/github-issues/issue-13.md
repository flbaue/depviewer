# #13 Move Java and Kotlin into a jvm extractor behind the contract

https://github.com/flbaue/depviewer/issues/13
State on export (2026-11-27): open, opened 2026-11-26

Step 4 of the migration in [ADR 0001](https://github.com/flbaue/depviewer/blob/main/docs/adr/0001-language-support.md) (#9).

Move Java and Kotlin analysis out of the core into a `jvm` extractor behind the contract, so every language uses the same path and the core becomes language-neutral.

## Scope

- `extractors/jvm/` (or a separate entry point in the existing Java sources): javac for Java and the built-in Kotlin parser, in **one run** per scan so Java ↔ Kotlin references keep linking as today. Needs only the JDK.
- The core keeps discovery, running extractors, merging, cross-language fallback linking and output; `JavaScanner`/`KotlinScanner` move out.
- Classpath handling (`--cp`, `target/dependency`, `lib`, `libs`) passed through the request.

## Done when

- **Identical JSON** (except `generated` and the new `analysis` field) before and after on all fixtures and on a large real Java/Kotlin project, compared as described in AGENTS.md step 4. Only then is the in-process path removed.
- The conformance checker (step 1) passes on the `jvm` extractor.
- Scan time and peak memory measured before/after on the large project; the extra process start and the fragment over stdout are acceptable (ADR: well under a second; fragments of tens of MB).
- AGENTS.md and `multi-language.md` updated: no language-specific code left in the core.
