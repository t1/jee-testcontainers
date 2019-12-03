package test.wildfly;

import com.github.t1.testcontainers.jee.JeeContainer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.testcontainers.utility.MountableFile;
import test.jolokia.JolokiaData;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class DeploymentBehavior {
    private final JeeContainer container = JeeContainer.create();

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
            // TODO actually do the download instead by calling the Maven cli
        }
    }

    @Nested class MavenCentralUrl {
        @Test void shouldGetDeploymentFromMavenCentralUrl() {
            container.withDeployment("https://repo1.maven.org/maven2" + JolokiaData.REPO_PATH);

            assertThat(container.webContext()).isEqualTo("jolokia-war-unsecured");
            assertThat(container.getCopyToFileContainerPathMap()).containsValues(JolokiaData.TARGET_PATH);
            assertThat(getMountableFile().getResolvedPath()).endsWith("/jolokia-war-unsecured.war");
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
            container.withDeployment("file://" + JolokiaData.LOCAL_M2);

            assertThat(container.webContext()).isEqualTo("jolokia-war-unsecured");
            assertThat(container.getCopyToFileContainerPathMap()).containsValues(JolokiaData.TARGET_PATH);
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

            assertThat(container.webContext()).isEqualTo("bar");
            assertThat(container.getCopyToFileContainerPathMap()).containsValues("/opt/wildfly/standalone/deployments/bar.war");
            assertThat(getMountableFile().getResolvedPath()).isEqualTo("/foo/bar-12.14.17345-SNAPSHOT.war");
        }
    }

    private MountableFile getMountableFile() {
        return container.getCopyToFileContainerPathMap().keySet().iterator().next();
    }
}
