package test;

public class TestTools {
    public static void withSystemProperty(String property, String value, Runnable block) {
        String oldValue = System.getProperty(property);
        System.setProperty(property, value);
        try {
            block.run();
        } finally {
            if (oldValue == null) {
                System.clearProperty(property);
            } else {
                System.setProperty(property, oldValue);
            }
        }
    }
}
