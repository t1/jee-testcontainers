package test.unit;

import com.github.t1.testcontainers.jee.JeeContainer;
import com.github.t1.testcontainers.jee.OpenLibertyContainer;
import com.github.t1.testcontainers.jee.TomEeContainer;
import com.github.t1.testcontainers.jee.WildflyContainer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.testcontainers.utility.MountableFile;
import test.jolokia.JolokiaData;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static test.TestTools.withSystemProperty;

public class JeeContainerBehavior {
    private final JeeContainer container = JeeContainer.create();

    @Nested class General {
        @Test void shouldFailToGetTargetWhenContainerIsNotStarted() {
            Throwable throwable = catchThrowable(container::target);

            assertThat(throwable)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageStartingWith("Container is not started.");
        }
    }

    @Nested class ContainerSelection {
        @Test void shouldSelectWildflyByDefault() {
            assertThat(container).isInstanceOf(WildflyContainer.class);
        }

        @Test void shouldSelectWildflyBySystemProperty() {
            withSystemProperty(JeeContainer.CONTAINER_SELECTOR_PROPERTY, "wildfly", () -> {
                JeeContainer container = JeeContainer.create();

                assertThat(container).isInstanceOf(WildflyContainer.class);
                assertThat(container.getDockerImageName()).isEqualTo("quay.io/wildfly/wildfly-centos7:latest");
            });
        }

        @Test void shouldSelectVersionBySystemProperty() {
            withSystemProperty(JeeContainer.CONTAINER_SELECTOR_PROPERTY, "wildfly:18.0", () -> {
                JeeContainer container = JeeContainer.create();

                assertThat(container).isInstanceOf(WildflyContainer.class);
                assertThat(container.getDockerImageName()).isEqualTo("quay.io/wildfly/wildfly-centos7:18.0");
            });
        }

        @Test void shouldSelectOpenLibertyBySystemProperty() {
            withSystemProperty(JeeContainer.CONTAINER_SELECTOR_PROPERTY, "open-liberty", () -> {
                JeeContainer container = JeeContainer.create();

                assertThat(container).isInstanceOf(OpenLibertyContainer.class);
            });
        }

        @Test void shouldSelectTomEeBySystemProperty() {
            withSystemProperty(JeeContainer.CONTAINER_SELECTOR_PROPERTY, "tomee", () -> {
                JeeContainer container = JeeContainer.create();

                assertThat(container).isInstanceOf(TomEeContainer.class);
            });
        }

        @Test void shouldFailToSelectUnknownContainerBySystemProperty() {
            withSystemProperty(JeeContainer.CONTAINER_SELECTOR_PROPERTY, "unknown", () -> {
                Throwable throwable = catchThrowable(JeeContainer::create);

                assertThat(throwable).isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("unsupported container type 'unknown'");
            });
        }
    }

    @Nested class MavenUrn {
        @Test void shouldGetDeploymentFromMavenUrn() {
            container.withDeployment("urn:mvn:org.jolokia:jolokia-war-unsecured:" + JolokiaData.VERSION + ":war");

            assertThat(container.webContext()).isEqualTo("jolokia-war-unsecured");
            assertThat(container.getCopyToFileContainerPathMap()).containsValues(JolokiaData.TARGET_PATH);
            assertThat(getMountableFile().getResolvedPath()).isEqualTo(JolokiaData.LOCAL_M2);
        }

        @Test void shouldFailToGetDeploymentFromNonMavenUrn() {
            Throwable throwable = catchThrowable(() ->
                container.withDeployment("urn:xxx:org.jolokia:jolokia-war-unsecured:" + JolokiaData.VERSION + ":war"));

            assertThat(throwable).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("unsupported urn scheme 'xxx'");
        }

        @Test void shouldFailToGetDeploymentFromMavenUrnWithoutType() {
            Throwable throwable = catchThrowable(() ->
                container.withDeployment("urn:mvn:org.jolokia:jolokia-war-unsecured:" + JolokiaData.VERSION));

            assertThat(throwable).isInstanceOf(IllegalArgumentException.class)
                .hasMessageStartingWith("expected exactly 5 elements in 'mvn' urn");
        }

