package gg.getatlas.publish;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Map;

final class Refusal {

    private static final Map<String, String> EXPLANATIONS = Map.ofEntries(
        Map.entry("ERROR_REASON_SIGN_IN_REQUIRED", "the panel does not know this token; create one on your profile under Tokens"),
        Map.entry("ERROR_REASON_PERSONAL_TOKEN_NEEDED", "atlasDev needs a personal token; create one on your profile under Tokens"),
        Map.entry("ERROR_REASON_NOT_ALLOWED", "your role in the panel does not allow this"),
        Map.entry("ERROR_REASON_DRAFT_NAME_TAKEN", "another template has a draft with this name"),
        Map.entry("ERROR_REASON_TOKEN_SCOPE", "the token may not publish one of these templates"),
        Map.entry("ERROR_REASON_CLI_TOO_OLD", "this plugin does not match the panel's version; use the plugin version of the panel's release"),
        Map.entry("ERROR_REASON_FILE_MISSING", "the panel lost an uploaded file; run the release again"),
        Map.entry("ERROR_REASON_NOT_FOUND", "the panel has no template with one of these names"),
        Map.entry("ERROR_REASON_NO_NODE_ONLINE", "a draft needs a connected node to hold it"),
        Map.entry("ERROR_REASON_TEMPLATE_CHANGED", "a template changed while the release published; run it again"),
        Map.entry("ERROR_REASON_INVALID_INPUT", "the panel refused the release")
    );

    private Refusal() {
    }

    // The Connect JSON error carries each detail's decoded message under "debug" when the panel can name its type.
    static String explain(JsonObject error, int status) {
        if (error.has("details")) {
            for (JsonElement detail : error.getAsJsonArray("details")) {
                JsonObject info = detail.getAsJsonObject().getAsJsonObject("debug");

                if (info == null || !info.has("reason")) {
                    continue;
                }

                String reason = info.get("reason").getAsString();

                if (reason.equals("ERROR_REASON_TEMPLATE_HAS_DRAFT")) {
                    JsonObject metadata = info.getAsJsonObject("metadata");

                    return "template " + metadata.get("template").getAsString() + " has draft " + metadata.get("draft").getAsString() + " open; publish or discard it first";
                }

                if (reason.equals("ERROR_REASON_DRAFT_CLASHES")) {
                    JsonObject metadata = info.getAsJsonObject("metadata");

                    return "the build landed in draft " + metadata.get("draft").getAsString() + ", but it clashes with the live version of " + metadata.get("template").getAsString() + "; pick a side in the panel";
                }

                if (Refusal.EXPLANATIONS.containsKey(reason)) {
                    return Refusal.EXPLANATIONS.get(reason);
                }
            }
        }

        String message = error.has("message") ? error.get("message").getAsString() : "HTTP " + status;

        return "the panel refused the release: " + message;
    }
}
