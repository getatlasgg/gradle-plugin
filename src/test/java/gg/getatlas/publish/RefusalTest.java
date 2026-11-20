package gg.getatlas.publish;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

final class RefusalTest {

    static Stream<Arguments> refusals() {
        return Stream.of(
            Arguments.of(
                "{\"code\":\"permission_denied\",\"message\":\"scope\",\"details\":[{\"type\":\"google.rpc.ErrorInfo\",\"value\":\"\",\"debug\":{\"reason\":\"ERROR_REASON_TOKEN_SCOPE\",\"domain\":\"getatlas.gg\"}}]}",
                "the token may not publish one of these templates"
            ),
            Arguments.of(
                "{\"code\":\"failed_precondition\",\"details\":[{\"debug\":{\"reason\":\"ERROR_REASON_TEMPLATE_HAS_DRAFT\",\"metadata\":{\"template\":\"lobby\",\"draft\":\"lobby-halloween\"}}}]}",
                "template lobby has draft lobby-halloween open; publish or discard it first"
            ),
            Arguments.of(
                "{\"code\":\"aborted\",\"details\":[{\"debug\":{\"reason\":\"ERROR_REASON_DRAFT_CLASHES\",\"metadata\":{\"template\":\"games/survival-games\",\"draft\":\"survival-games-esmay\"}}}]}",
                "the build landed in draft survival-games-esmay, but it clashes with the live version of games/survival-games; pick a side in the panel"
            ),
            Arguments.of(
                "{\"code\":\"permission_denied\",\"details\":[{\"debug\":{\"reason\":\"ERROR_REASON_PERSONAL_TOKEN_NEEDED\"}}]}",
                "atlasDev needs a personal token; create one on your profile under Tokens"
            ),
            Arguments.of(
                "{\"code\":\"internal\",\"message\":\"database unavailable\",\"details\":[{\"type\":\"google.rpc.ErrorInfo\",\"value\":\"CgE=\"}]}",
                "the panel refused the release: database unavailable"
            ),
            Arguments.of("{}", "the panel refused the release: HTTP 502")
        );
    }

    @ParameterizedTest
    @MethodSource("refusals")
    void explainsTheReason(String body, String explanation) {
        JsonObject error = JsonParser.parseString(body).getAsJsonObject();

        Assertions.assertEquals(explanation, Refusal.explain(error, 502));
    }
}
