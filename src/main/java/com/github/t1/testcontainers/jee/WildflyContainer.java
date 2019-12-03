package com.github.t1.testcontainers.jee;

import org.jetbrains.annotations.NotNull;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;

public class WildflyContainer extends JeeContainer {
    private static final String WAR_DEPLOYED_MESSAGE = ".*WFLYSRV0010.*";

    public WildflyContainer() {
        // TODO use different versions via system properties
        super("quay.io/wildfly/wildfly-centos7");
        waitingFor(new LogMessageWaitStrategy().withRegEx(WAR_DEPLOYED_MESSAGE)); // war deployed message
    }

    @Override protected @NotNull String containerPath() {
        return "/opt/wildfly/standalone/deployments/";
    }
}
