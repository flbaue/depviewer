---
id: QR-004
type: quality-requirement
title: No dependency is invented
status: draft
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-012-spec-kotlin-support]]", "[[SRC-008-usage-reference]]"]
related: []
tags: [quality-requirement]
dimensions: [reliable]
priority: must
testable: true
---

# No dependency is invented

**Q42 dimensions.** Reliable.

depviewer prefers missing a dependency over reporting a wrong one, because a wrong edge produces a wrong violation.

**Scenario (Q42 quality scenario).**  <!-- follows [[Q42]], https://quality.arc42.org -->
- **Source.** A scan.
- **Stimulus.** Meets a reference it cannot resolve to exactly one declaration.
- **Environment.** Any language, with or without the library jars on the classpath.
- **Response.** No edge is produced. Unresolved Java references are listed under *External › (unresolved)*.
- **Measure.** The model checks of every fixture (`test/checks/*.py`) find no edge that the fixture does not contain.

**Applies to.** [[FR-001-scan-a-code-base-into-one-model]] and every language requirement.
**Tensions.** Completeness: name-based Kotlin misses some dependencies ([[ISS-010-name-based-kotlin-can-miss-or-misplace-violations]]).
