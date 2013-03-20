package org.osgi.service.remoteserviceadmin;

public interface EndpointEventListener {
    void endpointChanged(EndpointEvent event);
}
