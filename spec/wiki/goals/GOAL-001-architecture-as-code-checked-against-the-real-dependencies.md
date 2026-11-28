---
id: GOAL-001
type: goal
title: "Architecture as code, checked against the real dependencies"
status: accepted
created: 2026-11-27
updated: 2026-11-28
sources: ["[[SRC-001-vision-and-goal-of-depviewer]]", "[[SRC-005-readme-of-depviewer]]"]
related: ["[[ISS-015-the-vision-and-the-architecture-rules-spec-are-not-in-the-repository]]"]
tags: [goal]
stereotype: vision
parent: []
beneficiary: ["[[STK-001-software-architects]]", "[[STK-002-developers]]", "[[STK-003-ai-coding-agents]]"]
metric:
baseline:
target:
horizon:
short_title: Architecture as code
tile_claim: "Architecture as code, checked against compiler-exact dependencies."
---

# Architecture as code, checked against the real dependencies

## Vision

For **software architects, developers and AI coding agents** who need to keep a code base in the
structure they intended, **depviewer** is an **open-source architecture tool** that shows the real,
compiler-exact dependency structure, holds the intended architecture in one committed file and
reports every place where the code goes against it: in the browser, on the command line, in CI and
for agents.

Unlike commercial architecture tools, it is free, needs only a JDK for the core and works offline.

**Objectives.** see `parent:` backlinks.

## Impact

- **Addressed stakeholders:** see `beneficiary:`.
- **Served by:** projected from the FR backlinks.
