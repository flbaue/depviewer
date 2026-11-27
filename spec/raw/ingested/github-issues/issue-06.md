# #6 Architecture checks for AI agents: rules for forbidden layer, module, pattern and group dependencies

https://github.com/flbaue/depviewer/issues/6
State on export (2026-11-27): open, opened 2026-11-25

Today depviewer is built for humans: it writes an HTML viewer. AI coding agents (and CI) need the same model as something they can **query and check**: "does this change break the architecture?", "what depends on X?", "which dependencies violate our layering?".

## Goal

Make depviewer usable by AI agents to find architecture issues, with rules over the same groups people already define in the viewer:

- **Forbidden dependencies** between groups, layers, modules or packages: `domain` must not depend on `infrastructure`, a `core` package must not depend on UI packages, feature A must not reach into feature B's internals.
- **Layering**: an ordered list of layers where each may only depend on layers below it (the levelization the viewer already computes, as a rule).
- **Patterns**: groups defined by patterns instead of manual drag & drop - globs on paths/packages (`**/data/**`), name patterns (`*Repository`, `*ViewModel`), kinds (`interface`), language/module - e.g. "ViewModels must not use Repositories directly", "only `*Repository` may use the HTTP client package".
- **Cycles**: no cycles at a given level (package, file, type) or within a group.
- **Allowed externals**: e.g. the domain layer may only use the standard library.

## Ideas for the interface

- `depviewer check --rules architecture.json <project>`: prints violations and exits non-zero when there are any - usable in CI and by agents after a change.
- Machine-readable output (`--format json`): every violation with rule, source and target element, dependency kind and `file:line`, so an agent can jump to the code and fix it. A short text format for humans.
- Rules reuse the **views/groups format** exported from the viewer (`--views`), extended with pattern-based assignments and rule entries - so a person can draw the architecture in the viewer and an agent can enforce it. The viewer should show rule violations (e.g. highlight forbidden arcs) when rules are loaded.
- Query commands for exploration: `depviewer query dependents <element>`, `dependencies <element>`, `path <a> <b>` (why does A depend on B?), `cycles --level package`, with JSON output.
- Possibly an MCP server exposing scan/query/check as tools, so agents can use it without parsing CLI output.
- Docs aimed at agents: a section in README/AGENTS.md on how to run checks and read the output; keep output stable (versioned schema).

## Notes

- Follow the spec-first, test-driven workflow (AGENTS.md): spec in `docs/specs/`, fixtures with deliberate violations (the existing fixtures already contain cycles and layer breaks), failing tests, then implementation.
- Scanning large projects takes about a minute; agents checking repeatedly would benefit from reusing a previous scan (`check` against a saved `deps.json`) - and later incremental scans.
- Exactness matters for rules: Java, TypeScript and Dart are resolved by their compilers; Kotlin is name-based (see #5 for an exact mode). Violations should say how a dependency was resolved where that differs.
