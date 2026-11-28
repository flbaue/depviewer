package depviewer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Assignment precedence checks (spec/wiki/functional-requirements/FR-028-decide-each-elements-group-by-precedence.md)
 * on the scanned fixture model. Usage: AssignCheck <fixture.json> <fixture-architecture.json>. Compiled by
 * scripts/check.sh only.
 */
final class AssignCheck {
    static int failures;
    static List<Object> nodes;

    public static void main(String[] args) throws Exception {
        nodes = SelectorCheck.nodes(args[0]);
        String arch = Files.readString(Path.of(args[1]));

        // The architecture file as committed: patterns, one manual assignment, no overlap.
        Architecture.Resolution r = resolve(arch);
        overlaps(r);
        is(r, "com.example.domain.OrderRepository", "dom");            // type:**Repository@interface and its package
        is(r, "com.example.domain.event", "dom");                      // ** includes subpackages
        is(r, "com.example.service.OrderService", "svc");              // inherited from its package
        is(r, "src/main/java/com/example/infra/InMemoryOrderRepository.java", "infra");
        is(r, "com.example.infra.InMemoryOrderRepository", "infra");   // a type inherits its file's group
        is(r, "com.example.infra", null);                              // path: selects files, not packages
        is(r, "src/main/java/com/example/app/Main.java", "app");       // F: assignment
        is(r, "com.example.notify.NotificationsKt.format(OrderEvent, Double)", null);

        // FR-028 criterion: without @interface, dom and infra both select InMemoryOrderRepository.
        r = resolve(arch.replace("@interface", ""));
        overlaps(r, "T:fixture:com.example.infra.InMemoryOrderRepository");
        is(r, "com.example.infra.InMemoryOrderRepository", "dom");
        is(r, "com.example.infra.InMemoryOrderRepository.save(Order)", "dom");   // members move with their type
        is(r, "src/main/java/com/example/infra/InMemoryOrderRepository.java", "infra");
        Architecture.Overlap o = r.overlaps.get(0);
        if (!o.groups.equals(List.of("dom", "infra"))) fail("overlap groups: expected [dom, infra], got " + o.groups);

        // 1. an assignment beats a pattern, 2. a pattern beats inheritance
        r = resolve(view("{\"id\": \"dom\", \"match\": [\"package:com.example.domain.**\"]}, {\"id\": \"svc\"}",
                "\"P:fixture:com.example.domain\": \"svc\""));
        is(r, "com.example.domain", "svc");
        is(r, "com.example.domain.Order", "svc");
        is(r, "com.example.domain.event", "dom");
        overlaps(r);

        // "" on an ancestor: explicitly ungrouped, overrides a pattern further up
        r = resolve(view("{\"id\": \"all\", \"match\": [\"package:com.example\"]}", "\"P:fixture:com.example.service\": \"\""));
        is(r, "com.example.app", "all");
        is(r, "com.example.service", null);
        is(r, "com.example.service.OrderService", null);

        // T: assigns a type on its own; nested types and members move with it, the file keeps its group
        r = resolve(view("{\"id\": \"dom\", \"match\": [\"package:com.example.domain.**\"]}, {\"id\": \"infra\"}",
                "\"T:fixture:com.example.domain.Order\": \"infra\""));
        is(r, "com.example.domain.Order", "infra");
        is(r, "com.example.domain.Order.total()", "infra");
        is(r, "com.example.domain.Order.Line", "infra");
        is(r, "com.example.domain.Order.Line.amount", "infra");
        is(r, "src/main/java/com/example/domain/Order.java", "dom");
        overlaps(r);

        // Two groups select the same element: the first listed wins, one warning. An element a group
        // excludes with ! never gets that group by inheritance; it falls through to the next ancestor.
        r = resolve(view("{\"id\": \"dom\", \"match\": [\"package:com.example.domain.**\", \"!path:**/Status.java\"]},"
                + " {\"id\": \"all\", \"match\": [\"package:com.example.**\"]}", ""));
        overlaps(r, "P:fixture:com.example.domain", "P:fixture:com.example.domain.event");
        is(r, "com.example.domain", "dom");
        is(r, "src/main/java/com/example/domain/Order.java", "dom");
        is(r, "src/main/java/com/example/domain/Status.java", "all");
        is(r, "com.example.domain.Status", "all");
        r = resolve(view("{\"id\": \"dom\", \"match\": [\"package:com.example.domain.**\", \"!path:**/Status.java\"]}", ""));
        is(r, "src/main/java/com/example/domain/Status.java", null);
        is(r, "com.example.domain.Status", null);

        // A pattern that overrides the inherited group of a group it is nested in is a refinement, not an overlap.
        String nested = "{\"id\": \"core\", \"match\": [\"package:com.example.domain\"]},"
                + " {\"id\": \"dom\", \"parent\": \"core\", \"match\": [\"type:**Repository@interface\"]}";
        r = resolve(view(nested, ""));
        is(r, "com.example.domain.OrderRepository", "dom");
        overlaps(r);
        r = resolve(view(nested.replace(", \"parent\": \"core\"", ""), ""));
        overlaps(r, "T:fixture:com.example.domain.OrderRepository");

        // externals have keys too
        r = resolve(view("{\"id\": \"jdk\", \"match\": [\"ext:java.**\"]}", "\"XT:java.util.List\": \"\""));
        is(r, "java.util.ArrayList", "jdk");
        is(r, "java.util.List", null);

        if (failures > 0) {
            System.err.println(failures + " assignment check(s) failed");
            System.exit(1);
        }
        System.out.println("ok");
    }

    static String view(String groups, String assign) {
        return "{\"views\": [{\"id\": \"v\", \"name\": \"V\", \"groups\": [" + groups + "], \"assign\": {" + assign + "}}]}";
    }

    static Architecture.Resolution resolve(String file) {
        Map<?, ?> view = (Map<?, ?>) ((List<?>) ((Map<?, ?>) Json.parse(file)).get("views")).get(0);
        return Architecture.resolve(view, nodes);
    }

    static void is(Architecture.Resolution r, String label, String group) {
        int found = -1;
        for (int i = 0; i < nodes.size(); i++) if (SelectorCheck.label(nodes.get(i)).equals(label)) { found = i; break; }
        if (found < 0) { fail("no node " + label); return; }
        String got = r.group[found];
        if (group == null ? got != null : !group.equals(got)) fail(label + ": expected group " + group + ", got " + got);
    }

    static void overlaps(Architecture.Resolution r, String... keys) {
        List<String> got = new ArrayList<>();
        for (Architecture.Overlap o : r.overlaps) got.add(o.key);
        if (!got.equals(List.of(keys))) fail("overlaps: expected " + List.of(keys) + ", got " + got);
    }

    static void fail(String msg) {
        System.err.println("FAIL: " + msg);
        failures++;
    }
}
