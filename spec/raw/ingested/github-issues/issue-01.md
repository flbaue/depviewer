# #1 Viewer: arcs cover the tree labels on large projects

https://github.com/flbaue/depviewer/issues/1
State on export (2026-11-27): closed, opened 2026-11-25, closed 2026-11-25

On large projects, the long downward arcs (left side of the arc column) run across the tree labels, so names in the middle of the list become hard to read.

Seen on a real Flutter workspace (~650 Dart files, ~57k distinct dependencies) at package and file level: hundreds of arcs between distant rows sweep over the label area.

**Idea:** keep arcs inside the space between the widest label and the arc column (cap the horizontal radius of left-side arcs, like the right side is capped by the viewport), accepting taller/narrower ellipses. Alternatives: draw labels on an opaque background above the arcs, or fade arcs that are not related to the hovered/selected row more strongly when there are many.

**Done when:** labels stay readable with thousands of arcs; the fixture screenshots still look as before; checked by eye in light and dark mode.
