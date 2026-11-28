---
id: DT-010
type: domain-term
title: Group
status: accepted
created: 2026-11-27
updated: 2026-11-28
sources: ["[[SRC-010-viewer-reference]]", "[[SRC-002-spec-architecture-file-and-rule-checks]]", "[[SRC-004-guide-for-ai-coding-agents]]"]
related: []
tags: [domain-term]
aliases: [layer, component, bounded context]
bounded-context: depviewer
agreed: false
stereotype: entity
parent: []
source-of-truth:
attributes: []
relationships: [{to: "[[DT-010-group]]", label: parent, kind: aggregation, multiplicity: "0..1"}]
---

# Group

**Definition.** A named, coloured part of a view, such as a layer, that elements are assigned to. A group can be nested in another with `parent`.

**In context.** In a rule a group stands for itself and all its subgroups. A missing parent or a parent loop makes it a top-level group.

**Distinguish from.** A [[DT-005-module]] or package, which come from the code.
