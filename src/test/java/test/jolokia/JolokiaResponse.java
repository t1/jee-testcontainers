package test.jolokia;

import lombok.Data;

import static org.assertj.core.api.BDDAssertions.then;
import static test.jolokia.TestData.VERSION;

@Data
public class JolokiaResponse {
    JolokiaResponseRequest request;
    JolokiaResponseValue value;

    public void assertCurrent() {
        then(request).isEqualTo(JolokiaResponseRequest.builder().type("version").build());
        then(value).isNotNull();
        then(value.getAgent()).isEqualTo(VERSION);
        then(value.getProtocol()).isEqualTo("7.2");
    }
}
