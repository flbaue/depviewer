---
id: FR-054
type: functional-requirement
title: "Analyse C#"
status: draft
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-003-adr-0001-architecture-for-language-support]]", "[[SRC-001-vision-and-goal-of-depviewer]]"]
related: []
tags: [functional-requirement]
notation: prose
level: requirement
parent: ["[[FR-051-analyse-more-languages]]"]
building-blocks: []
priority: Could
release: m4-more-languages
lane: backbone
order: 3
goal: []
delivery:
delivery-checked:
---

# Analyse C#

## Rationale

Requested in issue #15.

## Requirement

C# is analysed by an extractor with the .NET toolchain, following the checklist "Adding a language" in AGENTS.md.

## Acceptance criteria

- [ ] A fixture `test/fixture-csharp/` with a deliberate cycle passes its model checks and the conformance test of [[CC-002-extractor-contract]].

## Hierarchy

- **Parent:** [[FR-051-analyse-more-languages]]
- **Children:** none.

> [!note] Open points
> Needs its own spec. Order of the new languages is not decided, Go and C# have the most exact official tooling.
