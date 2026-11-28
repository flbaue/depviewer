---
id: DT-015
type: domain-term
title: Rule
status: accepted
created: 2026-11-27
updated: 2026-11-28
sources: ["[[SRC-002-spec-architecture-file-and-rule-checks]]"]
related: []
tags: [domain-term]
aliases: []
bounded-context: depviewer
agreed: false
stereotype: entity
parent: []
source-of-truth:
attributes: []
relationships: []
---

# Rule

**Definition.** A statement about allowed dependencies between groups of a view, with an id, a kind (`layers`, `forbid`, `allow-only`, `independent`, `no-cycles`, `no-ungrouped`) and a severity.

**In context.** Belongs to one view, because groups are per view. Test sources are exempt unless `tests` is true.

**Distinguish from.** A [[DT-016-violation]], which is one place where the code breaks a rule.
