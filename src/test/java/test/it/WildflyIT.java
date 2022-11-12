package test.it;

import com.github.t1.testcontainers.jee.JeeContainer;
import com.github.t1.testcontainers.jee.WildflyContainer;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import test.app.Ping;

import static org.assertj.core.api.BDDAssertions.then;
import static test.TestTools.ping;
import static test.TestTools.war;

@WildFly
@Testcontainers
public class WildflyIT {
    @Container static JeeContainer CONTAINER = WildflyContainer.create()
        .withDeployment(war(Ping.class));

    @Test void shouldGetPingResponse() {
        then(ping(CONTAINER)).isEqualTo("default-pong");
        then(CONTAINER.getDockerImageName()).isEqualTo("quay.io/wildfly/wildfly:latest");
    }
}
