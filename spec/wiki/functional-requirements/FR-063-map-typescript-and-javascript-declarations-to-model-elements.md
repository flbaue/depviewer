---
id: FR-063
type: functional-requirement
title: "Map TypeScript and JavaScript declarations to model elements"
status: draft
created: 2026-11-27
updated: 2026-11-27
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
order: 1
goal: []
delivery:
delivery-checked:
---

# Map TypeScript and JavaScript declarations to model elements

## Rationale

TypeScript and JavaScript code must fit the same tree as the other languages.

## Requirement

Source files are skipped below `node_modules`, `dist`, `build`, `out`, `coverage`, `.next` and nested `package.json` modules, and `.d.ts` files are skipped. Files sit in the package of their folder (`src/app`); files directly in the module folder have no package row.

| TypeScript / JavaScript | Kind |
|---|---|
| `class`, `abstract class` | `class` |
| `interface` | `interface` |
| `enum`, `const enum` (members `enumconst`) | `enum` |
| `type X = …` | `typealias` |
| `namespace` / `module` block | `namespace` |
| constructor | `ctor` |
| method, function, arrow function or function expression on a top-level `const`/`let` or class property | `method` |
| property, accessor pair, top-level `const`/`let`/`var`, constructor parameter property | `field` |

All top-level declarations are listed, exported or not. Members carry parameter types (`add(Product)`, untyped: `any`). `private` and `#x` are private, `protected` is protected, the rest public. Qualified names are `<path>#Name` and `<path>#Class.member`, externals `<package>.<Name>` (`(builtin).Map`).

## Acceptance criteria

- [ ] `test/checks/typescript.py` finds the namespace, the type alias and the arrow function of `test/fixture-ts/` with these kinds.
- [ ] An unexported top-level function is listed.

## Hierarchy

- **Parent:** [[FR-005-analyse-typescript-and-javascript-with-the-typescript-compiler]]
- **Children:** none.
