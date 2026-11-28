---
id: DN-003
type: deployment-node
title: Node.js process
status: accepted
created: 2026-11-27
updated: 2026-11-28
sources: ["[[SRC-013-spec-typescript-and-javascript-support]]"]
related: []
tags: [deployment-node]
stereotype: container
parent: "[[DN-001-developer-machine]]"
hosts: ["[[BB-009-typescript-extractor]]"]
connects: []
technology: "Node.js 18+, typescript from the project or pinned"
drivers: []
decisions: []
state: current
superseded-by:
---

# Node.js process

> follows [[arc42]] (section 7)

## Purpose

Runs the TypeScript extractor, one process per module today.
