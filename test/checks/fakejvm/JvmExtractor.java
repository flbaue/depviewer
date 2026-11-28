package depviewer;

/**
 * A stand-in for the jvm extractor, for scripts/check.sh only: put first on the classpath, it
 * answers as FAKE_JVM says, so every way a required extractor can fail is checked
 * (spec/wiki/building-blocks/BB-011-jvm-extractor.md, spec/wiki/crosscutting-concepts/CC-002-extractor-contract.md).
 *   unsupported  --describe: no supported JDK      describe2  --describe: contract 2
 *   contract2    fragment of contract 2             null       a fragment that is no object
 *   broken       a fragment whose nodes are no list
 */
final class JvmExtractor {
    public static void main(String[] args) {
        String mode = System.getenv().getOrDefault("FAKE_JVM", "");
        if (args.length > 0 && args[0].equals("--describe")) {
            System.out.println("{\"name\": \"fake-jvm\", \"version\": \"0\", \"contract\": " + (mode.equals("describe2") ? 2 : 1)
                    + ", \"languages\": [\"java\", \"kotlin\"], \"toolchain\": {\"tool\": \"java\", \"version\": \"17.0.0\", \"source\": \""
                    + (args.length > 1 ? args[1] : "path") + "\", \"path\": \"/fake/java\", \"supported\": " + !mode.equals("unsupported")
                    + ", \"range\": \">=17\"}}");
            return;
        }
        switch (mode) {
            case "contract2": System.out.println("{\"contract\": 2, \"language\": \"java\", \"errors\": 0, \"nodes\": [], \"externals\": [], \"edges\": []}"); break;
            case "null": System.out.println("null"); break;
            default: System.out.println("{\"contract\": 1, \"language\": \"java\", \"errors\": 0, \"nodes\": 7, \"externals\": [], \"edges\": []}");
        }
    }
}
