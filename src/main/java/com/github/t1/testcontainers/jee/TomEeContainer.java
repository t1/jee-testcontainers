package com.github.t1.testcontainers.jee;

import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;

public class TomEeContainer extends JeeContainer {
    public TomEeContainer() { this(null); }

    public TomEeContainer(String tag) {
        super(tagged("tomee", tag));
        setContainerDeploymentPath("/usr/local/tomee/webapps/");
        addExposedPort(8080);
        waitingFor(new LogMessageWaitStrategy()
            .withRegEx(".*Deployment of web application archive .* has finished.*"));
    }
}
