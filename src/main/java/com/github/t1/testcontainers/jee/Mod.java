package com.github.t1.testcontainers.jee;

public interface Mod {
    Deployable apply(Deployable deployable);
}
