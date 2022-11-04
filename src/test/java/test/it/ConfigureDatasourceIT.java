package test.it;

import com.github.t1.testcontainers.jee.JeeContainer;
import com.github.t1.testcontainers.jee.WildflyContainer;
import com.github.t1.testcontainers.tools.DeployableBuilder;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import test.app.DAO;
import test.app.PgSettings;
import test.app.REST;

import java.util.UUID;

import static com.github.t1.testcontainers.tools.DeployableBuilder.war;
import static jakarta.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.BDDAssertions.then;

@Slf4j
@WildFly
@Testcontainers
public class ConfigureDatasourceIT {
    private static final String POSTGRESQL_VERSION = "14.2";
    private static final String DATABASE_NAME = "test";

    static DeployableBuilder buildApp() {
        return war("ROOT").withClasses(REST.class, DAO.class, PgSettings.class).withPersistenceXml(DATABASE_NAME);
    }

    static final Network NETWORK = Network.newNetwork();

    @SuppressWarnings("resource")
    @Container static PostgreSQLContainer<?> DB = new PostgreSQLContainer<>("postgres:" + POSTGRESQL_VERSION)
        .withDatabaseName(DATABASE_NAME)
        .withPassword(UUID.randomUUID().toString())
        .withNetwork(NETWORK)
        .withNetworkAliases("db");

    @Container static JeeContainer APP = WildflyContainer.create("rdohna/wildfly", "27.0-jdk17")
        .withDataSource(DB)
        .withDeployment(buildApp())
        .withNetwork(NETWORK);

    @Test void shouldReadFromDataSource() {
        Response response = APP.target().path("/dao").request().get();

        then(response.getStatusInfo()).isEqualTo(OK);
        then(response.readEntity(String.class)).isEqualTo(POSTGRESQL_VERSION);
    }
}
