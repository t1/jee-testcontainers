package com.github.t1.testcontainers.jee;

import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;

public class PayaraContainer extends JeeContainer {
    public PayaraContainer() { this(null); }

    public PayaraContainer(String tag) {
        super(tagged("payara/server-full", tag));
        withContainerDeploymentPath("/opt/payara/deployments/");
        addExposedPort(8080);
        waitingFor(new LogMessageWaitStrategy().withRegEx(".*was successfully deployed in [0-9,]{1,10} milliseconds.*"));
    }
}
