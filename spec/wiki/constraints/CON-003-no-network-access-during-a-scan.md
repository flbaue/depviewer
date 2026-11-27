---
id: CON-003
type: constraint
title: No network access during a scan
status: draft
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-001-vision-and-goal-of-depviewer]]", "[[SRC-011-spec-multi-language-architecture]]", "[[SRC-003-adr-0001-architecture-for-language-support]]"]
related: []
tags: [constraint]
category: technical
---

# No network access during a scan

**Constraint.** A scan does not use the network. The only exceptions are the one-time install of an extractor's pinned dependencies and, once [[FR-010-choose-the-toolchain-in-a-fixed-order]] is built, a pinned toolchain download. Both are announced on stderr and can be done ahead of time.

**Rationale / origin.** The code being scanned may be confidential, and depviewer must work offline.

**Impact.** Toolchains come from the user's machine first ([[DR-001-every-language-is-analysed-by-an-extractor-behind-one-versioned-contract]]). [[EIF-006-package-registries]] is the only network neighbour.
