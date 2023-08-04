package com.github.t1.testcontainers.tools;

import org.assertj.core.api.AbstractAssert;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.github.t1.testcontainers.tools.LogLine.parseLines;

@SuppressWarnings("UnusedReturnValue")
abstract public class AbstractLogLinesAssert<SELF extends AbstractLogLinesAssert<SELF>>
        extends AbstractAssert<SELF, Iterator<LogLine>> {
    public AbstractLogLinesAssert(String actual) {this(parseLines(actual));}

    public AbstractLogLinesAssert(Iterator<LogLine> actual) {this(actual, AbstractLogLinesAssert.class);}

    protected AbstractLogLinesAssert(Iterator<LogLine> actual, Class<?> selfType) {super(actual, selfType);}

    public AbstractLogLinesAssert<SELF> hasFollowingMessage(String expected) {
        return hasFollowing(LogLine.message(expected));
    }

    public AbstractLogLinesAssert<SELF> hasFollowing(LogLine expected) {
        while (actual.hasNext()) {
            LogLine next = actual.next();
            if (next.matches(expected)) return this;
        }
        throw failure("expected logs to contain <%s>", expected);
    }

    public AbstractLogLinesAssert<SELF> hasNoFollowingMessage(String expected) {
        return hasNoFollowing(LogLine.message(expected));
    }

    public AbstractLogLinesAssert<SELF> hasNoFollowing(LogLine expected) {
        while (actual.hasNext()) {
            LogLine next = actual.next();
            if (next.matches(expected)) {
                throw failure("expected logs to NOT contain <%s>", expected);
            }
        }
        return this;
    }

    public LogLinesAssert thread(String thread) {
        List<LogLine> filtered = new ArrayList<>();
        while (actual.hasNext()) {
            LogLine next = actual.next();
            if (thread.equals(next.getThread())) filtered.add(next);
        }
        var iterator = filtered.iterator();
        return new LogLinesAssert(iterator);
    }
}
