#!/bin/sh
# Checks for depviewer. Run from anywhere: scripts/check.sh
#
#  1. DepScan.java compiles warning-free (javac -Xlint:all -Werror)
#  2. scanning test/fixture produces the expected model (test/check_model.py)
#  3. the generated HTML has its data and views embedded
#  4. the viewer script parses (node --check), if node is installed
#  5. the generated viewer runs in headless Chrome and renders rows, arcs and the
#     embedded "Layers" view, if Chrome/Chromium is installed
#
# Steps 4 and 5 are skipped with a note when the tool is missing; CI runs all of them.
# Outputs go to build/ (git-ignored): build/fixture.html is handy for a manual look.
set -eu

cd "$(dirname "$0")/.."
OUT=build
mkdir -p "$OUT"
fail() { echo "FAIL: $*" >&2; exit 1; }
step() { printf '\n== %s\n' "$*"; }

step "compile scanner"
javac -Xlint:all -Werror -d "$OUT/classes" DepScan.java
echo ok

step "template markers"
for m in '/\*DEPS_DATA\*/null' '/\*DEPS_VIEWS\*/null'; do
  n=$(grep -c "$m" viewer.html || true)
  [ "$n" = 1 ] || fail "viewer.html must contain $m exactly once (found $n)"
done
echo ok

step "scan fixture"
./depviewer -o "$OUT/fixture.json" test/fixture
./depviewer -o "$OUT/fixture.html" --views test/fixture-views.json test/fixture 2>/dev/null

step "model"
python3 -I test/check_model.py "$OUT/fixture.json"

step "generated html"
grep -q '/\*DEPS_DATA\*/null' "$OUT/fixture.html" && fail "data was not embedded"
grep -q '/\*DEPS_VIEWS\*/null' "$OUT/fixture.html" && fail "views were not embedded"
grep -q '"name": "Layers"' "$OUT/fixture.html" || fail "views file content missing"
echo ok

step "viewer script syntax"
if command -v node >/dev/null 2>&1; then
  python3 -I -c 'import sys; s=open(sys.argv[1]).read(); a=s.index("<script>")+8; print(s[a:s.index("</script>")])' viewer.html > "$OUT/viewer.js"
  node --check "$OUT/viewer.js"
  echo ok
else
  echo "skipped: node not installed"
fi

step "viewer smoke test (headless browser)"
CHROME=""
for c in "${CHROME_BIN:-}" google-chrome google-chrome-stable chromium chromium-browser \
         "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome" \
         "/Applications/Chromium.app/Contents/MacOS/Chromium"; do
  [ -n "$c" ] || continue
  if command -v "$c" >/dev/null 2>&1 || [ -x "$c" ]; then CHROME=$c; break; fi
done
if [ -n "$CHROME" ]; then
  url="file://$(pwd)/$OUT/fixture.html"
  "$CHROME" --headless=new --disable-gpu --no-sandbox --no-first-run --user-data-dir="$OUT/chrome-profile" \
    --window-size=1400,1000 --virtual-time-budget=5000 --dump-dom "$url" > "$OUT/fixture.dom.html" 2>/dev/null
  body=$(grep -o '<body[^>]*>' "$OUT/fixture.dom.html" | head -1)
  rows=$(echo "$body" | sed -n 's/.*data-rows="\([0-9]*\)".*/\1/p')
  arcs=$(echo "$body" | sed -n 's/.*data-arcs="\([0-9]*\)".*/\1/p')
  view=$(echo "$body" | sed -n 's/.*data-view="\([^"]*\)".*/\1/p')
  [ -n "$rows" ] || fail "viewer did not render (script error?) - open $OUT/fixture.html and check the console"
  [ "$rows" -gt 5 ] || fail "viewer rendered only $rows rows"
  [ "$arcs" -gt 0 ] || fail "viewer drew no arcs"
  [ "$view" = "Layers" ] || fail "embedded view not active (got '$view')"
  for g in Application Service Domain; do
    grep -q "<span class=\"nm\">$g</span>" "$OUT/fixture.dom.html" || fail "group row '$g' missing"
  done
  echo "ok ($rows rows, $arcs arcs, view '$view')"
else
  echo "skipped: no Chrome/Chromium found (set CHROME_BIN)"
fi

printf '\nAll checks passed.\n'
