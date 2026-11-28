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
pages = sorted(root.glob("wiki/*/*.md")) + sorted(root.glob("raw/sources/*.md"))
if not pages:
    sys.exit(f"FAIL: no wiki pages under {root}")

errors, seen = [], {}
for page in pages:
    where = page.relative_to(root)
    text = page.read_text(encoding="utf-8")
    front = re.match(r"---\n(.*?)\n---\n", text, re.S)
    found = front and re.search(r"^id:\s*['\"]?([A-Z]+-\d+)['\"]?\s*$", front[1], re.M)
    if not found:
        errors.append(f"{where}: no id in the frontmatter")
        continue
    page_id = found[1]
    if not page.stem.startswith(page_id + "-"):
        errors.append(f"{where}: id {page_id} does not match the filename")
    if page_id in seen:
        errors.append(f"{where}: duplicate id {page_id}, also in {seen[page_id]}")
    seen.setdefault(page_id, where)

for e in errors:
    print("FAIL:", e)
if errors:
    sys.exit(1)
print(f"ok ({len(pages)} pages, {len(seen)} ids)")
