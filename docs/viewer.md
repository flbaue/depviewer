# The viewer

Reference for the viewer's features. For a guided first tour see the
[quick start](quick-start.md#4-read-the-picture).

## Tree and arcs

- The tree shows module › package › file › type › method/field. Package chains with a single
  child are merged (`com.example.domain`).
- Arcs connect the **visible** rows. A collapsed node rolls up the dependencies of everything
  inside it, so you can look at the same code at package, file, type or member level.
- Downward dependencies arc on the left (green), upward ones on the right (blue). Upward
  dependencies that close a cycle are red, and every row that is part of a cycle gets a red bar.
  Thicker arcs stand for more references.
- Arcs never cover the labels: long arcs become tall, narrow ellipses instead.

## Sorting and levels

- **Levelize** (default) orders siblings so that each one only depends on rows below it.
  Separator lines mark the levels. Siblings in a cycle share a level.
- **A–Z**, **Lines** (lines of code) and **Elements** (number of types and members) are the
  other orders.
- **Show: Packages · Files · Types · Members** expands the whole tree to that level; `+`/`−` or a
  double-click opens or closes a single row.

## Details and navigation

- Hover a row or an arc to highlight it and the rows it connects.
- Click a row for its details: kind, location, size, level, and everything it depends on and is
  used by, grouped by the visible row on the other side. Click an arc for the individual
  references (kind and `file:line`). Click a reference to jump to its element.
- **Find…** highlights matching rows; **Enter** / **Shift+Enter** jump to the next/previous match
  and expand the tree as needed. **Esc** clears the selection.
- Toggles: **Tests**, **External** (JDK and library types; external types nothing visible uses are
  hidden) and **Parent deps** (members using their own class).

## Views and groups

A view regroups the code into your own structure — layers, bounded contexts, features — without
changing it. Each view is an independent set of groups; keep as many as you like.

- **+ Group** creates an empty group, and a view if none is selected. **+ Sub** on a group row
  creates a nested group; you can also drag one group onto another, or pick a parent in the
  group's details.
- Drag modules, packages or files onto a group row, or pick the group in the details panel of a
  selected element. A package takes everything inside it; a more specific assignment wins. Types
  and members move with their file. Dragging an element (or a group) onto an ungrouped row moves
  it back out.
- Collapse groups to see, and levelize, the dependencies between them: every blue or red arc
  between groups goes against the order you defined. **Only groups** hides what is not assigned.
- The **⋯** menu creates, duplicates, renames, deletes, exports and imports views.

Views are stored in the browser (localStorage, per project title). *⋯ › Export views* saves them as
JSON, which can be imported again or embedded when scanning with `depviewer --views views.json …`,
so a team can share one agreed architecture next to the code.

## Architecture files

Scanned with `--views architecture.json` (or `check --out`), a view with `match` patterns and
`rules` shows what `depviewer check` sees ([usage](usage.md#checking-the-architecture)):

- An element placed by a pattern says **by pattern** next to its group in the details. Dragging it
  elsewhere, also onto an ungrouped row, is a manual assignment and says **manual (overrides
  pattern)**. Its **✕** removes the manual assignment, and the pattern applies again. The viewer
  does not edit patterns or rules, and places a type matched on its own with its file.
- An arc's details list the rule violations on it, with rule, severity and **exact** or **by name**
  (Kotlin). An arc with an error is drawn in the violation colour even when it points downward.
- *Export views* writes `version`, `match` and `rules` back unchanged, with your manual assignments
  and without what the scanner resolved. Patterns, rules and violations always come from the
  embedded file: browser storage only keeps your manual changes.

## Large projects

Only the rows around the visible part of the tree, and the arcs that touch them, are drawn, so
expanding, sorting and scrolling stay fast with tens of thousands of dependencies
([spec](../spec/wiki/quality-requirements/QR-001-the-viewer-stays-responsive-on-large-projects.md)).
