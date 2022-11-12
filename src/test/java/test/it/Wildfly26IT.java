package test.it;

import com.github.t1.testcontainers.jee.JeeContainer;
import com.github.t1.testcontainers.jee.WildflyContainer;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import test.DemoApp;

import static test.TestTools.WILDFLY_IMAGE;
import static test.TestTools.WILDFLY_VERSION;

@WildFly
@Testcontainers
public class Wildfly26IT {
    private static final DemoApp APP = DemoApp.EE8;

    @Container static JeeContainer CONTAINER = WildflyContainer.create(WILDFLY_IMAGE, WILDFLY_VERSION)
        .withDeployment(APP.urn());

    @Test void shouldGetResponse() {
        APP.check(CONTAINER);
    }
}
