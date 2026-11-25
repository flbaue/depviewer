# depviewer

A small dependency explorer for Java projects: browse the package/file/class/member tree and see
the dependencies between them as arcs, levelized and with cycles highlighted.

> **AI notice:** this project was written almost entirely by an AI coding agent
> ([Claude Code](https://claude.com/claude-code)) under the direction of its human maintainer.
> See [How it was built](#how-it-was-built) below. Review it like any third-party code before you
> rely on it.

```
./depviewer [-o deps.html] [--no-tests] [--cp <classpath>] <project-dir>...
open deps.html
```

To try it without a project of your own, run `scripts/check.sh` and open `build/fixture.html`.
That's a small example with a deliberate package cycle and a "Layers" view.

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

## How it was built

depviewer was created in a few conversational sessions with Claude Code, Anthropic's AI coding
agent. The maintainer supplied the idea, a screenshot of the kind of dependency view they wanted
from a commercial architecture tool, and a sample DDD project to test against. They set the
direction and scope of each step and reviewed the results. The agent wrote the code and the docs.

Roughly how it went:

1. **Design.** The agent chose to resolve dependencies with the JDK's own compiler API rather than
   parse Java by hand, so overloads, inherited members and imports resolve exactly. The output is
   a single self-contained HTML file, so nothing needs installing beyond a JDK.
2. **Build and verify.** After each step the agent ran the scanner on the sample project, checked
   the extracted dependencies, and opened the viewer in a browser to look at the result and click
   through it. Bugs it found that way (e.g. implicit `super()` calls showing up as dependencies,
   cycle colouring, package-name compaction inside groups) were fixed before moving on.
3. **Iterate.** Features such as groups and views were added on request in later sessions,
   following the same loop.
4. **Guard rails.** `scripts/check.sh`, the fixture project and CI were added so that changes
   (by humans or agents) are checked automatically. [AGENTS.md](AGENTS.md) documents the structure
   and invariants for future AI-assisted work.

What this means for you: the code has been exercised on real and synthetic projects, but it has
not been audited line by line, and the viewer's interactive parts are tested by hand rather than
by automated UI tests. Issues and fixes are very welcome.

## Contributing

Contributions are welcome, from humans and AI-assisted alike. Bug reports with a small Java
snippet that reproduces a wrong or missing dependency are especially useful. See
[CONTRIBUTING.md](CONTRIBUTING.md) for how to propose changes, and [AGENTS.md](AGENTS.md) for the
technical guide (written for AI agents, useful for everyone). Run `scripts/check.sh` before
opening a pull request.

## License

MIT, see [LICENSE](LICENSE).
