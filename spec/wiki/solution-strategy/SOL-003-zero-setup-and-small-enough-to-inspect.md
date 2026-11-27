---
id: SOL-003
type: solution-strategy
title: Zero setup and small enough to inspect
status: draft
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-001-vision-and-goal-of-depviewer]]", "[[SRC-004-guide-for-ai-coding-agents]]", "[[SRC-011-spec-multi-language-architecture]]", "[[SRC-036-chat-a-go-scanner-core-and-playwright-ui-tests-for-the-viewer]]"]
related: []
tags: [solution-strategy]
drivers: ["[[QR-005-java-projects-scan-with-nothing-but-a-jdk]]", "[[CON-001-the-scanner-core-needs-only-a-jdk-17]]", "[[CON-002-the-viewer-is-one-self-contained-html-file]]", "[[CON-003-no-network-access-during-a-scan]]", "[[CON-009-no-new-tooling-or-dependencies-without-the-maintainer]]"]
decisions: ["[[DR-002-the-scanner-core-is-a-static-go-binary]]"]
state: current
superseded-by:
---

# Zero setup and small enough to inspect

> follows [[arc42]] (section 4)

## Strategy


- **Only a JDK for the core** ([[QR-005-java-projects-scan-with-nothing-but-a-jdk]], [[CON-001-the-scanner-core-needs-only-a-jdk-17]]) → plain Java 17 without a build tool. The [[BB-001-depviewer-wrapper-script]] compiles it with javac on first use and whenever the sources change. Target: a static Go binary, so a project needs only its own toolchain ([[QR-009-a-project-scans-with-the-depviewer-binary-and-its-own-toolchain]], [[DR-002-the-scanner-core-is-a-static-go-binary]]).
- **Share without a server** ([[CON-002-the-viewer-is-one-self-contained-html-file]], [[GOAL-005-the-real-structure-of-a-code-base-at-a-glance]]) → the viewer is one HTML template. The scanner replaces two markers with the model and the views, so the result is one offline file.
- **Offline and private** ([[CON-003-no-network-access-during-a-scan]]) → no network during a scan. Only the extractors' pinned dependencies are installed once from [[EIF-006-package-registries]].
- **Small** ([[CON-009-no-new-tooling-or-dependencies-without-the-maintainer]]) → no frameworks, plain JSON output, one file per language in the core.
