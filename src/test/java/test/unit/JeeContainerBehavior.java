package test.unit;

import com.github.t1.testcontainers.jee.*;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.testcontainers.utility.MountableFile;
import test.DemoApp;

import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static com.github.t1.testcontainers.jee.AddLibMod.addLib;
import static com.github.t1.testcontainers.jee.ConfigMod.config;
import static com.github.t1.testcontainers.jee.JeeContainer.*;
import static com.github.t1.testcontainers.jee.NamedAsMod.namedAs;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;
import static org.testcontainers.containers.PackageAccessUtil.copyToTransferableContainerPathMap;
import static test.DemoApp.TARGET_PATH;
import static test.TestTools.withSystemProperty;

public class JeeContainerBehavior {
    private final JeeContainer container = JeeContainer.create();

    @Nested class General {
        @Test void shouldFailToGetTargetWhenContainerIsNotStarted() {
            Throwable throwable = catchThrowable(container::target);

            then(throwable)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageStartingWith("Container is not started.");
        }

        @Test void shouldNotConfigureTestcontainerReuseByDefault() {
            then(container.isShouldBeReused()).isFalse();
        }

        @Test void shouldConfigureTestcontainerReuse() {
            JeeContainer container = JeeContainer.create().withReuse(true);

            then(container.isShouldBeReused()).isTrue();
        }

        @Test void shouldConfigureTestcontainerReuseBySystemProperty() {
            withSystemProperty(TESTCONTAINER_REUSE_PROPERTY, "true", () -> {
                JeeContainer container = JeeContainer.create();

                then(container.isShouldBeReused()).isTrue();
            });
        }

        @Test void shouldConfigureTestcontainerFixedPort() {
            int port = 32894576;

            JeeContainer container = JeeContainer.create().withPortBoundToFixedPort(port, 9990);

            then(container.getPortBindings()).containsExactly(port + ":9990/tcp");
        }

        @Test void shouldConfigureTestcontainerFixedMainPortByWithMethod() {
            int port = 32894576;

            JeeContainer container = JeeContainer.create().withMainPortBoundToFixedPort(port);

            then(container.getPortBindings()).containsExactly(port + ":8080/tcp");
        }

        @Test void shouldConfigureTestcontainerFixedMainPortBySystemProperty() {
            Integer port = 32894576;
            withSystemProperty(FIX_MAIN_PORT_PROPERTY, port.toString(), () -> {
                JeeContainer container = JeeContainer.create();

                then(container.getPortBindings()).containsExactly(port + ":8080/tcp");
            });
        }
    }

    @Nested class ContainerSelection {
        @Test void shouldSelectWildflyByDefault() {
            then(container).isInstanceOf(WildflyContainer.class);
        }

        @Test void shouldSelectWildflyBySystemProperty() {
            withSystemProperty(CONTAINER_SELECTOR_PROPERTY, "wildfly", () -> {
                JeeContainer container = JeeContainer.create();

                then(container).isInstanceOf(WildflyContainer.class);
                then(container.getDockerImageName()).isEqualTo("quay.io/wildfly/wildfly:latest");
            });
        }

        @Test void shouldSelectImageBaseAndVersionBySystemProperty() {
            withSystemProperty(CONTAINER_SELECTOR_PROPERTY, "jboss/wildfly:18.0.1.Final", () -> {
                JeeContainer container = JeeContainer.create();

                then(container).isInstanceOf(WildflyContainer.class);
                then(container.getDockerImageName()).isEqualTo("jboss/wildfly:18.0.1.Final");
            });
        }

        @Test void shouldSelectVersionBySystemProperty() {
            withSystemProperty(CONTAINER_SELECTOR_PROPERTY, "quay.io/wildfly/wildfly:26.1.2.Final-jdk11", () -> {
                JeeContainer container = JeeContainer.create();

                then(container).isInstanceOf(WildflyContainer.class);
                then(container.getDockerImageName()).isEqualTo("quay.io/wildfly/wildfly:26.1.2.Final-jdk11");
            });
        }

