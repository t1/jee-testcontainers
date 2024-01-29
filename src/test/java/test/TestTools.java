package test;

import com.github.t1.testcontainers.jee.JeeContainer;
import com.github.t1.testcontainers.tools.DeployableBuilder;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import lombok.SneakyThrows;
import org.junit.jupiter.api.function.Executable;
import test.app.PingApi;
import test.app.REST;

import java.io.IOException;
import java.net.ServerSocket;

import static java.util.Locale.ROOT;
import static org.assertj.core.api.BDDAssertions.then;

public class TestTools {
    public static final Jsonb JSONB = JsonbBuilder.create();

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
            then(serverSocket.getLocalPort()).isGreaterThan(0);
            return serverSocket.getLocalPort();
        }
    }

    public static DeployableBuilder war(Class<?> mainClass) {
        return DeployableBuilder
                .war(mainClass.getSimpleName().toLowerCase(ROOT))
                .withClasses(mainClass, REST.class);
    }

    public static String ping(JeeContainer container) {
        var api = container.restClient(PingApi.class);

        return api.ping();
    }
}
