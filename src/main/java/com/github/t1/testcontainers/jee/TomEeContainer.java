package com.github.t1.testcontainers.jee;

import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.utility.DockerImageName;

public class TomEeContainer extends JeeContainer {
    public TomEeContainer() {this((String) null);}

    public TomEeContainer(String tag) {
        this(DockerImageName.parse(tagged("tomee", tag)));
    }

    public TomEeContainer(DockerImageName dockerImageName) {
        super(dockerImageName);
        withContainerDeploymentPath("/usr/local/tomee/webapps/");
        waitingFor(new LogMessageWaitStrategy()
            .withRegEx(".*Deployment of web application archive .* has finished.*"));
    }
}
