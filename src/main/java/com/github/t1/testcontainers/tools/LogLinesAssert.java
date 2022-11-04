package com.github.t1.testcontainers.tools;

import org.assertj.core.api.AbstractAssert;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.github.t1.testcontainers.tools.LogLine.parseLines;

@SuppressWarnings("UnusedReturnValue")
public class LogLinesAssert<SELF extends LogLinesAssert<SELF, ACTUAL>, ACTUAL extends Iterator<LogLine>>
    extends AbstractAssert<SELF, ACTUAL> {
    @SuppressWarnings("unchecked")
    public LogLinesAssert(String actual) {this((ACTUAL) parseLines(actual));}

    public LogLinesAssert(ACTUAL actual) {this(actual, LogLinesAssert.class);}

    protected LogLinesAssert(ACTUAL actual, Class<?> selfType) {super(actual, selfType);}

    public LogLinesAssert<SELF, ACTUAL> hasFollowingMessage(String expected) {
        return hasFollowing(LogLine.message(expected));
    }

    public LogLinesAssert<SELF, ACTUAL> hasFollowing(LogLine expected) {
        while (actual.hasNext()) {
            LogLine next = actual.next();
            if (next.matches(expected)) return this;
        }
        throw failure("expected logs to contain <%s>", expected);
    }

    public LogLinesAssert<SELF, ACTUAL> hasNoFollowingMessage(String expected) {
        return hasNoFollowing(LogLine.message(expected));
    }

    public LogLinesAssert<SELF, ACTUAL> hasNoFollowing(LogLine expected) {
        while (actual.hasNext()) {
            LogLine next = actual.next();
            if (next.matches(expected)) {
                throw failure("expected logs to NOT contain <%s>", expected);
            }
        }
        return this;
    }

    public LogLinesAssert<SELF, ACTUAL> thread(String thread) {
        List<LogLine> filtered = new ArrayList<>();
        while (actual.hasNext()) {
            LogLine next = actual.next();
            if (thread.equals(next.getThread())) filtered.add(next);
        }
        @SuppressWarnings("unchecked")
        var iterator = (ACTUAL) filtered.iterator();
        return new LogLinesAssert<>(iterator);
    }
}
