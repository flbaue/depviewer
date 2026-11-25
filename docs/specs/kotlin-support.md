# Spec: Kotlin support

Status: implemented
Tests: `test/check_model.py` (section "Kotlin"), `scripts/check.sh` (smoke test)

## Goal

Projects that contain Kotlin (`.kt`) files, alone or mixed with Java, are scanned into the same
model as Java code, so the viewer shows their structure and dependencies. Dependencies between
Java and Kotlin code are included in both directions.

## Constraints

- No new dependencies: no Kotlin compiler, no build-tool integration. A JDK 17+ is still all
  that is needed.
- Consequently Kotlin references are resolved **by name**, not by a type checker. The scanner
  must prefer missing a dependency over inventing a wrong one: if a name cannot be resolved
  unambiguously, it produces no edge.
- Kotlin script files (`.kts`, e.g. Gradle build scripts) are not scanned.

## Source discovery

- `src/main/kotlin` and `src/test/kotlin` are source roots, like `src/main/java` / `src/test/java`.
  Test roots are flagged as tests.
- `.kt` files are also picked up from Java source roots (and vice versa), and from a directory
  without the standard layout.

## Model

| Kotlin | Node kind | Notes |
|---|---|---|
| file | `file` | top-level functions and properties are direct children of the file |
| `class`, `data class`, `sealed class`, `value class` | `class` | |
| `interface`, `fun interface` | `interface` | |
| `enum class` | `enum` | entries are `enumconst` children |
| `annotation class` | `annotation` | |
| `object`, `companion object` | `object` | an unnamed companion is called `Companion` |
| `typealias` | `typealias` | |
| primary / secondary constructor | `ctor` | named like the class, with parameter types: `Order(String, Int)` |
| `fun` | `method` | extension functions are shown as `Receiver.name(...)` |
| `val` / `var`, constructor `val`/`var` parameters | `field` | |

- Qualified names follow Kotlin/JVM conventions: types `pkg.Outer.Inner`; top-level members
  belong to the file facade `pkg.FileNameKt` (or the name given by `@file:JvmName`).
- Visibility: `private` → private, `protected` → protected, `internal` → package-level, default →
  public.

## Dependencies (Kotlin → anything)

Names are resolved in this order:

1. **Types**: enclosing classes and their nested types → explicit imports (including `as`
   aliases) → same package → star imports → Kotlin built-ins (`String`, `List`, …, shown as
   external `kotlin.*` types). Package-qualified names (`a.b.Type`) are resolved too.
2. **Constructor calls** `Type(...)` → `new` edge to the constructor (or to the type if it has
   no declared constructor).
3. **Members with a receiver** `x.name`: the receiver's type comes from
   - a parameter / local variable / lambda parameter declared `name: Type`,
   - a local `val x = Type(...)`,
   - a property or function with a declared (or simply inferred) type, including call results
     and chains such as `event.order.total()`,
   - `this` / `super`, a type name (static access, objects, companions, enum entries).
   Members are looked up in the type, its companion object and its supertypes, plus extension
   functions declared for the type. Kotlin property syntax on Java getters (`order.status` →
   `getStatus()`) is supported.
4. **Unknown receiver** (e.g. lambda `it`): the member is linked only if exactly one type used
   by the declaration (or, failing that, by the property types of its class) has a member with
   that name.
5. **Names without receiver** `name` / `name(...)`: members of the enclosing classes (incl.
   companions and supertypes) → top-level declarations of the same package → imported
   functions → star imports.
6. Supertypes: `extends` for a class with constructor call (`: Base()`), for interfaces
   extending interfaces and for targets known to be classes; `implements` otherwise.
7. Annotations are type uses. Labels (`return@forEach`), named arguments, string contents and
   keywords produce nothing. String template expressions (`"$x"`, `"${a.b}"`) are resolved.

## Dependencies (Java → Kotlin)

javac cannot see Kotlin code, so these references arrive as compile errors. They are matched by
name:

- A Java reference to a Kotlin type (`EmailNotifier`, `new EmailNotifier(...)`) links to the Kotlin
  type; `new` links to its constructor when it has exactly one constructor, else to the type.
- A Java member reference on a Kotlin type links to the Kotlin member when the name matches:
  `NotificationService.INSTANCE.register(...)` → `NotificationService.register(Notifier)`,
  `NotificationsKt.format(...)` → the top-level `format(...)`.
- The qualified name is taken from the Java file's imports, star imports or package.
- These references must **not** show up as `External › (unresolved)` entries such as
  `?NotificationService.INSTANCE`.

## Viewer

- New kinds `object` (icon "O") and `typealias` (icon "A") are shown like other types.
- Kotlin files get a "K" file icon; Java files keep "J".

## Out of scope / known limitations

- No type inference beyond the rules above (no generics resolution, no smart casts, no
  overload resolution: an overloaded name links to the first matching overload).
- Local functions and classes inside function bodies are not separate nodes; their references
  count for the enclosing declaration.
- `.kts` files, Kotlin Multiplatform source sets (`src/commonMain/kotlin`, …), and Java calls into
  Kotlin through generated names other than `INSTANCE`, `Companion` and file facades.
