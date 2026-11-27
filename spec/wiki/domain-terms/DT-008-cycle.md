---
id: DT-008
type: domain-term
title: Cycle
status: draft
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-010-viewer-reference]]", "[[SRC-002-spec-architecture-file-and-rule-checks]]"]
related: []
tags: [domain-term]
aliases: []
bounded-context: depviewer
agreed: false
stereotype:
parent: []
source-of-truth:
attributes: []
relationships: []
---

# Cycle

**Definition.** A set of elements that depend on each other directly or indirectly.

**In context.** Highlighted in the viewer with red arcs and a red bar on each row. The `no-cycles` rule reports one violation per cycle.

**Distinguish from.** An upward dependency that is not part of a cycle, shown blue.
