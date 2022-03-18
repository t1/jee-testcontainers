package com.github.t1.testcontainers.jee;

import java.nio.file.FileSystem;

interface ModStore {
    void apply(FileSystem fileSystem);
}
