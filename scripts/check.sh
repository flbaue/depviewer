#!/bin/sh
# Checks for depviewer. Run from anywhere: scripts/check.sh
#
#  1. the scanner (src/depviewer) compiles warning-free (javac -Xlint:all -Werror)
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
javac -Xlint:all -Werror -d "$OUT/classes" src/depviewer/*.java
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
python3 -I test/checks/jvm.py "$OUT/fixture.json"

step "TypeScript/JavaScript (spec/wiki/functional-requirements/FR-005-analyse-typescript-and-javascript-with-the-typescript-compiler.md)"
NODE_BIN=${DEPVIEWER_NODE:-node}
if command -v "$NODE_BIN" >/dev/null 2>&1; then
  ./depviewer -o "$OUT/fixture-ts.json" test/fixture-ts
  python3 -I test/checks/typescript.py "$OUT/fixture-ts.json"
  ./depviewer -o "$OUT/fixture-ts.html" test/fixture-ts 2>/dev/null
  HAVE_TS=1
else
  echo "skipped: node not installed (set DEPVIEWER_NODE)"
  HAVE_TS=
fi

step "Dart (spec/wiki/functional-requirements/FR-006-analyse-dart-with-the-dart-analyzer.md)"
DART_BIN=${DEPVIEWER_DART:-dart}
if command -v "$DART_BIN" >/dev/null 2>&1; then
  (cd test/fixture-dart && "$DART_BIN" pub get --offline >/dev/null)   # no dependencies: works offline
  ./depviewer -o "$OUT/fixture-dart.json" test/fixture-dart 2> "$OUT/fixture-dart.log" || { cat "$OUT/fixture-dart.log"; fail "Dart scan failed"; }
  cat "$OUT/fixture-dart.log"
  python3 -I test/checks/dart.py "$OUT/fixture-dart.json"
  # FR-067: one note per module for the files analysis_options.yaml excludes.
  for m in shop_app shop_core; do
    [ "$(grep -c "^dart: $m: 1 file(s) skipped - excluded" "$OUT/fixture-dart.log")" = 1 ] || fail "no exclude note for module $m"
  done
  ./depviewer -o "$OUT/fixture-dart.html" test/fixture-dart 2>/dev/null
  HAVE_DART=1
else
  echo "skipped: dart not installed (set DEPVIEWER_DART)"
  HAVE_DART=
fi

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
  for g in Application Core Service Domain Infrastructure; do
    grep -q "<span class=\"nm\">$g</span>" "$OUT/fixture.dom.html" || fail "group row '$g' missing"
  done
  # Service and Domain are subgroups of Core: one indentation level (16px) deeper than top-level groups.
  for g in Service:2a9d8f Domain:8e5cd9; do
    grep -q "padding-left:22px;--gc:#${g#*:}\"" "$OUT/fixture.dom.html" || fail "group '${g%%:*}' is not nested under Core"
  done
  # Kotlin in the viewer (spec/wiki/functional-requirements/FR-004-analyse-kotlin-by-name.md): "K" file icon, object kind; ?level=4 expands to members.
  "$CHROME" --headless=new --disable-gpu --no-sandbox --no-first-run --user-data-dir="$OUT/chrome-profile" \
    --window-size=1400,1000 --virtual-time-budget=5000 --dump-dom "$url?level=4" > "$OUT/fixture-members.dom.html" 2>/dev/null
  grep -q 'class="ic k-file kt"' "$OUT/fixture-members.dom.html" || fail "Kotlin file icon missing"
  grep -q 'class="ic k-object"' "$OUT/fixture-members.dom.html" || fail "Kotlin object row missing (or ?level=4 not applied)"
  grep -Eq '<span class="nm[^"]*">Companion</span>' "$OUT/fixture-members.dom.html" || fail "companion object row missing"
  # Arcs stay out of the label area (spec/wiki/quality-requirements/QR-002-arcs-never-cover-the-labels.md); members give the longest arcs.
  python3 -I test/checks/viewer_arcs.py "$OUT/fixture-members.dom.html"
  echo "ok ($rows rows, $arcs arcs, view '$view'; Kotlin rows render)"
  # Performance on a large synthetic model (spec/wiki/crosscutting-concepts/CC-007-virtualised-rendering-of-rows-and-arcs.md); real time, no virtual time.
  python3 -I test/gen_large.py viewer.html "$OUT/large.html"
  "$CHROME" --headless=new --disable-gpu --no-sandbox --no-first-run --user-data-dir="$OUT/chrome-profile" \
    --window-size=1400,1000 --dump-dom "file://$(pwd)/$OUT/large.html?bench" > "$OUT/large.dom.html" 2>/dev/null
  python3 -I test/checks/viewer_perf.py "$OUT/large.dom.html"
  if [ -n "$HAVE_TS" ]; then
    "$CHROME" --headless=new --disable-gpu --no-sandbox --no-first-run --user-data-dir="$OUT/chrome-profile" \
      --window-size=1400,1000 --virtual-time-budget=5000 --dump-dom "file://$(pwd)/$OUT/fixture-ts.html?level=3" > "$OUT/fixture-ts.dom.html" 2>/dev/null
    grep -q '<span class="nm">features/payments</span>' "$OUT/fixture-ts.dom.html" || fail "folder packages must merge with '/' (features/payments)"
    grep -q 'class="ic k-file ts"' "$OUT/fixture-ts.dom.html" || fail "TypeScript file icon missing"
    grep -q 'class="ic k-file js"' "$OUT/fixture-ts.dom.html" || fail "JavaScript file icon missing"
    grep -q 'class="ic k-namespace"' "$OUT/fixture-ts.dom.html" || fail "namespace row missing"
    echo "ok (TypeScript fixture renders)"
  fi
  if [ -n "$HAVE_DART" ]; then
    "$CHROME" --headless=new --disable-gpu --no-sandbox --no-first-run --user-data-dir="$OUT/chrome-profile" \
      --window-size=1400,1000 --virtual-time-budget=5000 --dump-dom "file://$(pwd)/$OUT/fixture-dart.html?level=3" > "$OUT/fixture-dart.dom.html" 2>/dev/null
    grep -q 'class="ic k-file dart"' "$OUT/fixture-dart.dom.html" || fail "Dart file icon missing"
    grep -q 'class="ic k-mixin"' "$OUT/fixture-dart.dom.html" || fail "mixin row missing"
    grep -q 'class="ic k-extension"' "$OUT/fixture-dart.dom.html" || fail "extension row missing"
    echo "ok (Dart fixture renders)"
  fi
else
  echo "skipped: no Chrome/Chromium found (set CHROME_BIN)"
fi

printf '\nAll checks passed.\n'
