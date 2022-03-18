package com.github.t1.testcontainers.jee;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.createFile;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.notExists;
import static java.nio.file.Files.readAllLines;
import static java.nio.file.Files.write;

@Slf4j
public @Value class ConfigMod implements Mod {
    public static Mod config(String key, String value) {return new ConfigMod(key, value);}

    String key;
    String value;

    @Override public Deployable apply(Deployable deployable) {
        JarOutputDeployable jarOutputDeployable = JarOutputDeployable.of(deployable);
        jarOutputDeployable.mod(ConfigModStore.class).put(key, value);
        return jarOutputDeployable;
    }

    public static class ConfigModStore implements ModStore {
        private final Map<String, String> configs = new LinkedHashMap<>();

        public void put(String key, String value) {
            configs.put(key, value);
        }

        @Override public void apply(FileSystem jar) {
            if (configs.isEmpty()) return;
            try {
                Path metaInf = jar.getPath("WEB-INF/classes/META-INF");
                if (notExists(metaInf)) createDirectories(metaInf);
                Path configFile = metaInf.resolve("microprofile-config.properties");
                List<String> lines = new ArrayList<>();
                if (exists(configFile)) lines.addAll(readAllLines(configFile));
                else createFile(configFile);
                configs.entrySet().stream().map(entry -> entry.getKey() + ": " + entry.getValue()).forEach(lines::add);
                write(configFile, lines);
            } catch (IOException | RuntimeException e) {
                throw new RuntimeException("can't add configs " + configs, e);
            }
        }
    }
}
