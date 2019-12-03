package test.jolokia;

import lombok.Data;

import static org.assertj.core.api.Assertions.assertThat;

@Data
public class JolokiaResponse {
    JolokiaResponseValue value;

    public void assertCurrent() {
        assertThat(value.getAgent()).isEqualTo(JolokiaData.VERSION);
        assertThat(value.getProtocol()).isEqualTo("7.2");
        assertThat(value.getInfo().getProduct()).isEqualTo("WildFly Full");
        assertThat(value.getInfo().getVendor()).isEqualTo("RedHat");
    }
}
