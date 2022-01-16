package com.github.t1.testcontainers.jee;

import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;

public class TomEeContainer extends JeeContainer {
    public TomEeContainer() {this(null);}

    public TomEeContainer(String tag) {
        super(tagged("tomee", tag));
        withContainerDeploymentPath("/usr/local/tomee/webapps/");
        waitingFor(new LogMessageWaitStrategy()
            .withRegEx(".*Deployment of web application archive .* has finished.*"));
    }
}
