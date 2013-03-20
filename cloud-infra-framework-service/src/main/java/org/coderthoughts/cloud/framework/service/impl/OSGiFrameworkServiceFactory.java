package org.coderthoughts.cloud.framework.service.impl;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.cxf.dosgi.dsw.ClientInfo;
import org.apache.cxf.dosgi.dsw.RemoteServiceFactory;
import org.coderthoughts.cloud.framework.service.api.FrameworkStatus;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

public class OSGiFrameworkServiceFactory implements RemoteServiceFactory<FrameworkStatus> {
    private ServiceTracker monitorAdminServiceTracker;
    private final ConcurrentMap<Object, FrameworkStatusImpl> services = new ConcurrentHashMap<Object, FrameworkStatusImpl>();

    public OSGiFrameworkServiceFactory(ServiceTracker monitorAdminST) {
        monitorAdminServiceTracker = monitorAdminST;
    }

    @Override
    public FrameworkStatus getService(ClientInfo clientInfo, ServiceReference reference) {
        System.out.println("*** Called getService: " + clientInfo + "#" + reference);
        return getService(clientInfo);
    }

    @Override
    public void ungetService(ClientInfo clientIP, ServiceReference reference, FrameworkStatus service) {
        System.out.println("*** Called ungetService: " + clientIP + "#" + reference + "#" + service);
    }

    private FrameworkStatus getService(ClientInfo clientInfo) {
        FrameworkStatusImpl newSvc = new FrameworkStatusImpl(clientInfo, monitorAdminServiceTracker);
        FrameworkStatusImpl oldSvc = services.putIfAbsent(clientInfo, newSvc);
        return oldSvc == null ? newSvc : oldSvc;
    }
}
