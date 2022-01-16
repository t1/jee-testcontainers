package test.it;

import com.github.t1.testcontainers.jee.JeeContainer;
import com.github.t1.testcontainers.jee.WildflyContainer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.BDDAssertions.then;
import static test.TestTools.someFreePort;
import static test.jolokia.TestData.VERSION;

@WildFly
@Testcontainers
@Slf4j
public class ExposeFixedPortIT {
    private static final int FIXED_PORT = someFreePort();

    @Container static JeeContainer CONTAINER =
        // JeeContainer.create()
        new WildflyContainer("rdohna/wildfly", "25.0.1.Final-jdk11")
            .withFixedExposedPort(FIXED_PORT, 8080)
            .withDeployment("urn:mvn:org.jolokia:jolokia-war-unsecured:" + VERSION + ":war");

    @Test void shouldGetJolokiaResponse() {
        int port = CONTAINER.baseUri().getPort();

        then(port).isEqualTo(FIXED_PORT);
    }
}
