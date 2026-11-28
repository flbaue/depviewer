---
id: FR-051
type: functional-requirement
title: Analyse more languages
status: accepted
created: 2026-11-27
updated: 2026-11-28
sources: ["[[SRC-001-vision-and-goal-of-depviewer]]", "[[SRC-003-adr-0001-architecture-for-language-support]]"]
related: ["[[DR-001-every-language-is-analysed-by-an-extractor-behind-one-versioned-contract]]"]
tags: [functional-requirement]
notation: prose
level: feature
parent: []
building-blocks: ["[[BB-011-jvm-extractor]]"]
priority: Should
release: m4-more-languages
lane: backbone
order: 10
goal: ["[[GOAL-001-architecture-as-code-checked-against-the-real-dependencies]]"]
delivery:
delivery-checked:
---

# Analyse more languages

## Rationale

Many teams use Go, C#, Python or Swift, and Kotlin is only approximate today.

## Requirement

New languages and exact Kotlin come as extractors behind the contract of [[CC-002-extractor-contract]], each with its own spec, fixture and model checks.

## Hierarchy

- **Parent:** none.
- **Children:** see `parent:` backlinks.
