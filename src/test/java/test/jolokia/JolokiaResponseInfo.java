package test.jolokia;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data @SuperBuilder @NoArgsConstructor
public class JolokiaResponseInfo {
    String product;
    String vendor;
    String version;
}
