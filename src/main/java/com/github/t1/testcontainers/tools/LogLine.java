package com.github.t1.testcontainers.tools;

import lombok.Builder;
import lombok.Value;
import lombok.With;
import org.slf4j.event.Level;

import java.time.LocalTime;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Value @With @Builder
public class LogLine {
    public static Iterator<LogLine> parseLines(String logs) {
        return Stream.of(logs.split("\n")).map(LogLine::parseLine).collect(toList()).iterator();
    }

    private static LogLine parseLine(String line) {
        Matcher matcher = LOG.matcher(line);
        if (matcher.matches()) {
            return LogLine.builder()
                .timestamp(LocalTime.parse(matcher.group("timestamp").replace(',', '.')))
                .level(Level.valueOf(matcher.group("level").trim()))
                .logger(matcher.group("logger"))
                .thread(matcher.group("thread"))
                .message(matcher.group("message"))
                .build();
        } else {
            return message(line);
        }
    }

    public static LogLine message(String message) {
        return LogLine.builder().message(message).build();
    }

    LocalTime timestamp;
    Level level;
    String logger;
    String thread;
    String message;

    @Override public String toString() {
        return
            (timestamp == null ? "" : timestamp + " ") +
            (level == null ? "" : level + " ") +
            (logger == null ? "" : "[" + logger + "] ") +
            (thread == null ? "" : "(" + thread + ") ") +
            (message == null ? "" : message);
    }

    public boolean matches(LogLine that) {
        return
            (that.timestamp == null || that.timestamp.equals(this.timestamp)) &&
            (that.level == null || that.level.equals(this.level)) &&
            (that.logger == null || that.logger.equals(this.logger)) &&
            (that.thread == null || that.thread.equals(this.thread)) &&
            (that.message == null || that.message.equals(this.message));
    }

    private static final String TIMESTAMP = "(?<timestamp>\\d\\d:\\d\\d:\\d\\d,\\d\\d\\d)";
    private static final String LEVEL = "(?<level>TRACE|DEBUG|INFO |WARN |ERROR)";
    private static final String LOGGER = "\\[(?<logger>.*?)]";
    private static final String THREAD = "\\((?<thread>.*?)\\)";
    private static final String MESSAGE = "(?<message>.*)";
    private static final Pattern LOG = Pattern.compile(TIMESTAMP + " " + LEVEL + " " + LOGGER + " " + THREAD + " " + MESSAGE);
}
