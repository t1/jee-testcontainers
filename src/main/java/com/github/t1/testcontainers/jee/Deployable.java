package com.github.t1.testcontainers.jee;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.t1.testcontainers.jee.JeeContainer.CLIENT;
import static javax.ws.rs.core.Response.Status.OK;

@Slf4j
abstract class Deployable {

    abstract Path getLocalPath();

    abstract String getFileName();

    static Deployable create(URI deployable) {
        switch (scheme(deployable)) {
            case "file":
                return new FileDeployable(deployable);
            case "urn":
                return new UrnDeployable(deployable);
            default:
                return new UrlDeployable(deployable);
        }
    }

    private static String scheme(URI deployment) {
        String scheme = deployment.getScheme();
        return (scheme == null) ? "file" : scheme;
    }


    private static class FileDeployable extends Deployable {
        @Getter private final Path localPath;
        @Getter private final String fileName;

        private FileDeployable(URI deployment) {
            this.localPath = Paths.get(deployment);
            this.fileName = fileName(deployment);
        }
    }

    private static class UrnDeployable extends Deployable {
        private final String groupId;
        private final String artifactId;
        private final String version;
        private final String type;

        private UrnDeployable(URI deployment) {
            String[] split = deployment.getSchemeSpecificPart().split(":");
            if (!"mvn".equals(split[0]))
                throw new IllegalArgumentException("unsupported urn scheme '" + split[0] + "'");
            if (split.length != 5)
                throw new IllegalArgumentException("expected exactly 5 elements in 'mvn' urn '" + deployment + "': " +
                    "`urn:mvn:<group-id>:<artifact-id>:<version>:<type>`");
            this.groupId = split[1];
            this.artifactId = split[2];
            this.version = split[3];
            this.type = split[4];
        }

        @Override Path getLocalPath() {
            Path path = Paths.get(System.getProperty("user.home"))
                .resolve(".m2/repository")
                .resolve(groupId.replace('.', '/'))
                .resolve(artifactId)
                .resolve(version)
                .resolve(artifactId + "-" + version + "." + type);

            if (Files.notExists(path))
                throw new IllegalArgumentException("maven artifact is not downloaded to local repository in " + path + ". Execute:\n"
                    + "mvn dependency:get -Dartifact=" + groupId + ":" + artifactId + ":" + version + ":" + type);

            return path;
        }

        @Override String getFileName() {
            return artifactId + "." + type;
        }
    }

    private static class UrlDeployable extends Deployable {
        private final URI deployment;
        @Getter private final String fileName;

        private UrlDeployable(URI deployment) {
            this.deployment = deployment;
            this.fileName = fileName(deployment);
        }

        @Override Path getLocalPath() {
            return download(deployment);
        }

        @SneakyThrows(IOException.class)
        private Path download(URI deployment) {
            Path tempFile = Files.createTempDirectory("downloads").resolve(fileName);
            log.info("download " + deployment + " to " + tempFile);

            Response get = CLIENT.target(deployment).request().buildGet().invoke();
            if (get.getStatusInfo() != OK)
                throw new IllegalStateException("can't download " + deployment
                    + ": " + get.getStatus() + " " + get.getStatusInfo());
            InputStream inputStream = get.readEntity(InputStream.class);

            Files.copy(inputStream, tempFile);

            return tempFile;
        }
    }

    static String fileName(URI uri) {
        Path path = Paths.get(uri.getSchemeSpecificPart());
        String fileName = path.getFileName().toString();
        Matcher matcher = FILE_NAME_PATTERN.matcher(fileName);
        if (matcher.matches())
            return matcher.group("filename") + matcher.group("extension");
        return fileName;
    }

    private static final Pattern FILE_NAME_PATTERN = Pattern.compile(
        "(?<filename>.*?)-(?<version>\\d+\\.\\d+\\.\\d+(-SNAPSHOT)?)(?<extension>\\.\\w{1,4})");
}
