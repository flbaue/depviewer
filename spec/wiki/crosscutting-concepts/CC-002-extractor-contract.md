---
id: CC-002
type: crosscutting-concept
title: Extractor contract
status: review
created: 2026-11-27
updated: 2026-11-28
sources: ["[[SRC-003-adr-0001-architecture-for-language-support]]", "[[SRC-011-spec-multi-language-architecture]]", "[[SRC-026-github-issue-10-extractor-contract-v1-versioned-spec-describe-handshake-and-confor]]"]
related: ["[[DR-001-every-language-is-analysed-by-an-extractor-behind-one-versioned-contract]]", "[[ISS-005-is-the-extractor-request-sent-on-stdin-or-in-a-file]]"]
tags: [crosscutting-concept]
category: patterns
applies-to: ["[[BB-006-toolchain-runner]]", "[[BB-009-typescript-extractor]]", "[[BB-010-dart-extractor]]", "[[BB-011-jvm-extractor]]"]
drivers: ["[[FR-010-choose-the-toolchain-in-a-fixed-order]]", "[[FR-012-link-the-modules-of-a-monorepo]]", "[[CON-010-extractors-pin-their-dependencies]]"]
decisions: ["[[DR-001-every-language-is-analysed-by-an-extractor-behind-one-versioned-contract]]"]
state: current
superseded-by:
---

# Extractor contract

> follows [[arc42]] (section 8)

## Concept

Every extractor speaks one versioned contract, version **1**, extending [[CC-009-fragment-format]] ([[DR-001-every-language-is-analysed-by-an-extractor-behind-one-versioned-contract]], [[SRC-026-github-issue-10-extractor-contract-v1-versioned-spec-describe-handshake-and-confor|issue #10]]). `Toolchain` holds the core's side, `CONTRACT = 1`.

**Handshake.** `<extractor> --describe <toolchain> <source>` prints one JSON object on stdout and exits 0: `name` and `version` of the extractor, `contract` (1), `languages`, `exact` (whether it resolves references exactly), and `toolchain`: `tool`, `version`, `source`, `path`, `supported`, `range`, and `problem` when it is not supported. The core asks it for every candidate toolchain ([[FR-010-choose-the-toolchain-in-a-fixed-order]]). An extractor whose `contract` is not 1 is reported and its language skipped.

**Request.** One run per scan and chosen toolchain ([[FR-012-link-the-modules-of-a-monorepo]]). The core writes a JSON file and passes its path as the only argument ([[ISS-005-is-the-extractor-request-sent-on-stdin-or-in-a-file]]):

```jsonc
{ "contract": 1, "language": "typescript",
  "root": "/abs/shop", "module": "shop-web",          // the first module, for one-module extractors
  "toolchain": { "tool": "typescript", "version": "5.9.3", "path": "/abs/node_modules/typescript" },
  "options": { "tests": true },                         // plus language-specific options
  "modules": [ { "name": "shop-web", "root": "/abs/shop" } ],
  "files": [ { "path": "src/cart.ts", "test": false, "module": "shop-web", "root": "/abs/shop" } ] }
```

The core computes the file lists and test flags; paths are relative to the file's module. An extractor ignores fields it does not know.

**Output.** The fragment of [[CC-009-fragment-format]] on stdout, with `contract` (1). Optional additions of version 1:

- `packageSep`: `"."` for dotted packages (Java, Kotlin); default `"/"` for folders.
- `packages`: `[{module, package}]`, created in this order before the nodes, so an extractor decides the order of package nodes.
- `languages`: `[{language, modules, tool, version, exact, source?}]` when one run covers several languages, e.g. Java and Kotlin; the core records each as provenance ([[FR-011-record-the-provenance-of-every-language-and-module]]). Without it, the run's language and toolchain are recorded.
- An external may carry `q` (its qualified name) and `kind`.
- An edge carries either `line` (one reference) or `count` and `lines` (several references, aggregated).

The core rejects a fragment whose `contract` is not 1 and skips the run's modules with a note. It ignores unknown fields. A node with a missing parent or an edge with a missing endpoint is dropped with a warning.

**Diagnostics.** Lines on stderr start with the fragment's language, e.g. `dart: shop_app: 1 file(s) skipped`. Errors the toolchain reports are counted in the fragment's `errors`. A non-zero exit skips the run with a note.

**Conformance.** `DEPVIEWER_FRAGMENTS=<folder>` keeps every run's request and raw fragment. `test/checks/contract.py <request> <fragment>` checks one run without knowing its language: the contract version, the shape, unique ids, parents before children, allowed node and edge kinds, every edge endpoint known, lines inside their file, and every file in a module and a file of the request. `scripts/check.sh` runs it on every extractor's run over its fixture, and first on broken fragments it must reject.
