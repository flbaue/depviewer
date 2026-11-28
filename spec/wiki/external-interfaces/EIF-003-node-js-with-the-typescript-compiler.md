---
id: EIF-003
type: external-interface
title: Node.js with the TypeScript compiler
status: accepted
created: 2026-11-27
updated: 2026-11-28
sources: ["[[SRC-013-spec-typescript-and-javascript-support]]", "[[SRC-009-supported-languages]]"]
related: ["[[CTX-001-depviewer-context]]"]
tags: [external-interface]
partner: Node.js 18+ and the typescript package
tier:
short_title: Node + tsc
flows:
  - {data: module folder and file list, direction: outbound, format: process arguments, trigger: scan with a package.json module, label: files}
  - {data: fragment, direction: inbound, format: JSON on stdout, label: fragment}
---

# Node.js with the TypeScript compiler

**Purpose.** Resolves TypeScript and JavaScript references exactly with the TypeScript type checker.

**Flows.** The core starts `extractors/typescript/extract.mjs` with Node and reads a JSON fragment
from stdout. The compiler is chosen as [[FR-010-choose-the-toolchain-in-a-fixed-order]]
says: `DEPVIEWER_TYPESCRIPT`, the project's own `node_modules/typescript`, the `tsc` on the `PATH`,
else the extractor's pinned copy. Node comes from `DEPVIEWER_NODE` or the `PATH`.

**Belongs to.** [[CTX-001-depviewer-context]]
