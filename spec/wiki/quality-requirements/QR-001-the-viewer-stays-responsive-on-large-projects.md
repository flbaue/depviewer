---
id: QR-001
type: quality-requirement
title: The viewer stays responsive on large projects
status: accepted
created: 2026-11-27
updated: 2026-11-28
sources: ["[[SRC-016-spec-viewer-performance-on-large-projects]]", "[[SRC-018-github-issue-2-viewer-slow-to-expand-and-re-sort-with-tens-of-thousands-of-arcs]]"]
related: []
tags: [quality-requirement]
dimensions: [efficient, usable]
priority: must
testable: true
---

# The viewer stays responsive on large projects

**Q42 dimensions.** Efficient, usable.

Expanding, sorting, hovering and scrolling stay fast with tens of thousands of dependencies.

**Scenario (Q42 quality scenario).**  <!-- follows [[Q42]], https://quality.arc42.org -->
- **Source.** An [[STK-001-software-architects|architect]] exploring a large project.
- **Stimulus.** Shows Members, sorts A–Z or Levelize, hovers a row, scrolls.
- **Environment.** The synthetic model of `test/gen_large.py` (660 files, 2,940 types, 8,820 members, 57,000 dependencies, about 10,000 rows and 38,000 arcs at member level) in headless Chrome, window 1400×1000.
- **Response.** The tree and arcs update.
- **Measure.** Members, A–Z and Levelize each take less than 200 ms on a developer machine, and `scripts/check.sh` fails above 500 ms. Hover and scroll take about 50 ms, failing above 150 ms. At member level fewer than 1,000 rows and fewer than 5,000 arcs are in the DOM.

**Applies to.** [[FR-013-explore-the-dependency-structure]] and its children.
**Tensions.** [[CON-002-the-viewer-is-one-self-contained-html-file]]: no framework or build step to help.
