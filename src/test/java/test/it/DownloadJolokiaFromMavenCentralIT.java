package test.it;

import com.github.t1.testcontainers.jee.JeeContainer;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import test.jolokia.JolokiaResponse;

import javax.json.bind.JsonbBuilder;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static test.jolokia.TestData.VERSION;

@WildFly
@Testcontainers
public class DownloadJolokiaFromMavenCentralIT {
    @Container static JeeContainer CONTAINER = JeeContainer.create()
        .withDeployment("https://repo1.maven.org/maven2/org/jolokia/jolokia-war-unsecured/" + VERSION
                        + "/jolokia-war-unsecured-" + VERSION + ".war");

    @Test void shouldGetJolokiaResponse() {
        String string = CONTAINER.target().request(APPLICATION_JSON_TYPE).get(String.class);
        JolokiaResponse response = JsonbBuilder.create().fromJson(string, JolokiaResponse.class);

        response.assertCurrent();
    }
}
