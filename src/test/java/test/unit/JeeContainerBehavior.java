package test.unit;

import com.github.t1.testcontainers.jee.JeeContainer;
import com.github.t1.testcontainers.jee.OpenLibertyContainer;
import com.github.t1.testcontainers.jee.PayaraContainer;
import com.github.t1.testcontainers.jee.TomEeContainer;
import com.github.t1.testcontainers.jee.WildflyContainer;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.testcontainers.utility.MountableFile;
import test.TestTools;

import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static com.github.t1.testcontainers.jee.AddLibMod.addLib;
import static com.github.t1.testcontainers.jee.ConfigMod.config;
import static com.github.t1.testcontainers.jee.JeeContainer.CONTAINER_SELECTOR_PROPERTY;
import static com.github.t1.testcontainers.jee.JeeContainer.FIX_MAIN_PORT_PROPERTY;
import static com.github.t1.testcontainers.jee.JeeContainer.TESTCONTAINER_REUSE_PROPERTY;
import static com.github.t1.testcontainers.jee.NamedAsMod.namedAs;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;
import static test.TestTools.withSystemProperty;
import static test.jolokia.TestData.LOCAL_M2;
import static test.jolokia.TestData.REPO_PATH;
import static test.jolokia.TestData.STANDALONE_DEPLOYMENTS;
import static test.jolokia.TestData.TARGET_PATH;
import static test.jolokia.TestData.TARGET_PATH_V;
import static test.jolokia.TestData.VERSION;
import static test.jolokia.TestData.WAR_V;

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

            then(TestTools.portBindings(container)).containsExactly(port + ":9990/tcp");
        }

        @Test void shouldConfigureTestcontainerFixedMainPortByWithMethod() {
            int port = 32894576;

            JeeContainer container = JeeContainer.create().withMainPortBoundToFixedPort(port);

            then(TestTools.portBindings(container)).containsExactly(port + ":8080/tcp");
        }

        @Test void shouldConfigureTestcontainerFixedMainPortBySystemProperty() {
            Integer port = 32894576;
            withSystemProperty(FIX_MAIN_PORT_PROPERTY, port.toString(), () -> {
                JeeContainer container = JeeContainer.create();

                then(TestTools.portBindings(container)).containsExactly(port + ":8080/tcp");
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
                then(container.getDockerImageName()).isEqualTo("rdohna/wildfly:latest");
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
            withSystemProperty(CONTAINER_SELECTOR_PROPERTY, "wildfly:26.0.1.Final-jdk11", () -> {
                JeeContainer container = JeeContainer.create();

                then(container).isInstanceOf(WildflyContainer.class);
                then(container.getDockerImageName()).isEqualTo("rdohna/wildfly:26.0.1.Final-jdk11");
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
            container.withDeployment("urn:mvn:org.jolokia:jolokia-war-unsecured:" + VERSION + ":war");

            then(container.webContext()).isEqualTo("jolokia-war-unsecured");
            then(container.getCopyToFileContainerPathMap()).containsValues(TARGET_PATH);
            then(getMountableFile().getResolvedPath()).isEqualTo(LOCAL_M2 + REPO_PATH);
        }

        @Test void shouldFailToGetDeploymentFromNonMavenUrn() {
            Throwable throwable = catchThrowable(() ->
                container.withDeployment("urn:xxx:org.jolokia:jolokia-war-unsecured:" + VERSION + ":war"));

            then(throwable).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("unsupported urn scheme 'xxx'");
        }

        @Test void shouldFailToGetDeploymentFromMavenUrnWithoutVersion() {
            Throwable throwable = catchThrowable(() ->
                container.withDeployment("urn:mvn:org.jolokia:jolokia-war-unsecured"));

            then(throwable).isInstanceOf(IllegalArgumentException.class)
                .hasMessageStartingWith("expected 4 or 5 elements in 'mvn' urn");
        }

        @Test void shouldGetJarDeploymentFromMavenUrnWithoutType() {
            container.withDeployment("urn:mvn:org.slf4j:slf4j-api:1.7.30");

            then(container.getCopyToFileContainerPathMap()).containsValues("/opt/jboss/wildfly/standalone/deployments/slf4j-api.jar");
            then(getMountableFile().getResolvedPath()).endsWith(".m2/repository/org/slf4j/slf4j-api/1.7.30/slf4j-api-1.7.30.jar");
        }

        @Test void shouldFailToGetDeploymentFromMavenUrnWithExtraElement() {
            Throwable throwable = catchThrowable(() ->
                container.withDeployment("urn:mvn:org.jolokia:jolokia-war-unsecured:" + VERSION + ":war:xxx"));

            then(throwable).isInstanceOf(IllegalArgumentException.class)
                .hasMessageStartingWith("expected 4 or 5 elements in 'mvn' urn");
        }

        @Test void shouldModifyName() {
            container.withDeployment("urn:mvn:org.jolokia:jolokia-war-unsecured:" + VERSION + ":war",
                namedAs("foo.war"));

            then(container.webContext()).isEqualTo("foo");
            then(container.getCopyToFileContainerPathMap()).containsValues("/opt/jboss/wildfly/standalone/deployments/foo.war");
            then(getMountableFile().getResolvedPath()).endsWith("/foo.war");
        }

        @Test void shouldAddOneLibrary() {
            container.withDeployment("urn:mvn:org.jolokia:jolokia-war-unsecured:" + VERSION + ":war",
                addLib("urn:mvn:org.slf4j:slf4j-api:1.7.30:jar"));

            then(container.getCopyToFileContainerPathMap()).containsValues(TARGET_PATH);
            then(filesInZip(getMountableFile().getResolvedPath())).containsExactly(
                "META-INF/",
                "META-INF/MANIFEST.MF",
                "META-INF/maven/org.jolokia/jolokia-war-unsecured/pom.properties",
                "META-INF/maven/org.jolokia/jolokia-war-unsecured/pom.xml",
                "WEB-INF/",
                "WEB-INF/classes/",
                "WEB-INF/lib/",
                "WEB-INF/lib/jolokia-core-1.6.2.jar",
                "WEB-INF/lib/jolokia-jsr160-1.6.2.jar",
                "WEB-INF/lib/json-simple-1.1.1.jar",
                "WEB-INF/lib/slf4j-api-1.7.30.jar",
                "WEB-INF/web.xml"
            );
        }

        @Test void shouldAddOneLibraryToJarWithoutLibraries() {
            container.withDeployment("urn:mvn:org.slf4j:slf4j-api:1.7.30:jar",
                addLib("urn:mvn:org.slf4j:slf4j-jdk14:1.7.30:jar"));

            then(container.getCopyToFileContainerPathMap())
                .containsValues("/opt/jboss/wildfly/standalone/deployments/slf4j-api.jar");
            then(filesInZip(getMountableFile().getResolvedPath())).contains(
                "WEB-INF/lib/slf4j-jdk14-1.7.30.jar"
            );
        }

        @Test void shouldAddTwoLibraries() {
            container.withDeployment("urn:mvn:org.jolokia:jolokia-war-unsecured:" + VERSION + ":war",
                addLib("urn:mvn:org.slf4j:slf4j-api:1.7.30:jar"),
                addLib("urn:mvn:org.slf4j:slf4j-jdk14:1.7.30:jar"));

            then(container.getCopyToFileContainerPathMap()).containsValues(TARGET_PATH);
            then(filesInZip(getMountableFile().getResolvedPath())).containsExactly(
                "META-INF/",
                "META-INF/MANIFEST.MF",
                "META-INF/maven/org.jolokia/jolokia-war-unsecured/pom.properties",
                "META-INF/maven/org.jolokia/jolokia-war-unsecured/pom.xml",
                "WEB-INF/",
                "WEB-INF/classes/",
                "WEB-INF/lib/",
                "WEB-INF/lib/jolokia-core-1.6.2.jar",
                "WEB-INF/lib/jolokia-jsr160-1.6.2.jar",
                "WEB-INF/lib/json-simple-1.1.1.jar",
                "WEB-INF/lib/slf4j-api-1.7.30.jar",
                "WEB-INF/lib/slf4j-jdk14-1.7.30.jar",
                "WEB-INF/web.xml"
            );
        }

        @Test void shouldAddOneConfig() {
            container.withDeployment("urn:mvn:org.jolokia:jolokia-war-unsecured:" + VERSION + ":war",
                config("foo", "bar"));

            then(container.getCopyToFileContainerPathMap()).containsValues(TARGET_PATH);
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
                "WEB-INF/lib/jolokia-core-1.6.2.jar",
                "WEB-INF/lib/jolokia-jsr160-1.6.2.jar",
                "WEB-INF/lib/json-simple-1.1.1.jar",
                "WEB-INF/web.xml"
            );
        }
    }

    @SneakyThrows(IOException.class)
    private static List<String> filesInZip(String path) {
        return new ZipFile(path).stream().map(ZipEntry::getName).sorted().collect(toList());
    }

    @Nested class MavenCentralUrl {
        @Test void shouldGetDeploymentFromMavenCentralUrl() {
            container.withDeployment("https://repo1.maven.org/maven2" + REPO_PATH);

            then(container.webContext()).isEqualTo("jolokia-war-unsecured-" + VERSION);
            then(container.getCopyToFileContainerPathMap()).containsValues(TARGET_PATH_V);
            then(getMountableFile().getResolvedPath()).endsWith(WAR_V);
        }

        @Test void shouldFailToGetDeploymentFromMavenUrnNotDownloadable() {
            String url = "https://repo1.maven.org/maven2/org/jolokia/jolokia-war-unsecured/1.5.999"
                         + "/jolokia-war-unsecured-1.5.999.war";
            Throwable throwable = catchThrowable(() -> container.withDeployment(url));

            then(throwable).isInstanceOf(IllegalStateException.class)
                .hasMessage("can't download " + url + ": 404 Not Found");
        }

        @Test void shouldModifyName() {
            container.withDeployment("https://repo1.maven.org/maven2" + REPO_PATH,
                namedAs("foo.war"));

            then(container.webContext()).isEqualTo("foo");
            then(container.getCopyToFileContainerPathMap()).containsValues("/opt/jboss/wildfly/standalone/deployments/foo.war");
            then(getMountableFile().getResolvedPath()).endsWith("/foo.war");
        }
    }

    @Nested class LocalFileUrl {
        @Test void shouldGetDeploymentFromLocalFile() {
            container.withDeployment("target/my-app.war");

            then(container.webContext()).isEqualTo("my-app");
            then(container.getCopyToFileContainerPathMap()).containsValues(STANDALONE_DEPLOYMENTS + "/my-app.war");
            then(getMountableFile().getResolvedPath()).isEqualTo(System.getProperty("user.dir") + "/target/my-app.war");
        }

        @Test void shouldGetDeploymentFromLocalFileUrl() {
            container.withDeployment("file://" + LOCAL_M2 + REPO_PATH);

            then(container.webContext()).isEqualTo("jolokia-war-unsecured-1.6.2");
            then(container.getCopyToFileContainerPathMap()).containsValues(TARGET_PATH_V);
            then(getMountableFile().getResolvedPath()).isEqualTo(LOCAL_M2 + REPO_PATH);
        }

        @Test void shouldGetDeploymentFromLocalFileWithoutVersion() {
            container.withDeployment("file:///foo/bar.war");

            then(container.webContext()).isEqualTo("bar");
            then(container.getCopyToFileContainerPathMap()).containsValues(STANDALONE_DEPLOYMENTS + "/bar.war");
            then(getMountableFile().getResolvedPath()).isEqualTo("/foo/bar.war");
        }

        @Test void shouldGetDeploymentFromLocalSnapshotFile() {
            container.withDeployment("file:///foo/bar-12.14.17345-SNAPSHOT.war");

            then(container.webContext()).isEqualTo("bar-12.14.17345-SNAPSHOT");
            then(container.getCopyToFileContainerPathMap()).containsValues(STANDALONE_DEPLOYMENTS + "/bar-12.14.17345-SNAPSHOT.war");
            then(getMountableFile().getResolvedPath()).isEqualTo("/foo/bar-12.14.17345-SNAPSHOT.war");
        }

        @Test void shouldModifyName() {
            container.withDeployment("file://" + LOCAL_M2 + REPO_PATH,
                namedAs("foo.war"));

            then(container.webContext()).isEqualTo("foo");
            then(container.getCopyToFileContainerPathMap()).containsValues("/opt/jboss/wildfly/standalone/deployments/foo.war");
            then(getMountableFile().getResolvedPath()).endsWith("/foo.war");
        }
    }

    private MountableFile getMountableFile() {
        return container.getCopyToFileContainerPathMap().keySet().iterator().next();
    }
}
