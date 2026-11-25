# Guide for AI coding agents

This file is for AI agents (Claude Code, Codex, Copilot, Cursor, …) and for humans who want the
short version. Read it fully before changing anything. `CLAUDE.md` just imports this file.

## What this is

depviewer shows the dependencies of a Java code base as arcs next to a collapsible
module › package › file › type › member tree, levelizes siblings, highlights cycles and lets users
define their own groupings (views). It is deliberately small: **no build tool, no dependencies,
no frameworks** — a JDK 17+ and a browser are all a user needs.

| File | Role |
|---|---|
| `src/depviewer/` | Scanner (Java 17, JDK only, package `depviewer`). `DepScan` = CLI, source discovery, orchestration, output; `Model` = nodes/edges/packages/externals/JSON writer shared by all languages; `JavaScanner` = javac Trees API (parses *and attributes*, exact); `KotlinScanner` = built-in lexer/parser, name-based resolution (`docs/specs/kotlin-support.md`). The `depviewer` wrapper compiles it into `build/depviewer-classes` on first use. Without the wrapper: `javac -d out src/depviewer/*.java && java -cp out depviewer.DepScan …`. |
| `docs/specs/` | Feature specs (behaviour, constraints, out of scope). Start here for any change. |
| `viewer.html` | Viewer template. One self-contained HTML file with inline CSS and vanilla JS. The scanner replaces `/*DEPS_DATA*/null` with the model and `/*DEPS_VIEWS*/null` with exported views. |
| `depviewer` | POSIX shell wrapper that passes `--template` and runs the scanner. |
| `scripts/check.sh` | **The** check. Run it before you say you are done. |
| `test/fixture/` | Small Java project with known dependencies (incl. a deliberate package cycle and an unresolvable test import). |
| `test/check_model.py` | Assertions about the model scanned from the fixture. |
| `test/fixture-views.json` | A "Layers" view used by the headless browser smoke test and the screenshots. |
| `scripts/screenshots.sh` | Regenerates `docs/screenshot-{light,dark}.png` for the README. Run it after visible UI changes and look at the result. |

## Workflow: spec first, then test-driven

The maintainer wants every feature and behaviour change done this way:

1. **Spec first.** Write or update a spec in `docs/specs/<feature>.md` before writing code: goal,
   constraints, the expected behaviour as checkable statements, and what is out of scope.
   Small bug fixes can skip the spec but not the test.
2. **Tests next, and watch them fail.** Turn the spec's statements into checks — fixture code
   plus assertions in `test/check_model.py` for the scanner, `scripts/check.sh` (headless
   smoke test) for the viewer. Run them and confirm they fail for the right reason.
3. **Implement until green**, then run the whole `scripts/check.sh`.
4. **Check for regressions on real code**: rescan a larger project with the previous scanner
   version (`git archive HEAD src | tar -x -C <tmp>`, compile it there) and the new one and compare
   the JSON (ignoring `generated`); differences must
   be intended.
5. Update the spec's status, the README and this file if behaviour or invariants changed.

If a test fails because the test itself was wrong, fix the test and say so in the commit — do
not weaken a test to make an implementation pass.

## How to verify your change

```sh
scripts/check.sh
```

It compiles the scanner with `-Xlint:all -Werror`, scans the fixture, checks the model, checks the
generated HTML, syntax-checks the viewer script (if `node` exists) and loads the generated viewer in
headless Chrome to confirm it renders rows, arcs and the embedded view (if Chrome exists). CI runs
everything on every push and pull request (`.github/workflows/check.yml`), with nothing skipped.

The automated checks do not click anything. **For any change to the viewer's behaviour or looks,
also open `build/fixture.html` in a browser and try the feature yourself** — if you can drive a
browser, do it; otherwise say plainly in your summary that the UI was not tried. Check both light
and dark mode and a narrow (phone) width if you touched layout or CSS.

When you change what the scanner emits on purpose, update `test/check_model.py` in the same commit.
When you fix a scanner bug, add the case to the fixture and a check that fails without your fix
(`check.sh` must have failed before the fix; confirm it).

## Data model (scanner → viewer contract)

```jsonc
{
  "title": "…", "generated": "yyyy-MM-dd HH:mm", "errors": 0,
  "edgeKinds": ["uses", "extends", "implements", "call", "new", "access"],
  "nodes": [ { "n": "name", "k": "kind", "p": parentIndex, "l": line, "loc": lines,
               "t": 1 /*test*/, "s": 1 /*static*/, "v": "+|#|-|~", "q": "qualified.name", "path": "src/…/X.java" } ],
  "edges": [ [fromIndex, toIndex, kindIndex, count, [line, …]] ]
}
```

- Node kinds: `module`, `package` (one node per name segment; the viewer merges chains),
  `file`, `class|interface|enum|record|annotation|object|typealias`, `ctor|method|field|enumconst`
  (Kotlin top-level functions/properties are direct children of their `file`),
  `ext-root`, `ext-package`, `ext-type`. Optional fields are omitted when empty/default.
- **Parents always come before children** in `nodes`; the viewer relies on it.
- Edges go from the innermost declaring element (member, or type for `extends`/annotations) to the
  most specific target that exists in the model (member → type → external top-level type).
  Imports, `this`/`super`, local variables and compiler-generated code produce no edges.
- Keep the JSON compact; real projects have hundreds of thousands of edges.

## Invariants that are easy to break

- **Persisted view keys.** User views (localStorage, exported JSON, `--views`) assign elements by
  key: `R:<module>`, `P:<module>:<package>`, `F:<module>:<path>`, `X`, `XP:<package>`,
  `XT:<qualified type>`. Changing how these are built silently breaks everyone's saved views. If
  you must, add a migration in `loadViews()`.
- Exported view format: `{ "current"?: id, "views": [ { id, name, groups: [{id, name, color, parent?: groupId}], assign: { key: groupId | "" } } ] }`;
  `""` means "explicitly ungrouped" (overrides an assigned ancestor). `parent` nests a group; a
  missing parent or a parent loop (hand-edited files) must degrade to a top-level group, never hang
  or crash — `inGroup()` guards the walks.
- The two template markers must stay in `viewer.html` exactly once each (`check.sh` enforces it).
- Everything inserted with `innerHTML` goes through `esc()`. Names come from scanned code and
  user input — never interpolate them raw. The scanner's `js()` escapes `<`, `>` and `&` so the
  data cannot close the `<script>` element.
- `document.body.dataset.rows/arcs/view` are read by the smoke test; keep them.

## Conventions

- Scanner: Java 17 language level, standard library only, one file per language plus the shared
  core (`src/depviewer/`). Keep `javac -Xlint:all` warning-free.
- Viewer: vanilla JS in one IIFE, no external scripts, fonts or CDNs (it must work offline from
  `file://`). Colours are CSS variables on `:root` with light and dark variants — add new colours
  to all three blocks. Browser storage access stays wrapped in `try/catch` (it throws in some
  contexts, e.g. sandboxed previews).
- Prefer small, focused changes; match the surrounding style; no new tooling or dependencies
  without asking the maintainer first.
- Do not commit generated output (`build/`, `deps.html`, `*.json` from scans) or data scanned from
  projects you do not own — the model contains their class and method names.

## Commits and pull requests

- One logical change per commit, imperative subject line, body explains *why*.
- Disclose AI assistance with a trailer, e.g. `Co-Authored-By: <Model name> <noreply@…>` or
  `Assisted-by: <tool>` — see CONTRIBUTING.md.
- PR description: what changed, why, how it was verified (paste the `check.sh` result; say what you
  tried by hand in the browser and what you did not).
