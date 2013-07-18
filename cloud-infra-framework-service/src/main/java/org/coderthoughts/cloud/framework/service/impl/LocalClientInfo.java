package org.coderthoughts.cloud.framework.service.impl;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import org.apache.cxf.dosgi.dsw.ClientContext;
import org.osgi.framework.BundleContext;

public class LocalClientInfo implements ClientContext {
    private BundleContext bundleContext;

    public LocalClientInfo(BundleContext ctx) {
        bundleContext = ctx;
    }

    @Override
    public String getHostIPAddress() {
        return "0.0.0.0";
    }

    @Override
    public String getFrameworkUUID() {
        return bundleContext.getProperty("org.osgi.framework.uuid");
    }

    @Override
    public Principal getPrincipal() {
        return null;
    }

    @Override
    public Map<String, Object> getProperties() {
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("HostIPAddress", getHostIPAddress());
        m.put("FrameworkUUID", getFrameworkUUID());
        return m;
    }
}
