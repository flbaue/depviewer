# Chat: a Go scanner core and Playwright UI tests for the viewer

Chat between the maintainer and Claude Code on 2026-11-27, written down by the agent at the maintainer's request. The maintainer's statements and answers are quoted or restated faithfully; recommendations are the agent's.

## Starting questions (maintainer)

1. Is Java the right language for this tool? It should be fast, simple, memory efficient and easy for an AI agent to maintain. What are the alternatives?
2. Should we add a UI test for viewer.html?

## Scanner language

Agent: Java analysis needs javac's Trees API, so the Java analysis stays Java. The core could be Go, Rust, TypeScript or Python; most time and memory go into javac, not the core.

Maintainer: "tooling for a specific project that is scanned is different from tooling that this application needs. for example, a user that just wants to scan a dart project, currently also needs jvm for this tool. I guess a static binary written in go would make the tool use easier for everyone not working in java."

Agent agreed: the core becomes a static Go binary, the Java and Kotlin analysis stays in the jvm extractor, and a JDK is needed only for Java and Kotlin projects.

Decisions in the grilling round (maintainer's answers to the agent's recommendations):

- Q1, yes: the requirement is general. A project scans with the depviewer binary and the toolchain of its own language, nothing else (Dart: Dart SDK only; TypeScript: Node only; Java: JDK only). Downloading the binary is the one install step. Pinned extractor dependencies installed once from the package registries stay allowed. This replaces "Java projects scan with nothing but a JDK".
- Q2, option c: the binary contains a prebuilt jar of the jvm extractor, compiled for Java 17 in CI. One file to download; the scan needs only a JDK, no compile step on first use.
- Q3, option a: the name-based Kotlin parser stays Java inside the jvm extractor; it is not ported to Go.
- Q4, option a: the switch to Go happens after the jvm extractor exists (DR-001 migration, GitHub issue 13), so each step is checked against identical JSON.
- Q5, "lgtm": the core is Go, standard library only, no third-party modules, built with `go build`. The Go version is pinned in `go.mod`, at least Go 1.23. `go vet` stays clean. CI builds release binaries for macOS, Linux and Windows on amd64 and arm64. Contributors need Go and a JDK.

## Viewer UI tests

Agent: today `check.sh` only dumps the DOM; nothing clicks. Options weighed: Playwright Test, puppeteer-core, a hand-written Chrome DevTools Protocol client, chromedp (Go), and a self-test script injected into a test copy of the viewer and read through `--dump-dom`.

Maintainer asked "what about playwright?" and then "playwright or chromedp?". Agent recommended Playwright: tests in the viewer's language stay independent of the core's language, auto-waiting and traces, built-in viewport and colour-scheme emulation, Node already in CI.

Decisions in the grilling round:

- Q6, yes: every delivered viewer requirement has at least one automated scenario that drives the real viewer in Chrome and checks the result (FR-014 to FR-024, FR-068, browser storage). The scenarios of QR-008 run in light, dark and at a narrow width. A new viewer requirement counts as delivered only once its scenario exists. The manual browser check stays for looks.
- Q7, option b: no decision record. Playwright Test, pinned in `test/ui/` with its own `package.json` and `package-lock.json`, uses the installed Chrome (`channel: 'chrome'`), no browser download. `check.sh` runs it when Node and Chrome exist and says so when it skips; CI never skips. Recorded in the spec-first concept, approved by the maintainer as new test tooling.
