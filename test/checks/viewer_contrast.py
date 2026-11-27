"""Text contrast of the viewer in light and dark (spec/wiki/quality-requirements/QR-008-the-viewer-reads-well-in-light-dark-and-on-a-narrow-screen.md).

Reads the colour variables of viewer.html and checks every pair of text colour and background the
viewer uses against WCAG AA (4.5:1). Also checks that the two dark blocks (prefers-color-scheme and
data-theme="dark") define the same colours.

Usage: python3 -I test/checks/viewer_contrast.py viewer.html
"""
import re
import sys

MIN = 4.5
html = open(sys.argv[1], encoding="utf-8").read()


def block(start, end):
    i = html.index(start)
    return dict(re.findall(r"--([\w-]+):\s*(#[0-9a-fA-F]{6})", html[i:html.index(end, i)]))


light = block(":root {", "}")
dark_media = block(':root:not([data-theme="light"]) {', "}")
dark = block(':root[data-theme="dark"] {', "}")


def lum(h):
    c = [int(h[i:i + 2], 16) / 255 for i in (1, 3, 5)]
    c = [x / 12.92 if x <= 0.03928 else ((x + 0.055) / 1.055) ** 2.4 for x in c]
    return 0.2126 * c[0] + 0.7152 * c[1] + 0.0722 * c[2]


def ratio(a, b):
    x, y = lum(a), lum(b)
    return (max(x, y) + 0.05) / (min(x, y) + 0.05)


rows = ["bg"] + [f"band{i}" for i in range(8)] + ["sel", "peer"]
PAIRS = (
    # text in rows, the header, the toolbar and the details panel
    [(fg, bg) for fg in ("fg", "muted") for bg in rows + ["panel", "toolbar"]]
    + [("cyc", "toolbar")]                                   # "cycles: n" in the header
    # one-letter kind icons: a letter on a coloured badge
    + [("ic-fg", b) for b in ("mod", "pkg", "ext", "cls", "ifc", "enm", "rec", "obj",
                              "tal", "ns", "mix", "xtn", "ann", "priv")]
    # file icons: a coloured letter on the page background
    + [(f, "bg") for f in ("file", "kt", "ts", "jsf", "dart")]
    # selected segment button and the cycle badge in the details
    + [("on-fg", "accent"), ("on-fg", "cyc")]
)

failures = []
if dark_media != dark:
    diff = sorted(k for k in set(dark) | set(dark_media) if dark.get(k) != dark_media.get(k))
    failures.append(f"the two dark blocks differ in: {', '.join(diff)}")
for name, theme in (("light", light), ("dark", dark)):
    for fg, bg in PAIRS:
        if fg not in theme or bg not in theme:
            failures.append(f"{name}: --{fg} or --{bg} is not defined")
            continue
        r = ratio(theme[fg], theme[bg])
        if r < MIN:
            failures.append(f"{name}: --{fg} {theme[fg]} on --{bg} {theme[bg]} is {r:.2f}:1")

if failures:
    print(f"viewer contrast FAILED ({len(failures)}):")
    for f in failures:
        print("  - " + f)
    sys.exit(1)
print(f"viewer contrast ok ({len(PAIRS)} pairs at {MIN}:1 in light and dark)")
