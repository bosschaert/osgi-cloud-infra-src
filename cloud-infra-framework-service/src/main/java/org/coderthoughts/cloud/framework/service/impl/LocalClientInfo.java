package org.coderthoughts.cloud.framework.service.impl;

import java.security.Principal;

import org.apache.cxf.dosgi.dsw.ClientInfo;
import org.osgi.framework.BundleContext;

public class LocalClientInfo implements ClientInfo {
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
}
