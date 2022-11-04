package test.app;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Path("/dao")
public class DAO {
    @PersistenceContext(unitName = "test") EntityManager em;

    @GET public String version() {
        log.info("start select");
        String version = em.createQuery(SELECT_VERSION, String.class).getSingleResult();
        log.info("found version {}", version);
        if (version.contains(" (")) version = version.substring(0, version.indexOf(" ("));
        log.info("return version {}", version);
        return version;
    }

    private static final String SELECT_VERSION = "select setting from PgSettings where name = 'server_version'";
}
