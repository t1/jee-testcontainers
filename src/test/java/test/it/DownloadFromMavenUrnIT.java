package test.it;

import com.github.t1.testcontainers.jee.JeeContainer;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import test.DemoApp;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import static java.nio.file.FileVisitResult.CONTINUE;
import static test.TestTools.EE8_IMAGE;

@WildFly
@Testcontainers
@Slf4j
public class DownloadFromMavenUrnIT {
    private static final DemoApp APP = DemoApp.EE8;

    @Container static JeeContainer CONTAINER = deleteLocalVersionAndCreateJeeContainer();

    private static JeeContainer deleteLocalVersionAndCreateJeeContainer() {
        deleteLocalVersion(); // this must happen before the container starts
        return JeeContainer.create(EE8_IMAGE)
            .withDeployment(APP.urn());
    }

    @SneakyThrows(IOException.class)
    private static void deleteLocalVersion() {
        Path dir = Paths.get(APP.localPath());
        if (Files.exists(dir)) {
            log.warn("deleting {}", dir);
            Files.walkFileTree(dir, new SimpleFileVisitor<>() {
                @Override public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.deleteIfExists(file);
                    return CONTINUE;
                }
            });
            Files.deleteIfExists(dir);
        }
    }

    @Test void shouldGetResponse() {
        APP.check(CONTAINER);
    }
}
