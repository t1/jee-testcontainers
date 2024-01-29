package test.it;

import com.github.t1.testcontainers.jee.JeeContainer;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.client.Invocation.Builder;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import test.DemoApp;
import test.app.Ping;

import static com.github.t1.testcontainers.jee.ConfigMod.config;
import static jakarta.ws.rs.client.Entity.json;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static jakarta.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.BDDAssertions.then;
import static test.TestTools.*;

@Slf4j
@WildFly
@Testcontainers
public class SetConfigPropertyIT {

    @Nested class UnConfigured {
        @Container JeeContainer UN_CONFIGURED_PING = JeeContainer.create()
                .withDeployment(war(Ping.class));

        @Test void shouldPingUnConfigured() {
            then(ping(UN_CONFIGURED_PING)).isEqualTo("default-pong");
        }
    }


    @Nested class Configured {
        @Container JeeContainer CONFIGURED_PING = JeeContainer.create()
                .withDeployment(war(Ping.class), config("pong", "foo"));

        @Test void shouldCreateNewConfig() {
            then(ping(CONFIGURED_PING)).isEqualTo("foo");
        }
    }


    @Nested class ConfigAppend {
        @Container JeeContainer DEMO = JeeContainer.create()
                .withDeployment(DemoApp.LATEST.urn(), config("smallrye.graphql.errorExtensionFields", "exception")); // this app also contains `code` -> overwrite

        @Test void shouldAppendToExistingConfig() {
            Builder request = DEMO.target().path("/graphql").request(APPLICATION_JSON_TYPE);
            try (Response response = request.post(json("{\"query\": \"{order(id:\\\"unknown\\\"){id orderDate}}\"}"))) {
                then(response.getStatusInfo()).isEqualTo(OK);
                JsonObject jsonObject = JSONB.fromJson(response.readEntity(String.class), JsonObject.class);
                log.info("response: {}", jsonObject);
                then(jsonObject.getValue("/errors/0/message")).isEqualTo(Json.createValue("no order with id unknown"));
                then(jsonObject.getValue("/errors/0/extensions").asJsonObject().keySet())
                        .describedAs("this MP config options should have been overwritten")
                        .containsExactly("exception");
            }
        }
    }
}
