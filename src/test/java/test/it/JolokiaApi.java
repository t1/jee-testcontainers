package test.it;

import javax.ws.rs.GET;

public interface JolokiaApi {
    // Jolokia returns Content-Type `text/plain`, even when it's json :-(
    @GET String get();
}
