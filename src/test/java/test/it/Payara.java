package test.it;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Tag("payara")
@DisabledIfSystemProperty(named = "os.arch", matches = "aarch64", disabledReason =
    "payara/server-full currently doesn't support arm64, and the emulation on M1 is too slow and brittle.")
@EnabledIfSystemProperty(named = "java.specification.version", matches = "8", disabledReason =
    "The official payara latest image currently only supports Java 8")
@Retention(RUNTIME)
public @interface Payara {
}
