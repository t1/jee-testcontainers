package test.app;

import lombok.extern.slf4j.Slf4j;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

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
