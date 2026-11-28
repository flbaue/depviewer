---
id: DT-003
type: domain-term
title: Dependency
status: accepted
created: 2026-11-27
updated: 2026-11-28
sources: ["[[SRC-004-guide-for-ai-coding-agents]]"]
related: []
tags: [domain-term]
aliases: [edge, reference]
bounded-context: depviewer
agreed: false
stereotype: entity
parent: []
source-of-truth:
attributes: []
relationships: [{to: "[[DT-002-element]]", label: from, kind: association, multiplicity: "1"}, {to: "[[DT-002-element]]", label: to, kind: association, multiplicity: "1"}]
---

# Dependency

**Definition.** A language-level reference from one element to another, with a kind (`uses`, `extends`, `implements`, `call`, `new`, `access`), a count and the line numbers.

**In context.** Goes from the innermost declaring element to the most specific target that exists in the model. Imports, `this`/`super`, local variables and compiler-generated code produce none.

**Distinguish from.** [[DT-004-arc]], the drawing of one or more dependencies between visible rows.
