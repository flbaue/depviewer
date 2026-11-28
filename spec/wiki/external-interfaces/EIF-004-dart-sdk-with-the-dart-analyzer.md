---
id: EIF-004
type: external-interface
title: Dart SDK with the Dart analyzer
status: accepted
created: 2026-11-27
updated: 2026-11-28
sources: ["[[SRC-014-spec-dart-support]]", "[[SRC-009-supported-languages]]"]
related: ["[[CTX-001-depviewer-context]]"]
tags: [external-interface]
partner: "Dart SDK 3.x and package:analyzer"
tier:
short_title: Dart SDK
flows:
  - {data: packages and file lists, direction: outbound, format: process arguments, trigger: scan with a pubspec.yaml module, label: files}
  - {data: fragment, direction: inbound, format: JSON on stdout, label: fragment}
---

# Dart SDK with the Dart analyzer

**Purpose.** Resolves Dart and Flutter references exactly with `package:analyzer`.

**Flows.** The core starts `extractors/dart/bin/extract.dart` once for all Dart packages of a scan
and reads one JSON fragment from stdout. `dart` comes from `DEPVIEWER_DART` or the `PATH`.

**Belongs to.** [[CTX-001-depviewer-context]]
