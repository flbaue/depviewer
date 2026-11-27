---
id: DT-024
type: domain-term
title: Exact
status: draft
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-003-adr-0001-architecture-for-language-support]]", "[[SRC-009-supported-languages]]"]
related: []
tags: [domain-term]
aliases: [exactness, name-based]
bounded-context: depviewer
agreed: false
stereotype:
parent: []
source-of-truth:
attributes: []
relationships: []
---

# Exact

**Definition.** Resolved by the language's compiler or type checker, as opposed to by name.

**In context.** Java, TypeScript/JavaScript and Dart are exact. Kotlin is name-based until [[FR-052-analyse-kotlin-with-the-kotlin-compiler]]. A violation carries `exact: false` when its source is name-based.

**Distinguish from.** Complete: an exact scan can still miss references to libraries that are not on the classpath.
