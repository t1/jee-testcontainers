package test.it;

import com.github.t1.testcontainers.jee.JeeContainer;
import com.github.t1.testcontainers.jee.WildflyContainer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.bind.JsonbBuilder;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import static com.github.t1.testcontainers.jee.ConfigMod.config;
import static java.nio.file.Files.deleteIfExists;
import static java.nio.file.Files.newOutputStream;
import static java.nio.file.Files.readAllBytes;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.BDDAssertions.then;

@Slf4j
@WildFly
@Testcontainers
public class SetConfigPropertyIT {
    static {
        Path path = Paths.get("target/ping.war");
        try {
            deleteIfExists(path);
            try (JarOutputStream jar = new JarOutputStream(newOutputStream(path))) {
                copy(jar, "test/it/Ping.class");
                copy(jar, "test/it/REST.class");
                addBeansXml(jar);
            }
        } catch (IOException | RuntimeException e) {
            throw new RuntimeException("can't create " + path, e);
        }
    }

    private static void copy(JarOutputStream jar, String file) throws IOException {
        jar.putNextEntry(new ZipEntry("WEB-INF/classes/" + file));
        jar.write(readAllBytes(Paths.get("target/test-classes/" + file)));
        jar.closeEntry();
    }

    private static void addBeansXml(JarOutputStream jar) throws IOException {
        jar.putNextEntry(new ZipEntry("WEB-INF/classes/META-INF/beans.xml"));
        jar.write(new byte[0]);
        jar.closeEntry();
    }


    @Container static JeeContainer UN_CONFIGURED_PING = JeeContainer.create()
        .withDeployment("target/ping.war");

    @Test void shouldPingUnConfigured() {
        Response response = UN_CONFIGURED_PING.target().path("/ping").request().get();

        then(response.getStatusInfo()).isEqualTo(OK);
        then(response.readEntity(String.class)).isEqualTo("default-pong");
    }


    @Container static JeeContainer CONFIGURED_PING = JeeContainer.create()
        .withDeployment("target/ping.war", config("pong", "foo"));

    @Test void shouldCreateNewConfig() {
        Response response = CONFIGURED_PING.target().path("/ping").request().get();

        then(response.getStatusInfo()).isEqualTo(OK);
        then(response.readEntity(String.class)).isEqualTo("foo");
    }


    @Container static JeeContainer DEMO = new WildflyContainer("26.0.1.Final-jdk11") // 'latest' contains GraphQL
        .withDeployment("urn:mvn:com.github.t1:wunderbar.demo.order:2.4.4:war", // and this contains GraphQL, too
            config("smallrye.graphql.errorExtensionFields", "exception")); // default contains `code`

    @Test void shouldAppendToExistingConfig() {
        Response response = DEMO.target().path("/graphql").request(APPLICATION_JSON_TYPE)
            .post(entity("{\"query\": \"{order(id:\\\"unknown\\\"){id orderDate}}\"}", APPLICATION_JSON_TYPE));

        then(response.getStatusInfo()).isEqualTo(OK);
        JsonObject jsonObject = JsonbBuilder.create().fromJson(response.readEntity(String.class), JsonObject.class);
        log.info("response: {}", jsonObject);
        then(jsonObject.getValue("/errors/0/message")).isEqualTo(Json.createValue("no order with id unknown"));
        then(jsonObject.getValue("/errors/0/extensions").asJsonObject().keySet()).containsExactly("exception");
    }
}
