---
id: DN-004
type: deployment-node
title: Dart process
status: draft
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-014-spec-dart-support]]"]
related: []
tags: [deployment-node]
stereotype: container
parent: "[[DN-001-developer-machine]]"
hosts: ["[[BB-010-dart-extractor]]"]
connects: []
technology: "Dart SDK 3.x, pinned analyzer"
drivers: []
decisions: []
state: current
superseded-by:
---

# Dart process

> follows [[arc42]] (section 7)

## Purpose

Runs the Dart extractor, one process per scan.
