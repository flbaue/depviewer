---
id: ISS-004
type: issue
title: "Should name-based violations fail the build by default?"
status: resolved
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-002-spec-architecture-file-and-rule-checks]]"]
related: ["[[FR-004-analyse-kotlin-by-name]]", "[[FR-036-provide-the-check-command-and-its-exit-codes]]", "[[FR-038-report-violations-as-versioned-json]]", "[[ISS-010-name-based-kotlin-can-miss-or-misplace-violations]]"]
tags: [issue]
severity: major
kind: question
category: requirements
raised-by: agent
owner: "[[STK-004-maintainer]]"
resolved: 2026-11-27
---

# Should name-based violations fail the build by default?

**What's unresolved.** Violations with `exact: false` (Kotlin today) may be wrong. It is open whether they fail by default or only warn.

**Affects.** [[FR-036-provide-the-check-command-and-its-exit-codes]], [[FR-038-report-violations-as-versioned-json]], [[FR-004-analyse-kotlin-by-name]].

**Context / evidence.** Open question in [[SRC-002-spec-architecture-file-and-rule-checks]]. Related risk [[ISS-010-name-based-kotlin-can-miss-or-misplace-violations]].

**Options.**
1. Fail like exact ones. Simple, but a wrong edge can block a build.
2. Report them as warnings unless a flag says otherwise.
3. Fail, but let a rule opt out with a field.

**Resolution.** Option 1, decided by the maintainer on 2026-11-27: name-based violations fail like exact ones. A wrong one goes into the baseline, or the rule uses severity `warning`, and `exact: false` stays visible in the report. Applied in [[FR-036-provide-the-check-command-and-its-exit-codes]]. The risk itself stays open in [[ISS-010-name-based-kotlin-can-miss-or-misplace-violations]].
