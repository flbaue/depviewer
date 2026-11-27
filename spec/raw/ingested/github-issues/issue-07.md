# #7 Kotlin: exact resolution with the Kotlin compiler (or a language server)

https://github.com/flbaue/depviewer/issues/7
State on export (2026-11-27): open, opened 2026-11-25

Kotlin is analysed by a built-in, name-based parser because the JDK has no Kotlin compiler (`docs/specs/kotlin-support.md`). That works without extra tooling, but it misses or approximates references that need real type information:

- overloads (an overloaded name links to the first match),
- generics, smart casts and type inference beyond declared types,
- extension functions and receivers resolved through inference,
- Kotlin Multiplatform source sets and `.kts` files (not scanned at all).

Java, TypeScript and Dart are resolved exactly by their own compilers. Kotlin should get the same option.

## Idea

Add an optional **Kotlin extractor** in `extractors/kotlin/`, following the pattern of the TypeScript and Dart extractors (`docs/specs/multi-language.md`). It would run only when its toolchain is available and fall back to the built-in parser otherwise. Two ways to get exact resolution:

1. **Kotlin compiler / Analysis API.** A small JVM program on the official Kotlin compiler (e.g. `kotlin-compiler-embeddable` or the Kotlin Analysis API) parses and resolves the sources, walks the resolved tree and prints the usual JSON fragment: declarations with lines, and references with kind and line. Closest to how `JavaScanner` uses javac. Questions: pinning the compiler version, download size, startup time, and the classpath (Gradle dependencies) needed to resolve library types.
2. **Kotlin language server (LSP).** Start a Kotlin language server for the project and ask it for definitions/references. Reuses the project import the server already does (Gradle, KMP), but needs one request per identifier, so it is likely much slower on large projects and harder to run headless in CI.

Probably (1) for the scan, with (2) only worth it if the server can export resolved references in bulk. #5 (read compiled `.class` files) is a third option that needs a build but no Kotlin tooling; the spec should compare all three.

## Done when

- A spec (update `docs/specs/kotlin-support.md` or a new one): toolchain and how it is found/installed (pinned, like the other extractors), what improves over the name-based parser, how Java ↔ Kotlin references are linked in both directions, fallback when the toolchain is missing, and what stays out of scope.
- Fixture cases the built-in parser gets wrong today (overloads, generics, smart casts, extension functions on inferred receivers), with failing checks in `test/checks/jvm.py` first.
- Extractor implemented until green; CI installs the toolchain so nothing is skipped.
- On a real Kotlin project: compare the name-based and exact output, and report how many references were missed or misattributed, and the scan time of both.

## Comment by flbaue, 2026-11-26

Per [ADR 0001](https://github.com/flbaue/depviewer/blob/main/docs/adr/0001-language-support.md): exact Kotlin is part of the `jvm` extractor (#13), and the compiler is chosen by the toolchain policy (#12): the Kotlin version the project declares in its Gradle build first, the user's installed compiler next, a pinned download as fallback, and the built-in name-based parser if none works. Comes after #10–#13.
