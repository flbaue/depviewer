---
id: DT-001
type: domain-term
title: Model
status: draft
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-004-guide-for-ai-coding-agents]]", "[[SRC-008-usage-reference]]"]
related: []
tags: [domain-term]
aliases: [scan model, deps.json]
bounded-context: depviewer
agreed: false
stereotype: entity
parent: []
source-of-truth:
attributes: []
relationships: [{to: "[[DT-002-element]]", label: contains, kind: composition, multiplicity: "1..*"}, {to: "[[DT-003-dependency]]", label: contains, kind: composition, multiplicity: "0..*"}]
---

# Model

**Definition.** The result of one scan: every element and dependency of the scanned code, written as compact JSON.

**In context.** Produced by the scanner, embedded in the viewer or written as `deps.json`, read by `check` and later by agents. Its format is the contract described in [[CC-001-the-model-as-the-contract-between-scanner-and-viewer]].

**Distinguish from.** [[DT-020-fragment]], the partial result of one extractor that the core merges into the model.
