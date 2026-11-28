---
id: CON-004
type: constraint
title: The architecture file is JSON and a superset of the views format
status: accepted
created: 2026-11-27
updated: 2026-11-28
sources: ["[[SRC-002-spec-architecture-file-and-rule-checks]]"]
related: ["[[ISS-002-does-the-json-reader-need-comment-support]]"]
tags: [constraint]
category: technical
---

# The architecture file is JSON and a superset of the views format

**Constraint.** The architecture file is JSON, read by the core's own reader. Every file exported by *Export views* is a valid architecture file without rules, and the viewer keeps loading it unchanged.

**Rationale / origin.** Follows from [[CON-001-the-scanner-core-needs-only-a-jdk-17]]: no YAML or custom DSL without a library. Existing exports must stay valid.

**Impact.** People edit JSON by hand, but comments are not supported: they would be lost on every export. Rules carry a `description` instead ([[ISS-002-does-the-json-reader-need-comment-support]]). Unknown fields are ignored and preserved ([[FR-026-read-the-architecture-file]]).
