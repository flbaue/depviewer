---
id: CON-002
type: constraint
title: The viewer is one self-contained HTML file
status: accepted
created: 2026-11-27
updated: 2026-11-28
sources: ["[[SRC-004-guide-for-ai-coding-agents]]", "[[SRC-005-readme-of-depviewer]]", "[[SRC-016-spec-viewer-performance-on-large-projects]]"]
related: []
tags: [constraint]
category: technical
---

# The viewer is one self-contained HTML file

**Constraint.** The viewer is a single HTML file with inline CSS and vanilla JavaScript in one IIFE. No external scripts, fonts or CDNs. It works offline from `file://`.

**Rationale / origin.** The result must be shareable without setup: attached to a ticket, published as a CI artifact, opened offline ([[GOAL-005-the-real-structure-of-a-code-base-at-a-glance]]).

**Impact.** No frameworks or build step for the viewer. Rendering large models fast is done by hand ([[CC-007-virtualised-rendering-of-rows-and-arcs]]). Browser storage access stays wrapped in `try/catch`.
