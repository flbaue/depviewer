# Spec: architecture file and rule checks
 
Status: draft (issue [#6](https://github.com/flbaue/depviewer/issues/6))
Tests: planned — `test/checks/rules.py` on `test/fixture/` with `test/fixture-architecture.json`
Depends on: the data model in [AGENTS.md](../../AGENTS.md), provenance from
[ADR 0001](../adr/0001-language-support.md) (section 4) for exactness
Goal of the project: [vision.md](../vision.md)
 
## Goal
 
Let a team write its intended architecture down **once**, in a committed file, and check the code
against it from the command line, in CI and from AI agents, with the same result the viewer shows.
 
Today groups are assigned by hand in the viewer and stored per browser; "violations" are only
visible as blue and red arcs. This spec makes the architecture declarative (groups defined by
patterns), adds explicit rules, and adds `depviewer check`.
 
## Constraints
 
- The core stays Java 17 with the JDK only. The architecture file is therefore **JSON** (read by
  `Json.java`); no YAML or custom DSL.
- The architecture file is a **superset of today's exported views format**. Every existing export
  is a valid architecture file without rules, and the viewer keeps working with it unchanged.
- Patterns are evaluated in **one place, the Java core**. The viewer does not implement pattern
  matching; it receives resolved assignments (see "Viewer").
- Persisted view keys (`R:`, `P:`, `F:`, `X`, `XP:`, `XT:`) keep their meaning. New keys may be
  added, existing ones must not change.
- Checking a saved model (`deps.json`) must not need any language toolchain.
## Architecture file
 
Default name `architecture.json` in the project root; any name can be passed with `--rules`.
 
```jsonc
{
  "version": 1,
  "current": "layers",
  "views": [
    {
      "id": "layers",
      "name": "Layers",
      "groups": [
        { "id": "app",   "name": "Application",    "color": "#e07a2f",
          "match": ["package:com.example.app.**"] },
        { "id": "core",  "name": "Core",           "color": "#577590" },
        { "id": "svc",   "name": "Service",        "color": "#2a9d8f", "parent": "core",
          "match": ["package:com.example.service.**"] },
        { "id": "dom",   "name": "Domain",         "color": "#8e5cd9", "parent": "core",
          "match": ["package:com.example.domain.**", "type:**Repository@interface"] },
        { "id": "infra", "name": "Infrastructure", "color": "#c9a227",
          "match": ["path:**/infra/**"] }
      ],
      "assign": { "F:fixture:src/main/java/com/example/app/Main.java": "app" },
      "rules": [
        { "id": "layering", "kind": "layers", "order": ["app", "infra", "svc", "dom"] },
        { "id": "domain-pure", "kind": "allow-only", "from": "dom",
          "to": ["dom", "ext:java.**"], "severity": "error",
          "description": "The domain only uses itself and the JDK." },
        { "id": "no-pkg-cycles", "kind": "no-cycles", "level": "package" }
      ]
    }
  ]
}
```
 
- `version` is new and optional (missing = 1). A file with an unknown major version is rejected
  with a clear message; unknown fields are ignored and preserved by the viewer on export.
- `groups`, `parent`, `assign` and `""` ("explicitly ungrouped") mean exactly what they mean today.
- `match` (new, optional) is a list of selectors (below). `rules` (new, optional) belongs to its
  view, because groups are per view. A file may have views without rules (pure visualisation).
### Selectors
 
A selector is `<kind>:<glob>`. `*` matches within one name segment, `**` across segments; the
segment separator is `.` for package and type names and `/` for paths.
 
| Selector | Matches | Example |
|---|---|---|
| `module:<glob>` | modules by name | `module:billing-*` |
| `package:<glob>` | packages by qualified name (`.` or `/` as stored) | `package:com.example.infra.**`, `package:src/features/**` |
| `path:<glob>` | files by module-relative path | `path:**/generated/**` |
| `type:<glob>` | types by qualified name; optional `@kind` suffix | `type:**Repository@interface` |
| `ext:<glob>` | external packages and types | `ext:org.springframework.**` |
 
A selector may be prefixed with `!` to exclude what an earlier selector of the same group matched.
 
### Assignment precedence
 
For each element the group is decided in this order:
 
1. An explicit entry in `assign` for the element itself.
2. A `match` that selects the element itself.
3. The group of its nearest ancestor, by the same two steps (today's rule: a package takes
   everything inside it, a more specific assignment wins).
If two groups' `match` select the same element, the group listed first wins and `check` reports an
**overlap warning** naming both groups and the element. Types can now be assigned on their own
(by `type:` selectors or a new persisted key `T:<module>:<qualified name>`); members always move
with their type.
 
## Rules
 
Common fields: `id` (required, unique in the view), `kind`, `severity` (`error` default, or
`warning`), `description`, `tests` (`false` default: test sources are exempt; `true`: checked too),
`edgeKinds` (default all).
 
A group in a rule stands for itself and all its subgroups. Dependencies between elements of the
same group (including its subgroups) never violate a rule between groups.
 
| Kind | Fields | Violation |
|---|---|---|
| `layers` | `order` (top to bottom), `strict` (default `false`) | a dependency from a lower to a higher layer; with `strict`, also skipping a layer |
| `forbid` | `from`, `to` (groups or `ext:` selectors) | any dependency from `from` to `to` |
| `allow-only` | `from`, `to` | a dependency from `from` to anything not listed (ungrouped code and externals included) |
| `independent` | `groups` | any dependency between two of the listed groups (e.g. bounded contexts) |
| `no-cycles` | `level` (`package`, `file`, `type`), optional `within` (group) | every cycle at that level; one violation per cycle, listing its members and the dependencies that close it |
| `no-ungrouped` | optional `scope` (selector) | an element in scope that is in no group |
 
Ungrouped code is only constrained by `allow-only`, `no-ungrouped` and `no-cycles`.
 
## `depviewer check`
 
```
./depviewer check --rules <file> [--view <id>] (<project-dir>... | --model deps.json)
                  [--format text|json] [--out <file>]
                  [--baseline <file>] [--update-baseline]
                  [--fail-on error|warning|never]
```
 
- Without `--model` it scans first, with the same options as a normal scan; with `--model` it
  checks a saved `.json` scan and needs no toolchain.
- Without `--view`, all views with rules are checked.
- Exit codes: `0` no failing violations, `1` failing violations, `2` invalid arguments, rules file
  or model. `--fail-on` (default `error`) decides which severities fail.
- `--out` additionally writes the HTML viewer with the resolved view and the violations embedded.
### Violations
 
One violation per rule and pair of **innermost** elements (as in the model's edges), with all its
lines. Text format, one line each, sorted by file and line:
 
```
src/main/java/com/example/domain/Order.java:25  error  layering  dom -> svc
    com.example.domain.Order.total() -> com.example.service.PriceCalculator.gross(double) (call)
```
 
JSON format (`--format json`), versioned:
 
```jsonc
{
  "format": 1,
  "model": { "title": "…", "generated": "…", "analysis": [ /* from ADR 0001 */ ] },
  "summary": { "error": 3, "warning": 1, "baselined": 12, "resolved": 2, "overlaps": 0 },
  "violations": [
    { "rule": "layering", "view": "layers", "severity": "error", "kind": "call",
      "from": { "key": "T:fixture:com.example.domain.Order", "q": "com.example.domain.Order.total()",
                "path": "src/main/java/com/example/domain/Order.java", "group": "dom" },
      "to":   { "key": "T:fixture:com.example.service.PriceCalculator",
                "q": "com.example.service.PriceCalculator.gross(double)", "group": "svc" },
      "lines": [25], "count": 1, "exact": true, "baselined": false,
      "fingerprint": "layering|com.example.domain.Order.total()|com.example.service.PriceCalculator.gross(double)|call" }
  ]
}
```
 
- `exact` comes from the provenance of the source element's language and module; `false` means the
  dependency was resolved by name. Until provenance exists (#12), `exact` is `false` for Kotlin and
  `true` otherwise.
- The `fingerprint` contains no line numbers, so it stays stable when code moves within a file.
### Baseline
 
- `--update-baseline` writes the fingerprints of all current violations to the baseline file
  (sorted, one JSON array, meant to be committed).
- With `--baseline`, violations whose fingerprint is in the baseline are reported as `baselined`
  and never fail the check. Baseline entries that no longer occur are reported as `resolved`, so
  the baseline can be shrunk with `--update-baseline` (ratchet).
## Viewer
 
- When a view comes from an architecture file (via `--views`/`--rules`), the scanner resolves all
  `match` selectors into concrete assignments and embeds them with a marker `"from": "match"`. The
  viewer shows such assignments like manual ones, labelled "by pattern", and does not let them be
  removed by drag (only overridden by a manual assignment).
- Embedded violations are shown in an arc's details (rule, severity, exact) and arcs carrying an
  error-severity violation are drawn as violations even if they point downward.
- *Export views* writes `match`, `rules` and `version` back unchanged and does not write the
  resolved `"from": "match"` assignments.
## Checkable statements (fixture)
 
On `test/fixture/` with `test/fixture-architecture.json` (the "Layers" view above):
 
- The `Domain → Service` dependency of the fixture's deliberate cycle is reported by `layering`
  with `Order.java:25` (`Order.total()` calling `PriceCalculator.gross`), and it is the only
  `layering` violation (infrastructure sits above the domain it implements); `check` exits `1`.
- `no-cycles` at `package` level reports exactly one cycle (`domain`, `service`).
- `type:**Repository@interface` selects `OrderRepository` but not `InMemoryOrderRepository`. In a
  variant without `@interface`, `InMemoryOrderRepository` is selected by both `dom` and `infra`:
  `check` reports one overlap warning and puts it into `dom`, the group listed first.
- `domain-pure` reports no violation for JDK types and one for any other external.
- After `--update-baseline`, the same check exits `0` with all violations `baselined`; removing the
  offending reference from a copy of the fixture reports it as `resolved`.
- `check --model build/fixture.json` gives the same JSON (except `generated`) as checking the
  project directory.
- A view file without `rules` and without `match` (today's `test/fixture-views.json`) loads in the
  viewer exactly as before, and `check` reports "no rules" and exits `0`.
- Test sources are exempt by default: a violating reference in `src/test/` is not reported, and is
  with `"tests": true`.
## Out of scope (follow-up issues)
 
- Query commands (`dependents`, `dependencies`, `path`, `cycles`) and agent documentation.
- SARIF output and checking only the files a change touches.
- MCP server.
- Metrics and thresholds (coupling, cycle size), diff between two scans.
- Editing `match` and `rules` in the viewer (it preserves them, but only manual assignments are
  editable there).
- Per-dependency exactness (ADR 0001 defers it).
## Proposed issues (split of #6)
 
1. Architecture file v1: format, selectors, assignment precedence, overlap warnings (this spec,
   sections up to "Assignment precedence").
2. Rule kinds and `depviewer check` with text and JSON output and exit codes.
3. Baseline and ratchet.
4. Viewer: resolved pattern assignments and embedded violations.
5. Query commands and "Using depviewer from an AI agent" docs.
6. MCP server.
## Open questions
 
- Should `check` find `architecture.json` automatically, or always need `--rules`?
- Does `Json.java` need comment support (`//`), since people will hand-edit the file?
- Default for `layers`: is ungrouped code allowed to depend on every layer and the other way round?
- Should name-based (`exact: false`) violations fail the build by default, or only warn?
 