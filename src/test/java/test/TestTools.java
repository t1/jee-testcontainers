package test;

import com.github.t1.testcontainers.tools.DeployableBuilder;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import lombok.SneakyThrows;
import org.junit.jupiter.api.function.Executable;
import org.testcontainers.containers.GenericContainer;
import test.app.Ping;
import test.app.REST;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.util.List;

import static com.github.t1.testcontainers.tools.DeployableBuilder.war;
import static org.assertj.core.api.BDDAssertions.then;

public class TestTools {
    public static final Jsonb JSONB = JsonbBuilder.create();
    public static final String WILDFLY_JAKARTA_VERSION = "27.0.0.Final-jdk17";
    public static final String JAKARTA_IMAGE = "wildfly:" + WILDFLY_JAKARTA_VERSION;

    public static void withSystemProperty(String property, String value, Executable block) {
        String oldValue = System.getProperty(property);
        System.setProperty(property, value);
        try {
            block.execute();
        } catch (Error | RuntimeException e) {
            throw e;
        } catch (Throwable e) {
            throw new RuntimeException(e);
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

    @SneakyThrows(ReflectiveOperationException.class)
    public static List<String> portBindings(GenericContainer<?> container) {
        Field portBindings = GenericContainer.class.getDeclaredField("portBindings");
        portBindings.setAccessible(true);
        @SuppressWarnings("unchecked")
        var strings = (List<String>) portBindings.get(container);
        return strings;
    }

    public static DeployableBuilder pingWar() {
        return war("ping").withClasses(Ping.class, REST.class);
    }
}
