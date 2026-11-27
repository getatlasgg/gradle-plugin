package gg.getatlas.publish;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

final class ReloadLines {

    private static final Map<String, String> REFUSALS = Map.ofEntries(
        Map.entry("REASON_FIELDS_CHANGED", "%s's fields changed"),
        Map.entry("REASON_METHOD_ADDED", "%s gained a method"),
        Map.entry("REASON_METHOD_REMOVED", "%s lost a method"),
        Map.entry("REASON_PARENT_CHANGED", "%s's parent class changed"),
        Map.entry("REASON_FILE_CHANGED", "%s changed"),
        Map.entry("REASON_FILE_NEW", "%s is new"),
        Map.entry("REASON_NEVER_LOADED", "%s was never loaded"),
        Map.entry("REASON_OTHER", "%s couldn't be swapped")
    );

    private ReloadLines() {
    }

    static String reloaded(JsonObject reloaded) {
        String name = ReloadLines.text(reloaded, "name");
        List<String> parts = new ArrayList<>();

        if (reloaded.has("jars")) {
            for (JsonElement element : reloaded.getAsJsonArray("jars")) {
                JsonObject jar = element.getAsJsonObject();
                int classes = jar.has("classes") ? jar.get("classes").getAsInt() : 0;

                parts.add((classes == 1 ? "1 class" : classes + " classes") + " from " + ReloadLines.text(jar, "path"));
            }
        }

        if (parts.isEmpty()) {
            return "  " + name + " had no changed classes to reload";
        }

        long milliseconds = reloaded.has("milliseconds") ? reloaded.get("milliseconds").getAsLong() : 0;

        return "  " + name + " reloaded " + String.join(", ", parts) + " in " + milliseconds + " ms";
    }

    static String restarted(String name, JsonObject refusal) {
        String text = refusal == null ? null : ReloadLines.REFUSALS.get(ReloadLines.text(refusal, "reason"));

        if (text == null) {
            return "  dev server " + name + " restarting";
        }

        return "  " + name + " restarts: " + text.formatted(ReloadLines.text(refusal, "subject"));
    }

    private static String text(JsonObject object, String key) {
        return object.has(key) ? object.get(key).getAsString() : "";
    }
}
