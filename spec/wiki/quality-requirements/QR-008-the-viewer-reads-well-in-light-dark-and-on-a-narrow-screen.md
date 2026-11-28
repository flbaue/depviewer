---
id: QR-008
type: quality-requirement
title: "The viewer reads well in light, dark and on a narrow screen"
status: accepted
created: 2026-11-27
updated: 2026-11-28
sources: ["[[SRC-004-guide-for-ai-coding-agents]]"]
related: ["[[ISS-012-what-makes-the-viewer-readable-on-a-narrow-screen]]"]
tags: [quality-requirement]
dimensions: [usable]
priority: should
testable: true
---

# The viewer reads well in light, dark and on a narrow screen

**Q42 dimensions.** Usable.

Changes to layout or CSS are checked in light mode, dark mode and at phone width.

**Scenario (Q42 quality scenario).**  <!-- follows [[Q42]], https://quality.arc42.org -->
- **Source.** A reader.
- **Stimulus.** Opens the viewer.
- **Environment.** Light or dark system theme, desktop or phone width.
- **Response.** Colours and layout adapt.
- **Measure.** At 320 CSS pixels wide the page needs no sideways scrolling outside the tree, and text reaches a contrast of at least 4.5:1 in light and dark (WCAG AA) ([[ISS-012-what-makes-the-viewer-readable-on-a-narrow-screen]]).

**Applies to.** [[BB-013-viewer]].
**Tensions.** None known.
