# depviewer

A small dependency explorer for Java projects: browse the package/file/class/member tree and see
the dependencies between them as arcs, levelized and with cycles highlighted.

```
./depviewer [-o deps.html] [--no-tests] [--cp <classpath>] <project-dir>...
open deps.html
```

- **DepScan.java** parses and attributes the sources with the JDK compiler (`javac` Trees API), so
  references are resolved exactly (overloads, inherited members, static imports…). It finds
  `src/main/java` / `src/test/java` folders (Maven/Gradle layout) and names modules after their
  `artifactId`. Needs JDK 17+, no other dependencies.
- **viewer.html** is the template; the scanner embeds the data and writes one self-contained HTML
  file. `-o deps.json` writes data only; open it in the viewer via *Open…*, drag & drop, or
  `viewer.html?data=deps.json`.

## Viewer

- Tree of module › package › file › type › method/field, with dependency arcs between the visible
  rows. Collapsed nodes aggregate the dependencies of everything inside them.
- **Levelize** sorts siblings so every element only depends on elements below it; separator lines
  mark levels. Downward deps arc on the left, upward deps on the right; upward deps that close a
  cycle are red, and cycle members get a red bar.
- Hover a row/arc to highlight it; click for the individual references (kind + line numbers);
  click a reference to jump to its source element.
- **Views & groups**: *+ Group* creates an empty group (and a view if none is selected). Drag
  modules, packages or files onto a group row — or pick the group in the details panel — to
  assign them; a package takes everything inside it, a more specific assignment wins, and dragging
  onto an ungrouped row removes it again. Collapse groups to see (and levelize) the dependencies
  between them. Each view is an independent set of groups, so you can keep one for layers, one
  for bounded contexts, one for patterns…
  Views are stored in the browser (localStorage, per project title); *⋯ › Export views* saves them
  as JSON, which you can import again or embed with `depviewer --views views.json …`.
- Toggles for tests, external (JDK/library) types and member→own-class dependencies.

Library types resolve when their jars are on the classpath: `target/dependency/*.jar`, `lib/` and
`libs/` are picked up automatically (`mvn dependency:copy-dependencies`), otherwise pass `--cp`.
Unresolvable references are grouped under *External › (unresolved)*.
