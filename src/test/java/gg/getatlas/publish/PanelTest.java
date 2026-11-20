package gg.getatlas.publish;

import org.gradle.api.GradleException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

final class PanelTest {

    @ParameterizedTest
    @ValueSource(strings = {"https://panel.example.gg", "https://panel.example.gg/", "http://localhost:8080", "http://127.0.0.1:8080", "http://[::1]:8080"})
    void acceptsHttpsAndLoopbackHttp(String address) {
        Assertions.assertDoesNotThrow(() -> Panel.of(address, "publish.secret"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "panel.example.gg", "http://panel.example.gg", "http://10.0.0.4:8080", "http://127.evil.example", "ftp://panel.example.gg"})
    void refusesAddressesThatSendTheTokenInTheClear(String address) {
        Assertions.assertThrows(GradleException.class, () -> Panel.of(address, "publish.secret"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "  "})
    void needsAToken(String token) {
        Assertions.assertThrows(GradleException.class, () -> Panel.of("https://panel.example.gg", token));
    }

    @ParameterizedTest
    @ValueSource(strings = {"https://panel.example.gg", "https://panel.example.gg//"})
    void namesTheReleasesMethod(String address) {
        Assertions.assertEquals("https://panel.example.gg/atlas.panel.v1.ReleasesService/Publish", Panel.of(address, "publish.secret").method("Publish").toString());
    }
}
