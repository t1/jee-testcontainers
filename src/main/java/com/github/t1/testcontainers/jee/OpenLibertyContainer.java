package com.github.t1.testcontainers.jee;

import lombok.SneakyThrows;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.utility.MountableFile;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.APPEND;

public class OpenLibertyContainer extends JeeContainer {
    private static final String SERVER_PATH = "/opt/ol/wlp/usr/servers/defaultServer";
    public static final String APPS_PATH = SERVER_PATH + "/apps/";

    private final Path serverXml;

    @SneakyThrows(IOException.class)
    public OpenLibertyContainer() {
        super("open-liberty");
        setContainerDeploymentPath(APPS_PATH);
        addExposedPort(9080);
        serverXml = Files.createTempDirectory(null).resolve("server.xml");
        withCopyFileToContainer(MountableFile.forHostPath(serverXml), SERVER_PATH + "/server.xml");
        waitingFor(new LogMessageWaitStrategy().withRegEx(".*CWWKZ0001I: Application .* started.*"));
    }

    @SneakyThrows(IOException.class)
    @Override public JeeContainer withDeployment(URI deployable) {
        super.withDeployment(deployable);
        Files.copy(OpenLibertyContainer.class.getResourceAsStream("/openliberty_server.xml"), serverXml);
        Files.write(serverXml, ("    <application"
            + " id=\"app_war\""
            + " type=\"war\""
            + " location=\"" + containerPath() + "\""
            + " context-root=\"/" + webContext() + "\" />\n"
            + "</server>\n").getBytes(UTF_8), APPEND);
        return self();
    }
}