        @Test void shouldFailToGetDeploymentFromMavenUrnWithExtraElement() {
            Throwable throwable = catchThrowable(() ->
                container.withDeployment("urn:mvn:org.jolokia:jolokia-war-unsecured:" + JolokiaData.VERSION + ":war:xxx"));

            assertThat(throwable).isInstanceOf(IllegalArgumentException.class)
                .hasMessageStartingWith("expected exactly 5 elements in 'mvn' urn");
        }

        @Test void shouldFailToGetDeploymentFromMavenUrnWhenNotDownloaded() {
            Throwable throwable = catchThrowable(() ->
                container.withDeployment("urn:mvn:org.jolokia:jolokia-war-unsecured:1.6.1:war"));

            assertThat(throwable).isInstanceOf(IllegalArgumentException.class)
                .hasMessageStartingWith("maven artifact is not downloaded to local repository");
        }
    }

    @Nested class MavenCentralUrl {
        @Test void shouldGetDeploymentFromMavenCentralUrl() {
            container.withDeployment("https://repo1.maven.org/maven2" + JolokiaData.REPO_PATH);

            assertThat(container.webContext()).isEqualTo("jolokia-war-unsecured-" + JolokiaData.VERSION);
            assertThat(container.getCopyToFileContainerPathMap()).containsValues(JolokiaData.TARGET_PATH_V);
            assertThat(getMountableFile().getResolvedPath()).endsWith(JolokiaData.WAR_V);
        }

        @Test void shouldFailToGetDeploymentFromMavenUrnNotDownloadable() {
            String url = "https://repo1.maven.org/maven2/org/jolokia/jolokia-war-unsecured/1.5.999"
                + "/jolokia-war-unsecured-1.5.999.war";
            Throwable throwable = catchThrowable(() -> container.withDeployment(url));

            assertThat(throwable).isInstanceOf(IllegalStateException.class)
                .hasMessage("can't download " + url + ": 404 Not Found");
        }
    }

    @Nested class LocalFileUrl {
        @Test void shouldGetDeploymentFromLocalFile() {
            container.withDeployment("target/my-app.war");

            assertThat(container.webContext()).isEqualTo("my-app");
            assertThat(container.getCopyToFileContainerPathMap())
                .containsValues("/opt/wildfly/standalone/deployments/my-app.war");
            assertThat(getMountableFile().getResolvedPath())
                .isEqualTo(System.getProperty("user.dir") + "/target/my-app.war");
        }

        @Test void shouldGetDeploymentFromLocalFileUrl() {
            container.withDeployment("file://" + JolokiaData.LOCAL_M2);

            assertThat(container.webContext()).isEqualTo("jolokia-war-unsecured-1.6.2");
            assertThat(container.getCopyToFileContainerPathMap()).containsValues(JolokiaData.TARGET_PATH_V);
            assertThat(getMountableFile().getResolvedPath()).isEqualTo(JolokiaData.LOCAL_M2);
        }

        @Test void shouldGetDeploymentFromLocalFileWithoutVersion() {
            container.withDeployment("file:///foo/bar.war");

            assertThat(container.webContext()).isEqualTo("bar");
            assertThat(container.getCopyToFileContainerPathMap()).containsValues("/opt/wildfly/standalone/deployments/bar.war");
            assertThat(getMountableFile().getResolvedPath()).isEqualTo("/foo/bar.war");
        }

        @Test void shouldGetDeploymentFromLocalSnapshotFile() {
            container.withDeployment("file:///foo/bar-12.14.17345-SNAPSHOT.war");

            assertThat(container.webContext()).isEqualTo("bar-12.14.17345-SNAPSHOT");
            assertThat(container.getCopyToFileContainerPathMap()).containsValues("/opt/wildfly/standalone/deployments/bar-12.14.17345-SNAPSHOT.war");
            assertThat(getMountableFile().getResolvedPath()).isEqualTo("/foo/bar-12.14.17345-SNAPSHOT.war");
        }
    }

    private MountableFile getMountableFile() {
        return container.getCopyToFileContainerPathMap().keySet().iterator().next();
    }
}
