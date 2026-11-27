# Viewer: arc geometry

Status: implemented (issue #1)

## Goal

Arcs must not cover the tree labels. On large projects, long downward arcs (drawn to the left
of the anchor column) used to sweep across the label area, making names in the middle of the list
hard to read.

## Behaviour

- Every row's anchor sits in one column at `x = ax`. Upward arcs bulge to the right of it,
  downward arcs to the left. An arc between rows `r1` and `r2` is half an ellipse with vertical
  radius `|y2 - y1| / 2`.
- The horizontal radius is `min(vertical radius, cap)`. On the right, the cap is the space up to
  the viewport edge (unchanged). On the left, the cap is the space between the **widest label** of
  the visible rows and the anchor column, minus a small margin. Long arcs therefore become tall,
  narrow ellipses instead of reaching into the labels.
- Checkable statement: for every downward arc, the leftmost point of the rendered ellipse lies at
  least 4px to the right of the widest label's right edge. The viewer publishes that edge as
  `document.body.dataset.labels` (pixels, rounded up) for the smoke test.
- Short arcs (vertical radius below the cap) look exactly as before.

## Out of scope

- Fading unrelated arcs more strongly on large projects, or drawing labels on an opaque background
  (alternatives listed in the issue; not needed once arcs stay out of the label area).
- Performance of drawing many arcs (issue #2).
