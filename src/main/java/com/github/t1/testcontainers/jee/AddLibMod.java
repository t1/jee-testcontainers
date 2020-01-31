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
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.Files.copy;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.exists;

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

        public void addLib(Deployable lib) { this.libs.add(lib); }

        @Override String getFileName() { return deployable.getFileName(); }

        @Override Path getLocalPath() {
            if (!done) {
                addLibs();
                done = true;
            }
            return deployable.getLocalPath();
        }

        @SneakyThrows(IOException.class)
        private void addLibs() {
            try (FileSystem jar = FileSystems.newFileSystem(deployable.getLocalPath(), null)) {
                Path libFolder = jar.getPath("/WEB-INF/lib/");
                if (!exists(libFolder))
                    createDirectories(libFolder);
                for (Deployable lib : libs) {
                    String fileName = lib.getLocalPath().getFileName().toString();
                    log.info("add {} to {}", fileName, jar + ":" + deployable.getLocalPath());
                    Path libPath = libFolder.resolve(fileName);
                    copy(lib.getLocalPath(), libPath);
                }
            }
        }
    }
}
