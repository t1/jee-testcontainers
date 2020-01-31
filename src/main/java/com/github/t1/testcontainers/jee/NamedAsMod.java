package com.github.t1.testcontainers.jee;

import lombok.Value;

public @Value class NamedAsMod implements Mod {
    public static Mod namedAs(String name) { return new NamedAsMod(name); }

    String name;

    @Override public Deployable apply(Deployable deployable) {
        return Deployable.copyOf(deployable, name);
    }
}
