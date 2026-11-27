---
id: FR-055
type: functional-requirement
title: Analyse Python
status: draft
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-003-adr-0001-architecture-for-language-support]]", "[[SRC-001-vision-and-goal-of-depviewer]]", "[[SRC-032-github-issue-16-language-python]]"]
related: []
tags: [functional-requirement]
notation: prose
level: requirement
parent: ["[[FR-051-analyse-more-languages]]"]
building-blocks: []
priority: Could
release: m4-more-languages
lane: backbone
order: 4
goal: []
delivery:
delivery-checked:
---

# Analyse Python

## Rationale

Requested in [[SRC-032-github-issue-16-language-python|issue #16]].

## Requirement

Python is analysed by an extractor with a Python toolchain still to be chosen, following the checklist "Adding a language" in AGENTS.md.

## Acceptance criteria

- [ ] A fixture `test/fixture-python/` with a deliberate cycle passes its model checks and the conformance test of [[CC-002-extractor-contract]].

## Hierarchy

- **Parent:** [[FR-051-analyse-more-languages]]
- **Children:** none.

> [!note] Open points
> Needs its own spec. Order of the new languages is not decided, Go and C# have the most exact official tooling.
