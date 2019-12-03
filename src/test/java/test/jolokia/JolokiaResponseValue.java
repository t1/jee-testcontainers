package test.jolokia;

import lombok.Data;

@Data
public class JolokiaResponseValue {
    String agent;
    String protocol;
    JolokiaResponseInfo info;
}
