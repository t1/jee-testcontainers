package test;

import lombok.SneakyThrows;

import java.io.IOException;
import java.net.ServerSocket;

import static org.assertj.core.api.BDDAssertions.then;

public class TestTools {
    public static void withSystemProperty(String property, String value, Runnable block) {
        String oldValue = System.getProperty(property);
        System.setProperty(property, value);
        try {
            block.run();
        } finally {
            if (oldValue == null) {
                System.clearProperty(property);
            } else {
                System.setProperty(property, oldValue);
            }
        }
    }

    @SneakyThrows(IOException.class)
    public static int someFreePort() {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            then(serverSocket).isNotNull();
            then(serverSocket.getLocalPort()).isGreaterThan(0);
            return serverSocket.getLocalPort();
        }
    }
}
