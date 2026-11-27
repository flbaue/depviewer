---
id: BB-010
type: building-block
title: Dart extractor
status: draft
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-014-spec-dart-support]]", "[[SRC-011-spec-multi-language-architecture]]"]
related: []
tags: [building-block]
parent: "[[CTX-001-depviewer-context]]"
drivers: ["[[FR-006-analyse-dart-with-the-dart-analyzer]]", "[[CON-010-extractors-pin-their-dependencies]]"]
decisions: ["[[DR-001-every-language-is-analysed-by-an-extractor-behind-one-versioned-contract]]"]
uses: ["[[EIF-004-dart-sdk-with-the-dart-analyzer]]"]
location: extractors/dart/
state: current
superseded-by:
---

# Dart extractor

> follows [[arc42]] (section 5, black box)

## Purpose / Responsibility

`bin/extract.dart` on `package:analyzer`. Analyses all Dart packages of a scan in one run and prints one fragment with the module of every file.

## Interfaces

- **Provides:** the fragment format of [[CC-002-extractor-contract]] on stdout.
- **Needs:** [[EIF-004-dart-sdk-with-the-dart-analyzer]].
