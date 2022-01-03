package test.it;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Tag("tomee")
@DisabledIfSystemProperty(named = "os.arch", matches = "aarch64", disabledReason =
    "The official tomee image currently doesn't support arm64, and the emulation on M1 is too slow and brittle. ")
@Retention(RUNTIME)
public @interface TomEE {
}
