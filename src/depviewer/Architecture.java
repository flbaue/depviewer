package depviewer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * The architecture file (spec FR-026): views and groups as exported by the viewer, plus the optional
 * {@code match} and {@code rules}. Fields this version does not know are ignored.
 */
final class Architecture {
    static final int VERSION = 1;

    /** A file that cannot be used; the message is meant for the user. */
    static final class Invalid extends Exception {
        private static final long serialVersionUID = 1L;
        Invalid(String msg) { super(msg); }
    }

    final String text;
    final List<Object> views;

    private Architecture(String text, List<Object> views) {
        this.text = text;
        this.views = views;
    }

    static Architecture load(Path file) throws Invalid {
        String text;
        try {
            text = Files.readString(file);
        } catch (IOException e) {
            throw new Invalid(file + ": cannot read (" + e.getMessage() + ")");
        }
        Object root;
        try {
            root = Json.parse(text);
        } catch (IllegalArgumentException e) {
            throw new Invalid(file + ": " + e.getMessage());
        }
        if (!(root instanceof Map)) throw new Invalid(file + ": expected a JSON object with a \"views\" array");
        Map<?, ?> m = (Map<?, ?>) root;
        Object v = m.get("version");
        if (v != null) {
            if (!(v instanceof Double) || (Double) v != Math.floor((Double) v))
                throw new Invalid(file + ": \"version\" must be a whole number");
            int major = ((Double) v).intValue();
            if (major != VERSION)
                throw new Invalid(file + ": version " + major + " is not supported (this depviewer reads version " + VERSION + ")");
        }
        Object views = m.containsKey("views") ? m.get("views") : List.of();
        if (!(views instanceof List)) throw new Invalid(file + ": \"views\" must be an array");
        @SuppressWarnings("unchecked") List<Object> list = (List<Object>) views;
        for (Object view : list) if (!(view instanceof Map)) throw new Invalid(file + ": every view must be an object");
        return new Architecture(text, list);
    }

    /** True when at least one view has a non-empty {@code rules} array. */
    boolean hasRules() {
        for (Object view : views) {
            Object r = ((Map<?, ?>) view).get("rules");
            if (r instanceof List && !((List<?>) r).isEmpty()) return true;
        }
        return false;
    }
}
