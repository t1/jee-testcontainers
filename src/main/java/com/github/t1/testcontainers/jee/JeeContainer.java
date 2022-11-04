package com.github.t1.testcontainers.jee;

import com.github.t1.testcontainers.tools.DeployableBuilder;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.slf4j.event.Level;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.net.URI;
import java.nio.file.Path;

/**
 * Testcontainer for Jakarta EE application servers.
 *
 * @see <a href="https://github.com/t1/jee-testcontainers">https://github.com/t1/jee-testcontainers</a>
 */
// we don't use the SELF type, as we don't want any JEE container specific config
@Slf4j
public abstract class JeeContainer extends GenericContainer<JeeContainer> {
    private static Client CLIENT;
    public static final String CONTAINER_SELECTOR_PROPERTY = "jee-testcontainer";
    public static final String FIX_MAIN_PORT_PROPERTY = "testcontainer-with-main-port-bound-to-fixed-port";
    public static final String TESTCONTAINER_REUSE_PROPERTY = "testcontainer-reuse";

    public static JeeContainer create() {
        var imageName = System.getProperty(CONTAINER_SELECTOR_PROPERTY, "wildfly");
        return create(imageName);
    }

    public static JeeContainer create(String imageNameString) {
        DockerImageName imageName = DockerImageName.parse(imageNameString);
        String repository = imageName.getRepository();
        String type = (repository.contains("/")) ? repository.split("/", 2)[1] : repository;
        switch (type) {
            case "wildfly":
                return new WildflyContainer(imageName);
            case "open-liberty":
                return new OpenLibertyContainer(imageName);
            case "tomee":
                return new TomEeContainer(imageName);
            case "payara":
                return new PayaraContainer(imageName);
            default:
                throw new IllegalArgumentException(
                    "unsupported container type '" + System.getProperty(CONTAINER_SELECTOR_PROPERTY) + "'");
        }
    }

    private String containerDeploymentPath;

    private Deployable deployable;

    public JeeContainer(DockerImageName image) {
        super(image);
        withLogConsumer(new StdoutLogConsumer());
        withReuse(Boolean.getBoolean(TESTCONTAINER_REUSE_PROPERTY));
        addExposedPort(mainPort());
        if (System.getProperties().containsKey(FIX_MAIN_PORT_PROPERTY))
            withMainPortBoundToFixedPort(Integer.getInteger(FIX_MAIN_PORT_PROPERTY));
    }

    protected static String tagged(String image, String tag) {
        return (tag == null) ? image : (image + ":" + tag);
    }

    public JeeContainer withPortBoundToFixedPort(int hostPort, int containerPort) {
        super.addFixedExposedPort(hostPort, containerPort);
        return this;
    }

    public JeeContainer withMainPortBoundToFixedPort(int hostPort) {
        super.addFixedExposedPort(hostPort, mainPort());
        return this;
    }

    public int mainPort() {return 8080;}

    @SuppressWarnings("UnusedReturnValue")
    public JeeContainer withContainerDeploymentPath(String containerDeploymentPath) {
        this.containerDeploymentPath = containerDeploymentPath;
        return this;
    }

    public JeeContainer withDeployment(DeployableBuilder builder, Mod... mods) {
        return withDeployment(builder.build(), mods);
    }

    public JeeContainer withDeployment(String deployableString, Mod... mods) {
        return withDeployment(URI.create(deployableString), mods);
    }

    public JeeContainer withDeployment(URI deployable, Mod... mods) {
        this.deployable = Deployable.create(deployable);
        for (Mod mod : mods)
            this.deployable = mod.apply(this.deployable);
        Path localPath = this.deployable.getLocalPath();
        log.info("deploy {} to {}", localPath, containerPath());
        withCopyToContainer(MountableFile.forHostPath(localPath), containerPath());
        return self();
    }

    public JeeContainer withLogLevel(Class<?> loggerClass, Level level) {
        return withLogLevel(loggerClass.getName(), level);
    }

    public JeeContainer withLogLevel(String loggerName, Level level) {
        log.warn("withLogLevel is not supported by {}", getClass().getName());
        return self();
    }

    public JeeContainer withDataSource(JdbcDatabaseContainer<?> db) {
        log.warn("withDataSource is not supported by {}", getClass().getName());
        return self();
    }

    protected String containerPath() {
        return containerDeploymentPath + this.deployable.getFileName();
    }

    public WebTarget target() {
        if (getContainerInfo() == null || getContainerInfo().getState() == null)
            throw new IllegalStateException(
                "Container is not started. Maybe you forgot the `@Testcontainers` or the `@Container` annotation,");
        return CLIENT().target(baseUri());
    }

    static Client CLIENT() {
        if (CLIENT == null)
            CLIENT = ClientBuilder.newClient();
        return CLIENT;
    }

    public URI baseUri() {
        var webContext = webContext();
        return URI.create("http://" + getHost() + ":" + getFirstMappedPort() + "/"
                          + ((webContext.isEmpty()) ? "" : (webContext + "/")));
    }

    public String webContext() {
        String fileName = deployable.getFileName();
        if (fileName.endsWith(".war") || fileName.endsWith(".ear"))
            fileName = fileName.substring(0, fileName.length() - 4);
        return fileName;
    }

    public <T> T restClient(Class<T> type) {
        return RestClientBuilder.newBuilder()
            .baseUri(baseUri())
            .build(type);
    }
}
