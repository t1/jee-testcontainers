package com.github.t1.testcontainers.jee;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.MountableFile;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.net.URI;

/**
 * Testcontainer for Jakarta EE application servers.
 * The {@link #create()} is used to build instances configured by system properties.
 */
// we don't use the SELF type, as we don't want any JEE container specific config
@Slf4j
public abstract class JeeContainer extends GenericContainer<JeeContainer> {
    static final Client CLIENT = ClientBuilder.newClient();
    private static final int EXPOSED_PORT = 8080;

    public static JeeContainer create() {
        // TODO switch to other containers via system properties
        return new WildflyContainer();
    }

    public JeeContainer(String dockerImageName) {
        super(dockerImageName);
        addExposedPort(EXPOSED_PORT);
        withLogConsumer(new StdoutLogConsumer());
        // TODO health wait strategy
        // TODO keep running
    }

    private Deployable deployable;

    public JeeContainer withDeployment(String deployableString) {
        return withDeployment(URI.create(deployableString));
    }

    public JeeContainer withDeployment(URI deployable) {
        this.deployable = Deployable.create(deployable);
        withCopyFileToContainer(MountableFile.forHostPath(this.deployable.getLocalPath()),
            containerPath() + this.deployable.getFileName());
        return self();
    }

    @NotNull protected abstract String containerPath();

    public WebTarget target() {
        return CLIENT.target(baseUri());
    }

    public URI baseUri() {
        return URI.create("http://" + getContainerIpAddress() + ":" + getMappedPort(EXPOSED_PORT) + "/" + webContext());
    }

    public String webContext() {
        String fileName = deployable.getFileName();
        if (fileName.endsWith(".war") || fileName.endsWith(".ear"))
            fileName = fileName.substring(0, fileName.length() - 4);
        return fileName;
    }
}
