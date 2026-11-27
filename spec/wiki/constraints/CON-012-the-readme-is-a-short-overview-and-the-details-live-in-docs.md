---
id: CON-012
type: constraint
title: The README is a short overview and the details live in docs
status: draft
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-024-github-issue-8-clean-up-readme-short-overview-and-goal-move-details-into-separate]]", "[[SRC-004-guide-for-ai-coding-agents]]"]
related: []
tags: [constraint]
category: organizational
---

# The README is a short overview and the details live in docs

**Constraint.** The README answers only what depviewer is, why to use it and how to try it, in about 40 to 60 lines: description, screenshot, one line per language, three lines of usage, a short AI notice, contributing and license. Everything else lives in `docs/` (`quick-start.md`, `usage.md`, `languages.md`, `viewer.md`, `how-it-was-built.md`) and is linked from the README. Text is not repeated between the README, the quick start and the reference docs.

**Rationale / origin.** The README had grown into a 120-line manual, so a newcomer had to read a lot before knowing whether depviewer was for them. The maintainer ([[STK-004-maintainer]]) moved the details out in issue 8 and kept the rule in the agent guide's workflow.

**Impact.** A behaviour change updates the matching file in `docs/`, and the README only when the overview changes. Repeated text would drift apart, so the docs link to each other instead.
