package test.it;

import com.github.t1.testcontainers.jee.JeeContainer;
import com.github.t1.testcontainers.jee.WildflyContainer;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import test.jolokia.JolokiaData;
import test.jolokia.JolokiaResponse;

import javax.json.bind.JsonbBuilder;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
public class Wildfly18IT {
    @Container static JeeContainer CONTAINER = new WildflyContainer("18.0")
        .withDeployment("urn:mvn:org.jolokia:jolokia-war-unsecured:" + JolokiaData.VERSION + ":war");

    @Test void shouldGetJolokiaResponse() {
        String string = CONTAINER.target().request(APPLICATION_JSON_TYPE).get(String.class);

        JolokiaResponse response = JsonbBuilder.create().fromJson(string, JolokiaResponse.class);
        response.assertCurrent();
        assertThat(response.getValue().getInfo().getProduct()).isEqualTo("WildFly Full");
        assertThat(response.getValue().getInfo().getVendor()).isEqualTo("RedHat");
        assertThat(response.getValue().getInfo().getVersion()).isEqualTo("18.0.0.Final");
    }
}
