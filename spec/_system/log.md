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

## [2026-11-27] capture | docs/specs, docs/adr, GitHub issues and the claude.ai project files
- Decided by the maintainer before the capture: the wiki replaces `docs/specs/` and `docs/adr/` and becomes the backlog; the user docs stay in `docs/`.
- Archived: the six specs to `raw/ingested/docs-specs/`, ADR 0001 to `raw/ingested/docs-adr/`, `vision.md` and `architecture rules.md` (renamed `claude-project-vision.md`, `claude-project-architecture-rules.md`), and snapshots of GitHub issues 1 to 17 with comments to `raw/ingested/github-issues/`. Origins and hashes updated in [[SRC-001-vision-and-goal-of-depviewer]], [[SRC-002-spec-architecture-file-and-rule-checks]], [[SRC-003-adr-0001-architecture-for-language-support]], [[SRC-011-spec-multi-language-architecture]] to [[SRC-016-spec-viewer-performance-on-large-projects]].
- New sources: [[SRC-017-github-issue-1-viewer-arcs-cover-the-tree-labels-on-large-projects]] to [[SRC-033-github-issue-17-language-swift]] (one per issue), [[SRC-034-quick-start-guide]], [[SRC-035-how-depviewer-was-built]].
- Created: [[FR-060-map-kotlin-declarations-to-model-elements]] to [[FR-068-show-the-kind-and-language-of-every-row]] (the spec details the first capture had summarised away), [[FR-069-give-native-platform-modules-of-flutter-apps-useful-names]] (issue 3), [[FR-070-read-dependencies-from-compiled-classes]] (issue 5), [[CC-009-fragment-format]] (current, superseded by [[CC-002-extractor-contract]]).
- Updated: parents FR-004, FR-005, FR-006 list their children; bare `#N` issue references in FRs, DR-001 and CC-002 now link the issue's source page (link fix only on the accepted DR-001, no change in meaning); CC-005 describes the wiki as the spec.
- Resolved: [[ISS-014-does-this-wiki-replace-docs-specs-and-docs-adr]] (option 1), [[ISS-015-the-vision-and-the-architecture-rules-spec-are-not-in-the-repository]].
- Outside `spec/`: root AGENTS.md, CONTRIBUTING.md, README and code comments point to wiki pages instead of `docs/specs/`.
- Grill gate: the maintainer answered the scope questions in chat; the closing round for FR-069 and FR-070 is pending.

## [2026-11-27] capture | Closing round for GitHub issues 3 and 5
- [[FR-069-give-native-platform-modules-of-flutter-apps-useful-names]]: the maintainer chose `android (<app>)` as module name and skipping `GeneratedPluginRegistrant.java`, no `--no-native`. Requirement and acceptance criteria updated.
- [[FR-070-read-dependencies-from-compiled-classes]]: stays its own requirement, not an option of [[FR-052-analyse-kotlin-with-the-kotlin-compiler]].
- No issues raised.
