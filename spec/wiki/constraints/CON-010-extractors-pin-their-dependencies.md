---
id: CON-010
type: constraint
title: Extractors pin their dependencies
status: draft
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-011-spec-multi-language-architecture]]", "[[SRC-003-adr-0001-architecture-for-language-support]]"]
related: []
tags: [constraint]
category: technical
---

# Extractors pin their dependencies

**Constraint.** Each extractor lives in `extractors/<language>/`, is written in the language of its toolchain and commits its lock file (`package-lock.json`, `pubspec.lock`).

**Rationale / origin.** Results must not change because a dependency moved.

**Impact.** Supported toolchain version ranges are tested in CI with the oldest and newest version ([[DR-001-every-language-is-analysed-by-an-extractor-behind-one-versioned-contract]]).
