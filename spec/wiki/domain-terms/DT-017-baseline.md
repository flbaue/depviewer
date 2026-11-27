---
id: DT-017
type: domain-term
title: Baseline
status: draft
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-002-spec-architecture-file-and-rule-checks]]"]
related: []
tags: [domain-term]
aliases: [ratchet]
bounded-context: depviewer
agreed: false
stereotype:
parent: []
source-of-truth:
attributes: []
relationships: []
---

# Baseline

**Definition.** A committed, sorted JSON list of the fingerprints of violations a team accepts for now.

**In context.** Baselined violations never fail the check. Entries that no longer occur are reported as resolved, so the baseline can only shrink (ratchet).

**Distinguish from.** The [[DT-014-architecture-file]], which holds the intended architecture.
