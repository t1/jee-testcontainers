package com.github.t1.testcontainers.jee;

import java.util.function.Consumer;

public interface Mod {
    Deployable apply(Deployable deployable);

    default <T extends ModStore> Deployable with(Deployable deployable, Class<T> type, Consumer<T> consumer) {
        JarDeployable jarDeployable = JarDeployable.of(deployable);
        consumer.accept(jarDeployable.mod(type));
        return jarDeployable;
    }
}
