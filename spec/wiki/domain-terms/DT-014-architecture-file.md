---
id: DT-014
type: domain-term
title: Architecture file
status: draft
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-002-spec-architecture-file-and-rule-checks]]", "[[SRC-001-vision-and-goal-of-depviewer]]"]
related: []
tags: [domain-term]
aliases: [architecture.json, rules file]
bounded-context: depviewer
agreed: false
stereotype:
parent: []
source-of-truth:
attributes: []
relationships: []
---

# Architecture file

**Definition.** The committed JSON file, by default `architecture.json` in the project root, that holds views, groups, selectors and rules.

**In context.** Edited by hand and partly in the viewer, read by `check`. A superset of the exported views format ([[CON-004-the-architecture-file-is-json-and-a-superset-of-the-views-format]]).

**Distinguish from.** The [[DT-017-baseline]], which holds accepted violations, not intent.
