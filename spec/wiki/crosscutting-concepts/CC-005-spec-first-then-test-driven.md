---
id: CC-005
type: crosscutting-concept
title: "Spec first, then test-driven"
status: accepted
created: 2026-11-27
updated: 2026-11-28
sources: ["[[SRC-035-how-depviewer-was-built]]", "[[SRC-004-guide-for-ai-coding-agents]]", "[[SRC-006-contributing-guide]]", "[[SRC-036-chat-a-go-scanner-core-and-playwright-ui-tests-for-the-viewer]]"]
related: ["[[ISS-014-does-this-wiki-replace-docs-specs-and-docs-adr]]", "[[QR-010-every-delivered-viewer-requirement-has-an-automated-ui-scenario]]"]
tags: [crosscutting-concept]
category: development
applies-to: []
drivers: ["[[QR-006-internal-refactors-do-not-change-the-output]]", "[[QR-004-no-dependency-is-invented]]"]
decisions: []
state: current
superseded-by:
---

# Spec first, then test-driven

> follows [[arc42]] (section 8)

## Concept


Every feature and behaviour change runs in this order (AGENTS.md):

1. Write or update the requirement in this wiki (`spec/`): goal, constraints, behaviour as checkable statements, out of scope.
2. Turn the statements into checks: fixture code plus assertions in `test/checks/<fixture>.py`, viewer checks in `scripts/check.sh`. Watch them fail.
3. Implement until green, then run the whole `scripts/check.sh` (javac `-Xlint:all -Werror`, model checks, HTML checks, headless Chrome smoke test).
4. Rescan a larger project with the previous and the new version and compare the JSON, ignoring `generated` ([[QR-006-internal-refactors-do-not-change-the-output]]).
5. Update the page status and the user docs in `docs/`.

Each language has a fixture with a deliberate cycle and some unresolvable or external references. CI runs every check with every toolchain, nothing skipped. A test that was wrong is fixed and the commit says so. A test is never weakened to make an implementation pass.

This wiki replaced `docs/specs/` and `docs/adr/` ([[ISS-014-does-this-wiki-replace-docs-specs-and-docs-adr]]). The viewer's interactive parts were tried by hand, not by automated UI tests ([[SRC-035-how-depviewer-was-built]]). Target: UI scenarios for every delivered viewer requirement ([[QR-010-every-delivered-viewer-requirement-has-an-automated-ui-scenario]]), written with Playwright Test, pinned in `test/ui/` with its own `package.json` and `package-lock.json`. They use the installed Chrome (`channel: 'chrome'`) and download no browser. `check.sh` runs them when Node and Chrome exist and says so when it skips them. CI never skips them. How things look is still tried by hand in light, dark and at phone width.
