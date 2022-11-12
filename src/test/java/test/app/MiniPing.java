package test.app;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("/ping")
public class MiniPing {
    @GET public String ping() {
        return "mini-pong";
    }
}
