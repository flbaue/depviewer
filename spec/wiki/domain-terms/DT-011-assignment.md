---
id: DT-011
type: domain-term
title: Assignment
status: draft
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-002-spec-architecture-file-and-rule-checks]]", "[[SRC-010-viewer-reference]]"]
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

# Assignment

**Definition.** The statement that an element belongs to a group in a view: explicit, by pattern, or inherited from the nearest assigned ancestor.

**In context.** Explicit assignments are stored under the element's [[DT-012-persisted-view-key]]. `""` means explicitly ungrouped and overrides an assigned ancestor. Precedence: [[FR-028-decide-each-elements-group-by-precedence]].

**Distinguish from.** A [[DT-013-selector]], which produces assignments for many elements.
