package com.github.t1.testcontainers.jee;

import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.utility.DockerImageName;

public class TomEeContainer extends JeeContainer {
    public static TomEeContainer create() {return create(null);}

    public static TomEeContainer create(String tag) {return create("tomee", tag);}

    public static TomEeContainer create(String image, String tag) {return new TomEeContainer(DockerImageName.parse(tagged(image, tag)));}

    /** use {@link #create()} instead */
    @Deprecated
    public TomEeContainer() {this((String) null);}

    /** use {@link #create()} instead */
    @Deprecated
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
