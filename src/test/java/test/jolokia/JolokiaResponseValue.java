package test.jolokia;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data @SuperBuilder @NoArgsConstructor
public class JolokiaResponseValue {
    String agent;
    String protocol;
    JolokiaResponseInfo info;
}
