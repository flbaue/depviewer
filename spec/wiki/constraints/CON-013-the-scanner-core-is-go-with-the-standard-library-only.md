---
id: CON-013
type: constraint
title: The scanner core is Go with the standard library only
status: draft
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-036-chat-a-go-scanner-core-and-playwright-ui-tests-for-the-viewer]]"]
related: ["[[CON-001-the-scanner-core-needs-only-a-jdk-17]]", "[[DR-002-the-scanner-core-is-a-static-go-binary]]"]
tags: [constraint]
category: technical
---

# The scanner core is Go with the standard library only

**Constraint.** The scanner core is Go, standard library only, with no third-party modules. It builds with `go build`. `go.mod` pins the Go version, at least Go 1.23. `go vet` stays clean. CI builds release binaries for macOS, Linux and Windows on amd64 and arm64.

**Rationale / origin.** Set by the [[STK-004-maintainer|maintainer]] with [[DR-002-the-scanner-core-is-a-static-go-binary]]. Applies once the Go core replaces the Java core. Until then [[CON-001-the-scanner-core-needs-only-a-jdk-17]] holds.

**Impact.** Contributors need Go and a JDK ([[BB-011-jvm-extractor]] stays Java). No YAML or parser libraries, so the architecture file stays JSON ([[CON-004-the-architecture-file-is-json-and-a-superset-of-the-views-format]]).
