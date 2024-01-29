package test.it;

import com.github.t1.testcontainers.jee.JeeContainer;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import test.DemoApp;

@WildFly
@Testcontainers
public class DownloadFromMavenUrlIT {
    private static final DemoApp APP = DemoApp.LATEST;

    @Container static JeeContainer CONTAINER = JeeContainer.create()
            .withDeployment(APP.url());

    @Test void shouldGetResponse() {
        APP.check(CONTAINER);
    }
}
