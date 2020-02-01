package com.github.t1.testcontainers.jee;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static java.nio.file.Files.copy;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.getPosixFilePermissions;
import static java.nio.file.Files.notExists;
import static java.nio.file.Files.setPosixFilePermissions;

@Slf4j
public @Value class AddLibMod implements Mod {
    public static Mod addLib(String uri) { return addLib(URI.create(uri)); }

    public static Mod addLib(URI uri) { return new AddLibMod(uri); }

    URI name;

    @Override public Deployable apply(Deployable deployable) {
        JarOutputDeployable copy = (deployable instanceof JarOutputDeployable)
            ? (JarOutputDeployable) deployable
            : new JarOutputDeployable(Deployable.copyOf(deployable));
        copy.addLib(Deployable.create(name));
        return copy;
    }

    @RequiredArgsConstructor
    private static class JarOutputDeployable extends Deployable {
        private final Deployable deployable;
        private final List<Deployable> libs = new ArrayList<>();
        private boolean done = false;

        @Override public String toString() {
            return "JarOutputDeployable[" + deployable + ":" + libs + "]";
        }

        public void addLib(Deployable lib) { this.libs.add(lib); }

        @Override String getFileName() { return deployable.getFileName(); }

        @Override Path getLocalPath() {
            if (!done) {
                addLibs(deployable.getLocalPath());
                done = true;
            }
            return deployable.getLocalPath();
        }

        @SneakyThrows(IOException.class)
        private void addLibs(Path path) {
            // opening the jar as a file system loses the read permission for group and other, but we need that
            Set<PosixFilePermission> permissions = getPosixFilePermissions(path);
            try (FileSystem jar = FileSystems.newFileSystem(path, null)) {
                Path libFolder = jar.getPath("WEB-INF/lib/");
                if (notExists(libFolder))
                    createDirectories(libFolder);
                for (Deployable lib : libs) {
                    String fileName = lib.getLocalPath().getFileName().toString();
                    log.info("add lib {} to {}", fileName, jar);
                    Path libPath = libFolder.resolve(fileName);
                    copy(lib.getLocalPath(), libPath);
                }
            } catch (IOException | RuntimeException e) {
                throw new RuntimeException("can't add libs " + libs + " to " + path, e);
            }
            setPosixFilePermissions(path, permissions);
        }
    }
}
