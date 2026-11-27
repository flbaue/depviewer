---
id: FR-026
type: functional-requirement
title: Read the architecture file
status: draft
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-002-spec-architecture-file-and-rule-checks]]"]
related: ["[[ISS-001-should-check-find-architecture-json-on-its-own]]", "[[ISS-002-does-the-json-reader-need-comment-support]]"]
tags: [functional-requirement]
notation: prose
level: requirement
parent: ["[[FR-025-declare-the-architecture-in-a-file]]"]
building-blocks: ["[[BB-012-architecture-checker]]"]
priority: Must
release: m1-rules-and-check
lane: backbone
order: 1
goal: []
delivery:
delivery-checked:
---

# Read the architecture file

## Rationale

The file must stay compatible with every export that exists today ([[CON-004-the-architecture-file-is-json-and-a-superset-of-the-views-format]]).

## Requirement

The default file is `architecture.json` in the project root, any other is passed with `--rules`. `version` is optional (missing means 1) and a file with an unknown major version is rejected with a clear message. `groups`, `parent`, `assign` and `""` keep their meaning. `match` and `rules` are new and optional. Unknown fields are ignored and kept on export. The file is plain JSON without comments, and a rule explains itself in its `description` ([[ISS-002-does-the-json-reader-need-comment-support]]).

## Acceptance criteria

- [ ] `test/fixture-views.json` loads in the viewer exactly as before, and `check` reports "no rules" and exits 0.
- [ ] A file with `"version": 2` is rejected with exit code 2.

## Hierarchy

- **Parent:** [[FR-025-declare-the-architecture-in-a-file]]
- **Children:** none.
