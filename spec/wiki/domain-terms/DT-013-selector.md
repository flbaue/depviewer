---
id: DT-013
type: domain-term
title: Selector
status: draft
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-002-spec-architecture-file-and-rule-checks]]"]
related: []
tags: [domain-term]
aliases: [pattern, match]
bounded-context: depviewer
agreed: false
stereotype:
parent: []
source-of-truth:
attributes: []
relationships: []
---

# Selector

**Definition.** A pattern `<kind>:<glob>` that selects elements by module, package, path, type (with optional `@kind`) or external name. A leading `!` excludes.

**In context.** Listed in a group's `match` in the architecture file and resolved by the core ([[CON-005-selectors-are-resolved-only-in-the-scanner-core]]).

**Distinguish from.** An explicit [[DT-011-assignment]] of one element.
