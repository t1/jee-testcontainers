package test.app;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("/ping")
public interface PingApi {
    @GET String ping();
}
