package test.it;

import com.github.t1.testcontainers.jee.JeeContainer;
import com.github.t1.testcontainers.jee.WildflyContainer;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import test.jolokia.JolokiaResponse;

import javax.json.bind.JsonbBuilder;

import static org.assertj.core.api.BDDAssertions.then;
import static test.jolokia.TestData.VERSION;

@WildFly
@Testcontainers
public class WildflyIT {
    @Container static JeeContainer CONTAINER = new WildflyContainer()
        .withDeployment("urn:mvn:org.jolokia:jolokia-war-unsecured:" + VERSION + ":war");

    @Test void shouldGetJolokiaResponse() {
        JolokiaApi jolokia = CONTAINER.restClient(JolokiaApi.class);

        JolokiaResponse response = JsonbBuilder.create().fromJson(jolokia.get(), JolokiaResponse.class);

        response.assertCurrent();
        then(response.getValue().getInfo().getProduct()).isEqualTo("WildFly Full");
        then(response.getValue().getInfo().getVendor()).isEqualTo("RedHat");
    }
}
