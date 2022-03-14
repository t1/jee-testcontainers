package com.github.t1.testcontainers.jee;

import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

public class OpenLibertyContainer extends JeeContainer {
    private static final String SERVER_PATH = "/opt/ol/wlp/usr/servers/defaultServer";

    public OpenLibertyContainer() {this((String) null);}

    public OpenLibertyContainer(String tag) {
        this(DockerImageName.parse(tagged("open-liberty", tag)));
    }

    public OpenLibertyContainer(DockerImageName dockerImageName) {
        super(dockerImageName);
        withContainerDeploymentPath(SERVER_PATH + "/dropins/");
        withCopyFileToContainer(MountableFile.forClasspathResource("/openliberty_server.xml"), SERVER_PATH + "/server.xml");
        waitingFor(new LogMessageWaitStrategy().withRegEx(".*CWWKZ0001I: Application .* started.*"));
    }
}
