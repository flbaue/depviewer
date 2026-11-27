# Index

Content catalog, one line per page, grouped by type. The agent updates this on
every capture (`_system/workflows/capture.md`, step 7).

## Domain terms

- [[DT-001-model]] Model (depviewer, draft)
- [[DT-002-element]] Element (depviewer, draft)
- [[DT-003-dependency]] Dependency (depviewer, draft)
- [[DT-004-arc]] Arc (depviewer, draft)
- [[DT-005-module]] Module (depviewer, draft)
- [[DT-006-external]] External (depviewer, draft)
- [[DT-007-levelization]] Levelization (depviewer, draft)
- [[DT-008-cycle]] Cycle (depviewer, draft)
- [[DT-009-view]] View (depviewer, draft)
- [[DT-010-group]] Group (depviewer, draft)
- [[DT-011-assignment]] Assignment (depviewer, draft)
- [[DT-012-persisted-view-key]] Persisted view key (depviewer, draft)
- [[DT-013-selector]] Selector (depviewer, draft)
- [[DT-014-architecture-file]] Architecture file (depviewer, draft)
- [[DT-015-rule]] Rule (depviewer, draft)
- [[DT-016-violation]] Violation (depviewer, draft)
- [[DT-017-baseline]] Baseline (depviewer, draft)
- [[DT-018-fingerprint]] Fingerprint (depviewer, draft)
- [[DT-019-extractor]] Extractor (depviewer, draft)
- [[DT-020-fragment]] Fragment (depviewer, draft)
- [[DT-021-ecosystem]] Ecosystem (depviewer, draft)
- [[DT-022-toolchain]] Toolchain (depviewer, draft)
- [[DT-023-provenance]] Provenance (depviewer, draft)
- [[DT-024-exact]] Exact (depviewer, draft)

## Goals

- [[GOAL-001-architecture-as-code-checked-against-the-real-dependencies]] Architecture as code, checked against the real dependencies (vision, draft)
- [[GOAL-002-teams-hold-new-code-to-their-architecture-in-ci]] Teams hold new code to their architecture in CI (objective, draft)
- [[GOAL-003-ai-agents-check-and-explain-their-own-changes]] AI agents check and explain their own changes (objective, draft)
- [[GOAL-004-one-answer-in-every-interface]] One answer in every interface (objective, draft)
- [[GOAL-005-the-real-structure-of-a-code-base-at-a-glance]] The real structure of a code base at a glance (objective, draft)

## Stakeholders

- [[STK-001-software-architects]] Software architects (draft)
- [[STK-002-developers]] Developers (draft)
- [[STK-003-ai-coding-agents]] AI coding agents (draft)
- [[STK-004-maintainer]] Maintainer (draft)
- [[STK-005-contributors]] Contributors (draft)

## Context

- [[CTX-001-depviewer-context]] depviewer context (draft)

## External interfaces

- [[EIF-001-scanned-code-base]] Scanned code base (draft)
- [[EIF-002-jdk]] JDK (draft)
- [[EIF-003-node-js-with-the-typescript-compiler]] Node.js with the TypeScript compiler (draft)
- [[EIF-004-dart-sdk-with-the-dart-analyzer]] Dart SDK with the Dart analyzer (draft)
- [[EIF-005-ci-pipeline]] CI pipeline (draft)
- [[EIF-006-package-registries]] Package registries (draft)

## Activity models


## Functional requirements

