package gg.getatlas.publish;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.UntrackedTask;

/**
 * Sends the release to the panel, which publishes it or puts it in drafts, or pushes the files into the token
 * owner's drafts.
 */
@UntrackedTask(because = "the panel decides whether a release changes a template")
public abstract class PublishTask extends DefaultTask {

    private static final String DEVELOPMENT = "(devel)";
    private static final String UNSTAMPED = "0.0.0";

    /**
     * The panel's address.
     *
     * @return the panel address
     */
    @Input
    public abstract Property<String> getPanel();

    /**
     * The publish token, kept out of the task's inputs.
     *
     * @return the token
     */
    @Internal
    public abstract Property<String> getToken();

    /**
     * The release's tag.
     *
     * @return the tag
     */
    @Input
    @Optional
    public abstract Property<String> getTag();

    /**
     * The link to the release's source.
     *
     * @return the link
     */
    @Input
    @Optional
    public abstract Property<String> getLink();

    /**
     * Has no effect, since every release lands in a draft per template.
     *
     * @return whether the release is a draft
     */
    @Input
    @Optional
    public abstract Property<Boolean> getDraft();

    /**
     * Whether the files go into the token owner's drafts instead of a release.
     *
     * @return whether this is a dev push
     */
    @Input
    @Optional
    public abstract Property<Boolean> getDev();

    /**
     * Whether a dev push starts or restarts the draft's dev server.
     *
     * @return whether the push runs the draft
     */
    @Input
    @Optional
    public abstract Property<Boolean> getServer();

    /**
     * A draft a dev push uses instead of the one the owner last worked in.
     *
     * @return the draft's name
     */
    @Input
    @Optional
    public abstract Property<String> getDraftName();

    /**
     * The files of the release.
     *
     * @return the entries
     */
    @Nested
    public abstract ListProperty<ReleaseEntry> getEntries();

    /**
     * Uploads the files the panel lacks, then publishes the release.
     */
    @TaskAction
    public void publish() {
        Panel panel = Panel.of(this.getPanel().getOrElse(""), this.getToken().getOrElse(""));
        String tag = this.getTag().getOrElse("");
        boolean dev = this.getDev().getOrElse(false);

        if (this.getEntries().get().isEmpty() || !dev && tag.isBlank()) {
            throw new GradleException("set atlas.tag and name at least one file with atlas.publish(template, path, file)");
        }

        Map<String, MeasuredFile> files = new LinkedHashMap<>();
        JsonArray entries = new JsonArray();

        for (ReleaseEntry entry : this.getEntries().get()) {
            MeasuredFile file = MeasuredFile.of(entry.getFile().getSingleFile().toPath());
            JsonObject named = new JsonObject();

            named.addProperty("template", entry.getTemplate().get());
            named.addProperty("path", entry.getPath().get());
            named.addProperty("sha256", file.hash());
            files.put(file.hash(), file);
            entries.add(named);
        }

        HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

        Releases releases = new Releases(panel, client);

        for (String hash : releases.missing(files.keySet())) {
            MeasuredFile file = files.get(hash);

            if (file != null) {
                this.getLogger().lifecycle("Uploading {}", file.path());
                releases.upload(file);
            }
        }

        JsonObject request = new JsonObject();

        request.add(dev ? "dev" : "release", dev ? this.push() : this.release(tag));
        request.add("entries", entries);
        request.addProperty("draft", this.getDraft().getOrElse(false));
        request.addProperty("cliVersion", PublishTask.clientVersion());

        for (String line : releases.publish(request, tag)) {
            this.getLogger().lifecycle(line);
        }
    }

    private JsonObject release(String tag) {
        JsonObject release = new JsonObject();

        release.addProperty("tag", tag);

        if (this.getLink().isPresent() && !this.getLink().get().isBlank()) {
            release.addProperty("link", this.getLink().get().strip());
        }

        return release;
    }

    private JsonObject push() {
        JsonObject push = new JsonObject();

        push.addProperty("server", this.getServer().getOrElse(false));

        if (this.getDraftName().isPresent() && !this.getDraftName().get().isBlank()) {
            push.addProperty("draft", this.getDraftName().get().strip());
        }

        return push;
    }

    private static String clientVersion() {
        String version = PublishTask.class.getPackage().getImplementationVersion();

        if (version == null || version.equals(PublishTask.UNSTAMPED)) {
            return PublishTask.DEVELOPMENT;
        }

        return "v" + version;
    }
}