        @Test void shouldSelectOpenLibertyBySystemProperty() {
            withSystemProperty(CONTAINER_SELECTOR_PROPERTY, "open-liberty", () -> {
                JeeContainer container = JeeContainer.create();

                then(container).isInstanceOf(OpenLibertyContainer.class);
                then(container.getDockerImageName()).isEqualTo("open-liberty:latest");
            });
        }

        @Test void shouldSelectTomEeBySystemProperty() {
            withSystemProperty(CONTAINER_SELECTOR_PROPERTY, "tomee", () -> {
                JeeContainer container = JeeContainer.create();

                then(container).isInstanceOf(TomEeContainer.class);
                then(container.getDockerImageName()).isEqualTo("tomee:latest");
            });
        }

        @Test void shouldSelectPayaraBySystemProperty() {
            withSystemProperty(CONTAINER_SELECTOR_PROPERTY, "payara", () -> {
                JeeContainer container = JeeContainer.create();

                then(container).isInstanceOf(PayaraContainer.class);
                then(container.getDockerImageName()).isEqualTo("payara/server-full:latest");
            });
        }

        @Test void shouldFailToSelectUnknownContainerBySystemProperty() {
            withSystemProperty(CONTAINER_SELECTOR_PROPERTY, "unknown", () -> {
                Throwable throwable = catchThrowable(JeeContainer::create);

                then(throwable).isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("unsupported container type 'unknown'");
            });
        }
    }

    @Nested class MavenUrn {
        @Test void shouldGetDeploymentFromMavenUrn() {
            var app = DemoApp.LATEST;
            container.withDeployment(app.urn());

            then(container.webContext()).isEqualTo(app.artifactId());
            then(copyToTransferableContainerPathMap(container)).containsValues(app.targetPath());
            then(getMountableFile().getResolvedPath()).isEqualTo(app.localPath());
        }

        @Test void shouldFailToGetDeploymentFromNonMavenUrn() {
            Throwable throwable = catchThrowable(() -> container.withDeployment("urn:xxx:" + DemoApp.LATEST.gav() + ":war"));

            then(throwable).isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("unsupported urn scheme 'xxx'");
        }

        @Test void shouldFailToGetDeploymentFromMavenUrnWithoutVersion() {
            Throwable throwable = catchThrowable(() ->
                    container.withDeployment("urn:mvn:" + DemoApp.LATEST.groupId() + ":" + DemoApp.LATEST.artifactId()));

            then(throwable).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageStartingWith("expected 4 or 5 elements in 'mvn' urn");
        }

        @Test void shouldGetJarDeploymentFromMavenUrnWithoutType() {
            container.withDeployment("urn:mvn:org.slf4j:slf4j-api:1.7.30");

            then(copyToTransferableContainerPathMap(container)).containsValues("/opt/jboss/wildfly/standalone/deployments/slf4j-api.jar");
            then(getMountableFile().getResolvedPath()).endsWith(".m2/repository/org/slf4j/slf4j-api/1.7.30/slf4j-api-1.7.30.jar");
        }

        @Test void shouldFailToGetDeploymentFromMavenUrnWithExtraElement() {
            Throwable throwable = catchThrowable(() -> container.withDeployment(DemoApp.LATEST.urn() + ":xxx"));

            then(throwable).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageStartingWith("expected 4 or 5 elements in 'mvn' urn");
        }

        @Test void shouldModifyName() {
            var app = DemoApp.LATEST;
            container.withDeployment(app.urn(), namedAs("foo.war"));

            then(container.webContext()).isEqualTo("foo");
            then(copyToTransferableContainerPathMap(container)).containsValues(TARGET_PATH + "/foo.war");
            then(getMountableFile().getResolvedPath()).endsWith("/foo.war");
        }

