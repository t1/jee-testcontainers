package test.it;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Tag("openliberty")
@DisabledIfSystemProperty(named = "os.arch", matches = "aarch64", disabledReason =
    "The official open-liberty image currently doesn't support arm64, and the emulation on M1 is too slow and brittle. " +
    "see https://github.com/OpenLiberty/ci.docker/issues/241")
@Retention(RUNTIME)
public @interface OpenLiberty {
}
