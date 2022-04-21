package com.github.t1.testcontainers.jee;

import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.utility.DockerImageName;

public class PayaraContainer extends JeeContainer {
    public static PayaraContainer create() {return create(null);}

    public static PayaraContainer create(String tag) {return create("payara/server-full", tag);}

    public static PayaraContainer create(String image, String tag) {return new PayaraContainer(DockerImageName.parse(tagged(image, tag)));}

    /** use {@link #create()} instead */
    @Deprecated
    public PayaraContainer() {this((String) null);}

    /** use {@link #create()} instead */
    @Deprecated
    public PayaraContainer(String tag) {
        this(DockerImageName.parse(tagged("payara/server-full", tag)));
    }

    public PayaraContainer(DockerImageName dockerImageName) {
        super(withName(dockerImageName));
        withContainerDeploymentPath("/opt/payara/deployments/");
        waitingFor(new LogMessageWaitStrategy().withRegEx(".*was successfully deployed in [0-9,]{1,10} milliseconds.*"));
    }

    private static DockerImageName withName(DockerImageName dockerImageName) {
        if (!dockerImageName.getUnversionedPart().contains("/"))
            dockerImageName = dockerImageName.withRepository(dockerImageName.getUnversionedPart() + "/server-full");
        return dockerImageName;
    }
}
