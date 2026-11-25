package gg.getatlas.publish;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import org.gradle.api.GradleException;

final class Releases {

    private static final int CHUNK = 1 << 20;

    private final Panel panel;
    private final HttpClient client;

    Releases(Panel panel, HttpClient client) {
        this.panel = panel;
        this.client = client;
    }

    List<String> missing(Collection<String> hashes) {
        JsonObject request = new JsonObject();
        JsonArray sha256 = new JsonArray();

        hashes.forEach(sha256::add);
        request.add("sha256", sha256);

        List<String> missing = new ArrayList<>();
        JsonObject response = this.call("MissingFiles", request);

        if (response.has("sha256")) {
            response.getAsJsonArray("sha256").forEach(hash -> missing.add(hash.getAsString()));
        }

        return missing;
    }

    void upload(MeasuredFile file) {
        try (InputStream content = Files.newInputStream(file.path())) {
            for (long offset = 0; offset < file.size(); ) {
                byte[] data = content.readNBytes(Releases.CHUNK);

                if (data.length == 0) {
                    throw new GradleException("uploading " + file.path() + ": the file shrank while it was read");
                }

                JsonObject chunk = new JsonObject();
                JsonObject request = new JsonObject();

                chunk.addProperty("sha256", file.hash());
                chunk.addProperty("size", Long.toString(file.size()));
                chunk.addProperty("offset", Long.toString(offset));
                chunk.addProperty("data", Base64.getEncoder().encodeToString(data));
                request.add("chunk", chunk);
                this.call("UploadFile", request);
                offset += data.length;
            }
        } catch (IOException exception) {
            throw new GradleException("uploading " + file.path(), exception);
        }
    }

    List<String> publish(JsonObject request, String tag) {
        List<String> lines = new ArrayList<>();
        JsonObject response = this.call("Publish", request);

        if (response.has("templates")) {
            for (JsonElement element : response.getAsJsonArray("templates")) {
                JsonObject released = element.getAsJsonObject();

                lines.add(Releases.outcome(released, tag));
                lines.addAll(DevServerLines.of(released));
            }
        }

        return lines;
    }

    private JsonObject call(String method, JsonObject body) {
        HttpRequest request = HttpRequest.newBuilder(this.panel.method(method))
            .header("Authorization", "Bearer " + this.panel.token())
            .header("Content-Type", "application/json")
            .header("Connect-Protocol-Version", "1")
            .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
            .build();

        HttpResponse<String> response;

        try {
            response = this.client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException exception) {
            throw new GradleException("reaching the panel at " + this.panel.address(), exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new GradleException("reaching the panel at " + this.panel.address(), exception);
        }

        JsonObject parsed = Releases.parse(response.body());

        if (response.statusCode() != 200) {
            throw new GradleException(Refusal.explain(parsed, response.statusCode()));
        }

        return parsed;
    }

    private static JsonObject parse(String body) {
        try {
            JsonElement parsed = JsonParser.parseString(body);

            return parsed.isJsonObject() ? parsed.getAsJsonObject() : new JsonObject();
        } catch (JsonParseException exception) {
            return new JsonObject();
        }
    }

    private static String outcome(JsonObject released, String tag) {
        String template = released.get("template").getAsString();

        if (released.has("published")) {
            return template + "  v" + released.get("published").getAsLong() + " (" + tag + ")";
        }

        if (released.has("unchanged")) {
            return template + "  v" + released.get("unchanged").getAsLong() + " already holds these files";
        }

        return template + "  draft " + released.get("draft").getAsString();
    }
}
