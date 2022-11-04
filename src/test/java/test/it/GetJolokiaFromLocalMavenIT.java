package test.it;

import com.github.t1.testcontainers.jee.JeeContainer;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import test.jolokia.JolokiaResponse;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static java.nio.file.FileVisitResult.CONTINUE;
import static test.TestTools.JSONB;
import static test.jolokia.TestData.LOCAL_M2;
import static test.jolokia.TestData.VERSION;

@WildFly
@Testcontainers
@Slf4j
public class GetJolokiaFromLocalMavenIT {

    @Container static JeeContainer CONTAINER = deleteLocalJolokiaVersionAndCreateJeeContainer();

    private static JeeContainer deleteLocalJolokiaVersionAndCreateJeeContainer() {
        deleteLocalJolokiaVersion(); // this must happen before the container starts
        return JeeContainer.create()
            .withDeployment("urn:mvn:org.jolokia:jolokia-war-unsecured:" + VERSION + ":war");
    }

    @SneakyThrows(IOException.class)
    private static void deleteLocalJolokiaVersion() {
        Path dir = Paths.get(LOCAL_M2, "org/jolokia/jolokia-war-unsecured", VERSION);
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

    @Test void shouldGetJolokiaResponse() {
        String string = CONTAINER.target().request(APPLICATION_JSON_TYPE).get(String.class);

        JolokiaResponse response = JSONB.fromJson(string, JolokiaResponse.class);

        response.assertCurrent();
    }
}
