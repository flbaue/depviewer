# depviewer spec

The requirements and architecture of depviewer as a [systems42](https://systems42.org) wiki: one
spec in plain markdown, structured by req42 (requirements) and arc42 (architecture), that humans
review and AI agents read before they change code.

| Section | Folder |
|---|---|
| R1 Vision & Goals | `wiki/goals/` |
| R3 Functional Requirements | `wiki/functional-requirements/` (prose notation: features and requirements with acceptance criteria) |
| A1 Solution Strategy | `wiki/solution-strategy/` |
| A2 Building Block View | `wiki/building-blocks/` |
| A3 Deployment View | `wiki/deployment-nodes/` |
| A4 Runtime View | `wiki/runtime-scenarios/` |
| A5 Crosscutting Concepts | `wiki/crosscutting-concepts/` |
| C1 Quality Requirements | `wiki/quality-requirements/` |
| C2 Constraints | `wiki/constraints/` |
| C3 Scope & Context | `wiki/context/`, `wiki/external-interfaces/` |
| C4 Stakeholders | `wiki/stakeholders/` |
| C5 Domain Terms | `wiki/domain-terms/` |
| C6 Issues and risks | `wiki/issues/` |
| C7 Decisions | `wiki/decisions/` |

R2, R5 and R6 (resources, team, roadmap) are project management and not captured. The roadmap is
the `release` field of the functional requirements.

- Start at `_system/index.md`, the catalog of every page. `_system/log.md` records how the wiki was
  filled.
- Every page was captured from the existing documentation on 2026-11-27 and is `draft` until the
  maintainer promotes it. Open questions are in `wiki/issues/`.
- `raw/sources/` records where each statement came from. Paths there are relative to this folder.
- Browse it with the systems42 dashboard: `./dashboard.sh up` (Docker) or `./dashboard.sh uv`.
  It reads the pinned version from `_system/wiki.yaml` and never writes to the wiki.
- Agents: `AGENTS.md` in this folder is the schema for working on the wiki, with the workflows in
  `_system/workflows/`. The method files come from
  [systems42-dashboard](https://github.com/systems42-org/systems42-dashboard) @ daa2285.
