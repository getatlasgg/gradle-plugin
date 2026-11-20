package gg.getatlas.publish;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.gradle.api.GradleException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class MeasuredFileTest {

    @TempDir
    private Path folder;

    @Test
    void hashesAndCountsTheFile() throws IOException {
        Path file = Files.writeString(this.folder.resolve("lobby.txt"), "lobby\n");
        MeasuredFile measured = MeasuredFile.of(file);

        Assertions.assertEquals("7f07bea36c89ceea182c33a888e71fd3a1e75242aecd473a379bab353710729a", measured.hash());
        Assertions.assertEquals(6, measured.size());
    }

    @Test
    void refusesAnEmptyFile() throws IOException {
        Path file = Files.createFile(this.folder.resolve("empty.jar"));

        Assertions.assertThrows(GradleException.class, () -> MeasuredFile.of(file));
    }
}
