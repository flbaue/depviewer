---
id: EIF-001
type: external-interface
title: Scanned code base
status: accepted
created: 2026-11-27
updated: 2026-11-28
sources: ["[[SRC-008-usage-reference]]", "[[SRC-002-spec-architecture-file-and-rule-checks]]"]
related: ["[[CTX-001-depviewer-context]]"]
tags: [external-interface]
partner: "The project being scanned, on the local disk"
tier:
short_title: Code base
flows:
  - {data: source files and build markers, direction: inbound, format: files on disk, trigger: every scan, label: sources}
  - {data: architecture file and baseline, direction: inbound, format: JSON, trigger: depviewer check, label: rules}
  - {data: baseline, direction: outbound, format: JSON, trigger: check --update-baseline, label: baseline}
---

# Scanned code base

**Purpose.** The project the user points depviewer at. It supplies the code to analyse and, once
the architecture file exists, the intended architecture and the baseline.

**Flows.** Each scan reads source files below the given directories, and the build markers
`pom.xml`, `package.json` and `pubspec.yaml` that name the modules ([[FR-002-find-modules-by-their-build-markers]]). `depviewer check`
reads `architecture.json` and the baseline, and writes the baseline on `--update-baseline`
([[FR-039-accept-existing-violations-in-a-baseline]]). Nothing is ever written into the sources.

**Belongs to.** [[CTX-001-depviewer-context]]
