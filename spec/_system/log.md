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

## [2026-11-27] implement | QR-008 The viewer reads well in light, dark and on a narrow screen
- Fixed in `viewer.html`: the view menu opens leftwards, so it fits at 320 CSS pixels, and colours were adjusted so all text reaches 4.5:1 in light and dark (darker badges and muted text in light, dark letters on badges and on the selected button in dark).
- New check `test/checks/viewer_contrast.py` in `check.sh` (48 colour pairs per theme). It failed before the fix. Width checked by hand at 320 pixels in a browser, screenshots regenerated.

## [2026-11-27] inspect | Whole wiki, after QR-008 was met
- Clean: broken links, orphans, `related:` reciprocity, concept clusters, index coverage, sources, metamodel lint. `delivery.py`: 27 FRs, no drift. No issue is open. No wiki page changed since the previous inspect.
- Source drift: [[SRC-004-guide-for-ai-coding-agents]] no longer matches `AGENTS.md`. The QR-008 commit added one sentence naming `viewer_contrast.py`; no new requirement. Hash updated, confirmed by the maintainer.
- [[QR-008-the-viewer-reads-well-in-light-dark-and-on-a-narrow-screen]]: contrast is now checked by `check.sh`; the 320-pixel width is checked by hand only.
- Unchanged: 42 of 70 FRs `Must`, 25 of them delivered. No fixes, no new issues.

## [2026-11-27] implement | FR-026 Read the architecture file
- New `src/depviewer/Architecture.java` loads and validates the architecture file. A bare `depviewer check [--rules <file>] (<project-dir>... | --model <file>)` finds `architecture.json` in the first project directory, prints `no rules` and exits 0 for a file without rules, and exits 2 for a missing or invalid file, an unknown major `version`, or a file with rules (rule checking is not built yet). `--views` uses the same loader.
- Evidence: new checks in `scripts/check.sh` (failed before the change on the unknown `--rules`), all passing with every language locally. Both criteria of [[FR-026-read-the-architecture-file]] pass. Scans of a Java project and of the fixture with `--views` are byte-identical to the previous scanner. `delivery: implemented`, pending the maintainer's review in the PR.
- Updated: [[BB-012-architecture-checker]] (`location`, `state: current`), `docs/usage.md`, `AGENTS.md`. Not in scope: viewer export of unknown top-level fields ([[FR-043-keep-rules-and-patterns-on-export]]), rule evaluation and the full `check` options ([[FR-029-check-the-code-against-architecture-rules]], [[FR-036-provide-the-check-command-and-its-exit-codes]]).

## [2026-11-27] implement | FR-027 Select code by patterns
- `src/depviewer/Architecture.java` parses `match` selectors (`module:`, `package:`, `path:`, `type:` with `@kind`, `ext:`, leading `!`) and selects nodes of the model JSON. `**` matches zero or more segments, decided by the maintainer: `package:com.example.domain.**` includes `com.example.domain`. A bad selector is rejected with exit 2 when the file is loaded.
- Evidence: new `test/checks/SelectorCheck.java` in `check.sh` (both criteria plus `*`, `!`, every kind, `/` packages) on the fixture with the new `com.example.domain.event`, and exit-code checks for bad selectors (the previous scanner accepted them). A Java project scans identically to the previous scanner. Both criteria of [[FR-027-select-code-by-patterns]] pass. `delivery: implemented`, pending the maintainer's review in the PR.
- Not in scope: precedence and overlap warnings ([[FR-028-decide-each-elements-group-by-precedence]]), embedding resolved assignments ([[FR-041-show-pattern-assignments-as-by-pattern]]). Open for FR-028: a `!path:` exclusion of a file is undone if its package is selected and the file inherits from it.

