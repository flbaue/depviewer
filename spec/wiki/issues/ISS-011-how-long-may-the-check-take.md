---
id: ISS-011
type: issue
title: "How long may the check take?"
status: resolved
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-001-vision-and-goal-of-depviewer]]"]
related: ["[[GOAL-002-teams-hold-new-code-to-their-architecture-in-ci]]", "[[QR-003-the-check-takes-about-as-long-as-a-scan]]"]
tags: [issue]
severity: minor
kind: gap
category: requirements
raised-by: agent
owner: "[[STK-004-maintainer]]"
resolved: 2026-11-27
---

# How long may the check take?

**What's unresolved.** "About the time of a scan" is not a measure, so [[QR-003-the-check-takes-about-as-long-as-a-scan]] and [[GOAL-002-teams-hold-new-code-to-their-architecture-in-ci]] cannot be tested.

**Affects.** [[QR-003-the-check-takes-about-as-long-as-a-scan]], [[GOAL-002-teams-hold-new-code-to-their-architecture-in-ci]].

**Context / evidence.** Success criterion in [[SRC-001-vision-and-goal-of-depviewer]].

**Options.**
1. Check time at most 1.2 × scan time on a named real project.
2. An absolute limit, for example 60 s for 100,000 dependencies.

**Resolution.** Option 1, decided by the maintainer on 2026-11-27: `check` on a project directory takes at most 1.2 times a plain scan of the same project, on a project with at least 50,000 dependencies. Applied in [[QR-003-the-check-takes-about-as-long-as-a-scan]] and [[GOAL-002-teams-hold-new-code-to-their-architecture-in-ci]].
