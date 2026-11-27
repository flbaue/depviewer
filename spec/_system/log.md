# Log

Append-only. One entry per operation. Keep the prefix exact so the log is
greppable: `grep '^## \[' _system/log.md | tail -5`.

<!-- Format (indented so the grep above does not match it):
    ## [YYYY-MM-DD] <op> | <subject>
    - Source / scope, grill gate, created, updated, issues, open questions
-->

## [2026-11-27] new-project | depviewer
- Created in `spec/` of the depviewer repository from systems42-dashboard @ daa2285 (method files copied by hand, `new-project.sh` needs rsync). Left out: `docs/`, `.obsidian/`, `Makefile`, `CHANGELOG.md` and `_system/apps/`; `./dashboard.sh` fetches the pinned dashboard release.

## [2026-11-27] recover | Existing depviewer documentation
- Sources: [[SRC-001-vision-and-goal-of-depviewer]] to [[SRC-016-spec-viewer-performance-on-large-projects]], the vision and architecture-rules spec from the claude.ai project, ADR 0001, AGENTS.md, README, CONTRIBUTING, LICENSE, the user docs and the six implemented specs in `docs/specs/`.
- Grill gate: no grilling session with a human. The capture was done by an agent in one pass, so every page is `draft`, and the questions that came up are filed as issues instead of a closing round.
- Created: 5 goals, 5 stakeholders, 1 context, 6 external interfaces, 59 functional requirements (prose notation, 11 features), 8 quality requirements, 11 constraints, 24 domain terms, 4 solution strategies, 15 building blocks, 4 runtime scenarios, 6 deployment nodes, 8 crosscutting concepts, 1 decision record, 15 issues, 16 sources.
- ADR 0001 became [[DR-001-every-language-is-analysed-by-an-extractor-behind-one-versioned-contract]] (accepted, alias ADR-0001). Releases follow the priorities of [[SRC-001-vision-and-goal-of-depviewer]]: `delivered`, `m1-rules-and-check`, `m2-extractor-contract`, `m3-agent-interface`, `m4-more-languages`, `unscheduled`. `delivery` stays empty until an Implement run checks it.
- Open: [[ISS-001-should-check-find-architecture-json-on-its-own]] to [[ISS-015-the-vision-and-the-architecture-rules-spec-are-not-in-the-repository]]. The most important is [[ISS-014-does-this-wiki-replace-docs-specs-and-docs-adr]]: whether this wiki replaces `docs/specs/` and `docs/adr/`.

## [2026-11-27] cleanup | Fit the wiki into the depviewer repository
- Removed the `new-project` skill and the spawn, sync and migration scripts (`new-project.sh`, `test-new-project.sh`, `migrate-*.py`); they belong to the systems42 source repository.
- Named no commercial tools in [[GOAL-001-architecture-as-code-checked-against-the-real-dependencies]] and [[STK-001-software-architects]].
- The root `AGENTS.md` now points to `spec/`, and the root `.claude/skills/` links the wiki skills.
