---
id: QR-007
type: quality-requirement
title: Names from scanned code cannot inject markup
status: accepted
created: 2026-11-27
updated: 2026-11-28
sources: ["[[SRC-004-guide-for-ai-coding-agents]]"]
related: []
tags: [quality-requirement]
dimensions: [secure]
priority: must
testable: true
---

# Names from scanned code cannot inject markup

**Q42 dimensions.** Secure.

Names come from scanned code and user input, so they are never trusted.

**Scenario (Q42 quality scenario).**  <!-- follows [[Q42]], https://quality.arc42.org -->
- **Source.** A scanned project.
- **Stimulus.** Contains a name such as `</script><img src=x onerror=alert(1)>`.
- **Environment.** The viewer opened from `file://`.
- **Response.** The name shows as text.
- **Measure.** No script runs and the embedded data does not close the `<script>` element.

**Applies to.** [[BB-013-viewer]], [[CC-004-names-from-scanned-code-are-untrusted]].
**Tensions.** None known.

> [!assumption]
> No automated check with such a name exists yet. The rule itself is in AGENTS.md.
