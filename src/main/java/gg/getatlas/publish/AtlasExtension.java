package gg.getatlas.publish;

import javax.inject.Inject;
import org.gradle.api.Action;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

/**
 * The release a build sends to an Atlas panel: a tag and the files it puts into templates.
 */
public abstract class AtlasExtension {

    private final ObjectFactory objects;
    private final DomainObjectSet<ReleaseEntry> entries;
    private final DevSettings dev;

    @Inject
    public AtlasExtension(ObjectFactory objects) {
        this.objects = objects;
        this.entries = objects.domainObjectSet(ReleaseEntry.class);
        this.dev = objects.newInstance(DevSettings.class);
    }

    /**
     * The panel's https address; {@code ATLAS_PANEL} when unset.
     *
     * @return the panel address
     */
    public abstract Property<String> getPanel();

    /**
     * A token from the panel, personal for {@code atlasDev}; {@code ATLAS_TOKEN} when unset.
     *
     * @return the token
     */
    public abstract Property<String> getToken();

    /**
     * The release's tag, such as {@code v1.4.2}; a dash before any {@code +} makes it a prerelease.
     *
     * @return the tag
     */
    public abstract Property<String> getTag();

    /**
     * An http or https link to the release's source, such as a commit.
     *
     * @return the link
     */
    public abstract Property<String> getLink();

    /**
     * Has no effect, since every release lands in a draft per template.
     *
     * @return whether the release is a draft
     */
    public abstract Property<Boolean> getDraft();

    /**
     * How {@code atlasDev} pushes the entries into drafts.
     *
     * @return the dev settings
     */
    public DevSettings getDev() {
        return this.dev;
    }

    /**
     * Configures how {@code atlasDev} pushes the entries into drafts.
     *
     * @param action the configuration
     */
    public void dev(Action<? super DevSettings> action) {
        action.execute(this.dev);
    }

    /**
     * The files the release puts into templates.
     *
     * @return the entries
     */
    public DomainObjectSet<ReleaseEntry> getEntries() {
        return this.entries;
    }

    /**
     * Puts one file into a template at a path.
     *
     * @param template the template's name, such as {@code games/duels}
     * @param path the path inside the template, such as {@code plugins/Duels.jar}
     * @param source anything {@code files()} accepts that holds one file, such as a jar task
     */
    public void publish(String template, String path, Object source) {
        ReleaseEntry entry = this.objects.newInstance(ReleaseEntry.class);

        entry.getTemplate().set(template);
        entry.getPath().set(path);
        entry.getFile().from(source);
        this.entries.add(entry);
    }
}
