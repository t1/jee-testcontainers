package test.it;

import com.github.t1.testcontainers.jee.JeeContainer;
import com.github.t1.testcontainers.jee.WildflyContainer;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import test.DemoApp;

/** 26.x was the last EE8 container, i.e. still with <code>javax</code> packages and not yet <code>jakarta</code> */
@WildFly
@Testcontainers
public class Wildfly26IT {
    private static final DemoApp APP = DemoApp.EE8;

    @Container static JeeContainer CONTAINER = WildflyContainer.create("rdohna/wildfly", "26.1-jdk11")
            .withDeployment(APP.urn());

    @Test void shouldGetResponse() {
        APP.check(CONTAINER);
    }
}
