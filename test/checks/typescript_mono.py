"""Model checks for test/fixture-ts-mono, a TypeScript monorepo with npm workspaces
(spec/wiki/functional-requirements/FR-012-link-the-modules-of-a-monorepo.md).

check.sh scans a copy whose node_modules links the workspace packages, as `npm install` would.

Usage: python3 -I test/checks/typescript_mono.py <deps.json>
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from common import Checker  # noqa: E402

c = Checker(sys.argv[1])
check, node, edge, nodes = c.check, c.node, c.edge, c.nodes

core, app = node("@acme/core", "module"), node("@acme/app", "module")
c.parents_first()
check(nodes[node("src/price.ts", "file")]["path"] == "src/price.ts", "file paths are relative to their module")

def module_of(i):
    while nodes[i].get("p") is not None:
        i = nodes[i]["p"]
    return i

P, C = "src/price.ts#Price", "src/cart.ts#Cart"
check(module_of(node(P, "class")) == core, "Price must sit in @acme/core")
check(module_of(node(C, "class")) == app, "Cart must sit in @acme/app")
check(sum(1 for n in nodes if c.qname(n) == P) == 1, "Price must be declared once")

# The import by package name links to the declaration in the sibling package, not to an external.
edge(C + ".total", P, "uses")
edge(C + ".total", P + ".Price(number)", "new")
edge(C + ".add(Price)", P + ".add(Price)", "call")
ext = [c.qname(n) for n in nodes if n["k"].startswith("ext") and ("acme" in c.qname(n) or "(other)" in c.qname(n))]
check(not ext, f"workspace packages must not show up as externals: {ext}")

an = c.analysis("typescript", ["@acme/app", "@acme/core"], "typescript", True, "depviewer-typescript-extractor")
check(len(c.data.get("analysis") or []) == 1, "one extractor run, one analysis entry")

c.finish("typescript monorepo")
