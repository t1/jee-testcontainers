package test.it;

import com.github.t1.testcontainers.jee.JeeContainer;
import com.github.t1.testcontainers.jee.WildflyContainer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import test.TestTools;

import static org.assertj.core.api.BDDAssertions.then;
import static test.jolokia.TestData.VERSION;

@WildFly
@Testcontainers
@Slf4j
public class ExposeFixedMainPortIT {
    private static final int FIXED_PORT = TestTools.someFreePort();

    @Container static JeeContainer CONTAINER =
        // JeeContainer.create()
        new WildflyContainer("rdohna/wildfly", "25.0.1.Final-jdk11")
            .withFixedExposedMainPort(FIXED_PORT)
            .withDeployment("urn:mvn:org.jolokia:jolokia-war-unsecured:" + VERSION + ":war");

    @Test void shouldGetJolokiaResponse() {
        int port = CONTAINER.baseUri().getPort();

        then(port).isEqualTo(FIXED_PORT);
    }
}
