---
id: QR-010
type: quality-requirement
title: Every delivered viewer requirement has an automated UI scenario
status: accepted
created: 2026-11-27
updated: 2026-11-28
sources: ["[[SRC-036-chat-a-go-scanner-core-and-playwright-ui-tests-for-the-viewer]]"]
related: ["[[CC-005-spec-first-then-test-driven]]"]
tags: [quality-requirement]
dimensions: [maintainable, reliable]
priority: should
testable: true
---

# Every delivered viewer requirement has an automated UI scenario

**Q42 dimensions.** Maintainable, reliable.

Viewer interactions are checked by tests that click, not only by hand.

**Scenario (Q42 quality scenario).**  <!-- follows [[Q42]], https://quality.arc42.org -->
- **Source.** A [[STK-005-contributors|contributor]] or an [[STK-003-ai-coding-agents|AI coding agent]].
- **Stimulus.** Changes `viewer.html` and runs `scripts/check.sh`.
- **Environment.** A machine or CI runner with Node and Chrome.
- **Response.** UI scenarios drive the generated viewer in Chrome and fail when an interaction breaks.
- **Measure.** Each delivered viewer requirement has at least one scenario: [[FR-014-roll-up-dependencies-of-collapsed-rows]] to [[FR-024-share-views-as-a-file]], [[FR-068-show-the-kind-and-language-of-every-row]], and restoring state from browser storage ([[CC-008-themes-and-browser-storage-in-the-viewer]]). The scenarios of [[QR-008-the-viewer-reads-well-in-light-dark-and-on-a-narrow-screen]] run in light, dark and at 320 CSS pixels. A new viewer requirement counts as delivered only once its scenario exists.

**Applies to.** [[BB-013-viewer]].
**Tensions.** How things look is still checked by hand. A scenario cannot judge it.
