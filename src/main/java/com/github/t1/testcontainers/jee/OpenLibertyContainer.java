package com.github.t1.testcontainers.jee;

import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;

public class OpenLibertyContainer extends JeeContainer {
    public OpenLibertyContainer() { this(null); }

    public OpenLibertyContainer(String tag) {
        super(tagged("open-liberty", tag));
        setContainerDeploymentPath("/opt/ol/wlp/usr/servers/defaultServer/dropins/");
        addExposedPort(9080);
        waitingFor(new LogMessageWaitStrategy().withRegEx(".*CWWKZ0001I: Application .* started.*"));
    }
}
