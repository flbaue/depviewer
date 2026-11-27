---
id: DT-020
type: domain-term
title: Fragment
status: draft
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-011-spec-multi-language-architecture]]", "[[SRC-003-adr-0001-architecture-for-language-support]]"]
related: ["[[CC-009-fragment-format]]"]
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

# Fragment

**Definition.** The JSON an extractor prints: nodes, externals, edges, errors, and the module of every file.

**In context.** ids are local to one fragment, the core prefixes them with language and module. The versioned contract is [[CC-002-extractor-contract]].

**Distinguish from.** The [[DT-001-model]], the merged result of all fragments.
