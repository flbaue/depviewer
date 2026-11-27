---
id: CC-008
type: crosscutting-concept
title: Themes and browser storage in the viewer
status: draft
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-004-guide-for-ai-coding-agents]]", "[[SRC-008-usage-reference]]"]
related: []
tags: [crosscutting-concept]
category: user-experience
applies-to: ["[[BB-013-viewer]]"]
drivers: ["[[QR-008-the-viewer-reads-well-in-light-dark-and-on-a-narrow-screen]]", "[[CON-002-the-viewer-is-one-self-contained-html-file]]"]
decisions: []
state: current
superseded-by:
---

# Themes and browser storage in the viewer

> follows [[arc42]] (section 8)

## Concept


- Colours are CSS variables on `:root` with light and dark variants. A new colour goes into all three blocks.
- Browser storage access is wrapped in `try/catch`, because it throws in some contexts such as sandboxed previews.
- Views are stored per project title, so `--title` decides which saved views a file opens with.
