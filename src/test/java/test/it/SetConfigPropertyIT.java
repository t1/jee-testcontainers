package test.it;

import com.github.t1.testcontainers.jee.JeeContainer;
import com.github.t1.testcontainers.jee.WildflyContainer;
import com.github.t1.testcontainers.tools.DeployableBuilder;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import test.app.Ping;
import test.app.REST;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.bind.JsonbBuilder;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.Response;

import static com.github.t1.testcontainers.jee.ConfigMod.config;
import static com.github.t1.testcontainers.tools.DeployableBuilder.war;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.BDDAssertions.then;

@Slf4j
@WildFly
@Testcontainers
public class SetConfigPropertyIT {
    static DeployableBuilder buildPing() {
        return war("ping").withClasses(Ping.class, REST.class);
    }


    @Container static JeeContainer UN_CONFIGURED_PING = JeeContainer.create()
        .withDeployment(buildPing());

    @Test void shouldPingUnConfigured() {
        Response response = UN_CONFIGURED_PING.target().path("/ping").request().get();

        then(response.getStatusInfo()).isEqualTo(OK);
        then(response.readEntity(String.class)).isEqualTo("default-pong");
    }


    @Container static JeeContainer CONFIGURED_PING = JeeContainer.create()
        .withDeployment(buildPing(), config("pong", "foo"));


    @Test void shouldCreateNewConfig() {
        Response response = CONFIGURED_PING.target().path("/ping").request().get();

        then(response.getStatusInfo()).isEqualTo(OK);
        then(response.readEntity(String.class)).isEqualTo("foo");
    }


    @Container static JeeContainer DEMO = WildflyContainer.create("26.0.1.Final-jdk11") // 'latest' contains GraphQL
        .withDeployment("urn:mvn:com.github.t1:wunderbar.demo.order:2.4.4:war", // and this contains GraphQL, too
            config("smallrye.graphql.errorExtensionFields", "exception")); // default contains `code`

    @Test void shouldAppendToExistingConfig() {
        Builder request = DEMO.target().path("/graphql").request(APPLICATION_JSON_TYPE);
        try (Response response = request.post(entity("{\"query\": \"{order(id:\\\"unknown\\\"){id orderDate}}\"}", APPLICATION_JSON_TYPE))) {

            then(response.getStatusInfo()).isEqualTo(OK);
            JsonObject jsonObject = JsonbBuilder.create().fromJson(response.readEntity(String.class), JsonObject.class);
            log.info("response: {}", jsonObject);
            then(jsonObject.getValue("/errors/0/message")).isEqualTo(Json.createValue("no order with id unknown"));
            then(jsonObject.getValue("/errors/0/extensions").asJsonObject().keySet()).containsExactly("exception");
        }
    }
}
