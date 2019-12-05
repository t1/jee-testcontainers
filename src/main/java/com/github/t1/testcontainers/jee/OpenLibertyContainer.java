package com.github.t1.testcontainers.jee;

import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;

public class OpenLibertyContainer extends JeeContainer {
    private static final String SERVER_PATH = "/opt/ol/wlp/usr/servers/defaultServer";
    public static final String DROPINS_PATH = SERVER_PATH + "/dropins/";

    public OpenLibertyContainer() { this(null); }

    public OpenLibertyContainer(String tag) {
        super(tagged("open-liberty", tag));
        setContainerDeploymentPath(DROPINS_PATH);
        addExposedPort(9080);
        waitingFor(new LogMessageWaitStrategy().withRegEx(".*CWWKZ0001I: Application .* started.*"));
    }
}
