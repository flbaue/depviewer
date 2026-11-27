---
id: DT-016
type: domain-term
title: Violation
status: draft
created: 2026-11-27
updated: 2026-11-27
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
relationships: [{to: "[[DT-015-rule]]", label: breaks, kind: association, multiplicity: "1"}, {to: "[[DT-003-dependency]]", label: caused by, kind: association, multiplicity: "1..*"}]
---

# Violation

**Definition.** One place where the code breaks a rule: one per rule and pair of innermost elements, with all its lines.

**In context.** Reported by `check` as text or JSON, embedded in the viewer, marked `exact` and `baselined` where that applies.

**Distinguish from.** An upward [[DT-004-arc]] in the viewer, which goes against a levelization but not necessarily against a rule.
