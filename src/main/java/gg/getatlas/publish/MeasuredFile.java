package gg.getatlas.publish;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import org.gradle.api.GradleException;

record MeasuredFile(Path path, String hash, long size) {

    static MeasuredFile of(Path path) {
        try (DigestInputStream content = new DigestInputStream(Files.newInputStream(path), MessageDigest.getInstance("SHA-256"))) {
            long size = content.transferTo(OutputStream.nullOutputStream());

            if (size == 0) {
                throw new GradleException("reading " + path + ": the file is empty");
            }

            String hash = HexFormat.of().formatHex(content.getMessageDigest().digest());

            return new MeasuredFile(path, hash, size);
        } catch (IOException | NoSuchAlgorithmException exception) {
            throw new GradleException("reading " + path, exception);
        }
    }
}
