package test.app;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Slf4j
@Path("/ping")
public class Ping {
    @Inject @ConfigProperty(name = "pong", defaultValue = "default-pong") String pong;

    @GET public String ping() {
        log.info("got pinged");
        log.debug("pinged on debug");
        log.trace("pinged on trace");
        return pong;
    }
}
