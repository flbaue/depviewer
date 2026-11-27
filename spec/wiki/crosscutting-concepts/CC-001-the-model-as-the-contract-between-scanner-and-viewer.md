---
id: CC-001
type: crosscutting-concept
title: The model as the contract between scanner and viewer
status: draft
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-004-guide-for-ai-coding-agents]]"]
related: []
tags: [crosscutting-concept]
category: domain
applies-to: ["[[BB-004-model-and-json-writer]]", "[[BB-013-viewer]]", "[[BB-012-architecture-checker]]", "[[BB-014-query-commands]]"]
drivers: ["[[GOAL-004-one-answer-in-every-interface]]"]
decisions: []
state: current
superseded-by:
---

# The model as the contract between scanner and viewer

> follows [[arc42]] (section 8)

## Concept


The model is one JSON document:

```jsonc
{ "title": "…", "generated": "yyyy-MM-dd HH:mm", "errors": 0,
  "edgeKinds": ["uses", "extends", "implements", "call", "new", "access"],
  "nodes": [ { "n": "name", "k": "kind", "p": parentIndex, "l": line, "loc": lines,
               "t": 1, "s": 1, "v": "+|#|-|~", "q": "qualified.name", "path": "src/…/X.java" } ],
  "edges": [ [fromIndex, toIndex, kindIndex, count, [line, …]] ] }
```

- Parents always come before children. The viewer relies on it.
- Edges go from the innermost declaring element to the most specific target in the model: member, then type, then external top-level type.
- Optional fields are omitted when empty. Folder-based packages (TypeScript, Dart) carry `"sep": "/"`.
- `/*DEPS_DATA*/null` and `/*DEPS_VIEWS*/null` appear exactly once in `viewer.html`. `scripts/check.sh` enforces it.
- Planned additions: `analysis` ([[FR-011-record-the-provenance-of-every-language-and-module]]), resolved assignments and violations ([[FR-041-show-pattern-assignments-as-by-pattern]], [[FR-042-mark-arcs-that-violate-a-rule]]).
