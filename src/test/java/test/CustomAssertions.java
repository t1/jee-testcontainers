package test;

import com.github.t1.testcontainers.jee.JeeContainer;
import com.github.t1.testcontainers.tools.LogLinesAssert;
import org.assertj.core.api.BDDAssertions;

public class CustomAssertions extends BDDAssertions {
    public static LogLinesAssert thenLogsIn(JeeContainer container) {
        return new LogLinesAssert(container.getLogs());
    }
}
