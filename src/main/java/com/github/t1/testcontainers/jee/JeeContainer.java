package com.github.t1.testcontainers.jee;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.MountableFile;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.net.URI;

/**
 * Testcontainer for Jakarta EE application servers.
 * Use {@link #create()} to build instances. Configure them by system properties:
 * <ul>
 * <li>Set `jee-testcontainer` to `wildfly` or `open-liberty` to select the corresponding container.</li>
 * </ul>
 * <p>
 * Subclasses must expose at least one port, but when they expose multiple ports,
 * only the {@link #getFirstMappedPort() first port} is used for the {@link #baseUri()}.
 *
 * @see <a href="https://www.testcontainers.org">https://www.testcontainers.org</a>
 */
// we don't use the SELF type, as we don't want any JEE container specific config
@Slf4j
public abstract class JeeContainer extends GenericContainer<JeeContainer> {
    static final Client CLIENT = ClientBuilder.newClient();
    public static final String CONTAINER_SELECTOR_PROPERTY = "jee-testcontainer";

    public static JeeContainer create() {
        switch (System.getProperty(CONTAINER_SELECTOR_PROPERTY, "wildfly")) {
            case "wildfly":
                return new WildflyContainer();
            case "open-liberty":
                return new OpenLibertyContainer();
            default:
                throw new IllegalArgumentException("unsupported container type '"
                    + System.getProperty(CONTAINER_SELECTOR_PROPERTY) + "'");
        }
    }

    @Setter private String containerDeploymentPath;

    public JeeContainer(String dockerImageName) {
        super(dockerImageName);
        withLogConsumer(new StdoutLogConsumer());
    }

    private Deployable deployable;

    public JeeContainer withDeployment(String deployableString) {
        return withDeployment(URI.create(deployableString));
    }

    public JeeContainer withDeployment(URI deployable) {
        this.deployable = Deployable.create(deployable);
        withCopyFileToContainer(MountableFile.forHostPath(this.deployable.getLocalPath()), containerPath());
        return self();
    }

    protected String containerPath() {
        return containerDeploymentPath + this.deployable.getFileName();
    }

    public WebTarget target() {
        if (getContainerInfo() == null || getContainerInfo().getState() == null)
            throw new IllegalStateException("Container is not started. " +
                "Maybe you forgot the `@Testcontainers` or the `@Container` annotation,");
        return CLIENT.target(baseUri());
    }

    public URI baseUri() {
        return URI.create("http://" + getContainerIpAddress() + ":" + getFirstMappedPort() + "/" + webContext());
    }

    public String webContext() {
        String fileName = deployable.getFileName();
        if (fileName.endsWith(".war") || fileName.endsWith(".ear"))
            fileName = fileName.substring(0, fileName.length() - 4);
        return fileName;
    }
}
