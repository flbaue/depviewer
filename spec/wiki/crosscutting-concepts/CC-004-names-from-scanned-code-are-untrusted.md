---
id: CC-004
type: crosscutting-concept
title: Names from scanned code are untrusted
status: accepted
created: 2026-11-27
updated: 2026-11-28
sources: ["[[SRC-004-guide-for-ai-coding-agents]]"]
related: []
tags: [crosscutting-concept]
category: safety-security
applies-to: ["[[BB-013-viewer]]", "[[BB-004-model-and-json-writer]]"]
drivers: ["[[QR-007-names-from-scanned-code-cannot-inject-markup]]"]
decisions: []
state: current
superseded-by:
---

# Names from scanned code are untrusted

> follows [[arc42]] (section 8)

## Concept


- Everything the viewer inserts with `innerHTML` goes through `esc()`.
- The scanner's `js()` escapes `<`, `>` and `&` in the embedded model, so the data cannot close the `<script>` element.
- Names are never interpolated raw, whether they come from scanned code or from user input such as group names.
