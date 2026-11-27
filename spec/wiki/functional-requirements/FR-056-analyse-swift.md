---
id: FR-056
type: functional-requirement
title: Analyse Swift
status: draft
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-003-adr-0001-architecture-for-language-support]]", "[[SRC-001-vision-and-goal-of-depviewer]]", "[[SRC-033-github-issue-17-language-swift]]"]
related: []
tags: [functional-requirement]
notation: prose
level: requirement
parent: ["[[FR-051-analyse-more-languages]]"]
building-blocks: []
priority: Could
release: m4-more-languages
lane: backbone
order: 5
goal: []
delivery:
delivery-checked:
---

# Analyse Swift

## Rationale

Requested in [[SRC-033-github-issue-17-language-swift|issue #17]].

## Requirement

Swift is analysed by an extractor with the Swift toolchain, following the checklist "Adding a language" in AGENTS.md.

## Acceptance criteria

- [ ] A fixture `test/fixture-swift/` with a deliberate cycle passes its model checks and the conformance test of [[CC-002-extractor-contract]].

## Hierarchy

- **Parent:** [[FR-051-analyse-more-languages]]
- **Children:** none.

> [!note] Open points
> Needs its own spec. Order of the new languages is not decided, Go and C# have the most exact official tooling.
