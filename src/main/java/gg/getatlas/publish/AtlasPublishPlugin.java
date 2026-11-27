package gg.getatlas.publish;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.provider.ProviderFactory;

/**
 * Adds the {@code atlas} extension, the {@code atlasPublish} task that sends its release to the panel and the
 * {@code atlasDev} task that pushes the same files into the token owner's drafts.
 */
public final class AtlasPublishPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        ProviderFactory providers = project.getProviders();
        AtlasExtension atlas = project.getExtensions().create("atlas", AtlasExtension.class);

        atlas.getPanel().convention(providers.environmentVariable("ATLAS_PANEL"));
        atlas.getToken().convention(providers.environmentVariable("ATLAS_TOKEN"));
        atlas.getDraft().convention(false);
        atlas.getDev().getServer().convention(false);

        project.getTasks().register("atlasPublish", PublishTask.class, task -> {
            AtlasPublishPlugin.connect(task, atlas);
            task.setDescription("Publishes the release to the templates on the Atlas panel.");
            task.getTag().set(atlas.getTag());
            task.getLink().set(atlas.getLink());
            task.getDraft().set(atlas.getDraft());
        });

        project.getTasks().register("atlasDev", PublishTask.class, task -> {
            AtlasPublishPlugin.connect(task, atlas);
            task.setDescription("Pushes the files into your drafts on the Atlas panel.");
            task.getDev().set(true);
            task.getServer().set(atlas.getDev().getServer());
            task.getDraftName().set(providers.gradleProperty("atlas.draft"));
            task.getRestart().set(providers.gradleProperty("atlas.restart").map(value -> !value.equals("false")));
        });
    }

    private static void connect(PublishTask task, AtlasExtension atlas) {
        task.setGroup("publishing");
        task.getPanel().set(atlas.getPanel());
        task.getToken().set(atlas.getToken());
        task.getEntries().addAll(atlas.getEntries());
    }
}
