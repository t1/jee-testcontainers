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
 *
 * @see <a href="https://github.com/t1/jee-testcontainers">https://github.com/t1/jee-testcontainers</a>
 */
// we don't use the SELF type, as we don't want any JEE container specific config
@Slf4j
public abstract class JeeContainer extends GenericContainer<JeeContainer> {
    static final Client CLIENT = ClientBuilder.newClient();
    public static final String CONTAINER_SELECTOR_PROPERTY = "jee-testcontainer";
    public static final String TESTCONTAINER_REUSE_PROPERTY = "testcontainer-reuse";

    public static JeeContainer create() {
        switch (containerKey()) {
            case "wildfly":
                return new WildflyContainer();
            case "open-liberty":
                return new OpenLibertyContainer();
            case "tomee":
                return new TomEeContainer();
            case "payara":
                return new PayaraContainer();
            default:
                throw new IllegalArgumentException("unsupported container type '"
                    + System.getProperty(CONTAINER_SELECTOR_PROPERTY) + "'");
        }
    }

    private static String containerKey() { return containerSelector()[0]; }

    private static String containerTag() { return containerSelector().length == 1 ? null : containerSelector()[1]; }

    private static String[] containerSelector() {
        return System.getProperty(CONTAINER_SELECTOR_PROPERTY, "wildfly").split(":", 2);
    }

    @Setter private String containerDeploymentPath;

    private Deployable deployable;

    public JeeContainer(String image) {
        super(tagged(image, containerTag()));
        withLogConsumer(new StdoutLogConsumer());
        withReuse(Boolean.getBoolean(TESTCONTAINER_REUSE_PROPERTY));
    }

    protected static String tagged(String image, String tag) {
        return (tag == null) ? image : (image + ":" + tag);
    }

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
        return URI.create("http://" + getContainerIpAddress() + ":" + getFirstMappedPort() + "/" + webContext() + "/");
    }

    public String webContext() {
        String fileName = deployable.getFileName();
        if (fileName.endsWith(".war") || fileName.endsWith(".ear"))
            fileName = fileName.substring(0, fileName.length() - 4);
        return fileName;
    }
}
