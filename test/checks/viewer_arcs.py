"""Viewer arc geometry (spec/wiki/quality-requirements/QR-002-arcs-never-cover-the-labels.md), checked on a DOM dumped by headless Chrome.

Usage: python3 -I test/checks/viewer_arcs.py build/fixture-members.dom.html

Every downward arc (bulging left of the anchor column) must stay at least MARGIN px to the right
of the widest label, which the viewer publishes as <body data-labels="...">.
"""
import math
import re
import sys

MARGIN = 4

dom = open(sys.argv[1], encoding="utf-8").read()
body = re.search(r"<body[^>]*>", dom).group(0)
m = re.search(r'data-labels="(\d+)"', body)
if not m:
    sys.exit("FAIL: <body data-labels> missing (viewer must publish the widest label's right edge)")
labels = int(m.group(1))

arc = re.compile(r'<path class="arc [^"]*" data-a="\d+" d="M([\d.]+) ([\d.]+)A([\d.]+) ([\d.]+) 0 0 0 ([\d.]+) ([\d.]+)"')
checked, bad = 0, []
for x1, y1, rx, ry, x2, y2 in (tuple(map(float, g)) for g in arc.findall(dom)):
    if x2 >= x1:  # upward arc, bulges right
        continue
    checked += 1
    # SVG scales the radii up when the ellipse is too small for the chord (endpoints differ in x).
    hx, hy = (x1 - x2) / 2, (y1 - y2) / 2
    lam = (hx / rx) ** 2 + (hy / ry) ** 2 if ry else 1
    left = (x1 + x2) / 2 - rx * math.sqrt(max(1.0, lam))
    if left < labels + MARGIN:
        bad.append((round(left, 1), y1, y2))

if not checked:
    sys.exit("FAIL: no downward arcs found to check")
if bad:
    sys.exit(f"FAIL: {len(bad)} of {checked} downward arcs reach into the labels (right edge {labels}px), e.g. {bad[:3]}")
print(f"ok ({checked} downward arcs stay right of the labels at {labels}px)")
