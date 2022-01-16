package test.jolokia;

import lombok.Data;

import static org.assertj.core.api.BDDAssertions.then;
import static test.jolokia.TestData.VERSION;

@Data
public class JolokiaResponse {
    JolokiaResponseValue value;

    public void assertCurrent() {
        then(value.getAgent()).isEqualTo(VERSION);
        then(value.getProtocol()).isEqualTo("7.2");
    }
}
