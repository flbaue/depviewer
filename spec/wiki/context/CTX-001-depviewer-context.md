---
id: CTX-001
type: context
title: depviewer context
status: draft
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-001-vision-and-goal-of-depviewer]]", "[[SRC-003-adr-0001-architecture-for-language-support]]", "[[SRC-011-spec-multi-language-architecture]]"]
related: ["[[EIF-001-scanned-code-base]]", "[[EIF-002-jdk]]", "[[EIF-003-node-js-with-the-typescript-compiler]]", "[[EIF-004-dart-sdk-with-the-dart-analyzer]]", "[[EIF-005-ci-pipeline]]", "[[EIF-006-package-registries]]", "[[STK-001-software-architects]]", "[[STK-002-developers]]", "[[STK-003-ai-coding-agents]]"]
tags: [context]
kind: business
diagram: generated
---

# depviewer context

**Diagram.** _Projected_ from the external interfaces (`EIF.flows`) and the stakeholders'
`provides:`/`receives:`.

**In scope.** depviewer scans the source code of a project in Java, Kotlin, TypeScript/JavaScript
or Dart into one model of modules, packages, files, types, members and their dependencies. It shows
the model in a viewer that is one self-contained HTML file, holds the intended architecture in a
committed file next to the code and checks the code against it from the command line, in CI and for
AI agents.

**Out of scope.**
- Code quality, style or bug finding (SonarQube, linters). depviewer is only about structure and
  dependencies.
- Runtime or service-level dependencies, such as HTTP calls between services or message topics.
  Only language-level references count.
- Guessing references between ecosystems, such as Flutter to native code or a TypeScript frontend to
  a Java backend ([[DR-001-every-language-is-analysed-by-an-extractor-behind-one-versioned-contract]]).
- A hosted service or a multi-user server.

**External interfaces.**
- [[EIF-001-scanned-code-base]]: the project being scanned, inbound sources and rules.
- [[EIF-002-jdk]], [[EIF-003-node-js-with-the-typescript-compiler]], [[EIF-004-dart-sdk-with-the-dart-analyzer]]: the language toolchains that resolve references.
- [[EIF-005-ci-pipeline]]: runs the check and reads its result.
- [[EIF-006-package-registries]]: one-time install of extractor dependencies.

**User roles.**
- [[STK-001-software-architects]]: explores the structure and defines the intended architecture.
- [[STK-002-developers]]: scans and checks locally.
- [[STK-003-ai-coding-agents]]: queries and checks through the CLI, later through MCP.

**Explanation.** All analysis runs on the user's machine. The toolchains are the only neighbours
that do real work, and depviewer uses the user's own versions first ([[DR-001-every-language-is-analysed-by-an-extractor-behind-one-versioned-contract]]). Package
registries are contacted only once, to install an extractor's pinned dependencies ([[CON-003-no-network-access-during-a-scan]]).
