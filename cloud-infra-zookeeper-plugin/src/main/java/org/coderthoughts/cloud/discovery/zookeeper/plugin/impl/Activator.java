package org.coderthoughts.cloud.discovery.zookeeper.plugin.impl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.apache.cxf.dosgi.discovery.zookeeper.DiscoveryPlugin;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class Activator implements BundleActivator {
    private String publicDNS;
    private ServiceRegistration reg;

    @Override
    public void start(BundleContext context) throws Exception {
        publicDNS = System.getenv("OPENSHIFT_GEAR_DNS");
        if (publicDNS == null) {
            throw new Exception("Environment variable OPENSHIFT_GEAR_DNS is not set. It should be set to the public DNS name of the current instance.");
        }
        reg = context.registerService(DiscoveryPlugin.class.getName(), new DiscoveryPluginImpl(), null);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        reg.unregister();
    }

    private class DiscoveryPluginImpl implements DiscoveryPlugin {
        @Override
        public String process(Map<String, Object> properties, String endpointKey) {

            replaceProperty(properties, "endpoint.id");
            replaceProperty(properties, "org.apache.cxf.ws.address");

            int hash = endpointKey.indexOf('#');
            if (hash > 0 && endpointKey.length() > (hash + 1)) {
                int hash2 = endpointKey.substring(hash + 1).indexOf('#');
                if (hash2 > 0 && endpointKey.length() > (hash + hash2 + 2)) {
                    String newPath = publicDNS + "#80" + endpointKey.substring(hash + hash2 + 1);
                    /* */ System.out.println("#### " + newPath);
                    return newPath;
                }
            }

            return endpointKey;
        }

        private void replaceProperty(Map<String, Object> properties, String key) {
            Object eid = properties.get(key);
            if (eid instanceof String) {
                try {
                    URL eidURL = new URL((String) eid);
                    URL newURL = new URL(eidURL.getProtocol(), publicDNS, 80, eidURL.getFile());
                    properties.put(key, newURL.toExternalForm());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
