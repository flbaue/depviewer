---
id: CC-009
type: crosscutting-concept
title: Fragment format
status: accepted
created: 2026-11-27
updated: 2026-11-28
sources: ["[[SRC-011-spec-multi-language-architecture]]", "[[SRC-026-github-issue-10-extractor-contract-v1-versioned-spec-describe-handshake-and-confor]]"]
related: ["[[DT-020-fragment]]"]
tags: [crosscutting-concept]
category: patterns
applies-to: ["[[BB-006-toolchain-runner]]", "[[BB-009-typescript-extractor]]", "[[BB-010-dart-extractor]]"]
drivers: ["[[FR-012-link-the-modules-of-a-monorepo]]", "[[FR-007-skip-a-language-whose-toolchain-is-missing]]"]
decisions: []
state: current
superseded-by: "[[CC-002-extractor-contract]]"
---

# Fragment format

> follows [[arc42]] (section 8)

## Concept

A [[DT-019-extractor]] is started per run with a request file ([[CC-002-extractor-contract]]) and writes one JSON [[DT-020-fragment]] to stdout, diagnostics to stderr:

```jsonc
{ "contract": 1, "language": "typescript", "errors": 0,
  "nodes": [ { "id": "src/app/cart.ts", "kind": "file", "path": "src/app/cart.ts",
               "package": "src/app", "test": false, "loc": 42 },
             { "id": "src/app/cart.ts#Cart", "kind": "class", "name": "Cart",
               "parent": "src/app/cart.ts", "line": 3, "loc": 20, "vis": "+", "static": false,
               "q": "src/app/cart.ts#Cart" } ],
  "externals": [ { "id": "ext:react#useState", "package": "react", "name": "useState" } ],
  "edges": [ { "from": "src/app/cart.ts#Cart", "to": "ext:react#useState", "kind": "call", "line": 7 } ] }
```

- Ids are local to one fragment; the core prefixes them with language and module.
- A run covering several modules puts `module` on each input file and file node; paths are relative to that module.
- Parents come before children, and every non-file node has a parent in the fragment.
- `kind` takes the model's node kinds plus `namespace`, `mixin`, `extension`. Edge kinds are the model's six.
- `package` is a `/` path (`""` for the root). The core builds one package node per segment, marked so the viewer joins chains with `/`.
- Unknown fields are ignored. A missing parent or edge endpoint drops that node or edge with a warning.

[[CC-002-extractor-contract]] version 1 extends this format with `contract`, `packageSep`, `packages`, `languages`, qualified externals and aggregated edges.
