package test.it;

import com.github.t1.testcontainers.jee.JeeContainer;
import com.github.t1.testcontainers.tools.LogLine;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import test.app.Ping;

import static org.assertj.core.api.BDDAssertions.then;
import static org.slf4j.event.Level.DEBUG;
import static org.slf4j.event.Level.INFO;
import static test.CustomAssertions.thenLogsIn;
import static test.TestTools.ping;
import static test.TestTools.war;

@Slf4j
@WildFly
@Testcontainers
public class ConfigureLogIT {

    @Container static JeeContainer CONTAINER = JeeContainer.create()
        .withDeployment(war(Ping.class))
        .withLogLevel(Ping.class, DEBUG);

    @Test void shouldLogInfo() {
        then(ping(CONTAINER)).isEqualTo("default-pong");
        then(CONTAINER.target().path("/x").getUri()).hasPath("/ping/x");
        thenLogsIn(CONTAINER).hasFollowingMessage("got pinged");
        thenLogsIn(CONTAINER).hasFollowing(LogLine.message("got pinged")
            .withLevel(INFO)
            .withLogger(Ping.class.getName())
            .withThread("default task-1"));
        thenLogsIn(CONTAINER).thread("default task-1")
            .hasFollowingMessage("got pinged");
    }

    @Test void shouldNotLogTrace() {
        then(ping(CONTAINER)).isEqualTo("default-pong");
        thenLogsIn(CONTAINER).hasNoFollowingMessage("pinged on trace");
    }

    @Test void shouldLogDebug() {
        then(ping(CONTAINER)).isEqualTo("default-pong");
        thenLogsIn(CONTAINER).hasFollowingMessage("pinged on debug");
    }
}
