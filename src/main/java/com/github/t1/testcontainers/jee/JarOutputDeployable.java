package com.github.t1.testcontainers.jee;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import static java.nio.file.Files.getPosixFilePermissions;
import static java.nio.file.Files.setPosixFilePermissions;

@Slf4j
@RequiredArgsConstructor
class JarOutputDeployable extends Deployable {
    static JarOutputDeployable of(Deployable deployable) {
        return (deployable instanceof JarOutputDeployable)
            ? (JarOutputDeployable) deployable
            : new JarOutputDeployable(Deployable.copyOf(deployable));
    }

    private final Deployable deployable;
    private final Map<Class<? extends ModStore>, ModStore> mods = new LinkedHashMap<>();
    private boolean done = false;

    @Override public String toString() {
        return "JarOutputDeployable[" + deployable + ":" + mods + "]";
    }

    public <T extends ModStore> T mod(Class<T> type) {
        return type.cast(mods.computeIfAbsent(type, this::createModStore));
    }

    @SneakyThrows(ReflectiveOperationException.class)
    private <T extends ModStore> T createModStore(Class<T> type) {return type.getConstructor().newInstance();}

    @Override public String getFileName() {return deployable.getFileName();}

    @Override public Path getLocalPath() {
        if (!done) {
            mods.values().forEach(mod -> inJarFile(mod::apply));
            done = true;
        }
        return deployable.getLocalPath();
    }

    @SneakyThrows(IOException.class)
    private void inJarFile(Consumer<FileSystem> operation) {
        Path path = deployable.getLocalPath();
        // opening the jar as a file system loses the file permission for group and other, but we need that
        Set<PosixFilePermission> permissions = getPosixFilePermissions(path);
        //noinspection RedundantCast // the cast is required for JDK 13+
        try (FileSystem jar = FileSystems.newFileSystem(path, (ClassLoader) null)) {
            operation.accept(jar);
        }
        setPosixFilePermissions(path, permissions);
    }
}
