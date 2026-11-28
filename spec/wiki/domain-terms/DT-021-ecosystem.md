---
id: DT-021
type: domain-term
title: Ecosystem
status: accepted
created: 2026-11-27
updated: 2026-11-28
sources: ["[[SRC-003-adr-0001-architecture-for-language-support]]"]
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

# Ecosystem

**Definition.** A set of languages that compile together and are therefore analysed by one extractor, such as Java and Kotlin in `jvm`.

**In context.** References inside an ecosystem are linked by its extractor. References between ecosystems are not linked.

**Distinguish from.** A [[DT-005-module]], a unit of code.