        @Test void shouldAddOneLibrary() {
            container.withDeployment("urn:mvn:org.jolokia:jolokia-war-unsecured:1.7.1:war",
                    addLib("urn:mvn:org.slf4j:slf4j-api:1.7.30:jar"));

            then(copyToTransferableContainerPathMap(container)).containsValues(TARGET_PATH + "/jolokia-war-unsecured.war");
            then(filesInZip(getMountableFile().getResolvedPath())).containsExactly(
                    "META-INF/",
                    "META-INF/MANIFEST.MF",
                    "META-INF/maven/org.jolokia/jolokia-war-unsecured/pom.properties",
                    "META-INF/maven/org.jolokia/jolokia-war-unsecured/pom.xml",
                    "WEB-INF/",
                    "WEB-INF/classes/",
                    "WEB-INF/lib/",
                    "WEB-INF/lib/jolokia-core-1.7.1.jar",
                    "WEB-INF/lib/jolokia-jsr160-1.7.1.jar",
                    "WEB-INF/lib/json-simple-1.1.1.jar",
                    "WEB-INF/lib/slf4j-api-1.7.30.jar",
                    "WEB-INF/web.xml"
            );
        }

        @Test void shouldAddOneLibraryToJarWithoutLibraries() {
            container.withDeployment("urn:mvn:org.slf4j:slf4j-api:1.7.30:jar",
                    addLib("urn:mvn:org.slf4j:slf4j-jdk14:1.7.30:jar"));

            then(copyToTransferableContainerPathMap(container))
                    .containsValues("/opt/jboss/wildfly/standalone/deployments/slf4j-api.jar");
            then(filesInZip(getMountableFile().getResolvedPath())).contains(
                    "WEB-INF/lib/slf4j-jdk14-1.7.30.jar"
            );
        }

        @Test void shouldAddTwoLibraries() {
            container.withDeployment("urn:mvn:org.jolokia:jolokia-war-unsecured:1.7.1:war",
                    addLib("urn:mvn:org.slf4j:slf4j-api:1.7.30:jar"),
                    addLib("urn:mvn:org.slf4j:slf4j-jdk14:1.7.30:jar"));

            then(copyToTransferableContainerPathMap(container)).containsValues(TARGET_PATH + "/jolokia-war-unsecured.war");
            then(filesInZip(getMountableFile().getResolvedPath())).containsExactly(
                    "META-INF/",
                    "META-INF/MANIFEST.MF",
                    "META-INF/maven/org.jolokia/jolokia-war-unsecured/pom.properties",
                    "META-INF/maven/org.jolokia/jolokia-war-unsecured/pom.xml",
                    "WEB-INF/",
                    "WEB-INF/classes/",
                    "WEB-INF/lib/",
                    "WEB-INF/lib/jolokia-core-1.7.1.jar",
                    "WEB-INF/lib/jolokia-jsr160-1.7.1.jar",
                    "WEB-INF/lib/json-simple-1.1.1.jar",
                    "WEB-INF/lib/slf4j-api-1.7.30.jar",
                    "WEB-INF/lib/slf4j-jdk14-1.7.30.jar",
                    "WEB-INF/web.xml"
            );
        }

        @Test void shouldAddOneConfig() {
            container.withDeployment("urn:mvn:org.jolokia:jolokia-war-unsecured:1.7.1:war",
                    config("foo", "bar"));

            then(copyToTransferableContainerPathMap(container)).containsValues(TARGET_PATH + "/jolokia-war-unsecured.war");
            then(filesInZip(getMountableFile().getResolvedPath())).containsExactly(
                    "META-INF/",
                    "META-INF/MANIFEST.MF",
                    "META-INF/maven/org.jolokia/jolokia-war-unsecured/pom.properties",
                    "META-INF/maven/org.jolokia/jolokia-war-unsecured/pom.xml",
                    "WEB-INF/",
                    "WEB-INF/classes/",
                    "WEB-INF/classes/META-INF/",
                    "WEB-INF/classes/META-INF/microprofile-config.properties",
                    "WEB-INF/lib/",
                    "WEB-INF/lib/jolokia-core-1.7.1.jar",
                    "WEB-INF/lib/jolokia-jsr160-1.7.1.jar",
                    "WEB-INF/lib/json-simple-1.1.1.jar",
                    "WEB-INF/web.xml"
            );
        }
    }

