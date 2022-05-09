package com.github.t1.testcontainers.jee;

import com.github.dockerjava.api.command.InspectContainerResponse;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.event.Level;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import static com.github.t1.testcontainers.tools.DeployableBuilder.dbJndiName;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.joining;

public class WildflyContainer extends JeeContainer {
    public static WildflyContainer create() {return create(null);}

    public static WildflyContainer create(String tag) {return create("rdohna/wildfly", tag);}

    public static WildflyContainer create(String image, String tag) {return new WildflyContainer(DockerImageName.parse(tagged(image, tag)));}


    private static final String WAR_DEPLOYED_MESSAGE = ".*WFLYSRV0010.*";

    private final List<Supplier<String>> cli = new ArrayList<>();

    /** use {@link #create()} instead */
    @Deprecated
    public WildflyContainer() {this((String) null);}

    /** use {@link #create(String)} instead */
    @Deprecated
    public WildflyContainer(String tag) {this("rdohna/wildfly", tag);}

    /** use {@link #create(String, String)} instead */
    @Deprecated
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

    @Override public WildflyContainer withLogLevel(String loggerName, Level level) {
        return withCli("/subsystem=logging/logger=" + loggerName.replace("$", "\\$") + ":add(level=" + level + ")");
    }

    @Override public WildflyContainer withDataSource(JdbcDatabaseContainer<?> db) {
        this.dependsOn(db);
        String name = db.getDatabaseName();
        cli.add(() -> "/subsystem=datasources/data-source=" + name + ":add(" +
                      "jndi-name=" + dbJndiName(name) + ", " +
                      "connection-url=\"" + getJdbcUrl(db) + "\"," +
                      "driver-name=" + driver(db) + ", " +
                      "check-valid-connection-sql=\"SELECT 1\", " +
                      "user-name=" + db.getUsername() + ", " +
                      "password=" + db.getPassword() + ")");
        return this;
    }

    private String getJdbcUrl(JdbcDatabaseContainer<?> db) {
        String alias = db.getNetworkAliases().get(db.getNetworkAliases().size() - 1);
        Integer exposedPort = db.getExposedPorts().get(0);
        return db.getJdbcUrl().replaceFirst(db.getHost() + ":" + db.getFirstMappedPort(), alias + ":" + exposedPort);
    }

    private String driver(JdbcDatabaseContainer<?> db) {
        //noinspection SwitchStatementWithTooFewBranches
        switch (db.getDriverClassName()) {
            case "org.postgresql.Driver":
                return "postgresql";
            default:
                throw new RuntimeException("unknown driver class name: " + db.getDriverClassName());
        }
    }

    public WildflyContainer withCli(String command) {
        cli.add(() -> command);
        return this;
    }

    @Override protected void containerIsStarted(InspectContainerResponse containerInfo) {
        super.containerIsStarted(containerInfo);
        execCli();
    }

    @SneakyThrows({IOException.class, InterruptedException.class})
    private void execCli() {
        if (cli.isEmpty()) return;
        Instant start = Instant.now();
        String script = cli.stream().map(Supplier::get).collect(joining("\n"));
        String containerPath = "/tmp/" + UUID.randomUUID() + ".cli";
        copyFileToContainer(Transferable.of(script.getBytes(UTF_8)), containerPath);

        ExecResult execResult = execInContainer("bin/jboss-cli.sh", "--connect", "--file=" + containerPath);

        Logger logger = logger();
        logger.debug("start cli: {}", script);
        if (!execResult.getStdout().isEmpty()) logger.debug(execResult.getStdout());
        if (!execResult.getStderr().isEmpty()) logger.debug(execResult.getStderr());
        logger.debug("cli took {}", Duration.between(start, Instant.now()));
        if (execResult.getExitCode() != 0) {
            throw new RuntimeException("cli failed [" + execResult.getExitCode() + "]");
        }
    }

    @Override public String webContext() {
        String webContext = super.webContext();
        return "ROOT".equals(webContext) ? "" : webContext;
    }
}
