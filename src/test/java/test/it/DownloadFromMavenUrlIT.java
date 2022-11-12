package test.it;

import com.github.t1.testcontainers.jee.JeeContainer;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import test.DemoApp;

import static test.TestTools.EE8_IMAGE;

@WildFly
@Testcontainers
public class DownloadFromMavenUrlIT {
    private static final DemoApp APP = DemoApp.EE8;

    @Container static JeeContainer CONTAINER = JeeContainer.create(EE8_IMAGE)
        .withDeployment(APP.url());

    @Test void shouldGetResponse() {
        APP.check(CONTAINER);
    }
}
