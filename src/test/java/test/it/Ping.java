package test.it;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/ping")
public class Ping {
    @Inject @ConfigProperty(name = "pong", defaultValue = "default-pong") String pong;

    @GET public String ping() {return pong;}
}
