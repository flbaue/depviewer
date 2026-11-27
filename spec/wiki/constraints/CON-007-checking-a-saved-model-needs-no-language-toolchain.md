---
id: CON-007
type: constraint
title: Checking a saved model needs no language toolchain
status: draft
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-002-spec-architecture-file-and-rule-checks]]"]
related: []
tags: [constraint]
category: technical
---

# Checking a saved model needs no language toolchain

**Constraint.** `depviewer check --model deps.json` checks a saved scan with nothing but the JDK.

**Rationale / origin.** CI and agents can check against a model produced elsewhere, without installing Node or Dart.

**Impact.** The model must carry everything rules need, including provenance ([[FR-011-record-the-provenance-of-every-language-and-module]]).
