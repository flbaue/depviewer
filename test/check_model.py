"""Checks the model DepScan produces for test/fixture.

Usage: python3 test/check_model.py <deps.json>

Each check names one fact about the fixture sources. When you change the scanner on purpose,
update the expectation here in the same commit (and say why in the commit message).
"""
import json
import sys

failures = []


def check(cond, msg):
    if not cond:
        failures.append(msg)


data = json.load(open(sys.argv[1], encoding="utf-8"))
nodes, kinds = data["nodes"], data["edgeKinds"]


def qname(n):
    return n.get("q", n.get("path", n["n"]))


by_q = {}
for i, n in enumerate(nodes):
    by_q.setdefault(qname(n), i)
edges = {(f, t, kinds[k]): (c, lines) for f, t, k, c, lines in data["edges"]}


def node(q, kind=None):
    i = by_q.get(q)
    check(i is not None, f"missing node {q}")
    if i is not None and kind:
        check(nodes[i]["k"] == kind, f"{q}: expected kind {kind}, got {nodes[i]['k']}")
    return i


def edge(src, dst, kind, present=True):
    f, t = by_q.get(src), by_q.get(dst)
    if f is None or t is None:
        check(False, f"edge {src} -{kind}-> {dst}: endpoint missing")
        return
    found = (f, t, kind) in edges
    check(found == present, f"edge {src} -{kind}-> {dst} should {'exist' if present else 'not exist'}")
    if found and present:
        c, lines = edges[(f, t, kind)]
        check(c >= 1 and all(l > 0 for l in lines), f"edge {src} -{kind}-> {dst}: bad count/lines {c} {lines}")


# --- structure -------------------------------------------------------------
check(data.get("edgeKinds") == ["uses", "extends", "implements", "call", "new", "access"], "edgeKinds changed")
mod = node("fixture", "module")  # module name comes from the pom's artifactId
check(nodes[mod].get("p") is None, "module must be a root")
node("com.example.domain", "package")
node("src/main/java/com/example/domain/Order.java", "file")
node("com.example.domain.Order", "class")
node("com.example.domain.Order.Line", "class")
check(nodes[node("com.example.domain.Order.Line")]["p"] == node("com.example.domain.Order"), "nested class must sit under its outer class")
node("com.example.domain.OrderRepository", "interface")
node("com.example.domain.Status", "enum")
node("com.example.domain.Status.NEW", "enumconst")
node("com.example.domain.Order.Order(String)", "ctor")
node("com.example.domain.Order.total()", "method")
node("com.example.domain.Order.customer", "field")
node("com.example.service.OrderService.totals(List<Order>)", "method")
node("java.util.List", "ext-type")

# Compiler-generated members must not show up (default constructors etc.).
check("com.example.app.Main.Main()" not in by_q, "generated default constructor leaked into the model")

# Tests are flagged so the viewer can hide them.
check(nodes[node("src/test/java/com/example/domain/OrderTest.java", "file")].get("t") == 1, "test file not flagged")
check(nodes[node("src/main/java/com/example/domain/Order.java")].get("t") is None, "main file flagged as test")
check(nodes[node("?Test", "ext-type")]["p"] == node("(unresolved)", "ext-package"), "unresolved import not grouped")

# Parents come before children (the viewer relies on it).
for i, n in enumerate(nodes):
    if "p" in n:
        check(n["p"] < i, f"node {i} ({qname(n)}) appears before its parent")

# --- dependencies ----------------------------------------------------------
edge("com.example.infra.InMemoryOrderRepository", "com.example.domain.OrderRepository", "implements")
edge("com.example.app.Main.main(String[])", "com.example.service.OrderService.place(String)", "call")
edge("com.example.app.Main.main(String[])", "com.example.service.OrderService.OrderService(OrderRepository)", "new")
edge("com.example.service.OrderService.place(String)", "com.example.domain.OrderRepository.save(Order)", "call")
edge("com.example.service.OrderService.place(String)", "com.example.domain.Status.PLACED", "access")
edge("com.example.service.OrderService.totals(List<Order>)", "com.example.domain.Order.total()", "call")  # method reference
edge("com.example.domain.Order.total()", "com.example.service.PriceCalculator.gross(double)", "call")  # the package cycle
edge("com.example.domain.Order.total()", "com.example.domain.Order.Line.amount", "access")
edge("com.example.domain.Order.lines", "java.util.ArrayList", "new")

# No self loops, and implicit super() calls must not create dependencies on java.lang.Object.
check(all(f != t for f, t, _ in edges), "self-dependency found")
check("java.lang.Object" not in by_q, "dependency on java.lang.Object (explicit super() call?)")

if failures:
    print(f"model check FAILED ({len(failures)}):")
    for f in failures:
        print("  - " + f)
    sys.exit(1)
print(f"model check ok ({len(nodes)} nodes, {len(edges)} edges)")
