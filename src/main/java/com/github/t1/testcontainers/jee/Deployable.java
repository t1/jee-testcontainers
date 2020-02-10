package com.github.t1.testcontainers.jee;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.github.t1.testcontainers.jee.JeeContainer.CLIENT;
import static java.nio.file.Files.copy;
import static java.nio.file.Files.createTempDirectory;
import static java.nio.file.Files.notExists;
import static java.util.concurrent.TimeUnit.SECONDS;
import static javax.ws.rs.core.Response.Status.OK;

@Slf4j
public abstract class Deployable {

    public abstract Path getLocalPath();

    public abstract String getFileName();

    public static Deployable create(String deployable) { return create(URI.create(deployable)); }

    public static Deployable create(URI deployable) {
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

    public static Deployable copyOf(Deployable deployable) {
        return new CopyOfDeployable(deployable, deployable.getFileName());
    }

    static Deployable copyOf(Deployable deployable, String fileName) {
        return new CopyOfDeployable(deployable, fileName);
    }


    private static class FileDeployable extends Deployable {
        @Getter private final Path localPath;
        @Getter private final String fileName;

        private FileDeployable(URI deployment) {
            this.localPath = (deployment.getScheme() == null)
                ? Paths.get(deployment.toString()) : Paths.get(deployment);
            this.fileName = fileName(deployment);
        }

        @Override public String toString() {
            return "FileDeployable[" + localPath + "->" + fileName + "]";
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
            if (split.length < 4 || split.length > 5)
                throw new IllegalArgumentException("expected 4 or 5 elements in 'mvn' urn '" + deployment + "': " +
                    "`urn:mvn:<group-id>:<artifact-id>:<version>[:<type>]`");
            this.groupId = split[1];
            this.artifactId = split[2];
            this.version = split[3];
            this.type = (split.length > 4) ? split[4] : "jar";
        }

        @Override public String toString() {
            return "UrnDeployable[" + groupId + ":" + artifactId + ":" + version + ":" + type + "]";
        }

        @Override public Path getLocalPath() {
            Path path = Paths.get(System.getProperty("user.home"))
                .resolve(".m2/repository")
                .resolve(groupId.replace('.', '/'))
                .resolve(artifactId)
                .resolve(version)
                .resolve(artifactId + "-" + version + "." + type);

            if (notExists(path)) {
                download(groupId + ":" + artifactId + ":" + version + ":" + type);
            }

            return path;
        }

        @SneakyThrows({IOException.class, InterruptedException.class})
        private void download(String gavt) {
            ProcessBuilder builder = new ProcessBuilder("mvn", "dependency:get", "-Dartifact=" + gavt)
                .redirectErrorStream(true);
            Process process = builder.start();
            boolean inTime = process.waitFor(60, SECONDS);
            if (!inTime) {
                throw new RuntimeException("timeout download " + gavt);
            }
        }

        @Override public String getFileName() {
            return artifactId + "." + type;
        }
    }


    private static class UrlDeployable extends Deployable {
        private final URI uri;
        @Getter private final String fileName;
        private final Path tempFile;

        @SneakyThrows(IOException.class)
        private UrlDeployable(URI uri) {
            this.uri = uri;
            this.fileName = fileName(uri);
            this.tempFile = createTempDirectory("downloads").resolve(fileName);
        }

        @Override public String toString() {
            return "UrlDeployable[" + uri + ":" + fileName + "]";
        }

        @Override public Path getLocalPath() {
            if (notExists(tempFile))
                download();
            return tempFile;
        }

        @SneakyThrows(IOException.class)
        private void download() {
            log.info("download " + uri + " to " + tempFile);

            Response get = CLIENT.target(uri).request().buildGet().invoke();
            if (get.getStatusInfo() != OK)
                throw new IllegalStateException("can't download " + uri
                    + ": " + get.getStatus() + " " + get.getStatusInfo());
            InputStream inputStream = get.readEntity(InputStream.class);

            copy(inputStream, tempFile);
        }
    }

    static class CopyOfDeployable extends Deployable {
        private final Deployable deployable;
        private final Path tempFile;
        @Getter private final String fileName;

        @SneakyThrows(IOException.class)
        private CopyOfDeployable(Deployable deployable, String fileName) {
            this.deployable = deployable;
            this.fileName = fileName;
            this.tempFile = createTempDirectory("copies").resolve(fileName);
        }

        @Override public String toString() {
            return "CopyOfDeployable[" + deployable + ":" + fileName + "]";
        }

        @SneakyThrows(IOException.class)
        @Override public Path getLocalPath() {
            if (notExists(tempFile))
                copy(deployable.getLocalPath(), tempFile);
            return tempFile;
        }
    }

    private static String fileName(URI uri) {
        Path path = Paths.get(uri.getSchemeSpecificPart());
        return path.getFileName().toString();
    }
}
