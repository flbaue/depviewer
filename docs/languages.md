# Supported languages

One scan can cover a mixed repository. Each language is analysed as exactly as its available
tooling allows; the [requirements wiki](../spec/) has the details and known limitations.

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
Details: [spec](../spec/wiki/functional-requirements/FR-004-analyse-kotlin-by-name.md).

## TypeScript / JavaScript

Files `.ts .tsx .mts .cts .js .jsx .mjs .cjs` below each `package.json`, analysed with the
TypeScript compiler API, respecting each module's `tsconfig.json`. All modules of a scan are
analysed in one run, so in a monorepo an import of a sibling workspace package (linked into
`node_modules` by npm, yarn or pnpm) links to its declarations
([spec](../spec/wiki/functional-requirements/FR-012-link-the-modules-of-a-monorepo.md)). A package
whose types point to built `.d.ts` files instead of its sources still shows up as an external. Node is found as `node` on the
`PATH`, or set `DEPVIEWER_NODE`. Details: [spec](../spec/wiki/functional-requirements/FR-005-analyse-typescript-and-javascript-with-the-typescript-compiler.md).

The `typescript` package is chosen per module, in this order
([spec](../spec/wiki/functional-requirements/FR-010-choose-the-toolchain-in-a-fixed-order.md)):

1. `DEPVIEWER_TYPESCRIPT`, the folder of a `typescript` package;
2. the project's own, in `node_modules/typescript` of the module or a parent folder;
3. the package of the `tsc` on the `PATH`;
4. a pinned copy that `npm ci` installs into `extractors/typescript/` on first use.

Only TypeScript 5 and 6 are used (7 has no JavaScript compiler API). An unsupported version is
reported and the next one is tried. The scan prints which one it chose, for example
`TypeScript/JavaScript: typescript 5.9.3 (project: …/node_modules/typescript) for shop-web`. Two
machines can therefore choose different versions; set `DEPVIEWER_TYPESCRIPT` in CI to pin one.

## Dart

Analysed with the official Dart analyzer; all packages of a scan are analysed together, so
references between them link to the real declarations. The Dart SDK is chosen in this order:
`DEPVIEWER_DART`, the project's Flutter SDK (FVM's `.fvm/flutter_sdk`, or `flutterRoot` in
`.dart_tool/package_config.json`), then `dart` on the `PATH` (Flutter's bundled `dart` works). There
is no pinned SDK. Dart 3.5 or later before 4 is used; another version is reported and the next one
is tried. The extractor's dependencies are installed with
`dart pub get` on first use. Run `dart pub get` / `flutter pub get` in your project first so imports
of other packages resolve. Details: [spec](../spec/wiki/functional-requirements/FR-006-analyse-dart-with-the-dart-analyzer.md).

## How the languages fit together

All languages produce the same model (module › package › file › type › member, plus dependencies),
so the viewer, views and groups work the same for every language.
Architecture: [spec](../spec/wiki/decisions/DR-001-every-language-is-analysed-by-an-extractor-behind-one-versioned-contract.md).
