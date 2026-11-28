---
id: BB-013
type: building-block
title: Viewer
status: accepted
created: 2026-11-27
updated: 2026-11-28
sources: ["[[SRC-004-guide-for-ai-coding-agents]]", "[[SRC-010-viewer-reference]]", "[[SRC-016-spec-viewer-performance-on-large-projects]]"]
related: []
tags: [building-block]
parent: "[[CTX-001-depviewer-context]]"
drivers: ["[[FR-013-explore-the-dependency-structure]]", "[[FR-021-draw-the-architecture-as-views-and-groups]]", "[[FR-040-show-rule-violations-in-the-viewer]]", "[[CON-002-the-viewer-is-one-self-contained-html-file]]", "[[QR-001-the-viewer-stays-responsive-on-large-projects]]", "[[QR-002-arcs-never-cover-the-labels]]"]
decisions: []
uses: []
location: viewer.html
state: current
superseded-by:
---

# Viewer

> follows [[arc42]] (section 5, black box)

## Purpose / Responsibility

`viewer.html`: one self-contained HTML file with inline CSS and vanilla JS that renders the model as tree and arcs, levelizes, highlights cycles and edits views and groups. The scanner replaces `/*DEPS_DATA*/null` and `/*DEPS_VIEWS*/null`.

## Interfaces

- **Provides:** the UI of [[FR-013-explore-the-dependency-structure]], [[FR-021-draw-the-architecture-as-views-and-groups]], [[FR-040-show-rule-violations-in-the-viewer]], and `document.body.dataset.rows/arcs/view/labels/bench` for the smoke test.
- **Needs:** nothing at runtime. Browser storage for views.
