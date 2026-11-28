"""Conformance check of one extractor run against the extractor contract, version 1
(spec/wiki/crosscutting-concepts/CC-002-extractor-contract.md).

Usage: python3 -I test/checks/contract.py <request.json> <fragment.json>

`depviewer` keeps the request and the raw fragment of every extractor run in the folder that
DEPVIEWER_FRAGMENTS names. The check is language-neutral: it knows the contract, not the language.
"""
import json
import sys

NODE_KINDS = {"class", "interface", "enum", "record", "annotation", "object", "typealias", "namespace", "mixin",
              "extension", "ctor", "method", "field", "enumconst"}
EDGE_KINDS = {"uses", "extends", "implements", "call", "new", "access"}

failures = []


def check(cond, msg):
    if not cond and len(failures) < 50:
        failures.append(msg)
    return cond


def is_int(v, low=0):
    return isinstance(v, int) and not isinstance(v, bool) and v >= low


def major(v):
    return int(v) if isinstance(v, (int, float)) and not isinstance(v, bool) else None


request, frag = (json.load(open(p, encoding="utf-8")) for p in sys.argv[1:3])

# --- the request the core sent
check(major(request.get("contract")) == 1, f"request: contract {request.get('contract')!r}, expected 1")
modules = {m.get("name"): m.get("root") for m in request.get("modules") or []}
check(modules, "request: no modules")
files = {(f.get("module"), f.get("path")) for f in request.get("files") or []}
check(all(m in modules for m, _ in files), "request: a file names an unknown module")

# --- the fragment
check(isinstance(frag, dict), "fragment is not an object")
check(major(frag.get("contract")) == 1, f"fragment: contract {frag.get('contract')!r}, expected 1")
check(isinstance(frag.get("language"), str) and frag["language"], "fragment: no language")
check(is_int(frag.get("errors")), f"fragment: errors {frag.get('errors')!r} is not a count")
sep = frag.get("packageSep", "/")
check(sep in ("/", "."), f"fragment: packageSep {sep!r}")
for p in frag.get("packages") or []:
    check(p.get("module") in modules and isinstance(p.get("package"), str), f"fragment: package entry {p}")
for a in frag.get("languages") or []:
    check(isinstance(a.get("language"), str) and isinstance(a.get("tool"), str) and isinstance(a.get("exact"), bool)
          and all(m in modules for m in a.get("modules") or [None]), f"fragment: languages entry {a}")

ids, loc_of_file, file_of = {}, {}, {}
for i, n in enumerate(frag.get("nodes") or []):
    nid, kind = n.get("id"), n.get("kind")
    if kind == "package":   # creates a package at this place; it has no id and nothing refers to it
        check(n.get("module") in modules and isinstance(n.get("package"), str), f"node {i}: package entry {n}")
        continue
    if not check(isinstance(nid, str) and nid, f"node {i}: no id"):
        continue
    check(nid not in ids, f"node {nid}: duplicate id")
    if kind == "file":
        module = n.get("module", request.get("module"))
        check(module in modules, f"file {nid}: unknown module {module!r}")
        check((module, n.get("path")) in files, f"file {nid}: {n.get('path')!r} is not a file of the request")
        check(isinstance(n.get("package", ""), str), f"file {nid}: package is not a string")
        check(isinstance(n.get("test", False), bool), f"file {nid}: test is not a boolean")
        check(is_int(n.get("loc", 0)), f"file {nid}: loc {n.get('loc')!r}")
        loc_of_file[nid] = n.get("loc", 0)
        file_of[nid] = nid
    else:
        check(kind in NODE_KINDS, f"node {nid}: kind {kind!r} is not allowed")
        parent = n.get("parent")
        if check(parent in ids, f"node {nid}: parent {parent!r} missing or after its child"):
            file_of[nid] = file_of.get(parent)
        check(isinstance(n.get("name"), str) and n["name"], f"node {nid}: no name")
        line, loc = n.get("line"), n.get("loc", 0)
        size = loc_of_file.get(file_of.get(nid))
        if check(is_int(line, 1) and is_int(loc), f"node {nid}: line {line!r}, loc {loc!r}") and size:
            check(line + max(loc, 1) - 1 <= size, f"node {nid}: lines {line}+{loc} outside its file of {size} lines")
        check(n.get("vis") in (None, "+", "#", "-", "~"), f"node {nid}: vis {n.get('vis')!r}")
    ids[nid] = kind

externals = set()
for x in frag.get("externals") or []:
    xid = x.get("id")
    check(isinstance(xid, str) and xid not in ids and xid not in externals, f"external {xid!r}: missing or duplicate id")
    check(isinstance(x.get("package"), str) and isinstance(x.get("name"), str), f"external {xid}: package or name missing")
    externals.add(xid)

for i, e in enumerate(frag.get("edges") or []):
    f, t = e.get("from"), e.get("to")
    check(f in ids, f"edge {i}: from {f!r} is not a node")
    check(t in ids or t in externals, f"edge {i}: to {t!r} is neither a node nor an external")
    check(e.get("kind") in EDGE_KINDS, f"edge {i}: kind {e.get('kind')!r}")
    lines = e["lines"] if "lines" in e else [e.get("line")] if "line" in e else []
    if "count" in e:
        check(is_int(e["count"], 1), f"edge {i}: count {e['count']!r}")
    size = loc_of_file.get(file_of.get(f))
    for line in lines:
        if check(is_int(line, 1), f"edge {i}: line {line!r}") and size:
            check(line <= size, f"edge {i}: line {line} outside its file of {size} lines")

if failures:
    print(f"FAIL contract.py {sys.argv[2]}:\n  " + "\n  ".join(failures))
    sys.exit(1)
print(f"contract: ok ({frag.get('language')}, {len(ids)} nodes, {len(externals)} externals, {len(frag.get('edges') or [])} edges)")
