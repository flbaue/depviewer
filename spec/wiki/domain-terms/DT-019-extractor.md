---
id: DT-019
type: domain-term
title: Extractor
status: accepted
created: 2026-11-27
updated: 2026-11-28
sources: ["[[SRC-003-adr-0001-architecture-for-language-support]]", "[[SRC-011-spec-multi-language-architecture]]"]
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

# Extractor

**Definition.** A separate process that analyses the languages of one ecosystem and prints one fragment, run once per scan.

**In context.** Today TypeScript and Dart. Java and Kotlin follow in the `jvm` extractor ([[DR-001-every-language-is-analysed-by-an-extractor-behind-one-versioned-contract]]).

**Distinguish from.** The scanner core, which discovers, runs extractors, merges and writes, but analyses no language itself once [[DR-001-every-language-is-analysed-by-an-extractor-behind-one-versioned-contract]] is done.
