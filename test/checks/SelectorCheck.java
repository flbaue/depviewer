package depviewer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * Selector checks (spec/wiki/functional-requirements/FR-027-select-code-by-patterns.md) on the scanned
 * fixture models. Usage: SelectorCheck <fixture.json> [<fixture-ts.json>]. Compiled by scripts/check.sh only.
 */
final class SelectorCheck {
    static int failures;

    public static void main(String[] args) throws Exception {
        List<Object> jvm = nodes(args[0]);

        // FR-027 criterion 1
        expect(jvm, List.of("type:**Repository@interface"), "com.example.domain.OrderRepository");
        expect(jvm, List.of("type:**Repository"), "com.example.domain.OrderRepository", "com.example.infra.InMemoryOrderRepository");
        // FR-027 criterion 2: the package itself and every package below it
        expect(jvm, List.of("package:com.example.domain.**"), "com.example.domain", "com.example.domain.event");
        // * stays within one segment, ** crosses segments
        expect(jvm, List.of("package:com.example.*"), "com.example.app", "com.example.domain", "com.example.infra",
                "com.example.notify", "com.example.service");
        expect(jvm, List.of("package:com.*"), "com.example");
        expect(jvm, List.of("type:com.example.domain.*"), "com.example.domain.Order", "com.example.domain.OrderRepository",
                "com.example.domain.OrderTest", "com.example.domain.Status");
        expect(jvm, List.of("type:**.Line"), "com.example.domain.Order.Line");
        expect(jvm, List.of("type:**@enum"), "com.example.domain.Status", "com.example.notify.Channel");
        // ! excludes what an earlier selector of the same group matched
        expect(jvm, List.of("package:com.example.domain.**", "!package:com.example.domain.event"), "com.example.domain");
        expect(jvm, List.of("!package:com.example.domain", "package:com.example.domain"), "com.example.domain");
        expect(jvm, List.of("type:com.example.domain.Order*", "!type:**Test"), "com.example.domain.Order",
                "com.example.domain.OrderRepository");
        // the other kinds
        expect(jvm, List.of("module:fix*"), "fixture");
        expect(jvm, List.of("path:**/infra/**"), "src/main/java/com/example/infra/InMemoryOrderRepository.java");
        expect(jvm, List.of("path:src/test/**"), "src/test/java/com/example/domain/OrderTest.java",
                "src/test/kotlin/com/example/notify/FormatTest.kt");
        expect(jvm, List.of("ext:java.util.*"), "java.util.ArrayList", "java.util.Collection", "java.util.List",
                "java.util.function", "java.util.stream");
        expect(jvm, List.of("ext:kotlin"), "kotlin");
        // folder-based packages use / as the separator
        if (args.length > 1) {
            List<Object> ts = nodes(args[1]);
            expect(ts, List.of("package:src/**", "!package:src/__tests__"), "src", "src/features", "src/features/payments", "src/model", "src/services", "src/util");
            expect(ts, List.of("package:src/*"), "src/__tests__", "src/features", "src/model", "src/services", "src/util");
            expect(ts, List.of("package:src.**"));
        }

        // invalid selectors
        for (String bad : List.of("thing:x", "type:", "package:a@interface", "type:X@banana", "nokind", "!"))
            try {
                Architecture.Selector.parse(bad);
                fail("accepted invalid selector " + bad);
            } catch (IllegalArgumentException e) {
                // expected
            }

        if (failures > 0) {
            System.err.println(failures + " selector check(s) failed");
            System.exit(1);
        }
        System.out.println("ok");
    }

    @SuppressWarnings("unchecked")
    static List<Object> nodes(String file) throws Exception {
        return (List<Object>) ((Map<String, Object>) Json.parse(Files.readString(Path.of(file)))).get("nodes");
    }

    static void expect(List<Object> nodes, List<String> match, String... want) {
        List<Architecture.Selector> sel = new ArrayList<>();
        for (String s : match) sel.add(Architecture.Selector.parse(s));
        TreeSet<String> got = new TreeSet<>();
        Architecture.select(sel, nodes).stream().forEach(i -> got.add(label(nodes.get(i))));
        TreeSet<String> exp = new TreeSet<>(List.of(want));
        if (!got.equals(exp)) fail(match + ": expected " + exp + ", got " + got);
    }

    /** Files by path, everything else by full name. */
    static String label(Object node) {
        Object path = ((Map<?, ?>) node).get("path");
        return path != null ? (String) path : Architecture.name(node);
    }

    static void fail(String msg) {
        System.err.println("FAIL: " + msg);
        failures++;
    }
}
