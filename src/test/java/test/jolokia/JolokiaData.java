package test.jolokia;

public class JolokiaData {
    public static final String VERSION = "1.6.2";
    public static final String WAR = "/jolokia-war-unsecured.war";
    public static final String WAR_V = "/jolokia-war-unsecured-" + VERSION + ".war";
    public static final String REPO_PATH = "/org/jolokia/jolokia-war-unsecured/" + VERSION + WAR_V;
    public static final String LOCAL_M2 = System.getProperty("user.home") + "/.m2/repository" + REPO_PATH;
    public static final String TARGET_PATH = "/opt/wildfly/standalone/deployments" + WAR;
    public static final String TARGET_PATH_V = "/opt/wildfly/standalone/deployments" + WAR_V;
}
