---
id: DT-018
type: domain-term
title: Fingerprint
status: accepted
created: 2026-11-27
updated: 2026-11-28
sources: ["[[SRC-002-spec-architecture-file-and-rule-checks]]"]
related: []
tags: [domain-term]
aliases: []
bounded-context: depviewer
agreed: false
stereotype:
parent: []
source-of-truth:
attributes: []
relationships: []
---

# Fingerprint

**Definition.** The stable identity of a violation: view, rule, source element and target element, without line numbers or dependency kinds.

**In context.** Stays the same when code moves within a file, so a baseline survives ordinary edits.

**Distinguish from.** The violation's lines, which change with every edit above it.
