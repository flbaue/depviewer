"""Model checks for test/fixture (Java + Kotlin). Usage: python3 -I test/checks/jvm.py <deps.json>"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from common import Checker  # noqa: E402

c = Checker(sys.argv[1])
check, node, edge = c.check, c.node, c.edge
data, nodes, by_q, edges = c.data, c.nodes, c.by_q, c.edges


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
c.parents_first()

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

# --- Kotlin (docs/specs/kotlin-support.md) -----------------------------------
KT = "src/main/kotlin/com/example/notify/"
N = "com.example.notify."

# Source discovery and model
node(KT + "Notifications.kt", "file")
check(nodes[node("src/test/kotlin/com/example/notify/FormatTest.kt", "file")].get("t") == 1, "Kotlin test file not flagged")
node(N + "Notifier", "interface")
node(N + "EmailNotifier", "class")
node(N + "OrderEvent", "class")
node(N + "NotificationService", "object")
node(N + "Channel", "enum")
node(N + "Channel.SMS", "enumconst")
node(N + "Channel.Companion", "object")
node(N + "Channel.Companion.default()", "method")
node(N + "EmailNotifier.EmailNotifier(String)", "ctor")
node(N + "EmailNotifier.sender", "field")            # constructor `private val`
check(nodes[node(N + "EmailNotifier.sender")].get("v") == "-", "private constructor property not private")
node(N + "OrderEvent.status", "field")
node(N + "NotificationService.publish(OrderEvent)", "method")
fmt = node(N + "NotificationsKt.format(OrderEvent, Double)", "method")      # top-level -> file facade
check(nodes[fmt]["p"] == node(KT + "Notifications.kt"), "top-level function must sit directly under its file")
node(N + "ChannelKt.Channel.isInstant()", "method")  # extension function

# Kotlin -> Kotlin / Java
edge(N + "EmailNotifier", N + "Notifier", "implements")
edge(N + "OrderEvent.order", "com.example.domain.Order", "uses")
edge(N + "OrderEvent.status", "com.example.domain.Status", "uses")   # import ... as OrderStatus
edge(N + "NotificationService.publish(OrderEvent)", N + "OrderEvent.order", "access")
edge(N + "NotificationService.publish(OrderEvent)", "com.example.domain.Order.total()", "call")  # property chain
edge(N + "NotificationService.publish(OrderEvent)", N + "Notifier.send(String)", "call")        # lambda `it`
edge(N + "NotificationService.publish(OrderEvent)", N + "NotificationsKt.format(OrderEvent, Double)", "call")
edge(N + "NotificationsKt.format(OrderEvent, Double)", N + "OrderEvent.status", "access")       # string template
edge(N + "Channel.Companion.default()", N + "Channel.EMAIL", "access")
edge(N + "ChannelKt.Channel.isInstant()", N + "Channel.SMS", "access")
edge(N + "FormatTest.formatsTotal()", N + "OrderEvent.OrderEvent(Order, OrderStatus)", "new")
edge(N + "FormatTest.formatsTotal()", "com.example.domain.Order.Order(String)", "new")
edge(N + "EmailNotifier.send(String)", "kotlin.String", "uses")

# Java -> Kotlin (matched by name; javac cannot see Kotlin)
MAIN = "com.example.app.Main.main(String[])"
edge(MAIN, N + "EmailNotifier", "uses")
edge(MAIN, N + "NotificationService", "uses")
edge(MAIN, N + "NotificationService.register(Notifier)", "call")
edge(MAIN, N + "EmailNotifier.EmailNotifier(String)", "new")
edge(MAIN, N + "NotificationsKt.format(OrderEvent, Double)", "call")   # top-level function via file facade
edge(MAIN, N + "OrderEvent.OrderEvent(Order, OrderStatus)", "new")
# Unresolvable Java calls (e.g. a missing static import) must not be matched to a Java class of the
# same name - name matching is only for Kotlin targets. javac reports assertEquals() as OrderTest.assertEquals.
edge("com.example.domain.OrderTest.totalIsZeroForNewOrder()", "com.example.domain.OrderTest", "uses", present=False)
edge("com.example.domain.OrderTest.totalIsZeroForNewOrder()", "com.example.domain.Order.total()", "call")
bogus = [q for q in by_q if q.startswith("?") and q[1:].split(".")[0] in ("NotificationService", "EmailNotifier")]
check(not bogus, f"Java->Kotlin references must not end up as unresolved externals: {bogus}")

c.finish("jvm")
