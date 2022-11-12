package test.it;

import com.github.t1.testcontainers.jee.JeeContainer;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import test.app.Ping;

import static org.assertj.core.api.BDDAssertions.then;
import static test.TestTools.someFreePort;
import static test.TestTools.war;

@WildFly
@Testcontainers
public class ExposeFixedPortIT {
    private static final int FIXED_PORT = someFreePort();

    @Container static JeeContainer CONTAINER = JeeContainer.create()
        .withPortBoundToFixedPort(FIXED_PORT, 8080)
        .withDeployment(war(Ping.class));

    @Test void shouldGetResponse() {
        int port = CONTAINER.baseUri().getPort();

        then(port).isEqualTo(FIXED_PORT);
    }
}
