package test.it;

import com.github.t1.testcontainers.jee.JeeContainer;
import com.github.t1.testcontainers.jee.WildflyContainer;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static com.github.t1.testcontainers.jee.NamedAsMod.namedAs;
import static jakarta.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.BDDAssertions.then;
import static test.TestTools.WILDFLY_JAKARTA_VERSION;
import static test.TestTools.pingWar;

@WildFly
@Testcontainers
public class WildflyROOTIT {
    @Container static JeeContainer CONTAINER = WildflyContainer.create(WILDFLY_JAKARTA_VERSION)
        .withDeployment(pingWar(), namedAs("ROOT.war"));

    @Test void shouldGetRootResponse() {
        var webTarget = CONTAINER.target().path("/ping");

        Response response = webTarget.request().get();

        then(webTarget.getUri()).hasPath("/ping");
        then(response.getStatusInfo()).isEqualTo(OK);
        then(response.readEntity(String.class)).contains("pong");
    }
}
