package org.coderthoughts.cloud.framework.service.impl;

import java.util.Dictionary;
import java.util.Hashtable;

import org.coderthoughts.cloud.framework.service.api.FrameworkMetadataPublisher;
import org.osgi.framework.ServiceRegistration;

public class FrameworkMetadataPublisherImpl implements FrameworkMetadataPublisher {
    private final ServiceRegistration serviceRegistration;

    public FrameworkMetadataPublisherImpl(ServiceRegistration frameworkStatusReg) {
        serviceRegistration = frameworkStatusReg;
    }

    @Override
    public void addProperty(String key, Object value) {
        Dictionary<String, Object> props = getCurProperties();
        props.put(key, value);
        serviceRegistration.setProperties(props);
        /* */ System.out.println("Updated service properties with: " + key + "=" + value);
    }

    @Override
    public void removeProperty(String key) {
        Dictionary<String, Object> props = getCurProperties();
        props.remove(key);
        serviceRegistration.setProperties(props);
        /* */ System.out.println("Removed service property: " + key);
    }

    private Dictionary<String, Object> getCurProperties() {
        Dictionary<String, Object> dict = new Hashtable<String, Object>();

        for (String key : serviceRegistration.getReference().getPropertyKeys()) {
            dict.put(key, serviceRegistration.getReference().getProperty(key));
        }

        return dict;
    }
}
