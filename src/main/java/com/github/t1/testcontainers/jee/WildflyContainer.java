package com.github.t1.testcontainers.jee;

import com.github.dockerjava.api.command.InspectContainerResponse;
import lombok.SneakyThrows;
import org.slf4j.event.Level;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;

public class WildflyContainer extends JeeContainer {
    private static final String WAR_DEPLOYED_MESSAGE = ".*WFLYSRV0010.*";

    private final List<String> cli = new ArrayList<>();

    public WildflyContainer() {this((String) null);}

    public WildflyContainer(String tag) {this("rdohna/wildfly", tag);}

    public WildflyContainer(String image, String tag) {this(DockerImageName.parse(tagged(image, tag)));}

    public WildflyContainer(DockerImageName dockerImageName) {
        super(withRegistry(dockerImageName));
        withContainerDeploymentPath("/opt/jboss/wildfly/standalone/deployments/");
        waitingFor(new LogMessageWaitStrategy().withRegEx(WAR_DEPLOYED_MESSAGE));
    }

    private static DockerImageName withRegistry(DockerImageName dockerImageName) {
        if (!dockerImageName.getUnversionedPart().contains("/")) dockerImageName = dockerImageName.withRegistry("rdohna");
        return dockerImageName;
    }

    @Override public JeeContainer withLogLevel(String loggerName, Level level) {
        withCli("/subsystem=logging/logger=" + loggerName + ":add(level=" + level + ")");
        return self();
    }

    public void withCli(String command) {
        cli.add(command);
    }

    @Override protected void containerIsStarted(InspectContainerResponse containerInfo) {
        super.containerIsStarted(containerInfo);
        execCli();
    }

    @SneakyThrows({IOException.class, InterruptedException.class})
    private void execCli() {
        Instant start = Instant.now();
        String script = String.join("\n", cli);
        String containerPath = "/tmp/" + UUID.randomUUID() + ".cli";
        copyFileToContainer(Transferable.of(script.getBytes(UTF_8)), containerPath);
        execInContainer("bin/jboss-cli.sh", "--connect", "--file=" + containerPath);
        logger().debug("cli took {}", Duration.between(start, Instant.now()));
    }

    @Override public String webContext() {
        String webContext = super.webContext();
        return "ROOT".equals(webContext) ? "" : webContext;
    }
}
