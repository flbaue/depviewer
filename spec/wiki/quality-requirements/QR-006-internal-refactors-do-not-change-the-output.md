---
id: QR-006
type: quality-requirement
title: Internal refactors do not change the output
status: accepted
created: 2026-11-27
updated: 2026-11-28
sources: ["[[SRC-003-adr-0001-architecture-for-language-support]]", "[[SRC-004-guide-for-ai-coding-agents]]", "[[SRC-011-spec-multi-language-architecture]]"]
related: ["[[ISS-008-moving-java-and-kotlin-into-the-jvm-extractor-can-change-the-output]]"]
tags: [quality-requirement]
dimensions: [maintainable, reliable]
priority: must
testable: true
---

# Internal refactors do not change the output

**Q42 dimensions.** Maintainable, reliable.

Moving code between building blocks, such as Java and Kotlin into the jvm extractor, must not change what users get.

**Scenario (Q42 quality scenario).**  <!-- follows [[Q42]], https://quality.arc42.org -->
- **Source.** The [[STK-004-maintainer|maintainer]].
- **Stimulus.** Replaces an internal path of the scanner.
- **Environment.** The fixtures and a large real project.
- **Response.** The new version produces the same model.
- **Measure.** The JSON of Java- and Kotlin-only scans is identical before and after, ignoring `generated` and the new `analysis` field.

**Applies to.** [[BB-011-jvm-extractor]], [[CC-005-spec-first-then-test-driven]].
**Tensions.** None known.
