---
id: DT-009
type: domain-term
title: View
status: accepted
created: 2026-11-27
updated: 2026-11-28
sources: ["[[SRC-010-viewer-reference]]", "[[SRC-002-spec-architecture-file-and-rule-checks]]"]
related: []
tags: [domain-term]
aliases: []
bounded-context: depviewer
agreed: false
stereotype: entity
parent: []
source-of-truth:
attributes: []
relationships: [{to: "[[DT-010-group]]", label: has, kind: composition, multiplicity: "0..*"}, {to: "[[DT-015-rule]]", label: has, kind: composition, multiplicity: "0..*"}]
---

# View

**Definition.** An independent set of groups that regroups the code into a structure of its own, such as layers or bounded contexts, without changing the code.

**In context.** Kept in the browser per project title, exported as JSON and, with rules, the main content of the [[DT-014-architecture-file]].

**Distinguish from.** The tree of the code itself, which every view shares.
