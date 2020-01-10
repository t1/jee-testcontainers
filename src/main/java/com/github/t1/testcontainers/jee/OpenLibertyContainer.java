package com.github.t1.testcontainers.jee;

import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.utility.MountableFile;

public class OpenLibertyContainer extends JeeContainer {
    private static final String SERVER_PATH = "/opt/ol/wlp/usr/servers/defaultServer";

    public OpenLibertyContainer() { this(null); }

    public OpenLibertyContainer(String tag) {
        super(tagged("open-liberty", tag));
        setContainerDeploymentPath(SERVER_PATH + "/dropins/");
        addExposedPort(9080);
        withCopyFileToContainer(MountableFile.forClasspathResource("/openliberty_server.xml"), SERVER_PATH + "/server.xml");
        waitingFor(new LogMessageWaitStrategy().withRegEx(".*CWWKZ0001I: Application .* started.*"));
    }
}
