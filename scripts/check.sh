#!/bin/sh
# Checks for depviewer. Run from anywhere: scripts/check.sh
#
#  1. the scanner (src/depviewer) compiles warning-free (javac -Xlint:all -Werror)
#  2. scanning test/fixture produces the expected model (test/check_model.py)
#  3. the generated HTML has its data and views embedded
#  4. the viewer script parses (node --check), if node is installed
#  5. the generated viewer runs in headless Chrome and renders rows, arcs and the
#     embedded "Layers" view, if Chrome/Chromium is installed
#  6. the UI scenarios in test/ui click through the viewer, if Playwright Test is installed there
#
# Steps 4 to 6 are skipped with a note when the tool is missing; CI runs all of them.
# Outputs go to build/ (git-ignored): build/fixture.html is handy for a manual look.
set -eu

cd "$(dirname "$0")/.."
OUT=build
mkdir -p "$OUT"
fail() { echo "FAIL: $*" >&2; exit 1; }
step() { printf '\n== %s\n' "$*"; }

step "compile scanner"
javac -Xlint:all -Werror --release 17 -d "$OUT/classes" src/depviewer/*.java
echo ok

step "template markers"
for m in '/\*DEPS_DATA\*/null' '/\*DEPS_VIEWS\*/null'; do
  n=$(grep -c "$m" viewer.html || true)
  [ "$n" = 1 ] || fail "viewer.html must contain $m exactly once (found $n)"
done
echo ok

step "viewer contrast (spec/wiki/quality-requirements/QR-008-the-viewer-reads-well-in-light-dark-and-on-a-narrow-screen.md)"
python3 -I test/checks/viewer_contrast.py viewer.html

step "wiki IDs (spec/AGENTS.md, Commit policy)"
# The check must catch a planted duplicate before it vouches for the real wiki.
D="$OUT/wiki-ids/wiki/functional-requirements"
rm -rf "$OUT/wiki-ids" && mkdir -p "$D"
printf -- '---\nid: FR-001\n---\n' > "$D/FR-001-a.md"
python3 -I test/checks/wiki_ids.py "$OUT/wiki-ids" >/dev/null || fail "wiki_ids.py rejects a single valid page"
printf -- '---\nid: FR-001\n---\n' > "$D/FR-001-b.md"
case $(python3 -I test/checks/wiki_ids.py "$OUT/wiki-ids" || true) in
  *"duplicate id FR-001"*) ;;
  *) fail "wiki_ids.py missed a planted duplicate ID" ;;
esac
python3 -I test/checks/wiki_ids.py spec

step "scan fixture"
./depviewer -o "$OUT/fixture.json" test/fixture
./depviewer -o "$OUT/fixture.html" --views test/fixture-views.json test/fixture 2>/dev/null

step "model"
python3 -I test/checks/jvm.py "$OUT/fixture.json"
# Java and Kotlin run in the jvm extractor (spec/wiki/building-blocks/BB-011-jvm-extractor.md), whose JDK is chosen
# like any toolchain: a broken DEPVIEWER_JAVA is reported and the next candidate is used.
DEPVIEWER_JAVA="$OUT/no-such-java" ./depviewer -o "$OUT/fixture-java-next.json" test/fixture > "$OUT/fixture-java-next.log" 2>&1
grep -q "^Java/Kotlin: JDK (override: $OUT/no-such-java) could not be run: .*, trying the next one$" "$OUT/fixture-java-next.log" \
  || { cat "$OUT/fixture-java-next.log"; fail "a broken DEPVIEWER_JAVA is not reported"; }
python3 -I test/checks/jvm.py "$OUT/fixture-java-next.json" >/dev/null
# A working override is used and recorded as such.
J="$(cd "$(dirname "$(command -v java)")" && pwd -P)/java"
DEPVIEWER_JAVA="$J" ./depviewer -o "$OUT/fixture-java-override.json" test/fixture > "$OUT/fixture-java-override.log" 2>&1
grep -q "^Java/Kotlin: java [0-9.]* (override: .*) for fixture$" "$OUT/fixture-java-override.log" \
  || { cat "$OUT/fixture-java-override.log"; fail "DEPVIEWER_JAVA was not used"; }
python3 -I -c 'import json,sys; a=json.load(open(sys.argv[1]))["analysis"]; sys.exit(not [x for x in a if x["language"] == "java" and x["source"] == "override"])' \
  "$OUT/fixture-java-override.json" || fail "DEPVIEWER_JAVA is not recorded as source override"
# A java on the PATH that cannot run the extractor: reported with its reason, then the JDK that runs depviewer.
mkdir -p "$OUT/badjava" && printf '#!/bin/sh\necho "this java is broken" >&2\nexit 1\n' > "$OUT/badjava/java" && chmod +x "$OUT/badjava/java"
PATH="$(pwd -P)/$OUT/badjava:$PATH" "$J" -cp "$OUT/classes" depviewer.DepScan --template viewer.html -o "$OUT/fixture-badjava.json" test/fixture > "$OUT/fixture-badjava.log" 2>&1 \
  || { cat "$OUT/fixture-badjava.log"; fail "a broken java on the PATH stopped the scan"; }
