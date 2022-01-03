package test.it;

import com.github.t1.testcontainers.jee.JeeContainer;
import com.github.t1.testcontainers.jee.TomEeContainer;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import test.jolokia.JolokiaResponse;

import javax.json.bind.JsonbBuilder;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static org.assertj.core.api.Assertions.assertThat;
import static test.jolokia.TestData.VERSION;

@TomEE
@Testcontainers
public class TomEeIT {
    @Container static JeeContainer CONTAINER = new TomEeContainer()
        .withDeployment("urn:mvn:org.jolokia:jolokia-war-unsecured:" + VERSION + ":war");

    @Test void shouldGetJolokiaResponse() {
        String string = CONTAINER.target().request(APPLICATION_JSON_TYPE).get(String.class);

        JolokiaResponse response = JsonbBuilder.create().fromJson(string, JolokiaResponse.class);
        response.assertCurrent();
        assertThat(response.getValue().getInfo().getProduct()).isEqualTo("tomcat");
        assertThat(response.getValue().getInfo().getVendor()).isEqualTo("Apache");
    }
}
