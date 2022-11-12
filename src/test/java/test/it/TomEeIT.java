package test.it;

import com.github.t1.testcontainers.jee.JeeContainer;
import com.github.t1.testcontainers.jee.TomEeContainer;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import test.app.MiniPing;

import static com.github.t1.testcontainers.jee.NamedAsMod.namedAs;
import static org.assertj.core.api.BDDAssertions.then;
import static test.TestTools.ping;
import static test.TestTools.war;

@TomEE
@Testcontainers
public class TomEeIT {
    private static final String TAG = "9-jre17-Temurin-ubuntu-plume";
    @Container static JeeContainer CONTAINER = TomEeContainer.create(TAG) // `final` is yet still EE8
        .withDeployment(war(MiniPing.class), namedAs("ping.war")); // we have problems with MP config

    @Test void shouldGetResponse() {
        then(ping(CONTAINER)).isEqualTo("mini-pong");
        then(CONTAINER.getDockerImageName()).isEqualTo("tomee:" + TAG);
    }
}
