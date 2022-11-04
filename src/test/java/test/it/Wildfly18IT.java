package test.it;

import com.github.t1.testcontainers.jee.JeeContainer;
import com.github.t1.testcontainers.jee.WildflyContainer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import test.jolokia.JolokiaApi;
import test.jolokia.JolokiaResponse;

import static org.assertj.core.api.BDDAssertions.then;
import static test.TestTools.JSONB;
import static test.jolokia.TestData.VERSION;

@WildFly
@DisabledIfSystemProperty(named = "os.arch", matches = "aarch64", disabledReason =
    "jboss/wildfly currently doesn't support arm64, and the emulation on M1 is too slow and brittle. " +
    "see https://github.com/jboss-dockerfiles/wildfly/issues/155")
@Testcontainers
public class Wildfly18IT {
    private static final String WILDFLY_VERSION = "18.0.1.Final";

    @Container static JeeContainer CONTAINER = WildflyContainer.create("jboss/wildfly", WILDFLY_VERSION)
        .withDeployment("urn:mvn:org.jolokia:jolokia-war-unsecured:" + VERSION + ":war");

    @Test void shouldGetJolokiaResponse() {
        JolokiaApi jolokia = CONTAINER.restClient(JolokiaApi.class);

        JolokiaResponse response = JSONB.fromJson(jolokia.get(), JolokiaResponse.class);

        response.assertCurrent();
        then(response.getValue().getInfo().getProduct()).isEqualTo("WildFly Full");
        then(response.getValue().getInfo().getVendor()).isEqualTo("RedHat");
        then(response.getValue().getInfo().getVersion()).isEqualTo(WILDFLY_VERSION);
    }
}
