# #5 Idea: exact mode from compiled classes (--classes) for Java and Kotlin

https://github.com/flbaue/depviewer/issues/5
State on export (2026-11-27): open, opened 2026-11-25

Kotlin is analysed by a built-in, name-based parser (no Kotlin compiler in the JDK), so some references are missed (overloads, generics, smart casts - see `docs/specs/kotlin-support.md`).

**Idea:** an optional `--classes` mode that reads compiled output (`target/classes`, `build/classes/...`) when the project has been built. Class files contain every call, field access, `new` and type use already resolved by the real compiler, with line numbers, for Java and Kotlin alike - no extra tooling needed, the class file format can be read in a few hundred lines of Java.

**To work out in a spec:** mapping compiler-generated classes (Kotlin companions, lambdas, `WhenMappings`, file facades) back to source declarations; what is lost in bytecode (inlined constants, some annotations); how to combine with the source-based model (files, packages, line spans).

Language servers were considered as an alternative; they need per-identifier requests and project import, which is heavier and slower for this use.
