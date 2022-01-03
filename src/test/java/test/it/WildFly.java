package test.it;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Tag("wildfly")
@DisabledIfSystemProperty(named = "os.arch", matches = "aarch64", disabledReason =
    "jboss/wildfly currently doesn't support arm64, and the emulation on M1 is too slow and brittle. " +
    "see https://github.com/jboss-dockerfiles/wildfly/issues/155")
@Retention(RUNTIME)
public @interface WildFly {
}
