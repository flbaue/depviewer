# Supported languages

One scan can cover a mixed repository. Each language is analysed as exactly as its available
tooling allows; the specs in [specs/](specs/) have the details and known limitations.

| Language | How it is analysed | Exactness | Needs |
|---|---|---|---|
| Java | JDK compiler (`javac` Trees API): parses and attributes | Exact: overloads, inherited members, static imports… | JDK 17+ |
| Kotlin (`.kt`) | Built-in parser, references resolved by name | Approximate (see below) | JDK 17+ |
| TypeScript / JavaScript | TypeScript compiler API and type checker | Exact: imports, re-exports, default exports | Node.js 18+ |
| Dart (incl. Flutter) | Official Dart analyzer | Exact | Dart SDK 3.x |

## Java

Parsed and attributed by the JDK's own compiler, so references resolve the way `javac` sees them.
Library types resolve when their jars are on the classpath; see [usage](usage.md#libraries-and-the-classpath-java-kotlin).

## Kotlin

There is no Kotlin compiler in the JDK, so Kotlin is read by a built-in parser. References are
resolved by name: imports, the same package, declared types of parameters, variables and
properties, and call chains like `event.order.total()`. When a name is ambiguous it is left out
rather than guessed. Java ↔ Kotlin references are included in both directions.

Not resolved: overloads (an overloaded name links to the first match), generics, smart casts and
other type inference; `.kts` files and Kotlin Multiplatform source sets are not scanned. Exact
resolution with the Kotlin compiler is planned in
[#7](https://github.com/flbaue/depviewer/issues/7).
Details: [specs/kotlin-support.md](specs/kotlin-support.md).

## TypeScript / JavaScript

Files `.ts .tsx .mts .cts .js .jsx .mjs .cjs` below each `package.json`, analysed with the
TypeScript compiler API, respecting the project's `tsconfig.json`. Uses the project's own
`typescript` package if installed, else a pinned copy that `npm ci` installs into
`extractors/typescript/` on first use. Node is found as `node` on the `PATH`, or set
`DEPVIEWER_NODE`. Details: [specs/typescript-support.md](specs/typescript-support.md).

## Dart

Analysed with the official Dart analyzer; all packages of a scan are analysed together, so
references between them link to the real declarations. Needs `dart` on the `PATH` (Flutter's
bundled `dart` works), or set `DEPVIEWER_DART`. The extractor's dependencies are installed with
`dart pub get` on first use. Run `dart pub get` / `flutter pub get` in your project first so imports
of other packages resolve. Details: [specs/dart-support.md](specs/dart-support.md).

## How the languages fit together

All languages produce the same model (module › package › file › type › member, plus dependencies),
so the viewer, views and groups work the same for every language.
Architecture: [specs/multi-language.md](specs/multi-language.md).
