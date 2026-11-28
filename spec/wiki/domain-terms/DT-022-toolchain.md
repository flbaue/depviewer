---
id: DT-022
type: domain-term
title: Toolchain
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

# Toolchain

**Definition.** The compiler or analyzer an extractor uses, such as javac, the TypeScript compiler or the Dart analyzer, with its version and where it came from.

**In context.** Chosen in the order override, project, `PATH`, pinned download ([[FR-010-choose-the-toolchain-in-a-fixed-order]]). Recorded in [[DT-023-provenance]].

**Distinguish from.** The extractor, which uses the toolchain.
