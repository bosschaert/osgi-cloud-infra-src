package org.coderthoughts.cloud.framework.service.api;

public interface FrameworkMetadataPublisher {
    void addProperty(String key, Object value);
    void removeProperty(String key);
}
