package gg.getatlas.publish;

import org.gradle.api.provider.Property;

/**
 * How {@code atlasDev} treats the draft it pushes into.
 */
public abstract class DevSettings {

    /**
     * Whether a push starts the draft's dev server, or restarts it when one runs; off by default.
     *
     * @return whether the push runs the draft
     */
    public abstract Property<Boolean> getServer();
}
