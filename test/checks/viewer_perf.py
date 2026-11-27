"""Viewer performance on a large model (spec/wiki/quality-requirements/QR-001-the-viewer-stays-responsive-on-large-projects.md).

Usage: python3 -I test/checks/viewer_perf.py build/large.dom.html

Reads the timings the viewer published with ?bench (<body data-bench="{...}">).
"""
import html
import json
import re
import sys

dom = open(sys.argv[1], encoding="utf-8").read()
body = re.search(r"<body[^>]*>", dom).group(0)
m = re.search(r'data-bench="([^"]*)"', body)
if not m:
    sys.exit("FAIL: <body data-bench> missing - did the viewer load the large model and run ?bench?")
b = json.loads(html.unescape(m.group(1)))

LIMITS = {"members": 500, "sort": 500, "levelize": 500, "hover": 150, "scroll": 150}
bad = [f"{k} {b[k]} ms > {v} ms" for k, v in LIMITS.items() if b[k] > v]
if b["domRows"] >= 1000:
    bad.append(f"{b['domRows']} rows in the DOM (want < 1000)")
if b["domArcs"] >= 5000:
    bad.append(f"{b['domArcs']} arcs in the DOM (want < 5000)")
if bad:
    sys.exit("FAIL: viewer too slow on the large model: " + "; ".join(bad) + f"  (all: {b})")
print("ok (" + ", ".join(f"{k} {v}" for k, v in b.items()) + ")")
