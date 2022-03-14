package com.github.t1.testcontainers.jee;

import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.utility.DockerImageName;

public class WildflyContainer extends JeeContainer {
    private static final String WAR_DEPLOYED_MESSAGE = ".*WFLYSRV0010.*";

    public WildflyContainer() {this((String) null);}

    public WildflyContainer(String tag) {this("rdohna/wildfly", tag);}

    public WildflyContainer(String image, String tag) {this(DockerImageName.parse(tagged(image, tag)));}

    public WildflyContainer(DockerImageName dockerImageName) {
        super(withRegistry(dockerImageName));
        withContainerDeploymentPath("/opt/jboss/wildfly/standalone/deployments/");
        waitingFor(new LogMessageWaitStrategy().withRegEx(WAR_DEPLOYED_MESSAGE));
    }

    private static DockerImageName withRegistry(DockerImageName dockerImageName) {
        if (!dockerImageName.getUnversionedPart().contains("/")) dockerImageName = dockerImageName.withRegistry("rdohna");
        return dockerImageName;
    }

    @Override public String webContext() {
        String webContext = super.webContext();
        return "ROOT".equals(webContext) ? "" : webContext;
    }
}
