package com.github.t1.testcontainers.tools;

import java.util.Iterator;

@SuppressWarnings("UnusedReturnValue")
public class LogLinesAssert extends AbstractLogLinesAssert<LogLinesAssert> {
    public LogLinesAssert(String actual) {
        super(actual);
    }

    public LogLinesAssert(Iterator<LogLine> actual) {
        super(actual);
    }
}
