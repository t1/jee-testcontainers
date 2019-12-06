package test.jolokia;

import lombok.Data;

import static org.assertj.core.api.Assertions.assertThat;
import static test.jolokia.TestData.VERSION;

@Data
public class JolokiaResponse {
    JolokiaResponseValue value;

    public void assertCurrent() {
        assertThat(value.getAgent()).isEqualTo(VERSION);
        assertThat(value.getProtocol()).isEqualTo("7.2");
    }
}
