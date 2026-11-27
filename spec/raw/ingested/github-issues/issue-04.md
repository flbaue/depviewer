# #4 TypeScript: link references between packages of a monorepo

https://github.com/flbaue/depviewer/issues/4
State on export (2026-11-27): open, opened 2026-11-25

The TypeScript extractor runs once per `package.json` module. In a monorepo (npm/yarn/pnpm workspaces), references from one package to another therefore point to `node_modules/<package>` (symlinked) or show up as an external package, instead of linking to the other package's real declarations.

The same problem existed for Dart and was fixed by analysing all packages of a scan together (see `docs/specs/dart-support.md`, commit c3e1940). TypeScript needs the equivalent: e.g. one program per workspace, mapping symlinked workspace packages back to their source folders (respecting each package's `tsconfig.json`).

**Done when:** a spec update, a monorepo fixture (two packages, one importing the other via its package name) with failing tests first, then the fix. Not yet tested on a real TypeScript codebase at all - worth doing as part of this.

## Comment by flbaue, 2026-11-26

Will be fixed by #11 (extractors run once per scan, as decided in [ADR 0001](https://github.com/flbaue/depviewer/blob/main/docs/adr/0001-language-support.md)).
