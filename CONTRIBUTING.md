# Contributing

Contributions are welcome — bug reports, ideas, documentation, and code. This is a small hobby
project, so please be patient with responses.

## Reporting bugs and ideas

Open an issue. For scanner problems, the most useful thing is a **minimal Java snippet** that
shows the wrong or missing dependency (please don't paste code you are not allowed to share).
For viewer problems: browser, what you did, what you expected, and a screenshot if it helps.

Issues are the inbox. Accepted work is captured into the [requirements wiki](spec/), which is
the backlog, and the issue is closed with a link to its requirement.

## Making changes

1. Fork and branch from `main`.
2. Read [AGENTS.md](AGENTS.md) — it is the technical guide for humans and AI agents alike
   (structure, data model, invariants, conventions).
3. For a feature or behaviour change, open an issue first. The maintainer turns it into
   requirements in the [wiki under `spec/`](spec/), selects which requirement gets built and
   approves your plan in the issue. Then add tests that fail, then implement (see "Workflow" in
   AGENTS.md). AI-assisted changes also get a review in a fresh agent session before the PR.
   Make a focused change, and add no new dependencies or build tooling without discussing it
   first.
4. Run `scripts/check.sh` (needs JDK 17+ and python3; Node.js, the Dart SDK and Chrome enable the
   TypeScript, Dart and browser checks - CI runs all of them).
   For scanner fixes, add a case to the matching fixture (`test/fixture*/`) and a check to
   `test/checks/`.
   For viewer changes, also open `build/fixture.html` and try your change by hand.
5. Open a pull request describing what changed, why, and how you verified it.

## AI-assisted contributions

This project was itself written with an AI coding agent (see [How it was built](docs/how-it-was-built.md)), and AI-assisted
contributions are welcome under the same rules as any other:

- **You are responsible for what you submit.** Read and understand the change; don't submit code
  you could not explain or maintain.
- **Disclose it.** Mention the tool in the PR description and add a commit trailer such as
  `Co-Authored-By: <model> <noreply@…>` or `Assisted-by: <tool>`.
- **Verify it.** `scripts/check.sh` must pass, and say honestly what you tested by hand.
- Please don't open bulk or unreviewed AI-generated issues or PRs.

## License

By contributing you agree that your contribution is licensed under the [MIT License](LICENSE).
