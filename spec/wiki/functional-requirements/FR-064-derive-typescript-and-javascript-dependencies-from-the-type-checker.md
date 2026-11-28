---
id: FR-064
type: functional-requirement
title: "Derive TypeScript and JavaScript dependencies from the type checker"
status: accepted
created: 2026-11-27
updated: 2026-11-28
sources: ["[[SRC-013-spec-typescript-and-javascript-support]]"]
related: []
tags: [functional-requirement]
notation: prose
level: requirement
parent: ["[[FR-005-analyse-typescript-and-javascript-with-the-typescript-compiler]]"]
building-blocks: ["[[BB-009-typescript-extractor]]"]
priority: Must
release: delivered
lane: backbone
order: 2
goal: []
delivery: implemented
delivery-checked: "2026-11-28T08:27:38Z"
---

# Derive TypeScript and JavaScript dependencies from the type checker

## Rationale

The type checker resolves names exactly, so dependencies are as exact as javac's for Java.

## Requirement

Every identifier in a declaration (signature, initializer, body, decorator) that the type checker resolves gives an edge from the innermost declaring element. Top-level statements that are not declarations count for the file.

- A project declaration is the target; import aliases and re-exports are followed to the original.
- A declaration in `node_modules` gives an external package (scoped names kept, `@types/x` becomes `x`) with the top-level exported name.
- The standard library (`lib.*.d.ts`) gives the external package `(builtin)`.

Edge kinds: heritage `extends` and `implements`; a callee is `call`; `new X()` is `new` to the constructor, else the class; property access and variable reads and writes are `access`; everything else is `uses`. Imports give no edge. Unresolved names are counted in `errors`.

## Acceptance criteria

- [ ] A re-export through `index.ts` links to the original declaration.
- [ ] `new Map()` gives a `new` edge to `(builtin).Map`.

## Hierarchy

- **Parent:** [[FR-005-analyse-typescript-and-javascript-with-the-typescript-compiler]]
- **Children:** none.
