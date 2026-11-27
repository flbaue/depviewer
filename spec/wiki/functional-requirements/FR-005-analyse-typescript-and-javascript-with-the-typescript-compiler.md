---
id: FR-005
type: functional-requirement
title: Analyse TypeScript and JavaScript with the TypeScript compiler
status: draft
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-013-spec-typescript-and-javascript-support]]", "[[SRC-009-supported-languages]]"]
related: []
tags: [functional-requirement]
notation: prose
level: requirement
parent: ["[[FR-001-scan-a-code-base-into-one-model]]"]
building-blocks: ["[[BB-009-typescript-extractor]]"]
priority: Must
release: delivered
lane: backbone
order: 4
goal: []
delivery:
delivery-checked:
---

# Analyse TypeScript and JavaScript with the TypeScript compiler

## Rationale

Frontend and Node code should be as exact as Java.

## Requirement

Files `.ts .tsx .mts .cts .js .jsx .mjs .cjs` below each `package.json` are analysed with the TypeScript compiler API and type checker, respecting `tsconfig.json`/`jsconfig.json`. The project's own `typescript` package is used if installed, else the extractor's pinned copy. Test files (`*.test.*`, `*.spec.*`, `__tests__/`, `test/`, `tests/`) are flagged.

## Acceptance criteria

- [ ] `test/checks/typescript.py` passes on `test/fixture-ts/`.
- [ ] A path alias from `tsconfig.json` resolves to the real file.

## Hierarchy

- **Parent:** [[FR-001-scan-a-code-base-into-one-model]]
- **Children:** none.
