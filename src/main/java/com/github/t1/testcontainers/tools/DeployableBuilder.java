package com.github.t1.testcontainers.tools;

import lombok.SneakyThrows;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.JarOutputStream;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;

import static java.nio.charset.StandardCharsets.UTF_8;
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
        Stream.of(classes).forEach(c -> copyClassFile(jar, c.getName().replace('.', '/') + ".class"));
        return this;
    }

    public DeployableBuilder withFile(String name, String content) {
        copy(jar, name, content.getBytes(UTF_8));
        return this;
    }

    public DeployableBuilder withPersistenceXml(String name) {
        return withFile("WEB-INF/classes/META-INF/persistence.xml",
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
            "<persistence xmlns=\"https://jakarta.ee/xml/ns/persistence\" version=\"3.0\">\n" +
            "    <persistence-unit name=\"" + name + "\">\n" +
            "        <jta-data-source>" + dbJndiName(name) + "</jta-data-source>\n" +
            "    </persistence-unit>\n" +
            "</persistence>\n");
    }

    public static String dbJndiName(String name) {
        return "java:jboss/datasources/" + name; // this probably has to be made dependent on the JEE container
    }

    @SneakyThrows(IOException.class)
    private static void copyClassFile(JarOutputStream jar, String file) {
        copy(jar, "WEB-INF/classes/" + file, readAllBytes(Paths.get("target/test-classes/" + file)));
    }

    private static void addBeansXml(JarOutputStream jar) {
        copy(jar, "WEB-INF/classes/META-INF/beans.xml", new byte[0]);
    }

    @SneakyThrows(IOException.class)
    private static void copy(JarOutputStream jar, String fileName, byte[] bytes) {
        jar.putNextEntry(new ZipEntry(fileName));
        jar.write(bytes);
        jar.closeEntry();
    }

    @SneakyThrows(IOException.class)
    public URI build() {
        jar.close();
        return path.toUri();
    }
}
