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

A JVM module inside the `android/` folder of a Flutter app is named `android (<app>)`, where `<app>` is the `name` from the app's `pubspec.yaml`. `GeneratedPluginRegistrant.java` is not scanned. There is no option to leave native code out. References between Dart and native code are not guessed ([[CTX-001-depviewer-context]]).

## Acceptance criteria

- [ ] Scanning a Flutter app `my_app` with an `android/` folder gives a module `android (my_app)` and none named only `app`.
- [ ] `GeneratedPluginRegistrant.java` is not in the model and adds nothing to the error count.

## Hierarchy

- **Parent:** [[FR-001-scan-a-code-base-into-one-model]]
- **Children:** none.

> [!note] Open points
> Chosen by the [[STK-004-maintainer]] on 2026-11-27: rename and skip, no `--no-native` option. `ios/` (Swift, Objective-C) is not scanned today, and the behaviour there should be documented.
