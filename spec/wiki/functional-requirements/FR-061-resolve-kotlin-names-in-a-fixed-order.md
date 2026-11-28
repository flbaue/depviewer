---
id: FR-061
type: functional-requirement
title: "Resolve Kotlin names in a fixed order"
status: accepted
created: 2026-11-27
updated: 2026-11-28
sources: ["[[SRC-012-spec-kotlin-support]]"]
related: ["[[ISS-010-name-based-kotlin-can-miss-or-misplace-violations]]"]
tags: [functional-requirement]
notation: prose
level: requirement
parent: ["[[FR-004-analyse-kotlin-by-name]]"]
building-blocks: ["[[BB-008-kotlin-scanner]]"]
priority: Must
release: delivered
lane: backbone
order: 2
goal: []
delivery: implemented
delivery-checked: "2026-11-28T08:27:38Z"
---

# Resolve Kotlin names in a fixed order

## Rationale

Without a type checker, a fixed order keeps name resolution predictable, and an ambiguous name produces no edge ([[QR-004-no-dependency-is-invented]]).

## Requirement

1. **Types:** enclosing classes and their nested types, explicit imports (with `as` aliases), the same package, star imports, then Kotlin built-ins (external `kotlin.*`). Package-qualified names resolve too.
2. **Constructor calls** `Type(...)` give a `new` edge to the constructor, or to the type if it declares none.
3. **Members with a receiver** `x.name`: the receiver type comes from a declared `name: Type`, a local `val x = Type(...)`, a property or function with a declared or simply inferred type (including chains such as `event.order.total()`), `this`/`super`, or a type name. Members are looked up in the type, its companion, its supertypes and its extension functions. `order.status` finds the Java getter `getStatus()`.
4. **Unknown receiver** (e.g. `it`): linked only if exactly one type used by the declaration, else by its class's property types, has a member of that name.
5. **Names without receiver:** members of enclosing classes (with companions and supertypes), top-level declarations of the package, imported functions, star imports.
6. **Supertypes:** `extends` for `: Base()`, for interface to interface and for known classes, else `implements`.
7. Annotations are type uses. String templates (`"${a.b}"`) are resolved. Labels, named arguments, string contents and keywords give nothing.

Generics, smart casts and overloads are not resolved: an overloaded name links to the first match.

## Acceptance criteria

- [ ] `event.order.total()` in the fixture links to `Order.total()`.
- [ ] An `it.name` that matches members of two used types gives no edge.

## Hierarchy

- **Parent:** [[FR-004-analyse-kotlin-by-name]]
- **Children:** none.
