# #2 Viewer: slow to expand and re-sort with tens of thousands of arcs

https://github.com/flbaue/depviewer/issues/2
State on export (2026-11-27): closed, opened 2026-11-25, closed 2026-11-25

The viewer re-renders all rows and arcs on every change. Measured on a real Flutter workspace (~650 Dart files, ~57k distinct dependencies) in headless Chrome (JavaScript time only, layout/paint not included):

| Action | Rows | Arcs | Time |
|---|---|---|---|
| Show files | 623 | 4,442 | ~140 ms |
| Show types | 2,668 | 15,924 | ~490 ms |
| Show members | 8,292 | 33,725 | ~1,360 ms |
| Re-sort at member level | 8,292 | 33,725 | ~1,650 ms |
| Hover / select a row | | | 4–11 ms |

**Idea:** virtualise rendering - only create DOM rows for the visible part of the scroll area, and only draw arcs that touch visible rows (or are within some distance); avoid recomputing levelization when only the expansion changes.

**Done when:** expanding to members and re-sorting on a project of this size stays well under ~200 ms, with a smoke test that measures it (headless Chrome without virtual time).
