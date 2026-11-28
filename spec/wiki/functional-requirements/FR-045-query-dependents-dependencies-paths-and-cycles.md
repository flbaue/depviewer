---
id: FR-045
type: functional-requirement
title: "Query dependents, dependencies, paths and cycles"
status: review
created: 2026-11-27
updated: 2026-11-28
sources: ["[[SRC-001-vision-and-goal-of-depviewer]]", "[[SRC-002-spec-architecture-file-and-rule-checks]]"]
related: []
tags: [functional-requirement]
notation: prose
level: requirement
parent: ["[[FR-044-let-ai-agents-query-and-check-the-architecture]]"]
building-blocks: ["[[BB-014-query-commands]]"]
priority: Should
release: m3-agent-interface
lane: backbone
order: 1
goal: []
delivery:
delivery-checked:
---

# Query dependents, dependencies, paths and cycles

## Rationale

An agent asks "may I depend on X from here?" and "why does A depend on B?".

## Requirement

`depviewer query <command>` answers from a scan or a saved model, with the same input options as `check` (project directories with the scan options, or `--model deps.json`):

- `dependents <selector>`: the elements that depend on the selected ones.
- `dependencies <selector>`: the elements the selected ones depend on, externals included.
- `path <a> <b>`: one shortest chain of dependencies from `<a>` to `<b>`, at the level of the elements named. The same input always gives the same chain.
- `cycles --level <level>`: every cycle at that level.

Elements are named by the selectors of the architecture file ([[FR-027-select-code-by-patterns]]). `dependents` and `dependencies` list one entry per element at the selector's level, each with the references behind it (from and to member, kind, file and line); `--level` overrides the level. References inside the selection are left out. Each hop of a path carries its references the same way.

Queries include test sources and mark each test element and reference `test`. `--no-tests` leaves them out.

The text output is the default. `--format json` prints a versioned document like the report of `check` ([[FR-038-report-violations-as-versioned-json]]): `format: 1`, `title`, `generated`, `analysis`, elements with `key`, `q`, `path` and `kind`, and references with `kind`, `lines`, `count` and `exact`.

Exit codes: 0 when there is an answer, 1 when it is empty (no dependent, no dependency, no path, no cycle), 2 on an error, as for `check`.

## Acceptance criteria

- [ ] `path package:com.example.domain package:com.example.service` on the fixture returns the `Order.total()` → `PriceCalculator.gross(double)` reference.
- [ ] `dependents package:com.example.domain` on the fixture lists exactly the packages `com.example.app`, `com.example.domain.event`, `com.example.infra`, `com.example.notify` and `com.example.service`.
- [ ] `dependencies package:com.example.domain --no-tests` on the fixture lists exactly `com.example.service`, `java.lang` and `java.util`. Without `--no-tests`, it also lists `(unresolved)`, marked `test`.
- [ ] `cycles --level package` on the fixture reports exactly one cycle, of `com.example.domain` and `com.example.service`.
- [ ] `path package:com.example.service package:com.example.app` on the fixture exits 1.
- [ ] Each command's `--format json` output has `format: 1`, `title`, `generated` and `analysis`, and its elements and references have the fields named above.

## Hierarchy

- **Parent:** [[FR-044-let-ai-agents-query-and-check-the-architecture]]
- **Children:** none.
