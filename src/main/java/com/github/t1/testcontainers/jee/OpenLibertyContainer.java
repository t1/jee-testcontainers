package com.github.t1.testcontainers.jee;

import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

public class OpenLibertyContainer extends JeeContainer {
    public static OpenLibertyContainer create() {return create(null);}

    public static OpenLibertyContainer create(String tag) {return create("open-liberty", tag);}

    public static OpenLibertyContainer create(String image, String tag) {return new OpenLibertyContainer(DockerImageName.parse(tagged(image, tag)));}

    private static final String SERVER_PATH = "/opt/ol/wlp/usr/servers/defaultServer";

    /** use {@link #create()} instead */
    @Deprecated
    public OpenLibertyContainer() {this((String) null);}

    /** use {@link #create()} instead */
    @Deprecated
    public OpenLibertyContainer(String tag) {
        this(DockerImageName.parse(tagged("open-liberty", tag)));
    }

    public OpenLibertyContainer(DockerImageName dockerImageName) {
        super(dockerImageName);
        withContainerDeploymentPath(SERVER_PATH + "/dropins/");
        withCopyToContainer(MountableFile.forClasspathResource("/openliberty_server.xml"), SERVER_PATH + "/server.xml");
        waitingFor(new LogMessageWaitStrategy().withRegEx(".*CWWKZ0001I: Application .* started.*"));
    }
}
