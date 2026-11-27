---
id: ISS-012
type: issue
title: "What makes the viewer readable on a narrow screen?"
status: open
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
resolved: null
---

# What makes the viewer readable on a narrow screen?

**What's unresolved.** AGENTS.md asks to check light, dark and phone width, but gives no measure.

**Affects.** [[QR-008-the-viewer-reads-well-in-light-dark-and-on-a-narrow-screen]].

**Context / evidence.** [[SRC-004-guide-for-ai-coding-agents]], section "How to verify your change".

**Options.**
1. Reflow at 320 CSS pixels without sideways scrolling outside the tree.
2. Keep it a manual check and lower [[QR-008-the-viewer-reads-well-in-light-dark-and-on-a-narrow-screen]] to could.

**Resolution.**
