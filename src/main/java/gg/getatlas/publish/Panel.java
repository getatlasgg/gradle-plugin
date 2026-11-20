package gg.getatlas.publish;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;
import org.gradle.api.GradleException;

record Panel(URI address, String token) {

    private static final Pattern LOOPBACK = Pattern.compile("localhost|\\[::1]|127(\\.\\d{1,3}){3}");
    private static final String UNSET = "set ATLAS_PANEL to the panel's https address and ATLAS_TOKEN to a publish token";

    Panel {
        if (token.isBlank() || !Panel.reachable(address)) {
            throw new GradleException(Panel.UNSET);
        }
    }

    static Panel of(String address, String token) {
        try {
            return new Panel(new URI(address.strip().replaceAll("/+$", "")), token.strip());
        } catch (URISyntaxException exception) {
            throw new GradleException(Panel.UNSET, exception);
        }
    }

    URI method(String name) {
        return URI.create(this.address + "/atlas.panel.v1.ReleasesService/" + name);
    }

    // Plain http reaches only this machine, so the token never crosses a network in the clear.
    private static boolean reachable(URI address) {
        String host = address.getHost();

        return host != null && ("https".equals(address.getScheme()) || "http".equals(address.getScheme()) && Panel.LOOPBACK.matcher(host).matches());
    }
}