- [[FR-001-scan-a-code-base-into-one-model]] Scan a code base into one model (feature, Must, delivered, draft)
- [[FR-002-find-modules-by-their-build-markers]] Find modules by their build markers (requirement, Must, delivered, draft)
- [[FR-003-analyse-java-with-the-jdk-compiler]] Analyse Java with the JDK compiler (requirement, Must, delivered, draft)
- [[FR-004-analyse-kotlin-by-name]] Analyse Kotlin by name (requirement, Must, delivered, draft)
- [[FR-005-analyse-typescript-and-javascript-with-the-typescript-compiler]] Analyse TypeScript and JavaScript with the TypeScript compiler (requirement, Must, delivered, draft)
- [[FR-006-analyse-dart-with-the-dart-analyzer]] Analyse Dart with the Dart analyzer (requirement, Must, delivered, draft)
- [[FR-007-skip-a-language-whose-toolchain-is-missing]] Skip a language whose toolchain is missing (requirement, Must, delivered, draft)
- [[FR-008-resolve-library-types-from-the-classpath]] Resolve library types from the classpath (requirement, Should, delivered, draft)
- [[FR-009-write-the-viewer-or-the-model]] Write the viewer or the model (requirement, Must, delivered, draft)
- [[FR-010-choose-the-toolchain-in-a-fixed-order]] Choose the toolchain in a fixed order (requirement, Must, m2-extractor-contract, draft)
- [[FR-011-record-the-provenance-of-every-language-and-module]] Record the provenance of every language and module (requirement, Must, m2-extractor-contract, draft)
- [[FR-012-link-the-modules-of-a-monorepo]] Link the modules of a monorepo (requirement, Must, m2-extractor-contract, draft)
- [[FR-013-explore-the-dependency-structure]] Explore the dependency structure (feature, Must, delivered, draft)
- [[FR-014-roll-up-dependencies-of-collapsed-rows]] Roll up dependencies of collapsed rows (requirement, Must, delivered, draft)
- [[FR-015-levelize-and-sort-siblings]] Levelize and sort siblings (requirement, Must, delivered, draft)
- [[FR-016-highlight-cycles]] Highlight cycles (requirement, Must, delivered, draft)
- [[FR-017-show-details-and-jump-to-references]] Show details and jump to references (requirement, Must, delivered, draft)
- [[FR-018-find-elements-by-name]] Find elements by name (requirement, Should, delivered, draft)
- [[FR-019-show-or-hide-tests-externals-and-parent-dependencies]] Show or hide tests, externals and parent dependencies (requirement, Should, delivered, draft)
- [[FR-020-open-a-model-in-the-viewer]] Open a model in the viewer (requirement, Should, delivered, draft)
- [[FR-021-draw-the-architecture-as-views-and-groups]] Draw the architecture as views and groups (feature, Must, delivered, draft)
- [[FR-022-create-views-and-nested-groups]] Create views and nested groups (requirement, Must, delivered, draft)
- [[FR-023-assign-code-to-groups]] Assign code to groups (requirement, Must, delivered, draft)
- [[FR-024-share-views-as-a-file]] Share views as a file (requirement, Must, delivered, draft)
- [[FR-025-declare-the-architecture-in-a-file]] Declare the architecture in a file (feature, Must, m1-rules-and-check, draft)
- [[FR-026-read-the-architecture-file]] Read the architecture file (requirement, Must, m1-rules-and-check, draft)
- [[FR-027-select-code-by-patterns]] Select code by patterns (requirement, Must, m1-rules-and-check, draft)
- [[FR-028-decide-each-elements-group-by-precedence]] Decide each element's group by precedence (requirement, Must, m1-rules-and-check, draft)
- [[FR-029-check-the-code-against-architecture-rules]] Check the code against architecture rules (feature, Must, m1-rules-and-check, draft)
- [[FR-030-enforce-a-layer-order]] Enforce a layer order (requirement, Must, m1-rules-and-check, draft)
- [[FR-031-forbid-allow-only-or-separate-dependencies]] Forbid, allow only or separate dependencies (requirement, Must, m1-rules-and-check, draft)
- [[FR-032-report-cycles-as-violations]] Report cycles as violations (requirement, Must, m1-rules-and-check, draft)
- [[FR-033-require-every-element-to-be-grouped]] Require every element to be grouped (requirement, Should, m1-rules-and-check, draft)
- [[FR-034-exempt-test-sources-by-default]] Exempt test sources by default (requirement, Must, m1-rules-and-check, draft)
- [[FR-035-run-the-check-from-the-command-line-and-in-ci]] Run the check from the command line and in CI (feature, Must, m1-rules-and-check, draft)
- [[FR-036-provide-the-check-command-and-its-exit-codes]] Provide the check command and its exit codes (requirement, Must, m1-rules-and-check, draft)
- [[FR-037-report-violations-as-text]] Report violations as text (requirement, Must, m1-rules-and-check, draft)
- [[FR-038-report-violations-as-versioned-json]] Report violations as versioned JSON (requirement, Must, m1-rules-and-check, draft)
- [[FR-039-accept-existing-violations-in-a-baseline]] Accept existing violations in a baseline (requirement, Must, m1-rules-and-check, draft)
- [[FR-040-show-rule-violations-in-the-viewer]] Show rule violations in the viewer (feature, Must, m1-rules-and-check, draft)
- [[FR-041-show-pattern-assignments-as-by-pattern]] Show pattern assignments as by pattern (requirement, Must, m1-rules-and-check, draft)
- [[FR-042-mark-arcs-that-violate-a-rule]] Mark arcs that violate a rule (requirement, Must, m1-rules-and-check, draft)
- [[FR-043-keep-rules-and-patterns-on-export]] Keep rules and patterns on export (requirement, Must, m1-rules-and-check, draft)
- [[FR-044-let-ai-agents-query-and-check-the-architecture]] Let AI agents query and check the architecture (feature, Should, m3-agent-interface, draft)
- [[FR-045-query-dependents-dependencies-paths-and-cycles]] Query dependents, dependencies, paths and cycles (requirement, Should, m3-agent-interface, draft)
- [[FR-046-document-depviewer-for-ai-agents]] Document depviewer for AI agents (requirement, Should, m3-agent-interface, draft)
- [[FR-047-serve-scan-query-and-check-over-mcp]] Serve scan, query and check over MCP (requirement, Could, m3-agent-interface, draft)
- [[FR-048-review-only-what-a-change-touches]] Review only what a change touches (feature, Should, unscheduled, draft)
- [[FR-049-report-violations-as-sarif]] Report violations as SARIF (requirement, Should, unscheduled, draft)
- [[FR-050-check-only-the-files-a-change-touches]] Check only the files a change touches (requirement, Could, unscheduled, draft)
- [[FR-051-analyse-more-languages]] Analyse more languages (feature, Should, m4-more-languages, draft)
- [[FR-052-analyse-kotlin-with-the-kotlin-compiler]] Analyse Kotlin with the Kotlin compiler (requirement, Should, m4-more-languages, draft)
- [[FR-053-analyse-go]] Analyse Go (requirement, Could, m4-more-languages, draft)
- [[FR-054-analyse-c]] Analyse C# (requirement, Could, m4-more-languages, draft)
- [[FR-055-analyse-python]] Analyse Python (requirement, Could, m4-more-languages, draft)
- [[FR-056-analyse-swift]] Analyse Swift (requirement, Could, m4-more-languages, draft)
- [[FR-057-measure-and-compare-structure-over-time]] Measure and compare structure over time (feature, Could, unscheduled, draft)
- [[FR-058-compute-structure-metrics-and-trends]] Compute structure metrics and trends (requirement, Could, unscheduled, draft)
- [[FR-059-diff-two-scans-or-branches]] Diff two scans or branches (requirement, Could, unscheduled, draft)

