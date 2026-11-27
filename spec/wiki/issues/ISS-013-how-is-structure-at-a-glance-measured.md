---
id: ISS-013
type: issue
title: "How is \"structure at a glance\" measured?"
status: resolved
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-005-readme-of-depviewer]]"]
related: ["[[GOAL-005-the-real-structure-of-a-code-base-at-a-glance]]"]
tags: [issue]
severity: minor
kind: gap
category: requirements
raised-by: agent
owner: "[[STK-004-maintainer]]"
resolved: 2026-11-27
---

# How is "structure at a glance" measured?

**What's unresolved.** [[GOAL-005-the-real-structure-of-a-code-base-at-a-glance]] has no metric.

**Affects.** [[GOAL-005-the-real-structure-of-a-code-base-at-a-glance]].

**Context / evidence.** Taken from the README's "Why" in [[SRC-005-readme-of-depviewer]], which names no number.

**Options.**
1. Time for a new user to find the fixture's cycle with the quick start.
2. Drop the metric and keep the goal as a vision objective.

**Resolution.** A new option, decided by the maintainer on 2026-11-27: the metric is the steps from a checkout to a viewer that marks every package cycle, with the target of one `depviewer` command with only a JDK and cycles marked on first open. Applied in [[GOAL-005-the-real-structure-of-a-code-base-at-a-glance]].
