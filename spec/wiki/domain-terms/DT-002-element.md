---
id: DT-002
type: domain-term
title: Element
status: accepted
created: 2026-11-27
updated: 2026-11-28
sources: ["[[SRC-004-guide-for-ai-coding-agents]]", "[[SRC-010-viewer-reference]]"]
related: []
tags: [domain-term]
aliases: [node, row]
bounded-context: depviewer
agreed: false
stereotype: entity
parent: []
source-of-truth:
attributes: []
relationships: []
---

# Element

**Definition.** One node of the model: a module, package, file, type, member or external package or type.

**In context.** Shown as a row in the viewer's tree. Package nodes exist per name segment and the viewer merges chains with a single child. Parents always come before their children in the model.

**Distinguish from.** A [[DT-009-view|view]]'s [[DT-010-group|group]], which is drawn as a row but is not part of the code.
