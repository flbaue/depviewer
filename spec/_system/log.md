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

## [2026-11-27] inspect | Whole wiki
- Clean: broken links, orphans, index coverage, empty sources, FR children lists, architecture state fields, notation blocks, metamodel lint, concept clusters.
- Auto-fixed: 6 source hashes (SRC-004, SRC-005, SRC-006, SRC-009, SRC-010, SRC-034) that changed only by the link edits of the previous capture; 3 missing reciprocal `related:` links (ISS-010 to FR-061, FR-052 to FR-070, DT-020 to CC-009).
- For the human: `delivery.py` cannot run because the vault is not the Git root; 46 of 70 FRs are `Must`; no objective has a baseline or horizon; 8 FRs have no building block. QR-003, QR-008 and GOAL-005 lack numbers, already tracked in ISS-011, ISS-012, ISS-013. 13 issues stay open.

## [2026-11-27] inspect | Closing round
- Priorities: [[FR-040-show-rule-violations-in-the-viewer]], [[FR-041-show-pattern-assignments-as-by-pattern]], [[FR-042-mark-arcs-that-violate-a-rule]], [[FR-043-keep-rules-and-patterns-on-export]] lowered from Must to Should.
- Goals: [[GOAL-002-teams-hold-new-code-to-their-architecture-in-ci]] and [[GOAL-004-one-answer-in-every-interface]] get horizon `m1-rules-and-check`, [[GOAL-003-ai-agents-check-and-explain-their-own-changes]] `m3-agent-interface`; all three the baseline "no check exists".
- FRs without a building block stay as they are.
- `delivery.py`: fix proposed in a pull request (it changes `_system/`).

## [2026-11-27] inspect | Whole wiki, after delivery.py fix
- Clean: broken links, `related:` reciprocity, concept clusters, index coverage, empty sources, ID/filename match, source hashes (all 35 match), metamodel lint.
- `delivery.py` now runs: all 70 FRs skipped, none has `delivery` set, so 30 FRs on release `delivered` have no delivery baseline to check drift against.
- Orphan: [[SRC-024-github-issue-8-clean-up-readme-short-overview-and-goal-move-details-into-separate]] (README cleanup) produced no pages.
- Still open: QR-003 and QR-008 untestable (tracked in ISS-011, ISS-012); 42 of 70 FRs `Must`. No fixes, no new issues; 13 issues open.

## [2026-11-27] implement | Delivery baselines for the delivered requirements
- Check only, no code changed. Evidence: `scripts/check.sh` locally (TypeScript skipped, no Node), CI run 46066060043 on `d9309bd` (all languages), throwaway scans for criteria the fixtures miss, and the viewer criteria tried by hand in a browser. Confirmed by the maintainer.
- `delivery: implemented`: FR-002, FR-003, FR-004, FR-006, FR-007, FR-008, FR-009, FR-014, FR-015, FR-016, FR-017, FR-019, FR-022, FR-023, FR-024, FR-060, FR-061, FR-062, FR-064, FR-065, FR-066.
- Not recorded: [[FR-067-choose-dart-source-folders-and-skip-files-that-cannot-be-analysed]] fails (one exclude note per scan, not per module); [[FR-018-find-elements-by-name]] stops at the file, the criterion says type; [[FR-005-analyse-typescript-and-javascript-with-the-typescript-compiler]] (path alias), [[FR-063-map-typescript-and-javascript-declarations-to-model-elements]] (unexported function), [[FR-068-show-the-kind-and-language-of-every-row]] (TypeScript rows) and [[FR-020-open-a-model-in-the-viewer]] (drop) not verified.

## [2026-11-27] implement | FR-067 Choose Dart source folders and skip files that cannot be analysed
- The Dart extractor now prints the exclude note once per module, naming it. `check.sh` asserts it on the fixture's two packages; the model of the fixture is identical to the previous extractor's.
- Both criteria pass: excluded files are missing with one note per module (`check.sh`), `integration_test/` files carry the test flag (throwaway scan). `delivery: implemented`.

