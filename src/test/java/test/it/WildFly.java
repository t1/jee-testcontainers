package test.it;

import org.junit.jupiter.api.Tag;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Tag("wildfly")
@Retention(RUNTIME)
public @interface WildFly {
}
