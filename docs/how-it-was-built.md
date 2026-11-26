# How depviewer was built

depviewer was created in a few conversational sessions with Claude Code, Anthropic's AI coding
agent. The maintainer supplied the idea, a screenshot of the kind of dependency view they wanted
from a commercial architecture tool, and a sample DDD project to test against. They set the
direction and scope of each step and reviewed the results. The agent wrote the code and the docs.

Roughly how it went:

1. **Design.** The agent chose to resolve dependencies with the JDK's own compiler API rather than
   parse Java by hand, so overloads, inherited members and imports resolve exactly. The output is
   a single self-contained HTML file, so nothing needs installing beyond a JDK.
2. **Build and verify.** After each step the agent ran the scanner on the sample project, checked
   the extracted dependencies, and opened the viewer in a browser to look at the result and click
   through it. Bugs it found that way (e.g. implicit `super()` calls showing up as dependencies,
   cycle colouring, package-name compaction inside groups) were fixed before moving on.
3. **Iterate.** Features such as groups and views were added on request in later sessions,
   following the same loop.
4. **Guard rails.** `scripts/check.sh`, the fixture project and CI were added so that changes
   (by humans or agents) are checked automatically. [AGENTS.md](../AGENTS.md) documents the structure
   and invariants for future AI-assisted work.

What this means for you: the code has been exercised on real and synthetic projects, but it has
not been audited line by line, and the viewer's interactive parts are tested by hand rather than
by automated UI tests. Issues and fixes are very welcome.
