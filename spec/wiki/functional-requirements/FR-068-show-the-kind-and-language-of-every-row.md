---
id: FR-068
type: functional-requirement
title: "Show the kind and language of every row"
status: draft
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-011-spec-multi-language-architecture]]", "[[SRC-012-spec-kotlin-support]]"]
related: []
tags: [functional-requirement]
notation: prose
level: requirement
parent: ["[[FR-013-explore-the-dependency-structure]]"]
building-blocks: ["[[BB-013-viewer]]"]
priority: Should
release: delivered
lane: backbone
order: 9
goal: []
delivery:
delivery-checked:
---

# Show the kind and language of every row

## Rationale

In a mixed repository the reader needs to tell languages and declaration kinds apart at a glance.

## Requirement

Each row shows an icon for its kind, and file rows show their language: "J" Java, "K" Kotlin, "TS" TypeScript, "JS" JavaScript, "D" Dart. The kinds added for other languages have their own icons: `object` "O", `typealias` "A", `namespace` "N", `mixin` "m", `extension` "+". Merged package chains from folder-based packages join with `/` (`src/app/services`), the others with `.`.

## Acceptance criteria

- [ ] A Kotlin file row in `build/fixture.html` shows `K` and a TypeScript file `TS`.
- [ ] A collapsed chain of folder packages reads `features/payments` in the viewer of `test/fixture-ts/`.

## Hierarchy

- **Parent:** [[FR-013-explore-the-dependency-structure]]
- **Children:** none.
