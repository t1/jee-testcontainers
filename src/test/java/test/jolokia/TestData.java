package test.jolokia;

public class TestData {
    public static final String VERSION = "1.6.2";
    public static final String WAR = "/jolokia-war-unsecured.war";
    public static final String WAR_V = "/jolokia-war-unsecured-" + VERSION + ".war";
    public static final String REPO_PATH = "/org/jolokia/jolokia-war-unsecured/" + VERSION + WAR_V;
    public static final String LOCAL_M2 = System.getProperty("user.home") + "/.m2/repository";
    public static final String STANDALONE_DEPLOYMENTS = "/opt/jboss/wildfly/standalone/deployments";
    public static final String TARGET_PATH = STANDALONE_DEPLOYMENTS + WAR;
    public static final String TARGET_PATH_V = STANDALONE_DEPLOYMENTS + WAR_V;
}
