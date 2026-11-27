---
id: SRC-003
type: source
title: ADR 0001 architecture for language support
status: ingested
created: 2026-11-27
updated: 2026-11-27
tags: [source]
source-type: document
origin: "../docs/adr/0001-language-support.md"
captured: "2026-11-27"
sha256: ef908587372a94b30765f4b346eb8c76cf703318bb7bff5e67250ac8c7b9e209
ingested-pages: ["[[BB-002-scanner-core]]", "[[BB-006-toolchain-runner]]", "[[BB-011-jvm-extractor]]", "[[CC-002-extractor-contract]]", "[[CC-006-exactness-before-completeness]]", "[[CON-003-no-network-access-during-a-scan]]", "[[CON-010-extractors-pin-their-dependencies]]", "[[CTX-001-depviewer-context]]", "[[DR-001-every-language-is-analysed-by-an-extractor-behind-one-versioned-contract]]", "[[DT-019-extractor]]", "[[DT-020-fragment]]", "[[DT-021-ecosystem]]", "[[DT-022-toolchain]]", "[[DT-023-provenance]]", "[[DT-024-exact]]", "[[FR-010-choose-the-toolchain-in-a-fixed-order]]", "[[FR-011-record-the-provenance-of-every-language-and-module]]", "[[FR-012-link-the-modules-of-a-monorepo]]", "[[FR-051-analyse-more-languages]]", "[[FR-052-analyse-kotlin-with-the-kotlin-compiler]]", "[[FR-053-analyse-go]]", "[[FR-054-analyse-c]]", "[[FR-055-analyse-python]]", "[[FR-056-analyse-swift]]", "[[ISS-005-is-the-extractor-request-sent-on-stdin-or-in-a-file]]", "[[ISS-006-how-is-the-projects-kotlin-version-read-without-running-gradle]]", "[[ISS-007-should-the-provenance-record-the-extractors-own-version]]", "[[ISS-008-moving-java-and-kotlin-into-the-jvm-extractor-can-change-the-output]]", "[[ISS-009-results-can-differ-between-machines]]", "[[QR-006-internal-refactors-do-not-change-the-output]]", "[[SOL-002-each-language-is-analysed-by-its-own-toolchain]]"]
---

# ADR 0001 architecture for language support

Accepted decision of 2026-11-26: every language as an extractor behind a versioned contract, with toolchain order, provenance and migration plan.
