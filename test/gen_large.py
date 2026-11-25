"""Generate a large synthetic model and embed it into the viewer (docs/specs/viewer-performance.md).

Usage: python3 -I test/gen_large.py viewer.html build/large.html

Sized like a real mid-sized project (about 650 files, 2,700 types, 8,300 members and 57,000
distinct dependencies), with layered packages, some upward dependencies that close cycles, test
code and external types. Deterministic: the same input always gives the same model.
"""
import json
import random
import sys

R = random.Random(1)
nodes, edges = [], {}


def node(**d):
    nodes.append({k: v for k, v in d.items() if v is not None})
    return len(nodes) - 1


types_by_pkg, members_by_pkg, ext_types = [], [], []
layer_of = []  # per package: 0 = bottom layer
for m in range(3):
    mod = node(n=f"module{m}", k="module")
    com = node(n="com", k="package", p=mod)
    ex = node(n="example", k="package", p=com, q="com.example")
    for a in range(4):
        area = node(n=f"area{m}{a}", k="package", p=ex, q=f"com.example.area{m}{a}")
        for s in range(5):
            q = f"com.example.area{m}{a}.sub{s}"
            pkg = node(n=f"sub{s}", k="package", p=area, q=q)
            layer_of.append(m * 20 + a * 5 + s)
            ts, ms = [], []
            for f in range(11):
                test = f >= 9
                fname = f"File{m}{a}{s}{f:02d}"
                fi = node(n=fname + ".java", k="file", p=pkg, l=1, loc=120, t=1 if test else None,
                          path=f"src/{'test' if test else 'main'}/java/{q.replace('.', '/')}/{fname}.java")
                for t in range(4 + (f % 2)):
                    tn = f"{fname}Type{t}"
                    ty = node(n=tn, k="interface" if t == 3 else "class", p=fi, l=3 + t * 30, loc=28,
                              t=1 if test else None, v="+", q=f"{q}.{tn}")
                    ts.append(ty)
                    for k in range(3):
                        mn = f"op{k}(int)" if k else "value"
                        mem = node(n=mn, k="method" if k else "field", p=ty, l=5 + t * 30 + k * 8, loc=7,
                                   t=1 if test else None, v="+" if k else "-", q=f"{q}.{tn}.{mn}")
                        ms.append(mem)
            types_by_pkg.append(ts)
            members_by_pkg.append(ms)
ext = node(n="External", k="ext-root")
for p in ("java.util", "java.io", "java.time"):
    ep = node(n=p, k="ext-package", p=ext)
    for i in range(12):
        ext_types.append(node(n=f"Ext{i}", k="ext-type", p=ep, q=f"{p}.Ext{i}"))

npkg = len(members_by_pkg)
order = sorted(range(npkg), key=lambda i: layer_of[i])
rank = {p: i for i, p in enumerate(order)}
target_edges = 57000
while len(edges) < target_edges:
    sp = R.randrange(npkg)
    src = R.choice(members_by_pkg[sp])
    r = R.random()
    if r < 0.08:
        tgt, kind = R.choice(ext_types), 0
    else:
        if r < 0.11:   # upward: closes cycles between packages
            tp = order[min(npkg - 1, rank[sp] + R.randrange(1, 6))]
        elif r < 0.45:  # same package
            tp = sp
        else:          # downward, mostly to nearby layers
            tp = order[max(0, rank[sp] - 1 - int(R.expovariate(1 / 8)))]
        pool = members_by_pkg[tp] if R.random() < 0.8 else types_by_pkg[tp]
        tgt, kind = R.choice(pool), R.choice((0, 3, 3, 3, 4, 5))
    if tgt == src:
        continue
    key = (src, tgt, kind)
    if key not in edges:
        edges[key] = [src, tgt, kind, 1, [R.randrange(1, 120)]]
    else:
        edges[key][3] += 1

model = {"title": "large-synthetic", "generated": "", "errors": 0,
         "edgeKinds": ["uses", "extends", "implements", "call", "new", "access"],
         "nodes": nodes, "edges": list(edges.values())}
data = json.dumps(model, separators=(",", ":")).replace("<", "\\u003c").replace(">", "\\u003e").replace("&", "\\u0026")
template = open(sys.argv[1], encoding="utf-8").read()
assert template.count("/*DEPS_DATA*/null") == 1
open(sys.argv[2], "w", encoding="utf-8").write(template.replace("/*DEPS_DATA*/null", data))
files = sum(1 for n in nodes if n["k"] == "file")
types = sum(1 for n in nodes if n["k"] in ("class", "interface"))
members = sum(1 for n in nodes if n["k"] in ("method", "field"))
print(f"{files} files, {types} types, {members} members, {len(edges)} edges -> {sys.argv[2]}")