## [2026-11-27] clarify | FR-018 acceptance criterion
- The maintainer chose to fix the wording, not the viewer: *Find…* jumps to the first matching row (a file at Packages level), not to a type. [[FR-018-find-elements-by-name]] criterion reworded; behaviour unchanged.

## [2026-11-27] implement | FR-018 Find elements by name
- Check only, no code changed. The reworded criterion passes: in `build/fixture.html` at Packages, `Repository` and Enter expanded `infra` and selected `InMemoryOrderRepository.java` (tried by hand in a browser). `delivery: implemented`, confirmed by the maintainer.

## [2026-11-27] clarify | FR-068 acceptance criterion
- The maintainer chose to reword, not restructure the fixture: the folder-chain criterion of [[FR-068-show-the-kind-and-language-of-every-row]] now names `features/payments` from `test/fixture-ts/`, which `check.sh` already checks. Behaviour unchanged.

## [2026-11-27] implement | FR-005, FR-063 and FR-068 delivery
- Check only, no scanner or viewer code changed. [PR 19](https://github.com/flbaue/depviewer/pull/19) added a tsconfig path alias and an unexported top-level function to `test/fixture-ts/` with checks; the alias check fails without the `paths` entry.
- Evidence: `scripts/check.sh` with Node 26 locally and CI on PR 19, all passing. [[FR-005-analyse-typescript-and-javascript-with-the-typescript-compiler]] (`typescript.py`, path alias), [[FR-063-map-typescript-and-javascript-declarations-to-model-elements]] (kinds, unexported function), [[FR-068-show-the-kind-and-language-of-every-row]] (`K` and `TS` icons, `features/payments` row). `delivery: implemented`, confirmed by the maintainer.

## [2026-11-27] implement | FR-020 Open a model in the viewer
- Check only, no code changed. In a browser, a `deps.json` of `test/fixture` dropped on an empty `viewer.html` showed the same tree as the HTML of the same scan: 23 rows, 27 arcs, same header and rows. The drop was a scripted drop event carrying a real `File`, not a drag from the operating system. `delivery: implemented`, confirmed by the maintainer.

## [2026-11-27] inspect | Whole wiki, after the delivery baselines
- Clean: broken links, `related:` reciprocity, concept clusters, index coverage, empty sources, source hashes, metamodel lint. `delivery.py`: 27 FRs with no drift, the rest have no delivery record (not delivered yet, or features).
- Parents FR-004, FR-005, FR-006 and all their children are `implemented`.
- Unchanged from the previous inspect: orphan [[SRC-024-github-issue-8-clean-up-readme-short-overview-and-goal-move-details-into-separate]]; QR-003 and QR-008 untestable (ISS-011, ISS-012); 42 of 70 FRs `Must`; 13 issues open. No fixes, no new issues.

## [2026-11-27] capture | SRC-024 README cleanup as a standing rule
- Source: [[SRC-024-github-issue-8-clean-up-readme-short-overview-and-goal-move-details-into-separate]], with [[SRC-004-guide-for-ai-coding-agents]]. Grill gate: the maintainer chose in chat to capture the issue as a constraint rather than leave the source orphaned.
- Created: [[CON-012-the-readme-is-a-short-overview-and-the-details-live-in-docs]] (organizational). Updated: `ingested-pages` of SRC-024 and SRC-004, index.

## [2026-11-27] clarify | ISS-001 to ISS-004: architecture file and check
- Decided by the maintainer, each the recommended option: `check` finds `architecture.json` without `--rules` ([[ISS-001-should-check-find-architecture-json-on-its-own]]), plain JSON, no comments ([[ISS-002-does-the-json-reader-need-comment-support]]), layers ignore ungrouped code ([[ISS-003-how-does-ungrouped-code-relate-to-a-layers-rule]]), name-based violations fail like exact ones ([[ISS-004-should-name-based-violations-fail-the-build-by-default]]).
- Updated: [[FR-026-read-the-architecture-file]], [[FR-030-enforce-a-layer-order]], [[FR-036-provide-the-check-command-and-its-exit-codes]] (with new criteria), [[CON-004-the-architecture-file-is-json-and-a-superset-of-the-views-format]], [[BB-005-json-reader]]. None has a delivery record, so no drift.
- Still open: 9 issues. [[ISS-010-name-based-kotlin-can-miss-or-misplace-violations]] stays open as a risk.

## [2026-11-27] clarify | ISS-011 to ISS-013: missing measures
- Decided by the maintainer, each the recommended option. [[ISS-011-how-long-may-the-check-take]]: `check` takes at most 1.2 times a scan on a project with at least 50,000 dependencies. [[ISS-012-what-makes-the-viewer-readable-on-a-narrow-screen]]: no sideways scrolling outside the tree at 320 CSS pixels, text contrast at least 4.5:1 in light and dark. [[ISS-013-how-is-structure-at-a-glance-measured]]: one `depviewer` command with only a JDK, every package cycle marked on first open.
- Updated: [[QR-003-the-check-takes-about-as-long-as-a-scan]] and [[QR-008-the-viewer-reads-well-in-light-dark-and-on-a-narrow-screen]] (now `testable: true`), [[GOAL-002-teams-hold-new-code-to-their-architecture-in-ci]], [[GOAL-005-the-real-structure-of-a-code-base-at-a-glance]].
- Every quality requirement now has a measure. Still open: 6 issues, all about the extractor contract and Kotlin.

## [2026-11-27] clarify | ISS-005 to ISS-010: extractor contract and Kotlin
- Decided by the maintainer, each the recommended option. [[ISS-005-is-the-extractor-request-sent-on-stdin-or-in-a-file]]: request file. [[ISS-006-how-is-the-projects-kotlin-version-read-without-running-gradle]]: parse the `plugins` block or `gradle/libs.versions.toml`, never run Gradle. [[ISS-007-should-the-provenance-record-the-extractors-own-version]]: provenance records the extractor's name and version. [[ISS-008-moving-java-and-kotlin-into-the-jvm-extractor-can-change-the-output]]: mitigated by QR-006. [[ISS-009-results-can-differ-between-machines]]: accepted, visible through provenance. [[ISS-010-name-based-kotlin-can-miss-or-misplace-violations]]: accepted, FR-052 stays in `m4-more-languages`.
- Updated: [[CC-002-extractor-contract]], [[FR-010-choose-the-toolchain-in-a-fixed-order]], [[FR-011-record-the-provenance-of-every-language-and-module]], [[FR-052-analyse-kotlin-with-the-kotlin-compiler]], [[DT-023-provenance]], [[BB-008-kotlin-scanner]], [[BB-011-jvm-extractor]]. No page with a delivery record changed. The accepted [[DR-001-every-language-is-analysed-by-an-extractor-behind-one-versioned-contract]] is unchanged.
- No issue is open.

## [2026-11-27] inspect | Whole wiki, after all issues were settled
- Clean: broken links, orphans, `related:` reciprocity, concept clusters, index coverage, sources, source hashes, metamodel lint. `delivery.py`: 27 FRs, no drift. No issue is open.
- Auto-fixed: removed the stale "Open points" notes on [[FR-026-read-the-architecture-file]], [[FR-030-enforce-a-layer-order]] and [[FR-036-provide-the-check-command-and-its-exit-codes]], which pointed at the resolved ISS-001 to ISS-004.
- Priorities: 42 of 70 FRs are `Must`, but 25 of them are delivered. Of the 40 undelivered FRs, 17 are `Must`, all in `m1-rules-and-check` and `m2-extractor-contract`.
- For the human: [[QR-008-the-viewer-reads-well-in-light-dark-and-on-a-narrow-screen]] now has a measure, but nothing checks it yet. [[FR-004-analyse-kotlin-by-name]] keeps its note on ISS-010, which is still true and unchanged to avoid delivery drift.
