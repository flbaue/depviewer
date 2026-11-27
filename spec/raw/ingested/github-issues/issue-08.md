# #8 Clean up README: short overview and goal, move details into separate docs

https://github.com/flbaue/depviewer/issues/8
State on export (2026-11-27): closed, opened 2026-11-26, closed 2026-11-26

The README has grown into a reference manual (~120 lines). It mixes the project overview, CLI usage, per-language scanner details, the viewer's features, views and groups, library classpath tips, how the project was built, and contributing. Someone landing on the repository has to read a lot before they know what depviewer is and whether it is for them.

## Goal

A short README that answers **what is this, why would I use it, how do I try it**. Everything else moves to dedicated files and is linked.

## Proposal

**README keeps:**
- One-paragraph description and the goal: see the dependency structure of a code base as arcs next to its tree, find cycles and layer violations, and check it against the architecture you draw as groups.
- The screenshot.
- Supported languages in one line each, with a link to the details.
- Three-line usage (scan, open), plus links to the [quick start](https://github.com/flbaue/depviewer/blob/main/docs/quick-start.md) and the reference docs.
- A short AI notice (one or two sentences) with a link to the longer history.
- Contributing and license, one line each.

**Moves out (suggested):**
| Content | New home |
|---|---|
| Per-language scanner details (Java via javac, Kotlin name-based, TypeScript, Dart, toolchains, env vars) | `docs/languages.md` (user-facing; specs stay in `docs/specs/`) |
| CLI options, output formats (`.html`/`.json`), `?data=`, `?level=`, classpath and `--cp` tips | `docs/usage.md` |
| Viewer features, views & groups, export/import | `docs/viewer.md` (or merged into the quick start where it overlaps) |
| "How it was built" | `docs/how-it-was-built.md` |

## Done when

- README is roughly a screen or two (~40–60 lines) and reads as an overview, not a manual.
- No information is lost: everything removed from the README exists in one of the new files (or already in `docs/quick-start.md`) and is linked.
- No duplicated text between README, quick start and the new docs that would drift apart; the quick start links to the reference docs instead of repeating them.
- `depviewer --help` matches the documented languages (it still says only Java and Kotlin).
- All relative links and the light/dark screenshot work on GitHub.
- AGENTS.md updated if it refers to README sections.
