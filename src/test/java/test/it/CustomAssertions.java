package test.it;

import com.github.t1.testcontainers.jee.JeeContainer;
import com.github.t1.testcontainers.tools.LogLinesAssert;
import org.assertj.core.api.BDDAssertions;

import static com.github.t1.testcontainers.tools.LogLine.parseLines;

public class CustomAssertions extends BDDAssertions {
    public static LogLinesAssert<?, ?> thenLogsIn(JeeContainer container) {
        return new LogLinesAssert<>(parseLines(container.getLogs()));
    }
}
