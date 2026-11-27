---
id: FR-023
type: functional-requirement
title: Assign code to groups
status: draft
created: 2026-11-27
updated: 2026-11-27
sources: ["[[SRC-010-viewer-reference]]"]
related: []
tags: [functional-requirement]
notation: prose
level: requirement
parent: ["[[FR-021-draw-the-architecture-as-views-and-groups]]"]
building-blocks: ["[[BB-013-viewer]]"]
priority: Must
release: delivered
lane: backbone
order: 2
goal: []
delivery: implemented
delivery-checked: "2026-11-27T21:56:32+01:00"
---

# Assign code to groups

## Rationale

Assigning by hand must be quick for packages and precise for single files.

## Requirement

Modules, packages and files are dragged onto a group row or the group is picked in the details panel. A package takes everything inside it and a more specific assignment wins. Types and members move with their file. Dropping on an ungrouped row moves an element back out.

## Acceptance criteria

- [ ] Assigning `com.example` to Core and `com.example.app` to Application puts `Main.java` into Application.

## Hierarchy

- **Parent:** [[FR-021-draw-the-architecture-as-views-and-groups]]
- **Children:** none.
