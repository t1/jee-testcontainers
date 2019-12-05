package com.github.t1.testcontainers.jee;

import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;

public class WildflyContainer extends JeeContainer {
    private static final String WAR_DEPLOYED_MESSAGE = ".*WFLYSRV0010.*";

    public WildflyContainer() {
        super("quay.io/wildfly/wildfly-centos7");
        setContainerDeploymentPath("/opt/wildfly/standalone/deployments/");
        addExposedPort(8080);
        waitingFor(new LogMessageWaitStrategy().withRegEx(WAR_DEPLOYED_MESSAGE));
    }
}
