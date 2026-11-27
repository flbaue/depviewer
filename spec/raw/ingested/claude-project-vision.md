# Vision and goal
 
Status: draft, for discussion with the maintainer
Related: [#6](https://github.com/flbaue/depviewer/issues/6), [ADR 0001](adr/0001-language-support.md)
 
## Goal
 
depviewer is an **open-source tool to define, review and enforce the architecture of a code base**,
for humans and AI agents alike. It is a free alternative to commercial architecture tools such as
Sonargraph or Structure101: you see the real dependency structure, write down the architecture you
intend, and get every place where the code goes against it — in the browser, on the command line,
in CI and from an AI agent.
 
In one sentence: **architecture as code, checked against compiler-exact dependencies.**
 
## Who it is for
 
| User | Wants to | Through |
|---|---|---|
| Software architect | Understand a code base, agree on its target structure, watch erosion | Viewer, architecture file |
| Developer | Know whether a change breaks the architecture before review | CLI `check`, viewer |
| CI pipeline | Fail a build on *new* violations, without fixing all old ones first | CLI `check` with a baseline |
| AI coding agent | Ask "may I depend on X from here?", check its own change, explain why A depends on B | CLI with JSON output, later an MCP server |
 
## Principles
 
1. **The compiler is the source of truth.** Dependencies come from each language's own toolchain,
   resolved exactly wherever possible. Where they are not exact (today: Kotlin), the output says
   so, and so does every violation ([ADR 0001](adr/0001-language-support.md), section 4).
2. **Architecture as code.** The intended architecture lives in one committed file next to the
   code ([architecture-rules spec](specs/architecture-rules.md)). The viewer edits it; the CLI and
   agents check against it. Nothing that matters lives only in browser storage.
3. **One model, three interfaces.** Viewer, CLI and agent interface read the same scan model and
   the same architecture file, and report the same violations.
4. **Adoptable on legacy code.** Existing violations can be accepted in a baseline, so a team or an
   agent is only held to "do not make it worse" and can ratchet down from there.
5. **Zero setup for the core.** Scanning Java and checking rules need only a JDK. Other languages
   need their own toolchain and are skipped with a note when it is missing. No server, no account,
   no network during a scan, works offline.
6. **Small and inspectable.** No frameworks; the output is a single HTML file or plain JSON.
## Non-goals
 
- Code quality, style or bug finding (SonarQube, linters). depviewer is only about structure and
  dependencies.
- Runtime or service-level dependencies (HTTP calls between services, message topics). Only
  language-level references.
- Guessing references between ecosystems, e.g. Flutter ↔ native code (ADR 0001, section 6).
- A hosted service or multi-user server.
## Capabilities
 
| Capability | Status |
|---|---|
| Scan Java, Kotlin, TypeScript/JavaScript, Dart into one model | done |
| Viewer: tree, arcs, levelization, cycles, large projects | done |
| Views and groups drawn by hand, exported as JSON | done |
| One extractor contract for all languages, provenance, monorepos | in progress (#10–#13) |
| **Architecture file:** groups by pattern, rules (layers, forbidden, allowed-only, cycles, externals) | spec drafted ([architecture-rules](specs/architecture-rules.md)) |
| **`depviewer check`** with text/JSON output, exit codes and a baseline | spec drafted |
| Violations shown in the viewer | spec drafted |
| Query commands: `dependents`, `dependencies`, `path`, `cycles` | planned |
| SARIF output for code-review annotations; check only what a change touches | planned |
| MCP server exposing scan, query and check | planned |
| Metrics (coupling, cycle size, ACD/NCCD) and trends | idea |
| Diff of two scans or branches | idea |
| More languages: Go, C#, Python, Swift, exact Kotlin | planned (#7, #14–#17) |
 
## Priorities
 
The rules and the `check` command are what make depviewer an architecture tool rather than a
dependency viewer, so they come before new languages:
 
1. Architecture file and `check` with baseline (#6, split into the issues listed in the spec).
2. Extractor contract, provenance and the `jvm` extractor (#10–#13), in parallel; provenance is
   needed for the exactness of violations.
3. Query commands and agent documentation, then the MCP server.
4. New languages and exact Kotlin.
## How we know it works
 
- A team can describe its layers or bounded contexts in one file and get a CI check that fails only
  on new violations, on a real project with tens of thousands of dependencies, in about the time of
  a scan.
- An AI agent can, using only the CLI and its documentation, find whether its change adds a
  violation and explain the dependency path it introduced.
- Violations agree between the viewer and the CLI on the same model and file.
 