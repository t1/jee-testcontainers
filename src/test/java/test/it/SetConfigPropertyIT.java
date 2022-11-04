package test.it;

import com.github.t1.testcontainers.jee.JeeContainer;
import com.github.t1.testcontainers.jee.WildflyContainer;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.client.Invocation.Builder;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static com.github.t1.testcontainers.jee.ConfigMod.config;
import static jakarta.ws.rs.client.Entity.entity;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static jakarta.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.BDDAssertions.then;
import static test.TestTools.JAKARTA_IMAGE;
import static test.TestTools.JSONB;
import static test.TestTools.pingWar;

@Slf4j
@WildFly
@Testcontainers
public class SetConfigPropertyIT {

    @Nested class UnConfigured {
        @Container JeeContainer UN_CONFIGURED_PING = JeeContainer.create(JAKARTA_IMAGE)
            .withDeployment(pingWar());

        @Test void shouldPingUnConfigured() {
            var webTarget = UN_CONFIGURED_PING.target().path("/ping");

            Response response = webTarget.request().get();

            then(response.getStatusInfo()).isEqualTo(OK);
            then(response.readEntity(String.class)).isEqualTo("default-pong");
        }
    }


    @Nested class Configured {
        @Container JeeContainer CONFIGURED_PING = JeeContainer.create(JAKARTA_IMAGE)
            .withDeployment(pingWar(), config("pong", "foo"));


        @Test void shouldCreateNewConfig() {
            Response response = CONFIGURED_PING.target().path("/ping").request().get();

            then(response.getStatusInfo()).isEqualTo(OK);
            then(response.readEntity(String.class)).isEqualTo("foo");
        }
    }


    @Nested class ConfigAppend {
        // this is still with javax package names
        @Container JeeContainer DEMO = WildflyContainer.create("26.1.2.Final-jdk11") // 'latest' contains GraphQL
            .withDeployment("urn:mvn:com.github.t1:wunderbar.demo.order:2.4.6:war", // and this contains GraphQL, too
                config("smallrye.graphql.errorExtensionFields", "exception")); // default contains `code`

        @Test void shouldAppendToExistingConfig() {
            Builder request = DEMO.target().path("/graphql").request(APPLICATION_JSON_TYPE);
            try (Response response = request.post(entity("{\"query\": \"{order(id:\\\"unknown\\\"){id orderDate}}\"}", APPLICATION_JSON_TYPE))) {

                then(response.getStatusInfo()).isEqualTo(OK);
                JsonObject jsonObject = JSONB.fromJson(response.readEntity(String.class), JsonObject.class);
                log.info("response: {}", jsonObject);
                then(jsonObject.getValue("/errors/0/message")).isEqualTo(Json.createValue("no order with id unknown"));
                then(jsonObject.getValue("/errors/0/extensions").asJsonObject().keySet()).containsExactly("exception");
            }
        }
    }
}