## [2026-11-27] capture | Chat: a Go scanner core and Playwright UI tests for the viewer
- Source: [[SRC-036-chat-a-go-scanner-core-and-playwright-ui-tests-for-the-viewer]], the maintainer's answers to seven grilling questions, written down by the agent at the maintainer's request.
- Created: [[DR-002-the-scanner-core-is-a-static-go-binary]] (draft, after the jvm extractor exists), [[CON-013-the-scanner-core-is-go-with-the-standard-library-only]], [[QR-009-a-project-scans-with-the-depviewer-binary-and-its-own-toolchain]], [[QR-010-every-delivered-viewer-requirement-has-an-automated-ui-scenario]], [[BB-016-go-scanner-core]], [[DN-007-depviewer-process]].
- Updated: [[CON-001-the-scanner-core-needs-only-a-jdk-17]] and [[QR-005-java-projects-scan-with-nothing-but-a-jdk]] (replaced once the Go core is delivered, still current until then), [[CON-009-no-new-tooling-or-dependencies-without-the-maintainer]] (Playwright approved), [[CC-005-spec-first-then-test-driven]] (UI scenarios with Playwright Test, no decision record by the maintainer's choice), [[BB-001-depviewer-wrapper-script]], [[BB-002-scanner-core]], [[BB-011-jvm-extractor]], [[SOL-003-zero-setup-and-small-enough-to-inspect]], [[EIF-002-jdk]], [[DN-002-jvm-process]].
- The accepted [[DR-001-every-language-is-analysed-by-an-extractor-behind-one-versioned-contract]] is unchanged. DR-002 links it one way. No issue raised: every question was answered.

## [2026-11-27] inspect | Whole wiki, after the Go core and UI test capture
- Clean: broken links, orphans, index coverage, unsourced claims, untestable QRs, metamodel lint, concept clusters. `delivery.py`: no drift.
- Auto-fixed: reciprocal `related:` on [[BB-016-go-scanner-core]] and [[DR-002-the-scanner-core-is-a-static-go-binary]] (to [[QR-009-a-project-scans-with-the-depviewer-binary-and-its-own-toolchain]]). [[CC-005-spec-first-then-test-driven]] step 3 no longer lists UI scenarios as part of `check.sh`, since they do not exist yet. The target sentence already covers them.
- For the human: [[DR-001-every-language-is-analysed-by-an-extractor-behind-one-versioned-contract]] lacks the back-link to DR-002 (accepted page, needs a PR). Source drift on [[SRC-004-guide-for-ai-coding-agents]] and [[SRC-008-usage-reference]]: the FR-026 and FR-027 deliveries documented `Architecture`, `SelectorCheck.java`, the `check` command and selectors. No new requirement. Hashes updated, confirmed by the maintainer.
- No new issues.

## [2026-11-28] implement | FR-028 Decide each element's group by precedence
- `Architecture.resolve()` in `src/depviewer/Architecture.java` gives each element its group: own `assign` entry, first selecting group, nearest ancestor's group. A `!` exclusion also blocks inheritance. Types have the key `T:<module>:<qualified name>`, and members go with their type. `check` scans the project (or reads `--model`) when a view has patterns and prints one `warning  overlap` line per overlap. Exit codes are unchanged.
- Decided by the maintainer before the build: an overlap also counts when a pattern overrides a group inherited from an ancestor's pattern, `!` blocks inheritance (the point left open by FR-027), and the viewer does not handle `T:` keys yet. Added by the agent and written into [[FR-028-decide-each-elements-group-by-precedence]]: no warning for a subgroup refining its parent group. A second criterion covers the exclusion.
- Evidence: `test/checks/AssignCheck.java` and new `check` cases in `scripts/check.sh`, with `test/fixture-architecture.json`. They failed before the change. All checks pass locally, Dart skipped (no SDK). Scans of depviewer's own sources and of the fixture are identical to the previous scanner. Both criteria pass. `delivery: implemented`, pending the maintainer's review in the PR.
- Updated: [[BB-012-architecture-checker]], `docs/usage.md`, `AGENTS.md`. Not in scope: `T:` keys and dragging a type alone in the viewer ([[FR-041-show-pattern-assignments-as-by-pattern]]), the other `check` options ([[FR-036-provide-the-check-command-and-its-exit-codes]]), `overlaps` in the JSON report ([[FR-038-report-violations-as-versioned-json]]).

## [2026-11-28] clarify | Review of all draft pages
- Read all 175 drafts with the maintainer. No open issues. Metamodel lint and `delivery.py` were clean before the run.
- Decided by the maintainer: accept [[DR-002-the-scanner-core-is-a-static-go-binary]]. A [[DT-018-fingerprint]] holds view, rule, source and target, without dependency kinds, because rule ids are unique only within a view. Dragging a by-pattern element creates a manual override and is never refused ([[FR-041-show-pattern-assignments-as-by-pattern]]). Provenance shows in the header and in a module row's details ([[FR-011-record-the-provenance-of-every-language-and-module]]). `--update-baseline` exits 0 ([[FR-039-accept-existing-violations-in-a-baseline]]). [[QR-007-names-from-scanned-code-cannot-inject-markup]] is accepted with its note that no automated check exists yet.
- Edited: [[CON-005-selectors-are-resolved-only-in-the-scanner-core]], [[SOL-001-one-model-three-interfaces]] and [[CON-007-checking-a-saved-model-needs-no-language-toolchain]] for the Go core, [[FR-038-report-violations-as-versioned-json]] links the fingerprint. Editorial, same meaning: the `severity` and `edgeKinds` defaults moved to [[FR-029-check-the-code-against-architecture-rules]] from [[FR-034-exempt-test-sources-by-default]], and [[FR-030-enforce-a-layer-order]] no longer repeats [[FR-033-require-every-element-to-be-grouped]]. [[DR-001-every-language-is-analysed-by-an-extractor-behind-one-versioned-contract]] gets the back-link to DR-002 in `related:`.
- Accepted by the maintainer: 163 pages. Still draft, because they are not specified yet: [[FR-045-query-dependents-dependencies-paths-and-cycles]], [[FR-047-serve-scan-query-and-check-over-mcp]], [[FR-049-report-violations-as-sarif]], [[FR-050-check-only-the-files-a-change-touches]], [[FR-052-analyse-kotlin-with-the-kotlin-compiler]], [[FR-053-analyse-go]], [[FR-054-analyse-c]], [[FR-055-analyse-python]], [[FR-056-analyse-swift]], [[FR-058-compute-structure-metrics-and-trends]], [[FR-059-diff-two-scans-or-branches]], [[FR-070-read-dependencies-from-compiled-classes]].
- `delivery.py` marked the 30 delivered FRs `outdated` for the status and date change. The maintainer reviewed the diff, confirmed the meaning did not change, and `implemented` was restored with a new check timestamp.

## [2026-11-28] implement | FR-029 Check the code against architecture rules
- Built [[FR-030-enforce-a-layer-order]], [[FR-031-forbid-allow-only-or-separate-dependencies]], [[FR-032-report-cycles-as-violations]], [[FR-033-require-every-element-to-be-grouped]] and [[FR-034-exempt-test-sources-by-default]] in `src/depviewer/Rules.java`, on the groups `Architecture.resolve()` decides. Rules are validated when the file loads: an unknown kind or group exits 2.
- Decided by the agent, as the pages leave it open: a use of a type and a call of one of its members from the same element are one violation, at the member, so `Order.total()` gives one `layering` violation, not two. `no-cycles` uses the package a file is in, and nested types are no cycle at `type` level. `no-ungrouped` reports the topmost element in scope with code below it and nothing grouped.
- Evidence: the `architecture rules` step of `scripts/check.sh`, on copies of the fixture in `build/rules/`. It failed before the change. All criteria pass.
- The maintainer set the milestone as the goal of the session and approved the plan, with the decisions below, in the chat after the build had started.

## [2026-11-28] implement | FR-035 Run the check from the command line and in CI
- Built [[FR-036-provide-the-check-command-and-its-exit-codes]], [[FR-037-report-violations-as-text]], [[FR-038-report-violations-as-versioned-json]] and [[FR-039-accept-existing-violations-in-a-baseline]] in `DepScan.check` and `src/depviewer/Report.java`.
- Decided by the agent: the JSON field `kind` holds the rule's kind; dependency kinds are in `refs`, because one violation can have several. `model.analysis` is written once the scanner records it ([[FR-011-record-the-provenance-of-every-language-and-module]]). `--update-baseline` writes to the `--baseline` file, which it then needs.
- Evidence: the `check command` step of `scripts/check.sh` and `test/checks/rules.py`. All criteria pass. Scans of depviewer's own sources and of the Java and TypeScript fixtures are identical to the previous scanner.

## [2026-11-28] implement | FR-040 Show rule violations in the viewer
- Built [[FR-041-show-pattern-assignments-as-by-pattern]], [[FR-042-mark-arcs-that-violate-a-rule]] and [[FR-043-keep-rules-and-patterns-on-export]]. `--views` and `check --out` embed per view `matched` (pattern assignments) and `violations`. The viewer never stores either in the browser.
- Decided by the agent: `matched` is a map next to `assign` instead of `"from": "match"` markers, so the persisted view format does not change. The viewer leaves out `T:` keys and places types with their file.
- Evidence: Playwright Test scenarios in `test/ui/rules.spec.js` ([[QR-010-every-delivered-viewer-requirement-has-an-automated-ui-scenario]]), run by `check.sh` and in CI. They failed before the change. All criteria pass. Looked at by the agent in light, dark and at 360 pixels.
- Not measured: [[QR-003-the-check-takes-about-as-long-as-a-scan]].
- Review in a fresh session: fixed a broken `--model` exiting 1 instead of 2, `--view` reporting other views' baseline entries as resolved, ambiguous fingerprints (now view keys, a member as `<type key>#<name>`), the viewer ignoring a `!` exclusion under a manual assignment (`excluded` is now embedded), stale browser storage hiding new groups, and the text header showing only the first line. Not changed: `analysis` waits for [[FR-011-record-the-provenance-of-every-language-and-module]], and types placed with their file is the approved plan.
- Second review in a fresh session: the viewer now applies pattern assignments in the scanner's order (`matched` holds direct selections only), so patterns and manual assignments at different levels place files as `check` does. Duplicate view ids, `|` in ids and empty `edgeKinds` exit 2, `--out` is also written with `--update-baseline`, and the viewer shows `no-ungrouped` violations in an element's details. Not changed: groups and manual assignments stored in the browser still win over the file's, as they are the user's edits.
- [[QR-003-the-check-takes-about-as-long-as-a-scan]] measured on a generated Java project with 160,000 dependencies (40 packages in 4 layers, 1% upward calls, 345 violations): `check` took a median 4.28 s against 3.72 s for a plain scan over 9 runs, a ratio of 1.15 (at most 1.2). `check` now uses the scanned model in memory instead of parsing it back from JSON, and computes `ext:` selectors and sort keys once. No real project of that size was available.
- The JSON report now always has `model.analysis`: the model's value, `null` until [[FR-011-record-the-provenance-of-every-language-and-module]].
- [[QR-003-the-check-takes-about-as-long-as-a-scan]] on a real project: Guava's main sources (`guava/guava`, 594 Java files, 94,500 dependencies), with four groups and the rules `layers`, `allow-only`, `no-cycles` and `no-ungrouped`. Median over 7 runs: scan 7.20 s, check 7.46 s, a ratio of 1.04.

## [2026-11-28] clarify | FR-041 describes the embedded pattern assignments as built
- At the maintainer's request, [[FR-041-show-pattern-assignments-as-by-pattern]] now describes what the approved plan built: `matched` and `excluded` next to an unchanged `assign`, applied in the order of [[FR-028-decide-each-elements-group-by-precedence]], instead of assignments marked `"from": "match"`. It also states that a type selected on its own shows with its file. [[FR-043-keep-rules-and-patterns-on-export]] and [[CON-005-selectors-are-resolved-only-in-the-scanner-core]] link FR-041 instead of restating the old format.
- All three go back to `review`, for the maintainer to accept. The acceptance criteria are unchanged and pass again (`test/ui/rules.spec.js`, 9 of 9), so FR-041 and FR-043 stay `implemented` with a new `delivery-checked`.
- Accepted by the maintainer: [[FR-041-show-pattern-assignments-as-by-pattern]], [[FR-043-keep-rules-and-patterns-on-export]] and [[CON-005-selectors-are-resolved-only-in-the-scanner-core]]. Only the status changed, so FR-041 and FR-043 keep `implemented` with a new `delivery-checked`.

## [2026-11-28] implement | FR-010 Choose the toolchain in a fixed order
- Built in `src/depviewer/Toolchain.java`: per module, the candidates `override`, `project`, `path` and `pinned` are tried in order, and each extractor answers `--describe` with its name, version, contract and whether the candidate is supported. TypeScript is chosen as a package folder (`DEPVIEWER_TYPESCRIPT`, `node_modules/typescript` of the module or a parent, the package of `tsc` on the `PATH`, the extractor's pinned copy), Dart as an SDK (`DEPVIEWER_DART`, FVM's `.fvm/flutter_sdk` or `flutterRoot`, `dart` on the `PATH`; no pinned SDK).
- Decided by the agent: TypeScript 5 and 6 are supported, Dart 3.5 to below 4, as the pinned analyzer needs. Node.js stays the runtime (`DEPVIEWER_NODE`, `PATH`) and is not chosen in this order. `npm ci` for the pinned TypeScript runs only when no earlier candidate is supported. Until [[FR-011-record-the-provenance-of-every-language-and-module]] the choice is recorded in the scan's log, one line per run, e.g. `typescript 5.9.3 (project: …) for shop-web`. Both extractors report version `1.0.0`.
- Evidence: the `toolchain choice` step of `scripts/check.sh`, on copies of the TypeScript fixture in `build/toolchain/` (project, unsupported project, override, unsupported override, `PATH`, pinned), and a broken `DEPVIEWER_DART` in the Dart step. It failed before the change. All criteria pass. Scans of depviewer's sources and of the Java, TypeScript and Dart fixtures are identical to the previous scanner, also with TypeScript 6.0.2 from the `PATH`.
- The maintainer set the m2-extractor-contract milestone as the session goal, in the recommended order with one commit per FR; that goal is the plan approval.

## [2026-11-28] implement | FR-011 Record the provenance of every language and module
- Built in `Model.provenance()`, `DepScan` (Java and Kotlin), `Toolchain` (one entry per extractor run) and `Rules.Graph.exact()`, which now takes `exact` from the entry of the source's language and module and falls back to "Kotlin is name-based" for models without `analysis`. The viewer's header lists the languages and marks Kotlin `(approximate)`; a module's details show tool, version, source and extractor per language.
- Decided by the agent: an entry is `{language, modules, tool, version, source, exact, extractor: {name, version}}`, with no local paths, because models are shared. Java is `javac` of the JDK that runs depviewer, `source: path`. The built-in Kotlin parser is `depviewer-kotlin`, `source: pinned`, as it ships with depviewer. JavaScript has no entry of its own: the TypeScript extractor's entry is labelled TypeScript/JavaScript. The extractors' `--describe` also reports `exact`.
- Evidence: `test/checks/jvm.py`, `typescript.py` and `dart.py` (the entry of each language), the `toolchain choice` step of `check.sh` (`source: project` in the model), `test/checks/rules.py` (the report's `analysis`, and `exact: true` for Kotlin when a model's provenance says so) and `test/ui/provenance.spec.js`. The model checks and the UI scenarios failed before the change. All criteria pass. Scans are identical to the previous scanner apart from `analysis`. Looked at by the agent in light, dark and at 360 pixels.

## [2026-11-28] implement | FR-012 Link the modules of a monorepo
- Built: `Toolchain` runs each extractor once per chosen toolchain with all its modules, for TypeScript as already for Dart. `extract.mjs` builds one program per module with that module's `tsconfig.json` and matches declarations across programs by real path and position, so an import of a sibling workspace package, which npm, yarn or pnpm link into `node_modules`, resolves to the real declaration.
- Decided by the agent: one program per module instead of one for all, so each package keeps its own compiler options. Modules whose project TypeScript differs ([[FR-010-choose-the-toolchain-in-a-fixed-order]]) still run apart. Fragment ids carry a module prefix that `q` leaves out, so qualified names and view keys do not change. A package whose `types` point to built `.d.ts` files stays an external: mapping them back to sources is left open.
- Evidence: `test/fixture-ts-mono/` (two workspace packages, `@acme/app` imports `Price` from `@acme/core` through a re-export) and `test/checks/typescript_mono.py`, run by the `TypeScript monorepo` step of `check.sh` on a copy whose `node_modules` links the packages. It failed before the change (`(other).Price` external, two runs). All criteria pass. Scans of the TypeScript fixture, of axios, ts-node, eslint and ast-types, and of depviewer's own repository (three TypeScript modules in one run) are identical to the previous scanner apart from `analysis`.
