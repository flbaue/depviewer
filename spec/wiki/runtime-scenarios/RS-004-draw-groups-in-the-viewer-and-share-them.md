---
id: RS-004
type: runtime-scenario
title: Draw groups in the viewer and share them
status: accepted
created: 2026-11-27
updated: 2026-11-28
sources: ["[[SRC-010-viewer-reference]]", "[[SRC-008-usage-reference]]"]
related: []
tags: [runtime-scenario]
steps:
  - {from: "[[STK-001-software-architects]]", to: "[[BB-013-viewer]]", message: "open out.html, create a view and groups"}
  - {from: "[[STK-001-software-architects]]", to: "[[BB-013-viewer]]", message: drag packages onto groups}
  - {from: "[[BB-013-viewer]]", to: "[[DN-005-web-browser]]", message: store views in localStorage per title}
  - {from: "[[STK-001-software-architects]]", to: "[[BB-013-viewer]]", message: Export views}
  - {from: "[[STK-001-software-architects]]", to: "[[BB-001-depviewer-wrapper-script]]", message: "./depviewer --views views.json -o out.html <dirs>"}
realizes: ["[[FR-021-draw-the-architecture-as-views-and-groups]]", "[[FR-024-share-views-as-a-file]]"]
decisions: []
state: current
superseded-by:
---

# Draw groups in the viewer and share them

> follows [[arc42]] (section 6)

## Notes

After the rescan everyone who opens the file sees the same views, also in a fresh browser.
