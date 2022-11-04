package test.it;

import com.github.t1.testcontainers.jee.JeeContainer;
import com.github.t1.testcontainers.jee.WildflyContainer;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.BDDAssertions.then;
import static test.TestTools.WILDFLY_JAKARTA_VERSION;
import static test.TestTools.pingWar;

@WildFly
@Testcontainers
public class WildflyIT {
    @Container static JeeContainer CONTAINER = WildflyContainer.create(WILDFLY_JAKARTA_VERSION)
        .withDeployment(pingWar());

    @Path("/ping")
    public interface PingApi {
        @GET String ping();
    }

    @Test void shouldGetPingResponse() {
        PingApi ping = CONTAINER.restClient(PingApi.class);

        String pong = ping.ping();

        then(pong).isEqualTo("default-pong");
    }
}
