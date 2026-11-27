---
id: ISS-002
type: issue
title: "Does the JSON reader need comment support?"
status: resolved
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-002-spec-architecture-file-and-rule-checks]]"]
related: ["[[BB-005-json-reader]]", "[[CON-004-the-architecture-file-is-json-and-a-superset-of-the-views-format]]", "[[FR-026-read-the-architecture-file]]"]
tags: [issue]
severity: minor
kind: question
category: architecture
raised-by: agent
owner: "[[STK-004-maintainer]]"
resolved: 2026-11-27
---

# Does the JSON reader need comment support?

**What's unresolved.** People will edit the architecture file by hand and want to explain rules, but plain JSON has no comments.

**Affects.** [[CON-004-the-architecture-file-is-json-and-a-superset-of-the-views-format]], [[BB-005-json-reader]], [[FR-026-read-the-architecture-file]].

**Context / evidence.** Open question in [[SRC-002-spec-architecture-file-and-rule-checks]]. Rules already have a `description` field.

**Options.**
1. Accept `//` comments (JSONC) in the reader. Comments are lost when the viewer exports.
2. Plain JSON only, use `description`. Nothing to lose on export.

**Resolution.** Option 2, decided by the maintainer on 2026-11-27: plain JSON only, rules explain themselves in `description`, so nothing is lost on export. Applied in [[FR-026-read-the-architecture-file]], [[CON-004-the-architecture-file-is-json-and-a-superset-of-the-views-format]] and [[BB-005-json-reader]].
