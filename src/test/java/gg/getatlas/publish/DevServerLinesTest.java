package gg.getatlas.publish;

import com.google.gson.JsonParser;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

final class DevServerLinesTest {

    @Test
    void saysWhatHappenedToEachDevServer() {
        String body = "{\"template\":\"games/bedwars\",\"draft\":\"bedwars-esmay\",\"devServers\":["
            + "{\"started\":\"dev-bedwars-1\"},"
            + "{\"restarted\":\"dev-bedwars-2\"},"
            + "{\"held\":{\"reason\":\"ERROR_REASON_DEV_GROUP_UNCLEAR\",\"groups\":[\"bedwars-solo\",\"bedwars-duos\"]}},"
            + "{\"held\":{\"reason\":\"ERROR_REASON_EULA_REQUIRED\"}},"
            + "{\"held\":{\"reason\":\"ERROR_REASON_UNAVAILABLE\"}}"
            + "]}";

        List<String> expected = List.of(
            "  dev server dev-bedwars-1 starting",
            "  dev server dev-bedwars-2 restarting",
            "  no dev server: several groups run this template (bedwars-solo, bedwars-duos); open the one you want in the panel and push again",
            "  no dev server: accept the Minecraft EULA in the panel first",
            "  no dev server: the panel could not start it"
        );

        Assertions.assertEquals(expected, DevServerLines.of(JsonParser.parseString(body).getAsJsonObject()));
    }

    @Test
    void namesAReloadOrWhyItRestarted() {
        String body = "{\"devServers\":["
            + "{\"reloaded\":{\"name\":\"dev-lobby-1\",\"jars\":[{\"path\":\"plugins/Lobby.jar\",\"classes\":4}],\"milliseconds\":60}},"
            + "{\"restarted\":\"dev-lobby-2\",\"reloadRefused\":{\"reason\":\"REASON_FIELDS_CHANGED\",\"subject\":\"Greeter\"}}"
            + "]}";

        List<String> expected = List.of(
            "  dev-lobby-1 reloaded 4 classes from plugins/Lobby.jar in 60 ms",
            "  dev-lobby-2 restarts: Greeter's fields changed"
        );

        Assertions.assertEquals(expected, DevServerLines.of(JsonParser.parseString(body).getAsJsonObject()));
    }

    @Test
    void printsNothingWithoutDevServers() {
        Assertions.assertEquals(List.of(), DevServerLines.of(JsonParser.parseString("{\"template\":\"lobby\",\"published\":3}").getAsJsonObject()));
    }
}
