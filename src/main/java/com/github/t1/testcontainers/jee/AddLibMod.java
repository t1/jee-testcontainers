package com.github.t1.testcontainers.jee;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.Files.copy;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.notExists;

@Slf4j
public @Value class AddLibMod implements Mod {
    public static Mod addLib(String uri) {return addLib(URI.create(uri));}

    public static Mod addLib(URI uri) {return new AddLibMod(uri);}

    URI name;

    @Override public Deployable apply(Deployable deployable) {
        return with(deployable, AddLibModStore.class, d -> d.add(Deployable.create(name)));
    }

    public static class AddLibModStore implements ModStore {
        private final List<Deployable> libs = new ArrayList<>();

        public void add(Deployable name) {
            libs.add(name);
        }

        @Override public void apply(FileSystem jar) {
            if (libs.isEmpty()) return;
            try {
                Path libFolder = jar.getPath("WEB-INF/lib/");
                if (notExists(libFolder)) createDirectories(libFolder);
                for (Deployable lib : libs) {
                    String fileName = lib.getLocalPath().getFileName().toString();
                    log.info("add lib {}", fileName);
                    Path libPath = libFolder.resolve(fileName);
                    copy(lib.getLocalPath(), libPath);
                }
            } catch (IOException | RuntimeException e) {
                throw new RuntimeException("can't add libs " + libs, e);
            }
        }
    }
}
