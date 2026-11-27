---
id: QR-002
type: quality-requirement
title: Arcs never cover the labels
status: draft
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-015-spec-viewer-arc-geometry]]", "[[SRC-017-github-issue-1-viewer-arcs-cover-the-tree-labels-on-large-projects]]"]
related: []
tags: [quality-requirement]
dimensions: [usable]
priority: should
testable: true
---

# Arcs never cover the labels

**Q42 dimensions.** Usable.

Long arcs become tall, narrow half ellipses instead of sweeping across the tree labels.

**Scenario (Q42 quality scenario).**  <!-- follows [[Q42]], https://quality.arc42.org -->
- **Source.** An [[STK-001-software-architects|architect]].
- **Stimulus.** Looks at a large tree with long downward arcs.
- **Environment.** Any project size.
- **Response.** Every downward arc stays between the widest visible label and the anchor column.
- **Measure.** For every downward arc, the leftmost point lies at least 4 px right of the widest label's right edge, published as `document.body.dataset.labels`.

**Applies to.** [[FR-014-roll-up-dependencies-of-collapsed-rows]].
**Tensions.** None known.
