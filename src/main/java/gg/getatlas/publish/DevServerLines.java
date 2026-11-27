package gg.getatlas.publish;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

final class DevServerLines {

    private static final Map<String, String> HOLDS = Map.ofEntries(
        Map.entry("ERROR_REASON_NO_DEV_GROUP", "no group runs this template"),
        Map.entry("ERROR_REASON_DEV_SERVER_LIMIT", "every dev server of the plan is in use"),
        Map.entry("ERROR_REASON_EULA_REQUIRED", "accept the Minecraft EULA in the panel first"),
        Map.entry("ERROR_REASON_NODE_OFFLINE", "the draft's node is offline"),
        Map.entry("ERROR_REASON_NO_NODE_AVAILABLE", "the draft's node has no room for it"),
        Map.entry("ERROR_REASON_SERVER_BUSY", "the server is busy with another change")
    );

    private DevServerLines() {
    }

    static List<String> of(JsonObject released) {
        List<String> lines = new ArrayList<>();

        if (!released.has("devServers")) {
            return lines;
        }

        for (JsonElement element : released.getAsJsonArray("devServers")) {
            JsonObject server = element.getAsJsonObject();

            if (server.has("started")) {
                lines.add("  dev server " + server.get("started").getAsString() + " starting");
            } else if (server.has("reloaded")) {
                lines.add(ReloadLines.reloaded(server.getAsJsonObject("reloaded")));
            } else if (server.has("restarted")) {
                lines.add(ReloadLines.restarted(server.get("restarted").getAsString(), server.getAsJsonObject("reloadRefused")));
            } else if (server.has("held")) {
                lines.add("  no dev server: " + DevServerLines.held(server.getAsJsonObject("held")));
            }
        }

        return lines;
    }

    private static String held(JsonObject held) {
        String reason = held.has("reason") ? held.get("reason").getAsString() : "";

        if (reason.equals("ERROR_REASON_DEV_GROUP_UNCLEAR")) {
            List<String> groups = new ArrayList<>();

            if (held.has("groups")) {
                held.getAsJsonArray("groups").forEach(group -> groups.add(group.getAsString()));
            }

            return "several groups run this template (" + String.join(", ", groups) + "); open the one you want in the panel and push again";
        }

        return DevServerLines.HOLDS.getOrDefault(reason, "the panel could not start it");
    }
}