## Quality requirements

- [[QR-001-the-viewer-stays-responsive-on-large-projects]] The viewer stays responsive on large projects (must, draft)
- [[QR-002-arcs-never-cover-the-labels]] Arcs never cover the labels (should, draft)
- [[QR-003-the-check-takes-about-as-long-as-a-scan]] The check takes about as long as a scan (should, draft)
- [[QR-004-no-dependency-is-invented]] No dependency is invented (must, draft)
- [[QR-005-java-projects-scan-with-nothing-but-a-jdk]] Java projects scan with nothing but a JDK (must, draft)
- [[QR-006-internal-refactors-do-not-change-the-output]] Internal refactors do not change the output (must, draft)
- [[QR-007-names-from-scanned-code-cannot-inject-markup]] Names from scanned code cannot inject markup (must, draft)
- [[QR-008-the-viewer-reads-well-in-light-dark-and-on-a-narrow-screen]] The viewer reads well in light, dark and on a narrow screen (should, draft)

## Constraints

- [[CON-001-the-scanner-core-needs-only-a-jdk-17]] The scanner core needs only a JDK 17 (draft)
- [[CON-002-the-viewer-is-one-self-contained-html-file]] The viewer is one self-contained HTML file (draft)
- [[CON-003-no-network-access-during-a-scan]] No network access during a scan (draft)
- [[CON-004-the-architecture-file-is-json-and-a-superset-of-the-views-format]] The architecture file is JSON and a superset of the views format (draft)
- [[CON-005-selectors-are-resolved-only-in-the-scanner-core]] Selectors are resolved only in the scanner core (draft)
- [[CON-006-persisted-view-keys-keep-their-meaning]] Persisted view keys keep their meaning (draft)
- [[CON-007-checking-a-saved-model-needs-no-language-toolchain]] Checking a saved model needs no language toolchain (draft)
- [[CON-008-depviewer-is-open-source-under-the-mit-license]] depviewer is open source under the MIT license (draft)
- [[CON-009-no-new-tooling-or-dependencies-without-the-maintainer]] No new tooling or dependencies without the maintainer (draft)
- [[CON-010-extractors-pin-their-dependencies]] Extractors pin their dependencies (draft)
- [[CON-011-scanned-data-of-other-projects-is-never-committed]] Scanned data of other projects is never committed (draft)

