package test.it;

import com.github.t1.testcontainers.jee.JeeContainer;
import com.github.t1.testcontainers.tools.LogLine;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import test.app.Ping;

import static org.assertj.core.api.BDDAssertions.then;
import static test.TestTools.ping;
import static test.TestTools.war;

@WildFly
@Testcontainers
public class WarWithClassFromMainIT {
    @Container static JeeContainer CONTAINER = JeeContainer.create()
        .withDeployment(war(Ping.class).withClasses(LogLine.class));

    @Test void shouldGetPingResponse() {
        // To properly check that the class from `src/main/java` was copied,
        // we'd either have to add some functionality that is testable,
        // or we'd have to inspect the image; both is quite some effort to do.
        // So we currently only verify that it doesn't throw an exception.
        then(ping(CONTAINER)).isEqualTo("default-pong");
    }
}