    @SneakyThrows(IOException.class)
    private static List<String> filesInZip(String path) {
        try (ZipFile zipFile = new ZipFile(path)) {
            return zipFile.stream().map(ZipEntry::getName).sorted().collect(toList());
        }
    }

    @Nested class MavenCentralUrl {
        @Test void shouldGetDeploymentFromMavenCentralUrl() {
            var app = DemoApp.LATEST;
            container.withDeployment(app.url());

            then(container.webContext()).isEqualTo(app.id_v());
            then(copyToTransferableContainerPathMap(container)).containsValues(app.targetPath_v());
            then(getMountableFile().getResolvedPath()).endsWith(app.war_v());
        }

        @Test void shouldFailToGetDeploymentFromMavenUrlNotDownloadable() {
            String url = DemoApp.LATEST.url() + "x";
            Throwable throwable = catchThrowable(() -> container.withDeployment(url));

            then(throwable).isInstanceOf(IllegalStateException.class)
                    .hasMessage("can't download " + url + ": 404 Not Found");
        }

        @Test void shouldModifyName() {
            container.withDeployment(DemoApp.LATEST.url(), namedAs("foo.war"));

            then(container.webContext()).isEqualTo("foo");
            then(copyToTransferableContainerPathMap(container)).containsValues(TARGET_PATH + "/foo.war");
            then(getMountableFile().getResolvedPath()).endsWith("/foo.war");
        }
    }

    @Nested class LocalFileUrl {
        @Test void shouldGetDeploymentFromLocalFile() {
            container.withDeployment("target/my-app.war");

            then(container.webContext()).isEqualTo("my-app");
            then(copyToTransferableContainerPathMap(container)).containsValues(TARGET_PATH + "/my-app.war");
            then(getMountableFile().getResolvedPath()).isEqualTo(System.getProperty("user.dir") + "/target/my-app.war");
        }

        @Test void shouldGetDeploymentFromLocalFileUrl() {
            var app = DemoApp.LATEST;
            container.withDeployment(app.file());

            then(container.webContext()).isEqualTo(app.id_v());
            then(copyToTransferableContainerPathMap(container)).containsValues(app.targetPath_v());
            then(getMountableFile().getResolvedPath()).isEqualTo(app.localPath());
        }

        @Test void shouldGetDeploymentFromLocalFileWithoutVersion() {
            container.withDeployment("file:///foo/bar.war");

            then(container.webContext()).isEqualTo("bar");
            then(copyToTransferableContainerPathMap(container)).containsValues(TARGET_PATH + "/bar.war");
            then(getMountableFile().getResolvedPath()).isEqualTo("/foo/bar.war");
        }

        @Test void shouldGetDeploymentFromLocalSnapshotFile() {
            container.withDeployment("file:///foo/bar-12.14.17345-SNAPSHOT.war");

            then(container.webContext()).isEqualTo("bar-12.14.17345-SNAPSHOT");
            then(copyToTransferableContainerPathMap(container)).containsValues(TARGET_PATH + "/bar-12.14.17345-SNAPSHOT.war");
            then(getMountableFile().getResolvedPath()).isEqualTo("/foo/bar-12.14.17345-SNAPSHOT.war");
        }

        @Test void shouldModifyName() {
            container.withDeployment(DemoApp.LATEST.urn(), namedAs("foo.war"));

            then(container.webContext()).isEqualTo("foo");
            then(copyToTransferableContainerPathMap(container)).containsValues(TARGET_PATH + "/foo.war");
            then(getMountableFile().getResolvedPath()).endsWith("/foo.war");
        }
    }

    private MountableFile getMountableFile() {
        return (MountableFile) copyToTransferableContainerPathMap(container).keySet().iterator().next();
    }
}
