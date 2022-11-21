package test.it;

import com.github.t1.testcontainers.jee.JeeContainer;
import com.github.t1.testcontainers.jee.OpenLibertyContainer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import test.app.Ping;

import static org.assertj.core.api.BDDAssertions.then;
import static test.TestTools.ping;
import static test.TestTools.war;

@Disabled("currently fails [https://github.com/t1/jee-testcontainers/issues/28]")
@OpenLiberty
@Testcontainers
public class OpenLibertyIT {
    @Container static JeeContainer CONTAINER = OpenLibertyContainer.create().withDeployment(war(Ping.class));

    @Test void shouldGetResponse() {
        then(ping(CONTAINER)).isEqualTo("default-pong");
        then(CONTAINER.getDockerImageName()).isEqualTo("open-liberty:latest");
    }
}
