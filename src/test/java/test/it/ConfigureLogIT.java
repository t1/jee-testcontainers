package test.it;

import com.github.t1.testcontainers.jee.JeeContainer;
import com.github.t1.testcontainers.tools.LogLine;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import test.app.Ping;

import static jakarta.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.BDDAssertions.then;
import static org.slf4j.event.Level.DEBUG;
import static org.slf4j.event.Level.INFO;
import static test.CustomAssertions.thenLogsIn;
import static test.TestTools.JAKARTA_IMAGE;
import static test.TestTools.pingWar;

@Slf4j
@WildFly
@Testcontainers
public class ConfigureLogIT {

    @Container static JeeContainer PING = JeeContainer.create(JAKARTA_IMAGE)
        .withDeployment(pingWar())
        .withLogLevel(Ping.class, DEBUG);

    @Test void shouldLogInfo() {
        var webTarget = PING.target().path("/ping");

        Response response = webTarget.request().get();

        then(webTarget.getUri()).hasPath("/ping/ping");
        then(response.getStatusInfo()).isEqualTo(OK);
        thenLogsIn(PING).hasFollowingMessage("got pinged");
        thenLogsIn(PING).hasFollowing(LogLine.message("got pinged")
            .withLevel(INFO)
            .withLogger(Ping.class.getName())
            .withThread("default task-1"));
        thenLogsIn(PING).thread("default task-1")
            .hasFollowingMessage("got pinged");
    }

    @Test void shouldNotLogTrace() {
        Response response = PING.target().path("/ping").request().get();

        then(response.getStatusInfo()).isEqualTo(OK);
        thenLogsIn(PING).hasNoFollowingMessage("pinged on trace");
    }

    @Test void shouldLogDebug() {
        Response response = PING.target().path("/ping").request().get();

        then(response.getStatusInfo()).isEqualTo(OK);
        thenLogsIn(PING).hasFollowingMessage("pinged on debug");
    }
}
