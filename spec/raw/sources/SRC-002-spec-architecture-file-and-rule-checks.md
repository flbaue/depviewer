---
id: SRC-002
type: source
title: Spec architecture file and rule checks
status: ingested
created: 2026-11-27
updated: 2026-11-27
tags: [source]
source-type: spec
origin: "claude.ai project Debviewer, specs/architecture-rules.md"
captured: "2026-11-27"
sha256: n/a
ingested-pages: ["[[BB-005-json-reader]]", "[[BB-012-architecture-checker]]", "[[CC-003-persisted-view-keys]]", "[[CON-004-the-architecture-file-is-json-and-a-superset-of-the-views-format]]", "[[CON-005-selectors-are-resolved-only-in-the-scanner-core]]", "[[CON-006-persisted-view-keys-keep-their-meaning]]", "[[CON-007-checking-a-saved-model-needs-no-language-toolchain]]", "[[DT-008-cycle]]", "[[DT-009-view]]", "[[DT-010-group]]", "[[DT-011-assignment]]", "[[DT-012-persisted-view-key]]", "[[DT-013-selector]]", "[[DT-014-architecture-file]]", "[[DT-015-rule]]", "[[DT-016-violation]]", "[[DT-017-baseline]]", "[[DT-018-fingerprint]]", "[[EIF-001-scanned-code-base]]", "[[EIF-005-ci-pipeline]]", "[[FR-025-declare-the-architecture-in-a-file]]", "[[FR-026-read-the-architecture-file]]", "[[FR-027-select-code-by-patterns]]", "[[FR-028-decide-each-elements-group-by-precedence]]", "[[FR-029-check-the-code-against-architecture-rules]]", "[[FR-030-enforce-a-layer-order]]", "[[FR-031-forbid-allow-only-or-separate-dependencies]]", "[[FR-032-report-cycles-as-violations]]", "[[FR-033-require-every-element-to-be-grouped]]", "[[FR-034-exempt-test-sources-by-default]]", "[[FR-035-run-the-check-from-the-command-line-and-in-ci]]", "[[FR-036-provide-the-check-command-and-its-exit-codes]]", "[[FR-037-report-violations-as-text]]", "[[FR-038-report-violations-as-versioned-json]]", "[[FR-039-accept-existing-violations-in-a-baseline]]", "[[FR-040-show-rule-violations-in-the-viewer]]", "[[FR-041-show-pattern-assignments-as-by-pattern]]", "[[FR-042-mark-arcs-that-violate-a-rule]]", "[[FR-043-keep-rules-and-patterns-on-export]]", "[[FR-044-let-ai-agents-query-and-check-the-architecture]]", "[[FR-045-query-dependents-dependencies-paths-and-cycles]]", "[[FR-046-document-depviewer-for-ai-agents]]", "[[FR-048-review-only-what-a-change-touches]]", "[[FR-050-check-only-the-files-a-change-touches]]", "[[ISS-001-should-check-find-architecture-json-on-its-own]]", "[[ISS-002-does-the-json-reader-need-comment-support]]", "[[ISS-003-how-does-ungrouped-code-relate-to-a-layers-rule]]", "[[ISS-004-should-name-based-violations-fail-the-build-by-default]]", "[[ISS-015-the-vision-and-the-architecture-rules-spec-are-not-in-the-repository]]", "[[RS-002-check-a-change-in-ci-against-the-baseline]]", "[[RS-003-an-agent-checks-its-own-change-and-explains-a-violation]]", "[[SOL-001-one-model-three-interfaces]]", "[[SOL-004-architecture-as-code-with-a-ratchet]]"]
---

# Spec architecture file and rule checks

Draft spec for issue #6: architecture file, selectors, rule kinds, `depviewer check`, baseline, viewer changes and open questions.
