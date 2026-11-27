---
id: CC-007
type: crosscutting-concept
title: Virtualised rendering of rows and arcs
status: draft
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-016-spec-viewer-performance-on-large-projects]]", "[[SRC-004-guide-for-ai-coding-agents]]"]
related: []
tags: [crosscutting-concept]
category: under-the-hood
applies-to: ["[[BB-013-viewer]]"]
drivers: ["[[QR-001-the-viewer-stays-responsive-on-large-projects]]", "[[CON-002-the-viewer-is-one-self-contained-html-file]]"]
decisions: []
state: current
superseded-by:
---

# Virtualised rendering of rows and arcs

> follows [[arc42]] (section 8)

## Concept


- The whole tree is laid out (`top = index × rowHeight`), but only rows in a window of several screens around the visible part are in the DOM.
- Arcs are computed for the whole tree. Only arcs overlapping the window are drawn: with an end in the window on their own, passing through bundled per colour and stroke width.
- Never map DOM children to rows by index: use `data-r` and `rowEl(r)`, which returns `null` outside the window.
- `document.body.dataset.rows/arcs` count the whole tree. `?bench` publishes timings for [[QR-001-the-viewer-stays-responsive-on-large-projects]].