## Solution strategy

- [[SOL-001-one-model-three-interfaces]] One model, three interfaces (current, draft)
- [[SOL-002-each-language-is-analysed-by-its-own-toolchain]] Each language is analysed by its own toolchain (target, draft)
- [[SOL-003-zero-setup-and-small-enough-to-inspect]] Zero setup and small enough to inspect (current, draft)
- [[SOL-004-architecture-as-code-with-a-ratchet]] Architecture as code with a ratchet (planned, draft)

## Building blocks

- [[BB-001-depviewer-wrapper-script]] depviewer wrapper script (current, draft)
- [[BB-002-scanner-core]] Scanner core (current, draft)
- [[BB-003-cli-and-orchestration]] CLI and orchestration (current, draft)
- [[BB-004-model-and-json-writer]] Model and JSON writer (current, draft)
- [[BB-005-json-reader]] JSON reader (current, draft)
- [[BB-006-toolchain-runner]] Toolchain runner (current, draft)
- [[BB-007-java-scanner]] Java scanner (current, draft)
- [[BB-008-kotlin-scanner]] Kotlin scanner (current, draft)
- [[BB-009-typescript-extractor]] TypeScript extractor (current, draft)
- [[BB-010-dart-extractor]] Dart extractor (current, draft)
- [[BB-011-jvm-extractor]] jvm extractor (planned, draft)
- [[BB-012-architecture-checker]] Architecture checker (planned, draft)
- [[BB-013-viewer]] Viewer (current, draft)
- [[BB-014-query-commands]] Query commands (planned, draft)
- [[BB-015-mcp-server]] MCP server (planned, draft)

## Runtime scenarios

- [[RS-001-scan-a-mixed-repository-into-one-viewer-file]] Scan a mixed repository into one viewer file (current, draft)
- [[RS-002-check-a-change-in-ci-against-the-baseline]] Check a change in CI against the baseline (planned, draft)
- [[RS-003-an-agent-checks-its-own-change-and-explains-a-violation]] An agent checks its own change and explains a violation (planned, draft)
- [[RS-004-draw-groups-in-the-viewer-and-share-them]] Draw groups in the viewer and share them (current, draft)

## Deployment nodes

- [[DN-001-developer-machine]] Developer machine (current, draft)
- [[DN-002-jvm-process]] JVM process (current, draft)
- [[DN-003-node-js-process]] Node.js process (current, draft)
- [[DN-004-dart-process]] Dart process (current, draft)
- [[DN-005-web-browser]] Web browser (current, draft)
- [[DN-006-ci-runner]] CI runner (current, draft)

## Crosscutting concepts

- [[CC-001-the-model-as-the-contract-between-scanner-and-viewer]] The model as the contract between scanner and viewer (current, draft)
- [[CC-002-extractor-contract]] Extractor contract (target, draft)
- [[CC-003-persisted-view-keys]] Persisted view keys (current, draft)
- [[CC-004-names-from-scanned-code-are-untrusted]] Names from scanned code are untrusted (current, draft)
- [[CC-005-spec-first-then-test-driven]] Spec first, then test-driven (current, draft)
- [[CC-006-exactness-before-completeness]] Exactness before completeness (current, draft)
- [[CC-007-virtualised-rendering-of-rows-and-arcs]] Virtualised rendering of rows and arcs (current, draft)
- [[CC-008-themes-and-browser-storage-in-the-viewer]] Themes and browser storage in the viewer (current, draft)

