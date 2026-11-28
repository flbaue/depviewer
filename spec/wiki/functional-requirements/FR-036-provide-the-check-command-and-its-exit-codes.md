---
id: FR-036
type: functional-requirement
title: Provide the check command and its exit codes
status: accepted
created: 2026-11-27
updated: 2026-11-28
sources: ["[[SRC-002-spec-architecture-file-and-rule-checks]]"]
related: ["[[ISS-001-should-check-find-architecture-json-on-its-own]]", "[[ISS-004-should-name-based-violations-fail-the-build-by-default]]"]
tags: [functional-requirement]
notation: prose
level: requirement
parent: ["[[FR-035-run-the-check-from-the-command-line-and-in-ci]]"]
building-blocks: ["[[BB-003-cli-and-orchestration]]", "[[BB-012-architecture-checker]]"]
priority: Must
release: m1-rules-and-check
lane: backbone
order: 1
goal: []
delivery:
delivery-checked:
---

# Provide the check command and its exit codes

## Rationale

CI and agents decide by exit code.

## Requirement

`./depviewer check [--rules <file>] [--view <id>] (<project-dir>... | --model deps.json) [--format text|json] [--out <file>] [--baseline <file>] [--update-baseline] [--fail-on error|warning|never]`. Without `--model` it scans first with the normal scan options. Without `--rules` it reads `architecture.json` from the first project directory. With `--model` and no `--rules`, or when that file is missing, it exits 2 ([[ISS-001-should-check-find-architecture-json-on-its-own]]). Without `--view` all views with rules are checked. Exit codes: 0 no failing violations, 1 failing violations, 2 invalid arguments, rules file or model. `--fail-on` (default `error`) decides which severities fail. Name-based violations (`exact: false`) fail like exact ones, and a wrong one is accepted into the baseline ([[FR-039-accept-existing-violations-in-a-baseline]]) ([[ISS-004-should-name-based-violations-fail-the-build-by-default]]). `--out` also writes the viewer with the resolved view and violations embedded.

## Acceptance criteria

- [ ] `check` on the fixture with `test/fixture-architecture.json` exits 1.
- [ ] A project with `architecture.json` in its root is checked without `--rules`, and `check --model build/fixture.json` without `--rules` exits 2.
- [ ] A name-based `error` violation from Kotlin code makes `check` exit 1.
- [ ] `check --model build/fixture.json` gives the same JSON, except `generated`, as checking the project directory.

## Hierarchy

- **Parent:** [[FR-035-run-the-check-from-the-command-line-and-in-ci]]
- **Children:** none.