grep -qF "could not be run: this java is broken, trying the next one" "$OUT/fixture-badjava.log" || { cat "$OUT/fixture-badjava.log"; fail "the broken java on the PATH is not reported"; }
python3 -I test/checks/jvm.py "$OUT/fixture-badjava.json" >/dev/null
# If the jvm extractor fails, there is no model and check does not pass: Java and Kotlin are not optional.
rm -rf "$OUT/fixture-badkt" && cp -R test/fixture "$OUT/fixture-badkt"
printf 'package com.example.notify\n\377\376 broken\n' > "$OUT/fixture-badkt/src/main/kotlin/com/example/notify/Broken.kt"
rm -f "$OUT/fixture-badkt.json"
if ./depviewer -o "$OUT/fixture-badkt.json" "$OUT/fixture-badkt" > "$OUT/fixture-badkt.log" 2>&1; then cat "$OUT/fixture-badkt.log"; fail "a failed jvm extractor still exits 0"; fi
grep -q "^depviewer: Java/Kotlin: extractor failed for fixture (exit code [0-9]*); no model written" "$OUT/fixture-badkt.log" || { cat "$OUT/fixture-badkt.log"; fail "a failed jvm extractor is not reported"; }
[ ! -e "$OUT/fixture-badkt.json" ] || fail "a model was written although the jvm extractor failed"
set +e; ./depviewer check --rules test/fixture-architecture.json "$OUT/fixture-badkt" > "$OUT/fixture-badkt-check.log" 2>&1; got=$?; set -e
[ "$got" = 2 ] || { cat "$OUT/fixture-badkt-check.log"; fail "check with a failed jvm extractor: exit $got, expected 2"; }
# Every other way the jvm extractor can fail stops the scan too: a stand-in first on the classpath.
javac -Xlint:all -Werror --release 17 -d "$OUT/fakejvm" test/checks/fakejvm/JvmExtractor.java
for c in "unsupported:no supported JDK found for fixture" "describe2:the extractor speaks another contract" \
         "contract2:the extractor printed contract 2, this depviewer reads contract 1; fixture" \
         "null:the extractor printed no fragment for fixture" "broken:the extractor printed a broken fragment for fixture (\"nodes\" is not a list)"; do
  mode=${c%%:*} text=${c#*:}
  rm -f "$OUT/fakejvm.json"
  set +e
  FAKE_JVM=$mode "$J" -cp "$OUT/fakejvm:$OUT/classes" depviewer.DepScan --template viewer.html -o "$OUT/fakejvm.json" test/fixture > "$OUT/fakejvm.log" 2>&1; scan=$?
  FAKE_JVM=$mode "$J" -cp "$OUT/fakejvm:$OUT/classes" depviewer.DepScan check --rules test/fixture-architecture.json test/fixture > "$OUT/fakejvm-check.log" 2>&1; chk=$?
  set -e
  [ "$scan" = 1 ] && [ "$chk" = 2 ] && [ ! -e "$OUT/fakejvm.json" ] || { cat "$OUT/fakejvm.log" "$OUT/fakejvm-check.log"; fail "jvm $mode: scan exit $scan, check exit $chk, expected 1 and 2 with no model"; }
  grep -qF "Java/Kotlin: $text" "$OUT/fakejvm.log" || { cat "$OUT/fakejvm.log"; fail "jvm $mode: log lacks '$text'"; }
done

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

step "TypeScript monorepo (spec/wiki/functional-requirements/FR-012-link-the-modules-of-a-monorepo.md)"
if [ -n "$HAVE_TS" ]; then
  # A copy with node_modules linking the workspace packages, as `npm install` would.
  rm -rf "$OUT/fixture-ts-mono" && cp -R test/fixture-ts-mono "$OUT/fixture-ts-mono"
  mkdir -p "$OUT/fixture-ts-mono/node_modules/@acme"
  for p in core app; do ln -s "../../packages/$p" "$OUT/fixture-ts-mono/node_modules/@acme/$p"; done
  ./depviewer -o "$OUT/fixture-ts-mono.json" "$OUT/fixture-ts-mono" 2> "$OUT/fixture-ts-mono.log" || { cat "$OUT/fixture-ts-mono.log"; fail "monorepo scan failed"; }
  python3 -I test/checks/typescript_mono.py "$OUT/fixture-ts-mono.json"
  [ "$(grep -c "^TypeScript/JavaScript: typescript .* for " "$OUT/fixture-ts-mono.log")" = 1 ] || { cat "$OUT/fixture-ts-mono.log"; fail "expected one extractor run for both packages"; }
  # pnpm links typescript into each package's own node_modules: still one run for both.
  M="$OUT/fixture-ts-pnpm"; rm -rf "$M" && cp -R "$OUT/fixture-ts-mono" "$M"
  mkdir -p "$M/node_modules/.pnpm/typescript@5.9.3/node_modules"
  ln -s "$(pwd -P)/extractors/typescript/node_modules/typescript" "$M/node_modules/.pnpm/typescript@5.9.3/node_modules/typescript"
  for p in core app; do
    mkdir -p "$M/packages/$p/node_modules"
    ln -s "../../../node_modules/.pnpm/typescript@5.9.3/node_modules/typescript" "$M/packages/$p/node_modules/typescript"
  done
  ./depviewer -o "$M.json" "$M" 2> "$M.log" || { cat "$M.log"; fail "pnpm monorepo scan failed"; }
  python3 -I test/checks/typescript_mono.py "$M.json"
  # One run: typescript_mono.py found the links between the packages and one analysis entry for both.
  for p in app core; do
    grep -qxF "TypeScript/JavaScript: typescript 5.9.3 (project: $(pwd -P)/$M/packages/$p/node_modules/typescript) for @acme/$p" "$M.log" \
      || { cat "$M.log"; fail "pnpm: the log does not name the TypeScript of @acme/$p"; }
  done
  # One package has its own TypeScript, the other finds the same one through the PATH: one run,
  # and each package's source is recorded (spec/wiki/functional-requirements/FR-011-record-the-provenance-of-every-language-and-module.md).
  M="$OUT/fixture-ts-mixed"; rm -rf "$M" "$M-bin" && cp -R "$OUT/fixture-ts-mono" "$M" && mkdir -p "$M/packages/core/node_modules" "$M-bin"
  ln -s "$(pwd -P)/extractors/typescript/node_modules/typescript" "$M/packages/core/node_modules/typescript"
  ln -s "$(pwd -P)/extractors/typescript/node_modules/typescript/bin/tsc" "$M-bin/tsc"
  PATH="$(pwd -P)/$M-bin:$PATH" ./depviewer -o "$M.json" "$M" 2> "$M.log" || { cat "$M.log"; fail "mixed monorepo scan failed"; }
  python3 -I test/checks/typescript_mono.py "$M.json" @acme/app=path @acme/core=project
  for want in "typescript 5.9.3 (path: $(pwd -P)/extractors/typescript/node_modules/typescript) for @acme/app" \
              "typescript 5.9.3 (project: $(pwd -P)/$M/packages/core/node_modules/typescript) for @acme/core"; do
    grep -qxF "TypeScript/JavaScript: $want" "$M.log" || { cat "$M.log"; fail "mixed monorepo: log lacks '$want'"; }
  done
  [ "$(grep -c "^typescript\|^TypeScript/JavaScript: typescript" "$M.log")" = 2 ] || { cat "$M.log"; fail "mixed monorepo: expected two log lines"; }
else
  echo "skipped: node not installed (set DEPVIEWER_NODE)"
fi

step "toolchain choice (spec/wiki/functional-requirements/FR-010-choose-the-toolchain-in-a-fixed-order.md)"
if [ -n "$HAVE_TS" ]; then
  T="$(pwd -P)/$OUT/toolchain"; rm -rf "$T"; mkdir -p "$T"
  PINNED="$(pwd -P)/extractors/typescript/node_modules/typescript"
  # tc <copy> <text in the log> [VAR=value...]: scan a copy of the TypeScript fixture, check its model and log
  tc() {
    name=$1 want=$2; shift 2
    env "$@" ./depviewer -o "$T/$name.json" "$T/$name" > "$T/$name.log" 2>&1 || { cat "$T/$name.log"; fail "scan of $name failed"; }
    grep -qF -- "$want" "$T/$name.log" || { cat "$T/$name.log"; fail "$name: log lacks '$want'"; }
    python3 -I test/checks/typescript.py "$T/$name.json" >/dev/null
  }
  for c in plain project old; do cp -R test/fixture-ts "$T/$c"; done
  ln -s "$PINNED" "$T/project/node_modules/typescript"
  mkdir -p "$T/old/node_modules/typescript"
  printf '{"name": "typescript", "version": "3.9.7"}' > "$T/old/node_modules/typescript/package.json"
  # The project's own supported TypeScript is used.
  tc project "typescript 5.9.3 (project: $T/project/node_modules/typescript) for shop-web"
  # ... and recorded in the model (spec/wiki/functional-requirements/FR-011-record-the-provenance-of-every-language-and-module.md).
  python3 -I -c 'import json, sys; a = json.load(open(sys.argv[1]))["analysis"]; sys.exit(not (len(a) == 1 and a[0]["source"] == "project" and a[0]["version"] == "5.9.3" and "path" not in a[0]))' \
    "$T/project.json" || fail "the project's TypeScript is not recorded as source project"
  # The pinned TypeScript is installed only when it is reached: with no pinned copy at all, the
  # project's own TypeScript is used and npm ci never runs.
  H="$T/no-pinned"; mkdir -p "$H/extractors/typescript"
  cp extractors/typescript/extract.mjs extractors/typescript/package.json "$H/extractors/typescript/"
  DEPVIEWER_HOME="$H" java -cp "$OUT/classes" depviewer.DepScan --template viewer.html -o "$H.json" "$T/project" > "$H.log" 2>&1 \
    || { cat "$H.log"; fail "scan without a pinned TypeScript failed"; }
  grep -q "npm ci" "$H.log" && { cat "$H.log"; fail "npm ci ran although the project has a supported TypeScript"; }
  grep -qF "typescript 5.9.3 (project: $T/project/node_modules/typescript) for shop-web" "$H.log" || { cat "$H.log"; fail "no-pinned: the project's TypeScript was not used"; }
  [ ! -e "$H/extractors/typescript/node_modules" ] || fail "the pinned TypeScript was installed"
  # An unsupported project version is reported and the next candidate is tried.
  tc old "TypeScript 3.9.7 (project: $T/old/node_modules/typescript) is not supported (needs >=5.0.0 <7.0.0), trying the next one"
  grep "for shop-web" "$T/old.log" | grep -q "(project:" && fail "the unsupported project TypeScript was used"
  # The override comes first; an unsupported override is reported and the project's own is used.
  tc project "typescript 5.9.3 (override: $PINNED) for shop-web" DEPVIEWER_TYPESCRIPT="$PINNED"
  tc project "TypeScript 3.9.7 (override: $T/old/node_modules/typescript) is not supported" DEPVIEWER_TYPESCRIPT="$T/old/node_modules/typescript"
  grep -qF "typescript 5.9.3 (project: $T/project/node_modules/typescript) for shop-web" "$T/project.log" || fail "no fallback to the project's TypeScript"
  # Then the tsc on the PATH, then the extractor's pinned copy.
  mkdir -p "$T/bin" "$T/nobin"
  ln -s "$PINNED/bin/tsc" "$T/bin/tsc"
  tc plain "typescript 5.9.3 (path: $PINNED) for shop-web" PATH="$T/bin:$PATH"
  for tool in java javac node; do ln -s "$(command -v "$tool")" "$T/nobin/$tool"; done
  if PATH="$T/nobin:/usr/bin:/bin" sh -c 'command -v tsc' >/dev/null; then
    echo "pinned case skipped: /usr/bin or /bin has a tsc"
  else
    tc plain "typescript 5.9.3 (pinned: $PINNED) for shop-web" PATH="$T/nobin:/usr/bin:/bin"
  fi
  echo ok
else
  echo "skipped: node not installed (set DEPVIEWER_NODE)"
fi

step "Dart (spec/wiki/functional-requirements/FR-006-analyse-dart-with-the-dart-analyzer.md)"
DART_BIN=${DEPVIEWER_DART:-dart}
if command -v "$DART_BIN" >/dev/null 2>&1; then
  (cd test/fixture-dart && "$DART_BIN" pub get --offline >/dev/null)   # no dependencies: works offline
  ./depviewer -o "$OUT/fixture-dart.json" test/fixture-dart 2> "$OUT/fixture-dart.log" || { cat "$OUT/fixture-dart.log"; fail "Dart scan failed"; }
  cat "$OUT/fixture-dart.log"
  python3 -I test/checks/dart.py "$OUT/fixture-dart.json"
  grep -q "^Dart: dart [0-9.]* (\(override\|path\): .*) for shop_app, shop_core" "$OUT/fixture-dart.log" || fail "the chosen Dart SDK is not reported"
  # FR-010: a broken override is reported and the next candidate is tried.
  DEPVIEWER_DART="$OUT/no-such-dart" ./depviewer -o "$OUT/fixture-dart-next.json" test/fixture-dart > "$OUT/fixture-dart-next.log" 2>&1
  grep -qF "Dart: Dart SDK (override: $OUT/no-such-dart) could not be run, trying the next one" "$OUT/fixture-dart-next.log" \
    || { cat "$OUT/fixture-dart-next.log"; fail "a broken DEPVIEWER_DART is not reported"; }
  # An SDK below the analyzer's range, if one is given (CI has none): reported as not supported.
  if [ -n "${DEPVIEWER_OLD_DART:-}" ]; then
    DEPVIEWER_DART="$DEPVIEWER_OLD_DART" ./depviewer -o "$OUT/fixture-dart-old.json" test/fixture-dart > "$OUT/fixture-dart-old.log" 2>&1
    grep -q "^Dart: Dart SDK [0-9.]* (override: .*) is not supported (needs >=3.11.0 <4.0.0), trying the next one" "$OUT/fixture-dart-old.log" \
      || { cat "$OUT/fixture-dart-old.log"; fail "an unsupported DEPVIEWER_OLD_DART is not reported"; }
  fi
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

step "extractor contract (spec/wiki/crosscutting-concepts/CC-002-extractor-contract.md)"
# Every extractor run of a scan, checked as DEPVIEWER_FRAGMENTS keeps it: request and raw fragment.
C="$OUT/contract"; rm -rf "$C"; mkdir -p "$C/frag"
DEPVIEWER_FRAGMENTS="$C/frag" ./depviewer -o "$C/all.json" test/fixture ${HAVE_TS:+test/fixture-ts} ${HAVE_DART:+test/fixture-dart} > "$C/scan.log" 2>&1 \
  || { cat "$C/scan.log"; fail "scan with DEPVIEWER_FRAGMENTS failed"; }
runs=0
for r in "$C"/frag/*.request.json; do
  [ -e "$r" ] || break
  python3 -I test/checks/contract.py "$r" "${r%.request.json}.fragment.json"
  runs=$((runs + 1))
done
want=1; [ -n "$HAVE_TS" ] && want=$((want + 1)); [ -n "$HAVE_DART" ] && want=$((want + 1))
[ "$runs" -ge "$want" ] || fail "expected $want extractor runs in $C/frag, found $runs"
# The checker must catch a broken fragment before it vouches for the real ones.
if [ "$runs" -gt 0 ]; then
  for R in "$C"/frag/jvm-1.request.json $(ls "$C"/frag/*.request.json | grep -v /jvm- | head -1); do   # jvm and one other
  F="${R%.request.json}.fragment.json"
  broken() {   # broken <name> <text the checker must report> <python statement on the fragment f>
    python3 -I -c 'import json,sys; f=json.load(open(sys.argv[1])); exec(sys.argv[3]); json.dump(f,open(sys.argv[2],"w"))' "$F" "$C/$1.json" "$3"
    out=$(python3 -I test/checks/contract.py "$R" "$C/$1.json" || true)
    case $out in *"$2"*) ;; *) echo "$out"; fail "contract.py missed a fragment with $1" ;; esac
  }
  broken no-contract "contract None, expected 1" 'del f["contract"]'
  broken duplicate-id "duplicate id" 'f["nodes"].append(dict(f["nodes"][-1]))'
  broken child-first "missing or after its child" 'f["nodes"].reverse()'
  broken bad-package "package entry" 'f["nodes"].insert(0, {"kind": "package", "module": "nobody", "package": "x"})'
  broken bad-kind "is not allowed" 'next(n for n in f["nodes"] if n["kind"] not in ("file", "package"))["kind"] = "widget"'
  broken bad-endpoint "is neither a node nor an external" 'f["edges"][0]["to"] = "nowhere"'
  broken bad-edge-kind "kind 'imports'" 'f["edges"][0]["kind"] = "imports"'
  broken outside "outside its file" 'n = next(n for n in f["nodes"] if n["kind"] not in ("file", "package")); n["line"] = 100000'
  broken unknown-module "unknown module" 'next(n for n in f["nodes"] if n["kind"] == "file")["module"] = "nobody"'
  broken no-path "no path" 'next(n for n in f["nodes"] if n["kind"] == "file").pop("path")'
  broken bad-lines "is not a list" 'f["edges"][0]["lines"] = 7'
  done
fi
# An extractor that speaks another major version is rejected, and the scan goes on without it.
if [ -n "$HAVE_TS" ]; then
  H="$C/home"; mkdir -p "$H/extractors/typescript"
  cat > "$H/extractors/typescript/extract.mjs" <<'JS'
const v = +(process.env.FAKE_CONTRACT || 1), describe = process.argv[2] === "--describe";
if (!describe && process.env.FAKE_NULL) { console.log("null"); process.exit(0); }
if (!describe && process.env.FAKE_PARTIAL) {   // valid nodes, then an edge that is no object
  console.log(JSON.stringify({ contract: 1, language: "typescript", errors: 0, externals: [], edges: ["oops"],
    nodes: [{ id: "f", kind: "file", path: "src/model/cart.ts", package: "src/model", loc: 9 },
            { id: "g", kind: "class", name: "Ghost", parent: "f", line: 1, loc: 1 }] }));
  process.exit(0);
}
console.log(JSON.stringify(describe
  ? { name: "fake", version: "0", contract: describe && process.env.FAKE_DESCRIBE ? 2 : 1, languages: ["typescript"],
      toolchain: { tool: "typescript", version: "5.9.3", source: process.argv[4], path: process.argv[3], supported: true } }
  : { contract: v, language: "typescript", errors: 0, nodes: [], externals: [], edges: [] }));
JS
  printf '{"name": "fake", "version": "0"}' > "$H/extractors/typescript/package.json"
  fake() { rm -f "$C/fake.json"; env DEPVIEWER_HOME="$H" "$@" java -cp "$OUT/classes" depviewer.DepScan --template viewer.html -o "$C/fake.json" test/fixture-ts > "$C/fake.log" 2>&1 || true; }
  fake FAKE_CONTRACT=2
  grep -q "^TypeScript/JavaScript: the extractor printed contract 2, this depviewer reads contract 1; shop-web, skipped$" "$C/fake.log" \
    || { cat "$C/fake.log"; fail "a fragment of contract 2 was not rejected"; }
  fake FAKE_NULL=1   # a fragment that is no object skips the run, it does not stop the scan
  grep -q "^TypeScript/JavaScript: the extractor printed no fragment for shop-web, skipped$" "$C/fake.log" && [ -s "$C/fake.json" ] \
    || { cat "$C/fake.log"; fail "a fragment that is no object was not skipped"; }
  fake FAKE_PARTIAL=1   # a broken fragment leaves nothing behind, not even its valid nodes
  grep -q "^TypeScript/JavaScript: the extractor printed a broken fragment for shop-web (.*), skipped$" "$C/fake.log" \
    || { cat "$C/fake.log"; fail "a broken fragment was not skipped"; }
  grep -q '"Ghost"' "$C/fake.json" && fail "a skipped fragment left nodes in the model"
  fake FAKE_DESCRIBE=1
  grep -q "^TypeScript/JavaScript: extractor fake 0 speaks contract 2, this depviewer speaks contract 1; skipped$" "$C/fake.log" \
    || { cat "$C/fake.log"; fail "an extractor of contract 2 was not rejected"; }
  grep -q "npm ci\|for shop-web" "$C/fake.log" && { cat "$C/fake.log"; fail "after a contract mismatch no other toolchain may be tried"; }
fi
echo ok

step "generated html"
grep -q '/\*DEPS_DATA\*/null' "$OUT/fixture.html" && fail "data was not embedded"
grep -q '/\*DEPS_VIEWS\*/null' "$OUT/fixture.html" && fail "views were not embedded"
grep -q '"name": "Layers"' "$OUT/fixture.html" || fail "views file content missing"
echo ok

step "architecture file (spec/wiki/functional-requirements/FR-026-read-the-architecture-file.md)"
# expect <exit code> <text in output> <check arguments...>
expect() {
  want=$1 text=$2; shift 2
  set +e; ./depviewer check "$@" > "$OUT/check.log" 2>&1; got=$?; set -e
  [ "$got" = "$want" ] || { cat "$OUT/check.log"; fail "check $*: exit $got, expected $want"; }
  grep -q -- "$text" "$OUT/check.log" || { cat "$OUT/check.log"; fail "check $*: output lacks '$text'"; }
}
A="$OUT/arch"; rm -rf "$A"; mkdir -p "$A/project"
expect 0 'no rules' --rules test/fixture-views.json test/fixture
cp test/fixture-views.json "$A/project/architecture.json"
expect 0 'no rules' "$A/project"
expect 2 'architecture.json' "$A"
expect 2 'needs --rules' --model "$OUT/fixture.json"
printf '{"version": 2, "views": []}' > "$A/v2.json"
expect 2 'version 2 is not supported' --rules "$A/v2.json" test/fixture
printf '{"version": 1, "future": {"x": 1}, "views": [{"id": "a", "name": "A", "groups": [], "assign": {}, "match": [], "rules": [], "later": true}]}' > "$A/unknown.json"
expect 0 'no rules' --rules "$A/unknown.json" test/fixture
printf '{"views": [{"id": "a", "rules": [{"kind": "layers"}]}]}' > "$A/rules.json"
expect 2 'view "a": rule 1: "id" is required' --rules "$A/rules.json" test/fixture
printf '{"views": ' > "$A/broken.json"
expect 2 'invalid JSON' --rules "$A/broken.json" test/fixture
set +e; ./depviewer -o "$A/v2.html" --views "$A/v2.json" test/fixture > "$OUT/check.log" 2>&1; got=$?; set -e
[ "$got" = 1 ] && grep -q 'version 2 is not supported' "$OUT/check.log" || { cat "$OUT/check.log"; fail "--views accepted version 2"; }
echo ok

step "selectors (spec/wiki/functional-requirements/FR-027-select-code-by-patterns.md)"
javac -Xlint:all -Werror -cp "$OUT/classes" -d "$OUT/test-classes" test/checks/SelectorCheck.java
java -cp "$OUT/classes:$OUT/test-classes" depviewer.SelectorCheck "$OUT/fixture.json" ${HAVE_TS:+"$OUT/fixture-ts.json"}
[ -n "$HAVE_TS" ] || echo "folder-based packages skipped: node not installed"
printf '{"views": [{"id": "a", "groups": [{"id": "g", "match": ["package:com.**", "klass:Foo"]}]}]}' > "$A/badsel.json"
expect 2 'group "g": unknown selector kind "klass"' --rules "$A/badsel.json" test/fixture
printf '{"views": [{"id": "a", "groups": [{"id": "g", "match": "package:com.**"}]}]}' > "$A/badmatch.json"
expect 2 '"match" must be an array of strings' --rules "$A/badmatch.json" test/fixture
printf '{"views": [{"id": "a", "groups": [{"id": "g", "match": ["package:com.**", "!type:**Test"]}]}]}' > "$A/goodsel.json"
expect 0 'no rules' --rules "$A/goodsel.json" test/fixture
echo ok

step "assignment precedence (spec/wiki/functional-requirements/FR-028-decide-each-elements-group-by-precedence.md)"
javac -Xlint:all -Werror -cp "$OUT/classes:$OUT/test-classes" -d "$OUT/test-classes" test/checks/AssignCheck.java
java -cp "$OUT/classes:$OUT/test-classes" depviewer.AssignCheck "$OUT/fixture.json" test/fixture-architecture.json
expect 1 'layering' --rules test/fixture-architecture.json test/fixture
grep -q "warning  overlap" "$OUT/check.log" && { cat "$OUT/check.log"; fail "overlap reported for test/fixture-architecture.json"; }
# The viewer places every element where check does (FR-041): random views, decide() against resolve().
javac -Xlint:all -Werror -cp "$OUT/classes:$OUT/test-classes" -d "$OUT/test-classes" test/checks/DecideCheck.java
java -cp "$OUT/classes:$OUT/test-classes" depviewer.DecideCheck "$OUT/fixture.json" 300 1 "$OUT/decide-cases.json"
if command -v node >/dev/null 2>&1; then
  node test/checks/viewer_decide.mjs viewer.html "$OUT/fixture.json" "$OUT/decide-cases.json"
else
  echo "viewer comparison skipped: node not installed"
fi
sed 's/@interface//' test/fixture-architecture.json > "$A/overlap.json"
expect 1 'warning  overlap  T:fixture:com.example.infra.InMemoryOrderRepository  selected by dom and by infra through F:fixture:src/main/java/com/example/infra/InMemoryOrderRepository.java, goes to dom (view layers)' --rules "$A/overlap.json" test/fixture
[ "$(grep -c "warning  overlap" "$OUT/check.log")" = 1 ] || { cat "$OUT/check.log"; fail "expected exactly one overlap warning"; }
expect 1 'goes to dom' --rules "$A/overlap.json" --model "$OUT/fixture.json"
echo ok

step "architecture rules (spec/wiki/functional-requirements/FR-029-check-the-code-against-architecture-rules.md)"
# variant <name>: a fresh copy of the fixture in $R/<name>, to add or change code for one case.
R="$OUT/rules"; rm -rf "$R"; mkdir -p "$R"
variant() { cp -R test/fixture "$R/$1"; }
# rules <file> <rules JSON>: test/fixture-architecture.json with its rules replaced.
rules() { python3 -I -c 'import json,sys; a=json.load(open("test/fixture-architecture.json")); a["views"][0]["rules"]=json.loads(sys.argv[2]); json.dump(a,open(sys.argv[1],"w"),indent=1)' "$1" "$2"; }
count() { grep -c -- "$1" "$OUT/check.log" || true; }
LAYER_LINE='src/main/java/com/example/domain/Order.java:25  error  layering  dom -> svc'

# FR-030: the only layering violation is Order.total() -> PriceCalculator.gross; ungrouped notify code
# (Notifications.kt -> Order, Main -> NotificationService) gives none in either direction.
expect 1 "$LAYER_LINE" --rules test/fixture-architecture.json test/fixture
[ "$(count '  layering  ')" = 1 ] || { cat "$OUT/check.log"; fail "FR-030: expected exactly one layering violation"; }
grep -A1 -- "$LAYER_LINE" "$OUT/check.log" | grep -q '^    com.example.domain.Order.total() -> com.example.service.PriceCalculator.gross(double) (call)$' \
  || { cat "$OUT/check.log"; fail "FR-037: reference line missing under the layering violation"; }
rules "$R/strict.json" '[{"id": "layering", "kind": "layers", "order": ["app", "infra", "svc", "dom"], "strict": true}]'
expect 1 'InMemoryOrderRepository.java:[0-9]*  error  layering  infra -> dom' --rules "$R/strict.json" test/fixture

# FR-031: domain-pure reports no JDK type; a non-java.* external from the domain gives one violation.
grep -q '  domain-pure  dom -> (external)' "$OUT/check.log" && { cat "$OUT/check.log"; fail "FR-031: domain-pure reported a JDK type"; }
variant ext
printf 'package com.example.domain;\n\nclass Xml {\n    String ns() {\n        return javax.xml.XMLConstants.XML_NS_URI;\n    }\n}\n' > "$R/ext/src/main/java/com/example/domain/Xml.java"
expect 1 'Xml.java:5  error  domain-pure  dom -> (external)' --rules test/fixture-architecture.json "$R/ext"
[ "$(count '  domain-pure  dom -> (external)')" = 1 ] || { cat "$OUT/check.log"; fail "FR-031: expected one domain-pure violation for javax.xml"; }
rules "$R/forbid.json" '[{"id": "no-app-infra", "kind": "forbid", "from": "app", "to": "infra"}, {"id": "split", "kind": "independent", "groups": ["app", "infra"]}]'
expect 1 'Main.java:14  error  no-app-infra  app -> infra' --rules "$R/forbid.json" test/fixture
grep -q 'Main.java:14  error  split  app -> infra' "$OUT/check.log" || { cat "$OUT/check.log"; fail "FR-031: independent missed app -> infra"; }

# FR-032: exactly one package cycle, domain and service.
expect 1 'no-pkg-cycles' --rules test/fixture-architecture.json test/fixture
[ "$(count '  no-pkg-cycles  ')" = 1 ] || { cat "$OUT/check.log"; fail "FR-032: expected exactly one package cycle"; }
grep -q '  error  no-pkg-cycles  cycle com.example.domain, com.example.service$' "$OUT/check.log" || { cat "$OUT/check.log"; fail "FR-032: cycle members"; }

# FR-033: a new package without a group is reported.
variant misc
mkdir -p "$R/misc/src/main/java/com/example/misc"
printf 'package com.example.misc;\n\npublic class Util {\n}\n' > "$R/misc/src/main/java/com/example/misc/Util.java"
rules "$R/ungrouped.json" '[{"id": "all-grouped", "kind": "no-ungrouped"}]'
expect 1 'error  all-grouped  package com.example.misc is in no group' --rules "$R/ungrouped.json" "$R/misc"
rules "$R/ungrouped-scope.json" '[{"id": "all-grouped", "kind": "no-ungrouped", "scope": "package:com.example.notify.**"}]'
expect 1 'all-grouped  package com.example.notify is in no group' --rules "$R/ungrouped-scope.json" "$R/misc"
grep -q 'com.example.misc' "$OUT/check.log" && { cat "$OUT/check.log"; fail "FR-033: scope ignored"; }

# FR-034: a violating reference in src/test/ is reported only with "tests": true.
variant tests
printf 'package com.example.domain;\n\nclass PriceTest {\n    double gross() {\n        return com.example.service.PriceCalculator.gross(1);\n    }\n}\n' > "$R/tests/src/test/java/com/example/domain/PriceTest.java"
expect 1 "$LAYER_LINE" --rules test/fixture-architecture.json "$R/tests"
grep -q 'PriceTest' "$OUT/check.log" && { cat "$OUT/check.log"; fail "FR-034: a test source was checked"; }
rules "$R/tests.json" '[{"id": "layering", "kind": "layers", "order": ["app", "infra", "svc", "dom"], "tests": true}]'
expect 1 'src/test/java/com/example/domain/PriceTest.java:5  error  layering  dom -> svc' --rules "$R/tests.json" "$R/tests"

# FR-029: edgeKinds, severity and --fail-on.
rules "$R/kinds.json" '[{"id": "layering", "kind": "layers", "order": ["app", "infra", "svc", "dom"], "edgeKinds": ["extends", "implements"]}]'
expect 0 'errors: 0,' --rules "$R/kinds.json" test/fixture
rules "$R/warn.json" '[{"id": "layering", "kind": "layers", "order": ["app", "infra", "svc", "dom"], "severity": "warning"}]'
expect 0 'Order.java:25  warning  layering  dom -> svc' --rules "$R/warn.json" test/fixture
expect 1 'warning  layering' --rules "$R/warn.json" --fail-on warning test/fixture
expect 0 'error  layering' --rules test/fixture-architecture.json --fail-on never test/fixture
rules "$R/badrule.json" '[{"id": "x", "kind": "layers", "order": ["app", "nope"]}]'
expect 2 'rule "x": unknown group "nope"' --rules "$R/badrule.json" test/fixture
rules "$R/badkind.json" '[{"id": "x", "kind": "sideways"}]'
expect 2 'rule "x": unknown kind "sideways"' --rules "$R/badkind.json" test/fixture
rules "$R/nokinds.json" '[{"id": "x", "kind": "forbid", "from": "app", "to": "dom", "edgeKinds": []}]'
expect 2 'rule "x": "edgeKinds" needs at least 1 entry' --rules "$R/nokinds.json" test/fixture
rules "$R/pipe.json" '[{"id": "a|b", "kind": "forbid", "from": "app", "to": "dom"}]'
expect 2 'must not contain "|"' --rules "$R/pipe.json" test/fixture
python3 -I -c 'import json,sys; a=json.load(open("test/fixture-architecture.json")); a["views"].append(a["views"][0]); json.dump(a,open(sys.argv[1],"w"))' "$R/dupview.json"
expect 2 'view id "layers" is used twice' --rules "$R/dupview.json" test/fixture
echo ok

step "check command (spec/wiki/functional-requirements/FR-035-run-the-check-from-the-command-line-and-in-ci.md)"
# FR-036: exit 1 on the fixture; architecture.json found in the project root; --view; invalid arguments.
expect 1 'layering' --rules test/fixture-architecture.json test/fixture
variant root
cp test/fixture-architecture.json "$R/root/architecture.json"
expect 1 "$LAYER_LINE" "$R/root"
expect 2 'needs --rules' --model "$OUT/fixture.json"
expect 1 'layering' --rules test/fixture-architecture.json --view layers --model "$OUT/fixture.json"
expect 2 'no view "nope"' --rules test/fixture-architecture.json --view nope --model "$OUT/fixture.json"
expect 2 'unknown option --nope' --rules test/fixture-architecture.json --nope test/fixture
expect 2 '--format must be text or json' --rules test/fixture-architecture.json --format xml test/fixture
expect 2 '--fail-on must be error, warning or never' --rules test/fixture-architecture.json --fail-on all test/fixture
# FR-036: a name-based error violation from Kotlin code fails the check; FR-038: exact is false for it.
python3 -I -c 'import json,sys; a=json.load(open("test/fixture-architecture.json")); v=a["views"][0]; v["groups"].append({"id": "notify", "name": "Notify", "color": "#3a86ff", "match": ["package:com.example.notify.**"]}); v["rules"]=[{"id": "notify-no-domain", "kind": "forbid", "from": "notify", "to": "dom"}]; json.dump(a,open(sys.argv[1],"w"))' "$R/kotlin.json"
expect 1 'Notifications.kt:[0-9]*  error  notify-no-domain  notify -> dom' --rules "$R/kotlin.json" test/fixture
# json <out> <check arguments...>: the JSON report, which must exit 1 like the text one.
json() { o=$1; shift; set +e; ./depviewer check --format json "$@" > "$o" 2>/dev/null; got=$?; set -e; [ "$got" = 1 ] || fail "check --format json $*: exit $got, expected 1"; }
json "$R/kotlin-report.json" --rules "$R/kotlin.json" test/fixture
json "$R/dir.json" --rules test/fixture-architecture.json test/fixture
json "$R/model.json" --rules test/fixture-architecture.json --model "$OUT/fixture.json"
# FR-038: fingerprints ignore line numbers.
variant moved
python3 -I -c 'import sys; p=sys.argv[1]; s=open(p).read(); open(p,"w").write(s.replace("    // Deliberate upward", "\n    // Deliberate upward"))' "$R/moved/src/main/java/com/example/domain/Order.java"
json "$R/moved.json" --rules test/fixture-architecture.json "$R/moved"
# FR-011: exact follows the model's provenance, here a Kotlin entry that claims exact resolution.
python3 -I -c 'import json,sys; m=json.load(open(sys.argv[1])); [a.update(exact=True) for a in m["analysis"] if a["language"] == "kotlin"]; json.dump(m,open(sys.argv[2],"w"))' "$OUT/fixture.json" "$R/kotlin-exact-model.json"
json "$R/kotlin-exact.json" --rules "$R/kotlin.json" --model "$R/kotlin-exact-model.json"
python3 -I test/checks/rules.py "$R/dir.json" "$R/model.json" "$R/moved.json" "$R/kotlin-report.json" "$R/kotlin-exact.json"
# FR-039: --update-baseline, then everything is baselined; a removed reference shows as resolved.
expect 0 'wrote 3 fingerprints' --rules test/fixture-architecture.json --baseline "$R/baseline.json" --update-baseline test/fixture
expect 0 'errors: 0, warnings: 0, baselined: 3, resolved: 0' --rules test/fixture-architecture.json --baseline "$R/baseline.json" test/fixture
grep -q '  error  layering  dom -> svc  baselined$' "$OUT/check.log" || { cat "$OUT/check.log"; fail "FR-039: layering violation not baselined"; }
variant fixed
python3 -I -c 'import sys; p=sys.argv[1]; s=open(p).read(); open(p,"w").write(s.replace("return PriceCalculator.gross(net);", "return net;"))' "$R/fixed/src/main/java/com/example/domain/Order.java"
expect 0 'resolved  layers|layering|T:fixture:com.example.domain.Order#total()|T:fixture:com.example.service.PriceCalculator#gross(double)' --rules test/fixture-architecture.json --baseline "$R/baseline.json" "$R/fixed"
expect 2 '--update-baseline needs --baseline' --rules test/fixture-architecture.json --update-baseline test/fixture
# A baseline holds all views: checking one view leaves the others' entries alone.
python3 -I -c 'import json,sys; a=json.load(open("test/fixture-architecture.json")); w=json.loads(json.dumps(a["views"][0])); w["id"]="second"; a["views"].append(w); json.dump(a,open(sys.argv[1],"w"))' "$R/two.json"
expect 0 'wrote 6 fingerprints' --rules "$R/two.json" --baseline "$R/two-baseline.json" --update-baseline test/fixture
expect 0 'resolved: 0' --rules "$R/two.json" --view layers --baseline "$R/two-baseline.json" test/fixture
expect 0 'wrote 3 fingerprints' --rules "$R/two.json" --view layers --baseline "$R/two-baseline.json" --update-baseline test/fixture
[ "$(grep -c '"second|' "$R/two-baseline.json")" = 3 ] || { cat "$R/two-baseline.json"; fail "--update-baseline --view dropped another view's entries"; }
# FR-037: a violation with several lines lists them all.
rules "$R/lines.json" '[{"id": "svc-dom", "kind": "forbid", "from": "svc", "to": "dom"}]'
expect 1 'OrderService.java:17,18  error  svc-dom  svc -> dom' --rules "$R/lines.json" test/fixture
# A broken model is invalid input, not a failing check.
for m in '{"nodes":[{"n":"x"}],"edges":[]}' '{"nodes":["a"],"edges":[]}' '{"nodes":[{"n":"x","k":"module"}],"edges":[[0,5,0,1]]}'; do
  printf '%s' "$m" > "$R/bad-model.json"
  expect 2 'not a depviewer model' --rules test/fixture-architecture.json --model "$R/bad-model.json"
done
# --out writes the viewer with the resolved view and its violations.
expect 1 'layering' --rules test/fixture-architecture.json --out "$R/check.html" test/fixture
grep -q '"violations":\[{"rule":"layering"' "$R/check.html" || fail "--out: violations not embedded"
grep -q '"matched":{' "$R/check.html" || fail "--out: pattern assignments not embedded"
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

# An element a group excludes with "!" does not inherit that group from a manually assigned ancestor,
# in check and in the viewer (the scanner embeds the exclusion).
python3 -I -c 'import json,sys; a=json.load(open("test/fixture-architecture.json")); v=a["views"][0]; d=[g for g in v["groups"] if g["id"]=="dom"][0]; d["match"]=["!path:**/Status.java"]; v["assign"]["P:fixture:com.example.domain"]="dom"; v["rules"]=[{"id": "svc-dom", "kind": "forbid", "from": "svc", "to": "dom"}]; json.dump(a,open(sys.argv[1],"w"))' "$R/exclude.json"
expect 1 'svc-dom' --rules "$R/exclude.json" test/fixture
grep -q 'com.example.domain.Status\b' "$OUT/check.log" && { cat "$OUT/check.log"; fail "an excluded Status.java was checked as part of dom"; }

step "UI scenarios (spec/wiki/quality-requirements/QR-010-every-delivered-viewer-requirement-has-an-automated-ui-scenario.md)"
./depviewer -o "$OUT/fixture-arch.html" --views test/fixture-architecture.json test/fixture 2>/dev/null
./depviewer -o "$OUT/fixture-forbid.html" --views "$R/forbid.json" test/fixture 2>/dev/null
./depviewer -o "$OUT/fixture-exclude.html" --views "$R/exclude.json" test/fixture 2>/dev/null
./depviewer -o "$OUT/fixture-ungrouped.html" --views "$R/ungrouped.json" test/fixture 2>/dev/null
# levels.html: com.example.domain assigned to svc by hand. Its subpackage event is still selected by dom's
# pattern, and Status.java, which dom excludes, inherits svc (FR-028), in check and in the viewer.
python3 -I -c 'import json,sys; a=json.load(open("test/fixture-architecture.json")); v=a["views"][0]; d=[g for g in v["groups"] if g["id"]=="dom"][0]; d["match"]=["package:com.example.domain.**", "!path:**/Status.java"]; v["assign"]["P:fixture:com.example.domain"]="svc"; v["rules"].append({"id": "app-svc", "kind": "forbid", "from": "app", "to": "svc"}); json.dump(a,open(sys.argv[1],"w"))' "$R/levels.json"
./depviewer -o "$OUT/fixture-levels.html" --views "$R/levels.json" test/fixture 2>/dev/null
./depviewer check --rules "$R/levels.json" --format json test/fixture > "$R/levels-report.json" 2>/dev/null || true
python3 -I -c '
import json,sys
r=json.load(open(sys.argv[1])); g={}
for v in r["violations"]:
    for e in (v.get("from"),v.get("to")):
        if e and "path" in e: g.setdefault(e["path"].rsplit("/",1)[-1], e.get("group"))
want={"OrderPlaced.java":"dom","Status.java":"svc"}
bad={k:(g.get(k),w) for k,w in want.items() if g.get(k)!=w}
sys.exit("check groups differ: %s" % bad if bad else 0)' "$R/levels-report.json"
grep -q '"matched":{' "$OUT/fixture-arch.html" || fail "--views: pattern assignments not embedded"
grep -q '"violations":\[{"rule":"layering"' "$OUT/fixture-arch.html" || fail "--views: violations not embedded"
if command -v node >/dev/null 2>&1 && [ -d test/ui/node_modules/@playwright/test ]; then
  B="$(pwd)/$OUT"
  (cd test/ui && DEPVIEWER_BUILD="$B" npx playwright test --output "$B/ui-results")
else
  echo "skipped: Playwright Test not installed (run 'npm ci' in test/ui)"
fi

printf '\nAll checks passed.\n'
