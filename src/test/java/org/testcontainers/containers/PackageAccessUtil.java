package org.testcontainers.containers;

import org.testcontainers.images.builder.Transferable;

import java.util.Map;

public class PackageAccessUtil {
    public static Map<Transferable, String> copyToTransferableContainerPathMap(GenericContainer<?> container) {
        return container.getCopyToTransferableContainerPathMap();
    }
}
