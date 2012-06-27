package org.coderthoughts.cloud.framework.service.impl;

import java.util.Hashtable;
import java.util.UUID;

import org.coderthoughts.cloud.framework.service.api.OSGiFramework;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class Activator implements BundleActivator {
    private static final String OSGI_FRAMEWORK_UUID = "org.osgi.framework.uuid";
    private ServiceRegistration reg;

    @Override
    public void start(BundleContext context) throws Exception {
        String publicDNS = System.getenv("OPENSHIFT_GEAR_DNS");
        if (publicDNS == null) {
            throw new Exception("Environment variable OPENSHIFT_GEAR_DNS is not set. It should be set to the public DNS name of the current instance.");
        }

        String uuid = context.getProperty(OSGI_FRAMEWORK_UUID);
        if (uuid == null) {
            uuid = UUID.randomUUID().toString();
            System.setProperty(OSGI_FRAMEWORK_UUID, uuid);
        }

        Hashtable<String, Object> props = new Hashtable<String, Object>();
        props.put(OSGI_FRAMEWORK_UUID, uuid);
        props.put("org.coderthoughts.framework.ip", publicDNS);
        props.put("service.exported.interfaces", "*");
        props.put("service.exported.configs", "org.coderthoughts.configtype.cloud");

        OSGiFramework fwk = new OSGiFramework() {};
        reg = context.registerService(OSGiFramework.class.getName(), fwk, props);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        reg.unregister();
    }
}
