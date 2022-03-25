package test.it;

import com.github.t1.testcontainers.jee.JeeContainer;
import com.github.t1.testcontainers.tools.DeployableBuilder;
import com.github.t1.testcontainers.tools.LogLine;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.ws.rs.core.Response;

import static com.github.t1.testcontainers.tools.DeployableBuilder.war;
import static javax.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.BDDAssertions.then;
import static org.slf4j.event.Level.DEBUG;
import static org.slf4j.event.Level.INFO;
import static test.it.CustomAssertions.thenLogsIn;

@Slf4j
@WildFly
@Testcontainers
public class ConfigureLogIT {
    static DeployableBuilder buildPing() {
        return war("ping").withClasses(Ping.class, REST.class);
    }


    @Container static JeeContainer PING = JeeContainer.create()
        .withDeployment(buildPing())
        .withLogLevel(Ping.class, DEBUG);

    @Test void shouldLogInfo() {
        Response response = PING.target().path("/ping").request().get();

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
