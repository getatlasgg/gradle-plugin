package gg.getatlas.publish;

import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;

/**
 * One file of a release and the template path it goes to.
 */
public abstract class ReleaseEntry {

    /**
     * The template's name.
     *
     * @return the template
     */
    @Input
    public abstract Property<String> getTemplate();

    /**
     * The path inside the template.
     *
     * @return the path
     */
    @Input
    public abstract Property<String> getPath();

    /**
     * The file, which must be exactly one.
     *
     * @return the file
     */
    @InputFiles
    @PathSensitive(PathSensitivity.NONE)
    public abstract ConfigurableFileCollection getFile();
}
