---
id: FR-069
type: functional-requirement
title: "Give native platform modules of Flutter apps useful names"
status: draft
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-019-github-issue-3-flutter-projects-native-platform-code-shows-up-as-an-unhelpful-app]]"]
related: []
tags: [functional-requirement]
notation: prose
level: requirement
parent: ["[[FR-001-scan-a-code-base-into-one-model]]"]
building-blocks: ["[[BB-003-cli-and-orchestration]]"]
priority: Could
release: unscheduled
lane: backbone
order: 13
goal: []
delivery:
delivery-checked:
---

# Give native platform modules of Flutter apps useful names

## Rationale

In a Flutter app the Android code under `android/` is found as a JVM module named after its Gradle folder, usually `app`. The name says little next to the Dart packages, and `GeneratedPluginRegistrant.java` adds dozens of unresolved references because the Android and Flutter jars are not on the classpath.

## Requirement

Native platform code of a Flutter app is shown under a name that names its platform and app, and generated registrant code does not inflate the error count. References between Dart and native code are not guessed ([[CTX-001-depviewer-context]]).

## Acceptance criteria

- [ ] Scanning a Flutter app with an `android/` folder gives no module named only `app`.
- [ ] `GeneratedPluginRegistrant.java` adds no unresolved references to the error count.

## Hierarchy

- **Parent:** [[FR-001-scan-a-code-base-into-one-model]]
- **Children:** none.

> [!note] Open points
> The issue lists three options, not yet chosen: name the module `android (my_app)`, skip known generated files, or an option such as `--no-native` to leave native code out. `ios/` (Swift, Objective-C) is not scanned today, and the behaviour there should be documented.
