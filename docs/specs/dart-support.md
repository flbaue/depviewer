# Spec: Dart support

Status: draft
Tests: `test/checks/dart.py` on `test/fixture-dart/`, smoke test in `scripts/check.sh`
Depends on: [multi-language.md](multi-language.md)

## Goal

Scan Dart (incl. Flutter) packages with the official Dart analyzer (`package:analyzer`), so
references are resolved exactly.

## Toolchain

- Requires the Dart SDK (3.x) on `PATH` (`dart`), or `DEPVIEWER_DART` pointing to it. Flutter's
  bundled `dart` works.
- `extractors/dart/` is a Dart package depending on a pinned `analyzer`; `dart pub get` runs on
  first use.
- The scanned package should have run `dart pub get` / `flutter pub get` (so `package:` imports
  resolve). If `.dart_tool/package_config.json` is missing the scan still runs, imports of other
  packages count as errors, and a hint is printed.
- If Dart is missing, Dart modules are skipped with a message.

## Sources

- Module root: a folder with `pubspec.yaml`. Source folders: `lib/`, `bin/`, `web/`, `tool/`,
  and test folders `test/`, `integration_test/` (flagged as tests). Generated files
  (`*.g.dart`, `*.freezed.dart`) are included but nothing below `.dart_tool/` or `build/`.

## Model

| Dart | Node kind |
|---|---|
| file (library or part) | `file`, in the package of its folder (`lib/src/models`) |
| `class`, `abstract class`, `sealed class`, … | `class` |
| `abstract interface class`, `interface class` | `interface` |
| `mixin`, `mixin class` | `mixin` |
| `enum` | `enum`; values `enumconst` |
| `extension` | `extension` (unnamed: `extension on Type`) |
| `extension type` | `class` |
| `typedef` | `typealias` |
| constructors (unnamed, named `Foo.bar`, factory) | `ctor` named `Foo(...)` / `Foo.bar(...)` |
| methods, operators, top-level functions | `method` |
| fields, getters/setters, top-level variables | `field` |

- Top-level functions/variables are children of their file. Library-private names (`_x`) are
  private; `static` members are static.
- Qualified names: `<path>#Name`, `<path>#Class.member`.

## Dependencies

Every identifier that the analyzer resolves to an element produces an edge from the innermost
declaring node:

- project elements → that node (`part` files resolve to the declaring part's file);
- elements from other packages → external `<package>` (from the `package:<name>/` URI) with the
  top-level element name; `dart:` libraries → external `dart:core`, `dart:async`, …

Edge kinds: `extends` → `extends`; `implements`, `with` (mixins), `on` (mixin constraints) →
`implements`; invocation of a function/method → `call`; constructor invocation (with or without
`new`/`const`) → `new` to the constructor; getter/setter/field/variable access → `access`; type
annotations and everything else → `uses`. Imports/exports/part directives are not dependencies.

## Fixture expectations (test/fixture-dart)

A small shop app package (`name: shop_app`, no third-party dependencies): an abstract interface
and implementing class, a mixin, an enum with a method, an extension, a typedef, named and
factory constructors, a private member, top-level function and variable, a `part` file, a
deliberate folder cycle, a test file and uses of `dart:core`/`dart:async` types. Concrete
assertions live in `test/checks/dart.py`.

## Out of scope

Flutter widget-tree semantics (widgets are classes like any other), code generation, Dart macros.
