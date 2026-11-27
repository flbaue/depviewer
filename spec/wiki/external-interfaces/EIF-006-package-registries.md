---
id: EIF-006
type: external-interface
title: Package registries
status: draft
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-011-spec-multi-language-architecture]]", "[[SRC-009-supported-languages]]"]
related: ["[[CTX-001-depviewer-context]]"]
tags: [external-interface]
partner: npm registry and pub.dev
tier:
short_title: npm / pub.dev
flows:
  - {data: extractor dependencies, direction: inbound, format: "npm ci, dart pub get", trigger: first use of an extractor, label: packages}
---

# Package registries

**Purpose.** Deliver the pinned dependencies of the TypeScript and Dart extractors.

**Flows.** On first use the core runs `npm ci` in `extractors/typescript/` or `dart pub get` in
`extractors/dart/`, announced on stderr. This can be done ahead of time for offline use. It is the
only network access of a scan ([[CON-003-no-network-access-during-a-scan]]).

**Belongs to.** [[CTX-001-depviewer-context]]
