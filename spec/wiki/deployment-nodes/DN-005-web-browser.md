---
id: DN-005
type: deployment-node
title: Web browser
status: accepted
created: 2026-11-27
updated: 2026-11-28
sources: ["[[SRC-008-usage-reference]]", "[[SRC-010-viewer-reference]]"]
related: []
tags: [deployment-node]
stereotype: container
parent: "[[DN-001-developer-machine]]"
hosts: ["[[BB-013-viewer]]"]
connects: []
technology: "any current browser, light and dark mode"
drivers: ["[[CON-002-the-viewer-is-one-self-contained-html-file]]"]
decisions: []
state: current
superseded-by:
---

# Web browser

> follows [[arc42]] (section 7)

## Purpose

Shows the viewer from `file://` or HTTP and keeps views in localStorage.
