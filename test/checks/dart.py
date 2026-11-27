"""Model checks for test/fixture-dart (spec/wiki/functional-requirements/FR-006-analyse-dart-with-the-dart-analyzer.md).

Usage: python3 -I test/checks/dart.py <deps.json>
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from common import Checker  # noqa: E402

c = Checker(sys.argv[1])
check, node, edge, nodes = c.check, c.node, c.edge, c.nodes

PD, CD = "lib/src/models/product.dart#", "lib/src/models/cart.dart#"
MD, KD = "lib/src/util/money.dart#", "lib/src/services/checkout.dart#"
MAIN, TEST = "bin/main.dart#main()", "test/cart_test.dart#main()"

# --- structure ---------------------------------------------------------------
check(c.data.get("edgeKinds") == ["uses", "extends", "implements", "call", "new", "access"], "edgeKinds changed")
check(nodes[node("shop_app", "module")].get("p") is None, "module must be a root")      # name from pubspec.yaml
models = node("lib/src/models", "package")
check(nodes[models].get("sep") == "/", "folder packages must be marked with sep '/'")
check(nodes[node("lib/src/models/product.dart", "file")]["p"] == models, "file must sit in its folder's package")
node("lib/src/models/product_codes.dart", "file")      # part file
node("lib/shop_app.dart", "file")
check(nodes[node("test/cart_test.dart", "file")].get("t") == 1, "test file not flagged")
check(nodes[node("bin/main.dart", "file")].get("t") is None, "bin file flagged as test")
c.parents_first()

node(PD + "Priced", "interface")
node(PD + "Priced.price()", "method")
node(PD + "Category", "enum")
node(PD + "Category.books", "enumconst")
node(PD + "Category.discount", "field")                # getter
node(PD + "ProductId", "typealias")
node(PD + "Product", "class")
check(nodes[node(PD + "Product.count", "field")].get("s") == 1, "static field not static")
check(nodes[node(PD + "Product._base", "field")].get("v") == "-", "library-private field not private")
node(PD + "Product.Product(ProductId, double, Category)", "ctor")   # field formals take the field's type
node(PD + "Product.Product.free(ProductId)", "ctor")
node(PD + "Product.Product.parse(String)", "ctor")    # factory
node(PD + "Product.price()", "method")
check(nodes[node("lib/src/models/product_codes.dart#codeValue(String)", "method")]["p"] == node("lib/src/models/product_codes.dart"),
      "top-level function of a part file must sit under the part file")
node(CD + "Cart", "class")
check(nodes[node(CD + "Cart._items", "field")].get("v") == "-", "private field not private")
node(CD + "Cart.add(Product)", "method")
node(CD + "Cart.total()", "method")
node(CD + "Cart.describe()", "method")
node(CD + "Describable", "mixin")
node(CD + "Describable.describe()", "method")
check(nodes[node(MD + "currency", "field")]["p"] == node("lib/src/util/money.dart"), "top-level variable must sit under its file")
node(MD + "formatMoney(double)", "method")
node(MD + "CartChecks", "extension")
node(MD + "CartChecks.isEmpty", "field")
node(KD + "Checkout", "class")
node(KD + "Checkout._events", "field")
node(KD + "Checkout.receipt(Cart)", "method")
node(MAIN, "method")
node(TEST, "method")
for ext in ["dart:core.Map", "dart:core.String", "dart:async.Future", "dart:async.StreamController"]:
    node(ext, "ext-type")
check(nodes[node("dart:async.Future")]["p"] == node("dart:async", "ext-package"), "external type not in its package")

# Files excluded by analysis_options.yaml are skipped; the rest of the module is still scanned.
check(c.by_q.get("lib/src/generated/api.g.dart#GeneratedApi") is None, "analyzer-excluded file must be skipped")

# Workspace: the second package is its own module, and the app's references into it link to its
# real declarations (not to an external package).
core = node("shop_core", "module")
check(nodes[node("lib/currency.dart", "file")].get("p") is not None, "shop_core file missing")
node("lib/currency.dart#Currency", "class")
node("lib/currency.dart#Currency.format(double)", "method")
check(not any(n.get("k") == "ext-package" and n.get("n") == "shop_core" for n in nodes),
      "a scanned package must not appear as an external package")

# --- dependencies --------------------------------------------------------------
edge(PD + "Product", PD + "Priced", "implements")
edge(CD + "Cart", CD + "Describable", "implements")                       # with (mixin)
edge(PD + "Product.Product(ProductId, double, Category)", PD + "Product.count", "access")
edge(PD + "Product.Product.free(ProductId)", PD + "Product.Product(ProductId, double, Category)", "new")   # : this(...)
edge(PD + "Product.Product.parse(String)", PD + "Product.Product(ProductId, double, Category)", "new")
edge(PD + "Product.Product.parse(String)", "lib/src/models/product_codes.dart#codeValue(String)", "call")  # part file
edge(PD + "Product.price()", PD + "Product._base", "access")
edge(PD + "Product.price()", PD + "Category.discount", "access")
edge(PD + "Category.discount", PD + "Category.books", "access")
edge(CD + "Cart._items", "dart:core.Map", "uses")
edge(CD + "Cart._items", PD + "Product", "uses")
edge(CD + "Cart._items", PD + "ProductId", "uses")
edge(CD + "Cart.add(Product)", PD + "Product.id", "access")
edge(CD + "Cart.add(Product)", CD + "Cart._items", "access")
edge(CD + "Cart.total()", PD + "Product.price()", "call")
edge(CD + "Cart.describe()", MD + "formatMoney(double)", "call")           # models -> util
edge(CD + "Cart.describe()", CD + "Cart.total()", "call")
edge(MD + "formatMoney(double)", MD + "currency", "access")
edge(MD + "formatMoney(double)", "lib/currency.dart#Currency.format(double)", "call")   # into shop_core
edge(MD + "formatMoney(double)", "lib/currency.dart#Currency.eur", "access")
edge(MD + "CartChecks", CD + "Cart", "uses")                               # util -> models (cycle)
edge(MD + "CartChecks.isEmpty", CD + "Cart.total()", "call")
edge(KD + "Checkout._events", "dart:async.StreamController", "new")
edge(KD + "Checkout.receipt(Cart)", MD + "CartChecks.isEmpty", "access")   # extension getter
edge(KD + "Checkout.receipt(Cart)", MD + "formatMoney(double)", "call")
edge(KD + "Checkout.receipt(Cart)", CD + "Cart.total()", "call")
edge(KD + "Checkout.receipt(Cart)", KD + "Checkout._events", "access")
edge(KD + "Checkout.receipt(Cart)", "dart:async.StreamController", "call")
edge(KD + "Checkout.receipt(Cart)", "dart:async.Future", "uses")
edge(MAIN, CD + "Cart", "new")                                             # implicit default constructor
edge(MAIN, CD + "Cart.add(Product)", "call")
edge(MAIN, PD + "Product.Product.parse(String)", "new")                    # factory invocation
edge(MAIN, KD + "Checkout", "new")
edge(MAIN, KD + "Checkout.receipt(Cart)", "call")
edge(TEST, PD + "Product.Product.free(ProductId)", "new")

# The class name in a constructor declaration (`Product(this.id, ...)`) declares, it does not reference.
edge(PD + "Product.Product(ProductId, double, Category)", PD + "Product", "uses", present=False)
edge(PD + "Product.Product.free(ProductId)", PD + "Product", "uses", present=False)

# Imports alone are not dependencies.
edge("lib/src/util/money.dart", CD + "Cart", "uses", present=False)
check(all(f != t for f, t, _ in c.edges), "self-dependency found")

c.finish("dart")
