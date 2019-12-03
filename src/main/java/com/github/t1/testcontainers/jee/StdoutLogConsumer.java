package com.github.t1.testcontainers.jee;

import org.testcontainers.containers.output.OutputFrame;

import java.util.function.Consumer;

import static org.testcontainers.containers.output.OutputFrame.OutputType.STDOUT;

/**
 * A simple consumer for Testcontainers logs, printing the container's STDOUT in green and STDERR in blue,
 * both to the JVM <code>stdout</code>.
 */
public class StdoutLogConsumer implements Consumer<OutputFrame> {
    @Override public void accept(OutputFrame outputFrame) {
        String color = (outputFrame.getType() == STDOUT) ? GREEN : BLUE;
        System.out.print(color + outputFrame.getUtf8String() + RESET);
    }

    private static final String RESET = "\u001B[0m";
    private static final String BLUE = "\u001B[34m";
    private static final String GREEN = "\u001B[32m";
}
