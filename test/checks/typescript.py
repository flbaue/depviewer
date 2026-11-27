"""Model checks for test/fixture-ts (spec/wiki/functional-requirements/FR-005-analyse-typescript-and-javascript-with-the-typescript-compiler.md).

Usage: python3 -I test/checks/typescript.py <deps.json>
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from common import Checker  # noqa: E402

c = Checker(sys.argv[1])
check, node, edge, nodes = c.check, c.node, c.edge, c.nodes

P, C, M = "src/model/product.ts#", "src/model/cart.ts#", "src/util/money.ts#"
K, L = "src/services/checkout.ts#", "src/services/logger.ts#"
T = "src/__tests__/cart.test.ts"
R = "src/features/payments/refund.ts#"

# --- structure ---------------------------------------------------------------
check(c.data.get("edgeKinds") == ["uses", "extends", "implements", "call", "new", "access"], "edgeKinds changed")
mod = node("shop-web", "module")                       # name from package.json
check(nodes[mod].get("p") is None, "module must be a root")
model_pkg = node("src/model", "package")               # packages are folders...
check(nodes[model_pkg].get("sep") == "/", "folder packages must be marked with sep '/'")
check(nodes[node("src/model/product.ts", "file")]["p"] == model_pkg, "file must sit in its folder's package")
node("src/util/legacy.js", "file")                     # JavaScript is scanned too
node("src/util/index.ts", "file")
check(not any("node_modules" in c.qname(n) for n in nodes if n["k"] != "ext-type" and n["k"] != "ext-package"),
      "nothing from node_modules may become a project node")
check(nodes[node(T, "file")].get("t") == 1, "test file not flagged")
check(nodes[node("src/model/cart.ts")].get("t") is None, "main file flagged as test")
c.parents_first()

node(P + "Priced", "interface")
node(P + "Priced.price()", "method")
node(P + "Category", "enum")
node(P + "Category.Books", "enumconst")
node(P + "ProductId", "typealias")
node(P + "Product", "class")
node(P + "Product.Product(ProductId, number, Category)", "ctor")
node(P + "Product.id", "field")                       # constructor parameter property
check(nodes[node(P + "Product.base", "field")].get("v") == "-", "private parameter property not private")
check(nodes[node(P + "Product.count", "field")].get("s") == 1, "static field not static")
check(nodes[node(P + "Product.#secret", "field")].get("v") == "-", "#private field not private")
check(nodes[node(P + "Product.tags", "field")].get("v") == "-", "private field not private")
node(P + "Product.price()", "method")
node(C + "Cart", "class")
node(C + "Cart.add(Product)", "method")
node(C + "Cart.total()", "method")
check(nodes[node(M + "CURRENCY", "field")]["p"] == node("src/util/money.ts"), "top-level const must sit under its file")
check(nodes[node(M + "formatMoney(number)", "method")]["p"] == node("src/util/money.ts"), "top-level function must sit under its file")
node(M + "isEmpty(Cart)", "method")                    # arrow function assigned to a const
node("src/util/legacy.js#roundTo(any, any)", "method") # untyped JS parameters
node(L + "Logger", "class")
node(L + "Logger.info(string)", "method")
node(K + "events", "field")
node(K + "Checkout", "namespace")
node(K + "Checkout.receipt(Cart)", "method")
node(K + "pay(Cart)", "method")
node("src/features/payments/refund.ts#refund(Cart)", "method")
check(nodes[node(R + "refundNote(Cart)", "method")]["p"] == node("src/features/payments/refund.ts"),
      "unexported top-level function must sit under its file")
node(T + "#cart", "field")
node("@acme/events.Emitter", "ext-type")
check(nodes[node("@acme/events.Emitter")]["p"] == node("@acme/events", "ext-package"), "external type not in its package")
node("(builtin).Map", "ext-type")
node("(builtin).Promise", "ext-type")

# --- dependencies --------------------------------------------------------------
edge(P + "Product", P + "Priced", "implements")
edge(P + "Product.Product(ProductId, number, Category)", P + "Product.count", "access")
edge(P + "Product.price()", P + "Product.base", "access")
edge(P + "Product.price()", P + "Category.Books", "access")
edge(P + "Product.price()", P + "Product.#secret", "access")
edge(C + "Cart.items", "(builtin).Map", "new")
edge(C + "Cart.add(Product)", P + "Product", "uses")
edge(C + "Cart.add(Product)", P + "Product.id", "access")
edge(C + "Cart.add(Product)", C + "Cart.items", "access")
edge(C + "Cart.total()", P + "Product.price()", "call")
edge(C + "Cart.describe()", M + "formatMoney(number)", "call")      # model -> util
edge(C + "Cart.describe()", C + "Cart.total()", "call")
edge(M + "isEmpty(Cart)", C + "Cart", "uses")                        # util -> model (cycle)
edge(M + "isEmpty(Cart)", C + "Cart.total()", "call")
edge(M + "formatMoney(number)", M + "CURRENCY", "access")
edge(K + "Checkout.receipt(Cart)", "src/util/legacy.js#roundTo(any, any)", "call")   # via index.ts re-export
edge(K + "Checkout.receipt(Cart)", M + "formatMoney(number)", "call")                # via export *
edge(K + "Checkout.receipt(Cart)", L + "Logger", "new")                              # default import, no declared ctor
edge(K + "Checkout.receipt(Cart)", L + "Logger.info(string)", "call")
edge(K + "Checkout.receipt(Cart)", K + "events", "access")
edge(K + "Checkout.receipt(Cart)", "@acme/events.Emitter", "call")                   # member of an external class
edge(K + "events", "@acme/events.Emitter", "new")
edge(K + "pay(Cart)", "(builtin).Promise", "uses")
edge("src/features/payments/refund.ts#refund(Cart)", K + "pay(Cart)", "call")
edge(R + "refundNote(Cart)", M + "formatMoney(number)", "call")                    # via tsconfig path alias
check(not any(n["k"].startswith("ext") and n["n"].startswith("@util") for n in nodes), "path alias must not become an external")
edge(T + "#cart", C + "Cart", "new")
edge(T, T + "#cart", "access")                                                       # top-level statements count for the file
edge(T, C + "Cart.add(Product)", "call")
edge(T, P + "Product.Product(ProductId, number, Category)", "new")
edge(T, P + "Category.Food", "access")
edge(T, "(builtin).Error", "new")

# Imports alone are not dependencies.
edge("src/util/money.ts", C + "Cart", "uses", present=False)
check(all(f != t for f, t, _ in c.edges), "self-dependency found")

c.finish("typescript")