## Decisions

- [[DR-001-every-language-is-analysed-by-an-extractor-behind-one-versioned-contract]] Every language is analysed by an extractor behind one versioned contract (accepted)

## Issues

- [[ISS-001-should-check-find-architecture-json-on-its-own]] Should check find architecture.json on its own? (ambiguity, minor, open)
- [[ISS-002-does-the-json-reader-need-comment-support]] Does the JSON reader need comment support? (question, minor, open)
- [[ISS-003-how-does-ungrouped-code-relate-to-a-layers-rule]] How does ungrouped code relate to a layers rule? (ambiguity, major, open)
- [[ISS-004-should-name-based-violations-fail-the-build-by-default]] Should name-based violations fail the build by default? (question, major, open)
- [[ISS-005-is-the-extractor-request-sent-on-stdin-or-in-a-file]] Is the extractor request sent on stdin or in a file? (question, minor, open)
- [[ISS-006-how-is-the-projects-kotlin-version-read-without-running-gradle]] How is the project's Kotlin version read without running Gradle? (question, minor, open)
- [[ISS-007-should-the-provenance-record-the-extractors-own-version]] Should the provenance record the extractor's own version? (question, minor, open)
- [[ISS-008-moving-java-and-kotlin-into-the-jvm-extractor-can-change-the-output]] Moving Java and Kotlin into the jvm extractor can change the output (risk, major, open)
- [[ISS-009-results-can-differ-between-machines]] Results can differ between machines (risk, minor, open)
- [[ISS-010-name-based-kotlin-can-miss-or-misplace-violations]] Name-based Kotlin can miss or misplace violations (risk, major, open)
- [[ISS-011-how-long-may-the-check-take]] How long may the check take? (gap, minor, open)
- [[ISS-012-what-makes-the-viewer-readable-on-a-narrow-screen]] What makes the viewer readable on a narrow screen? (gap, minor, open)
- [[ISS-013-how-is-structure-at-a-glance-measured]] How is "structure at a glance" measured? (gap, minor, open)
- [[ISS-014-does-this-wiki-replace-docs-specs-and-docs-adr]] Does this wiki replace docs/specs and docs/adr? (question, major, open)
- [[ISS-015-the-vision-and-the-architecture-rules-spec-are-not-in-the-repository]] The vision and the architecture-rules spec are not in the repository (gap, minor, open)

## Sources

- [[SRC-001-vision-and-goal-of-depviewer]] Vision and goal of depviewer (ingested)
- [[SRC-002-spec-architecture-file-and-rule-checks]] Spec architecture file and rule checks (ingested)
- [[SRC-003-adr-0001-architecture-for-language-support]] ADR 0001 architecture for language support (ingested)
- [[SRC-004-guide-for-ai-coding-agents]] Guide for AI coding agents (ingested)
- [[SRC-005-readme-of-depviewer]] README of depviewer (ingested)
- [[SRC-006-contributing-guide]] Contributing guide (ingested)
- [[SRC-007-mit-license]] MIT license (ingested)
- [[SRC-008-usage-reference]] Usage reference (ingested)
- [[SRC-009-supported-languages]] Supported languages (ingested)
- [[SRC-010-viewer-reference]] Viewer reference (ingested)
- [[SRC-011-spec-multi-language-architecture]] Spec multi-language architecture (ingested)
- [[SRC-012-spec-kotlin-support]] Spec Kotlin support (ingested)
- [[SRC-013-spec-typescript-and-javascript-support]] Spec TypeScript and JavaScript support (ingested)
- [[SRC-014-spec-dart-support]] Spec Dart support (ingested)
- [[SRC-015-spec-viewer-arc-geometry]] Spec viewer arc geometry (ingested)
- [[SRC-016-spec-viewer-performance-on-large-projects]] Spec viewer performance on large projects (ingested)
