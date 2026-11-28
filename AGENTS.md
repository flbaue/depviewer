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
| `src/depviewer/` | Scanner (Java 17, JDK only, package `depviewer`). `DepScan` = CLI, source discovery, orchestration, output; `Model` = nodes/edges/packages/externals/JSON writer shared by all languages; `JavaScanner` = javac Trees API (parses *and attributes*, exact); `KotlinScanner` = built-in lexer/parser, name-based resolution ([FR-004](spec/wiki/functional-requirements/FR-004-analyse-kotlin-by-name.md)); `Toolchain` = runs a language extractor and merges its JSON fragment; `Json` = minimal JSON reader; `Architecture` = loads and validates the architecture file for `check` and `--views` ([FR-026](spec/wiki/functional-requirements/FR-026-read-the-architecture-file.md)) matches `match` selectors against the model's nodes ([FR-027](spec/wiki/functional-requirements/FR-027-select-code-by-patterns.md)) and decides each element's group, with overlap warnings ([FR-028](spec/wiki/functional-requirements/FR-028-decide-each-elements-group-by-precedence.md)). The `depviewer` wrapper compiles it into `build/depviewer-classes` on first use. Without the wrapper: `javac -d out src/depviewer/*.java && java -cp out depviewer.DepScan …`. |
| `spec/` | **The spec and the backlog.** Requirements and architecture as a [systems42](https://systems42.org) wiki (req42 + arc42): goals, functional and quality requirements, constraints, building blocks, decisions (ADR 0001 is [DR-001](spec/wiki/decisions/DR-001-every-language-is-analysed-by-an-extractor-behind-one-versioned-contract.md)) and open issues. Start here for any change. Read `spec/AGENTS.md` before you work there; its rules apply only inside `spec/`. Skills: `capture`, `grill-requirements`, `implement`, `recover`, `requirements-wiki` (linked in `.claude/skills/`). The old `docs/specs/`, `docs/adr/` and GitHub issues are archived in `spec/raw/ingested/` ([ISS-014](spec/wiki/issues/ISS-014-does-this-wiki-replace-docs-specs-and-docs-adr.md)). |
| `viewer.html` | Viewer template. One self-contained HTML file with inline CSS and vanilla JS. The scanner replaces `/*DEPS_DATA*/null` with the model and `/*DEPS_VIEWS*/null` with exported views. |
| `depviewer` | POSIX shell wrapper that passes `--template` and runs the scanner. |
| `extractors/typescript/` | TypeScript/JavaScript extractor: `extract.mjs` on the TypeScript compiler API (Node). Pinned in `package-lock.json`. |
| `extractors/dart/` | Dart extractor: `bin/extract.dart` on `package:analyzer`. Pinned in `pubspec.yaml`/`pubspec.lock`. |
| `scripts/check.sh` | **The** check. Run it before you say you are done. |
| `test/fixture/`, `test/fixture-ts/`, `test/fixture-dart/` | Small projects with known dependencies per language (Java + Kotlin, TypeScript/JavaScript, Dart), each with a deliberate cycle and some unresolvable or external references. |
| `test/checks/` | Model assertions: `jvm.py`, `typescript.py`, `dart.py` (one per fixture) on top of `common.py`. Viewer assertions on DOMs dumped by headless Chrome: `viewer_arcs.py` (arc geometry), `viewer_perf.py` (timings on the large model). `SelectorCheck.java` checks selectors on the scanned fixture models ([FR-027](spec/wiki/functional-requirements/FR-027-select-code-by-patterns.md)); `AssignCheck.java` checks assignment precedence and overlaps on the fixture with `test/fixture-architecture.json` ([FR-028](spec/wiki/functional-requirements/FR-028-decide-each-elements-group-by-precedence.md)). Both are compiled by `check.sh` only. `viewer_contrast.py` checks the text contrast of `viewer.html` in light and dark ([QR-008](spec/wiki/quality-requirements/QR-008-the-viewer-reads-well-in-light-dark-and-on-a-narrow-screen.md)). |
| `test/gen_large.py` | Generates a large synthetic model (~10,000 rows, ~38,000 arcs at member level) embedded in the viewer, for the performance check ([QR-001](spec/wiki/quality-requirements/QR-001-the-viewer-stays-responsive-on-large-projects.md)). |
| `test/fixture-views.json` | A "Layers" view used by the headless browser smoke test and the screenshots. |
| `scripts/screenshots.sh` | Regenerates `docs/screenshot-{light,dark}.png` for the README. Run it after visible UI changes and look at the result. |

## Workflow: spec first, then test-driven

The maintainer wants every feature and behaviour change done this way:

1. **Spec first.** Write or update the requirements in the wiki under `spec/` before writing code
   (a new idea goes into `spec/raw/` and is captured with the `capture` skill): goal, constraints,
   the expected behaviour as checkable acceptance criteria, and what is out of scope. Small bug
   fixes can skip the spec but not the test.
2. **Tests next, and watch them fail.** Turn the acceptance criteria into checks — fixture code
   plus assertions in `test/checks/<fixture>.py` for the scanner, `scripts/check.sh` (headless
   smoke test) for the viewer. Run them and confirm they fail for the right reason.
3. **Implement until green**, then run the whole `scripts/check.sh`.
4. **Check for regressions on real code**: rescan a larger project with the previous scanner
   version (`git archive HEAD src | tar -x -C <tmp>`, compile it there) and the new one and compare
   the JSON (ignoring `generated`); differences must
   be intended.
5. Update the wiki pages (`delivery`, `release`, child requirements), the user docs (`docs/usage.md`, `languages.md`, `viewer.md`,
   `quick-start.md`; the README only for the overview) and this file if behaviour or invariants
   changed.
6. **Review in a fresh agent session** before you open the PR or say you are done. Start a new
   session, or a subagent that does not inherit your conversation (not a fork), and give it only
   the diff against `main` (`git diff main...HEAD`), the requirement pages it implements and this
   file. Ask it for correctness bugs, broken invariants (below), spec criteria the change does not
   meet, missing or weakened tests, and docs left out of date — not for style. The reviewer reads
   the code itself; do not hand it your reasoning or your conclusions. Then deal with every finding:
   fix it (with a test where it is a bug) or note why not. Run `scripts/check.sh` again after
   fixes, and review again in a new fresh session if the fixes changed behaviour. Without a way to
   start a fresh session, say so in the PR instead of reviewing your own work and calling it
   independent. This also applies when you work through the `implement` skill: do the review
   before its PR step.

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

When you change what the scanner emits on purpose, update `test/checks/` in the same commit.
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
  `file`, `class|interface|enum|record|annotation|object|typealias|namespace|mixin|extension`,
  `ctor|method|field|enumconst` (top-level functions/variables of Kotlin, TypeScript and Dart are
  direct children of their `file`); folder-based packages (TypeScript, Dart) carry `"sep": "/"`,
  `ext-root`, `ext-package`, `ext-type`. Optional fields are omitted when empty/default.
- **Parents always come before children** in `nodes`; the viewer relies on it.
- Edges go from the innermost declaring element (member, or type for `extends`/annotations) to the
  most specific target that exists in the model (member → type → external top-level type).
  Imports, `this`/`super`, local variables and compiler-generated code produce no edges.
- Keep the JSON compact; real projects have hundreds of thousands of edges.

## Adding a language

Read the accepted decision [DR-001](spec/wiki/decisions/DR-001-every-language-is-analysed-by-an-extractor-behind-one-versioned-contract.md),
[CC-009](spec/wiki/crosscutting-concepts/CC-009-fragment-format.md) (fragment format) and
[CC-002](spec/wiki/crosscutting-concepts/CC-002-extractor-contract.md) (contract) first (every language becomes an extractor run once per scan,
using the user's toolchain first; its migration plan is in progress), then follow the workflow above:

1. Requirements in `spec/`: refine the language's FR (e.g. [FR-053](spec/wiki/functional-requirements/FR-053-analyse-go.md))
   into child requirements for toolchain, sources, model mapping, dependency rules and fixture
   expectations, as [FR-006](spec/wiki/functional-requirements/FR-006-analyse-dart-with-the-dart-analyzer.md) does for Dart.
2. Fixture `test/fixture-<language>/` and `test/checks/<language>.py`; add both to
   `scripts/check.sh` (skip with a note when the toolchain is missing) and the toolchain to the CI
   workflow. Watch the checks fail.
3. Extractor in `extractors/<language>/`, written against the language's official tooling, with
   pinned dependencies, printing the fragment format; discovery and a `Toolchain` command in
   `src/depviewer/`. File lists and test flags are computed in Java and passed to the extractor.
4. Read the extractor's full output on the fixture for wrong dependencies the checks do not
   cover, and compare a Java-only project against the previous version (must be identical).

When the API of a toolchain library is new to you, read its installed sources (e.g. in the pub
cache or `node_modules`) instead of writing from memory - the Dart analyzer API in particular
changes between major versions.

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
- `document.body.dataset.rows/arcs/view/labels` (and `bench` with `?bench`) are read by the smoke
  test; keep them. `rows`/`arcs` count the whole tree, not what is in the DOM.
- Rows and arcs are virtualised ([CC-007](spec/wiki/crosscutting-concepts/CC-007-virtualised-rendering-of-rows-and-arcs.md)): only rows `win.lo..win.hi`
  are in `#rows`. Never map DOM children to `rows` by index; use `data-r` / `rowEl(r)`, and expect
  `rowEl` to return `null` for rows outside the window.

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
  tried by hand in the browser and what you did not), and the fresh-session review: each finding
  and whether it was fixed or why not.
