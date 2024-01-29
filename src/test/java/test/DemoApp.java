package test;

import com.github.t1.testcontainers.jee.JeeContainer;

import static jakarta.ws.rs.client.Entity.json;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static org.assertj.core.api.BDDAssertions.then;

/** Coordinates of an arbitrarily chosen war available in Maven Central: wunderbar.demo.order */
public class DemoApp {
    // this version is also pre-fetched in `maven.yml`
    public static final DemoApp EE8 = new DemoApp("2.4.9");
    public static final DemoApp LATEST = new DemoApp("3.2");

    public static final String TARGET_PATH = "/opt/jboss/wildfly/standalone/deployments";

    private static final String PATH = "/com/github/t1";
    private static final String LOCAL_M2 = System.getProperty("user.home") + "/.m2/repository";


    private final String version;

    private DemoApp(String version) {this.version = version;}

    public String gav() {return groupId() + ":" + artifactId() + ":" + version;}

    public String groupId() {return "com.github.t1";}

    public String artifactId() {return "wunderbar.demo.order";}

    public String id_v() {return artifactId() + "-" + version;}

    public String war() {return "/" + artifactId() + ".war";}

    public String war_v() {return "/" + id_v() + ".war";}

    public String targetPath() {return TARGET_PATH + war();}

    public String targetPath_v() {return TARGET_PATH + war_v();}

    public String localPath() {return LOCAL_M2 + path();}

    public String urn() {return "urn:mvn:" + gav() + ":war";}

    public String url() {return "https://repo1.maven.org/maven2" + path();}

    private String path() {return PATH + "/" + artifactId() + "/" + version + war_v();}

    public String file() {return "file://" + LOCAL_M2 + PATH + "/" + artifactId() + "/" + version + war_v();}


    public void check(JeeContainer container) {
        var request = container.target().path("/graphql").request(APPLICATION_JSON_TYPE);

        var response = request.post(json("{\"query\":\"{query}\"}"));

        then(response.getStatus()).isEqualTo(200);
        then(response.readEntity(String.class)).contains("Field 'query' in type 'Query' is undefined"); // this is fine
    }
}
