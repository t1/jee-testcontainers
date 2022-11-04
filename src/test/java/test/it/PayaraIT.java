package test.it;

import com.github.t1.testcontainers.jee.JeeContainer;
import com.github.t1.testcontainers.jee.PayaraContainer;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import test.jolokia.JolokiaResponse;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static test.TestTools.JSONB;
import static test.jolokia.TestData.VERSION;

@Payara
@Testcontainers
public class PayaraIT {
    @Container static JeeContainer CONTAINER = PayaraContainer.create()
        .withDeployment("urn:mvn:org.jolokia:jolokia-war-unsecured:" + VERSION + ":war");

    @Test void shouldGetJolokiaResponse() {
        String string = CONTAINER.target().request(APPLICATION_JSON_TYPE).get(String.class);

        JolokiaResponse response = JSONB.fromJson(string, JolokiaResponse.class);

        response.assertCurrent();
    }
}
