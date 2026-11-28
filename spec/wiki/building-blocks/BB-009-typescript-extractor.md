---
id: BB-009
type: building-block
title: TypeScript extractor
status: accepted
created: 2026-11-27
updated: 2026-11-28
sources: ["[[SRC-013-spec-typescript-and-javascript-support]]", "[[SRC-011-spec-multi-language-architecture]]"]
related: []
tags: [building-block]
parent: "[[CTX-001-depviewer-context]]"
drivers: ["[[FR-005-analyse-typescript-and-javascript-with-the-typescript-compiler]]", "[[FR-012-link-the-modules-of-a-monorepo]]", "[[CON-010-extractors-pin-their-dependencies]]"]
decisions: ["[[DR-001-every-language-is-analysed-by-an-extractor-behind-one-versioned-contract]]"]
uses: ["[[EIF-003-node-js-with-the-typescript-compiler]]"]
location: extractors/typescript/
state: current
superseded-by:
---

# TypeScript extractor

> follows [[arc42]] (section 5, black box)

## Purpose / Responsibility

`extract.mjs` on the TypeScript compiler API. Analyses one module's TypeScript and JavaScript files and prints a fragment. Planned: once per scan for all modules ([[FR-012-link-the-modules-of-a-monorepo]]).

## Interfaces

- **Provides:** the fragment format of [[CC-002-extractor-contract]] on stdout.
- **Needs:** [[EIF-003-node-js-with-the-typescript-compiler]].
