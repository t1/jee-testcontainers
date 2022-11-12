package com.github.t1.testcontainers.jee;

import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.utility.DockerImageName;

public class TomEeContainer extends JeeContainer {
    public static TomEeContainer create() {return create(null);}

    public static TomEeContainer create(String tag) {return create("tomee", tag);}

    public static TomEeContainer create(String image, String tag) {return new TomEeContainer(DockerImageName.parse(tagged(image, tag)));}

    TomEeContainer(DockerImageName dockerImageName) {
        super(dockerImageName);
        withContainerDeploymentPath("/usr/local/tomee/webapps/");
    }

    @Override protected void waitUntilContainerStarted() {
        if (getWaitStrategy() == null) {
            // we don't set this in the constructor, as we need the containerPath to distinguish our deployment from the built-in apps.
            waitingFor(new LogMessageWaitStrategy()
                // the final . is required to also match the newline
                .withRegEx("Deployment of web application (archive|directory) \\[" + containerPath() + "\\] has finished in \\[\\d\\] ms."));
        }
        super.waitUntilContainerStarted();
    }
}
