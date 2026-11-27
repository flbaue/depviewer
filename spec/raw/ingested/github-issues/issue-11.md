# #11 Run extractors once per scan; TypeScript monorepo links

https://github.com/flbaue/depviewer/issues/11
State on export (2026-11-27): open, opened 2026-11-26

Step 2 of the migration in [ADR 0001](https://github.com/flbaue/depviewer/blob/main/docs/adr/0001-language-support.md) (#9). Fixes #4.

The TypeScript extractor runs once per `package.json` module, so references between packages of a monorepo (npm/yarn/pnpm workspaces) point to `node_modules/<package>` or show up as external packages. The ADR decides that every extractor runs **once per scan** with all modules of its language, as the Dart extractor already does.

## Scope

- The core starts the TypeScript extractor once per scan with all modules; every file carries its module.
- The extractor builds programs so that workspace packages imported by package name resolve to their source (symlinked `node_modules` entries mapped back to the workspace folder), respecting each package's `tsconfig.json`.
- Generalise the run logic in `Toolchain` so every extractor is run once per scan.

## Done when

- Spec update (`typescript-support.md`) and a monorepo fixture: two packages, one importing the other by package name, with failing checks first.
- References between the two packages link to the real declarations; nothing for them under External.
- Single-package projects give identical output.
- Tried on a real TypeScript code base (not done yet at all, see #4), with scan time compared to before.
