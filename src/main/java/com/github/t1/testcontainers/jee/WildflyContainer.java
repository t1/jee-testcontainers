package com.github.t1.testcontainers.jee;

import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;

public class WildflyContainer extends JeeContainer {
    private static final String WAR_DEPLOYED_MESSAGE = ".*WFLYSRV0010.*";

    public WildflyContainer() {this(null);}

    public WildflyContainer(String tag) {this("jboss/wildfly", tag);}

    public WildflyContainer(String image, String tag) {
        super(tagged(image, tag));
        withContainerDeploymentPath("/opt/jboss/wildfly/standalone/deployments/");
        waitingFor(new LogMessageWaitStrategy().withRegEx(WAR_DEPLOYED_MESSAGE));
    }
}
