#!/bin/sh
# Regenerates docs/screenshot-{light,dark}.png (used by the README) from the fixture demo.
# Needs Chrome/Chromium (set CHROME_BIN if it is not found). Run after visible UI changes.
set -eu
cd "$(dirname "$0")/.."

CHROME=""
for c in "${CHROME_BIN:-}" google-chrome google-chrome-stable chromium chromium-browser \
         "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome" \
         "/Applications/Chromium.app/Contents/MacOS/Chromium"; do
  [ -n "$c" ] || continue
  if command -v "$c" >/dev/null 2>&1 || [ -x "$c" ]; then CHROME=$c; break; fi
done
[ -n "$CHROME" ] || { echo "no Chrome/Chromium found (set CHROME_BIN)" >&2; exit 1; }

mkdir -p build docs
./depviewer -o build/fixture.html --views test/fixture-views.json test/fixture 2>/dev/null
url="file://$(pwd)/build/fixture.html"
for theme in dark:0 light:1; do
  name=${theme%%:*}; scheme=${theme##*:}
  "$CHROME" --headless=new --disable-gpu --no-sandbox --no-first-run --user-data-dir="build/chrome-shot-$name" \
    --hide-scrollbars --force-device-scale-factor=2 --window-size=1400,680 \
    --blink-settings=preferredColorScheme="$scheme" --virtual-time-budget=5000 \
    --screenshot="docs/screenshot-$name.png" "$url" 2>/dev/null
  echo "docs/screenshot-$name.png"
done
