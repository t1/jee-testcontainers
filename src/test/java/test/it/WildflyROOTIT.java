package test.it;

import com.github.t1.testcontainers.jee.JeeContainer;
import com.github.t1.testcontainers.jee.WildflyContainer;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import test.app.Ping;

import static com.github.t1.testcontainers.jee.NamedAsMod.namedAs;
import static org.assertj.core.api.BDDAssertions.then;
import static test.TestTools.ping;
import static test.TestTools.war;

@WildFly
@Testcontainers
public class WildflyROOTIT {
    @Container static JeeContainer CONTAINER = WildflyContainer.create()
        .withDeployment(war(Ping.class), namedAs("ROOT.war"));

    @Test void shouldGetRootResponse() {
        then(ping(CONTAINER)).isEqualTo("default-pong");
        then(CONTAINER.target().path("/x").getUri()).hasPath("/x"); // no app name
    }
}
