---
id: DT-007
type: domain-term
title: Levelization
status: draft
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-010-viewer-reference]]"]
related: []
tags: [domain-term]
aliases: [levelize, level]
bounded-context: depviewer
agreed: false
stereotype:
parent: []
source-of-truth:
attributes: []
relationships: []
---

# Levelization

**Definition.** Ordering siblings so that each depends only on the siblings below it. Siblings in a cycle share a level.

**In context.** The viewer's default sort. Separator lines mark the levels.

**Distinguish from.** The `layers` [[DT-015-rule|rule]], which is the intended order. Levelization shows the actual one.
