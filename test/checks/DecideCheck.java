package depviewer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Writes random architecture views over a model, each with the view the viewer gets embedded and the group
 * {@link Architecture#resolve} gives every element the viewer places, for test/checks/viewer_decide.mjs to compare
 * against the viewer's decide() (spec/wiki/functional-requirements/FR-041-show-pattern-assignments-as-by-pattern.md).
 * Usage: DecideCheck <model.json> <cases> <seed> <out.json>. Compiled by scripts/check.sh only.
 */
final class DecideCheck {
    static final List<String> VIEWER_KINDS = List.of("module", "package", "file", "ext-root", "ext-package", "ext-type");

    public static void main(String[] args) throws Exception {
        Map<?, ?> model = (Map<?, ?>) Json.parse(Files.readString(Path.of(args[0])));
        @SuppressWarnings("unchecked") List<Object> nodes = (List<Object>) model.get("nodes");
        int n = Integer.parseInt(args[1]);
        Random rnd = new Random(Long.parseLong(args[2]));
        Rules.Graph g = new Rules.Graph(model);

        // What selectors and assignments can name: the model's own packages, files, modules and externals.
        List<String> selectors = new ArrayList<>(), keys = new ArrayList<>();
        for (int i = 0; i < g.n; i++) {
            String k = g.kind[i];
            if (!VIEWER_KINDS.contains(k)) continue;
            keys.add(g.key[i]);
            String name = g.q[i];
            switch (k) {
                case "module": selectors.add("module:" + name); break;
                case "package": selectors.add("package:" + name); selectors.add("package:" + name + ".**"); break;
                case "file": selectors.add("path:" + g.path(i)); selectors.add("path:**/" + g.path(i).replaceAll(".*/", "")); break;
                case "ext-package": if (!name.startsWith("?")) selectors.add("ext:" + name + ".**"); break;
                default:
            }
        }

        List<Object> cases = new ArrayList<>();
        for (int c = 0; c < n; c++) {
            int groupCount = 2 + rnd.nextInt(4);
            List<Object> groups = new ArrayList<>();
            for (int gi = 0; gi < groupCount; gi++) {
                Map<String, Object> gr = new LinkedHashMap<>();
                gr.put("id", "g" + gi);
                gr.put("name", "G" + gi);
                gr.put("color", "#577590");
                if (rnd.nextInt(3) == 0) gr.put("parent", "g" + rnd.nextInt(groupCount));   // loops included
                List<Object> match = new ArrayList<>();
                for (int s = rnd.nextInt(4); s > 0; s--) match.add((rnd.nextInt(3) == 0 ? "!" : "") + selectors.get(rnd.nextInt(selectors.size())));
                if (!match.isEmpty()) gr.put("match", match);
                groups.add(gr);
            }
            Map<String, Object> assign = new LinkedHashMap<>();
            for (int a = rnd.nextInt(6); a > 0; a--) {
                int r = rnd.nextInt(6);
                assign.put(keys.get(rnd.nextInt(keys.size())), r == 0 ? "" : r == 1 ? "ghost" : "g" + rnd.nextInt(groupCount));
            }
            Map<String, Object> view = new LinkedHashMap<>();
            view.put("id", "v");
            view.put("name", "V");
            view.put("groups", groups);
            view.put("assign", assign);
            Map<String, Object> root = new LinkedHashMap<>();
            root.put("views", List.of(view));

            Map<?, ?> embedded = (Map<?, ?>) Json.parse(Report.embed(root, nodes, Map.of(), g));
            String[] group = Architecture.resolve(view, nodes).group;
            Map<String, Object> expected = new LinkedHashMap<>();
            for (int i = 0; i < g.n; i++) if (VIEWER_KINDS.contains(g.kind[i])) expected.put(g.key[i], group[i]);
            Map<String, Object> one = new LinkedHashMap<>();
            one.put("view", ((List<?>) embedded.get("views")).get(0));
            one.put("expected", expected);
            cases.add(one);
        }
        Files.writeString(Path.of(args[3]), Json.write(cases));
    }
}
