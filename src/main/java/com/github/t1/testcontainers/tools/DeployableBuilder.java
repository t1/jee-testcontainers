package com.github.t1.testcontainers.tools;

import lombok.SneakyThrows;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.JarOutputStream;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;

import static java.nio.file.Files.deleteIfExists;
import static java.nio.file.Files.newOutputStream;
import static java.nio.file.Files.readAllBytes;

public class DeployableBuilder {
    public static DeployableBuilder war(String name) {
        return new DeployableBuilder(name);
    }

    private final Path path;
    private final JarOutputStream jar;

    public DeployableBuilder(String name) {
        this.path = Paths.get("target/" + name + ".war");
        this.jar = createFile();
        addBeansXml(jar);
    }

    @SneakyThrows(IOException.class)
    private JarOutputStream createFile() {
        deleteIfExists(path);
        return new JarOutputStream(newOutputStream(path));
    }

    public DeployableBuilder withClasses(Class<?>... classes) {
        Stream.of(classes).forEach(c -> copy(jar, c.getName().replace('.', '/') + ".class"));
        return this;
    }

    @SneakyThrows(IOException.class)
    public URI build() {
        jar.close();
        return path.toUri();
    }

    @SneakyThrows(IOException.class)
    private static void copy(JarOutputStream jar, String file) {
        jar.putNextEntry(new ZipEntry("WEB-INF/classes/" + file));
        jar.write(readAllBytes(Paths.get("target/test-classes/" + file)));
        jar.closeEntry();
    }

    @SneakyThrows(IOException.class)
    private static void addBeansXml(JarOutputStream jar) {
        jar.putNextEntry(new ZipEntry("WEB-INF/classes/META-INF/beans.xml"));
        jar.write(new byte[0]);
        jar.closeEntry();
    }
}
