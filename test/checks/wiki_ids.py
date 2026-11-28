"""Unique IDs in the spec wiki (spec/AGENTS.md, Commit policy).

Every page in spec/wiki/ and every source record in spec/raw/sources/ must carry an `id:` that
matches its filename prefix (`FR-060-foo.md` holds `id: FR-060`), and no ID may appear twice.
Two PRs that each add a different `FR-060-*.md` merge without a Git conflict; this catches them.

Usage: python3 -I test/checks/wiki_ids.py [spec-dir]   (default: spec)
"""
import re
import sys
from pathlib import Path

root = Path(sys.argv[1] if len(sys.argv) > 1 else "spec")
pages = sorted(root.rglob("wiki/**/*.md")) + sorted(root.glob("raw/sources/*.md"))
if not pages:
    sys.exit(f"FAIL: no wiki pages under {root}")

errors, seen = [], {}
for page in pages:
    where = page.relative_to(root)
    text = page.read_text(encoding="utf-8-sig").replace("\r\n", "\n")
    front = re.match(r"---[ \t]*\n(.*?)\n---[ \t]*(?:\n|\Z)", text, re.S)
    found = front and re.search(r"^id:\s*['\"]?([A-Z]+)-(\d{3,})['\"]?\s*(#.*)?$", front[1], re.M)
    if not found:
        errors.append(f"{where}: no TYPE-NNN id in the frontmatter")
        continue
    page_id = f"{found[1]}-{found[2]}"
    if not page.stem.startswith(page_id + "-"):
        errors.append(f"{where}: id {page_id} does not match the filename")
    key = (found[1], int(found[2]))  # FR-60 and FR-060 are the same ID
    if key in seen:
        errors.append(f"{where}: duplicate id {page_id}, also in {seen[key]}")
    seen.setdefault(key, where)

for e in errors:
    print("FAIL:", e)
if errors:
    sys.exit(1)
print(f"ok ({len(pages)} pages, {len(seen)} ids)")
