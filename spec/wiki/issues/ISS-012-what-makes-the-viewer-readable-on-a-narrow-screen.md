---
id: ISS-012
type: issue
title: "What makes the viewer readable on a narrow screen?"
status: resolved
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-004-guide-for-ai-coding-agents]]"]
related: ["[[QR-008-the-viewer-reads-well-in-light-dark-and-on-a-narrow-screen]]"]
tags: [issue]
severity: minor
kind: gap
category: requirements
raised-by: agent
owner: "[[STK-004-maintainer]]"
resolved: 2026-11-27
---

# What makes the viewer readable on a narrow screen?

**What's unresolved.** AGENTS.md asks to check light, dark and phone width, but gives no measure.

**Affects.** [[QR-008-the-viewer-reads-well-in-light-dark-and-on-a-narrow-screen]].

**Context / evidence.** [[SRC-004-guide-for-ai-coding-agents]], section "How to verify your change".

**Options.**
1. Reflow at 320 CSS pixels without sideways scrolling outside the tree.
2. Keep it a manual check and lower [[QR-008-the-viewer-reads-well-in-light-dark-and-on-a-narrow-screen]] to could.

**Resolution.** Option 1 plus a contrast measure, decided by the maintainer on 2026-11-27: at 320 CSS pixels no sideways scrolling outside the tree, and text contrast of at least 4.5:1 in light and dark. Applied in [[QR-008-the-viewer-reads-well-in-light-dark-and-on-a-narrow-screen]].
