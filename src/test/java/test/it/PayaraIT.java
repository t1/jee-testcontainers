package test.it;

import com.github.t1.testcontainers.jee.JeeContainer;
import com.github.t1.testcontainers.jee.PayaraContainer;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import test.DemoApp;

import static org.assertj.core.api.BDDAssertions.then;

@Payara
@Testcontainers
public class PayaraIT {
    private static final DemoApp APP = DemoApp.LATEST;

    @Container static JeeContainer CONTAINER = PayaraContainer.create().withDeployment(APP.urn());

    @Test void shouldGetResponse() {
        APP.check(CONTAINER);
        then(CONTAINER.getDockerImageName()).isEqualTo("payara/server-full:latest");
    }
}
