---
id: DT-005
type: domain-term
title: Module
status: draft
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-008-usage-reference]]", "[[SRC-011-spec-multi-language-architecture]]"]
related: []
tags: [domain-term]
aliases: []
bounded-context: depviewer
agreed: false
stereotype:
parent: []
source-of-truth:
attributes: []
relationships: []
---

# Module

**Definition.** A unit of code found by its build marker: `src/{main,test}/{java,kotlin}`, `package.json` or `pubspec.yaml`.

**In context.** Named after `artifactId`, the `name` in `package.json` or `pubspec.yaml`, else its folder. One scan can hold several modules in several languages.

**Distinguish from.** [[DT-021-ecosystem]], which groups languages, not code.
