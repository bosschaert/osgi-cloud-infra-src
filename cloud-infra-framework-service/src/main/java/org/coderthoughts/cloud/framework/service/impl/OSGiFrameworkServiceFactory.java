package org.coderthoughts.cloud.framework.service.impl;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.cxf.dosgi.dsw.RemoteServiceFactory;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

public class OSGiFrameworkServiceFactory implements RemoteServiceFactory {
    private ServiceTracker monitorAdminServiceTracker;
    private final ConcurrentMap<Object, OSGiFrameworkImpl> services = new ConcurrentHashMap<Object, OSGiFrameworkImpl>();

    public OSGiFrameworkServiceFactory(ServiceTracker monitorAdminST) {
        monitorAdminServiceTracker = monitorAdminST;
    }

    @Override
    public Object getService(String clientIP, ServiceReference reference) {
        System.out.println("*** Called getService: " + clientIP + "#" + reference);
        return getService(clientIP);
    }

    @Override
    public void ungetService(String clientIP, ServiceReference reference, Object service) {
        System.out.println("*** Called ungetService: " + clientIP + "#" + reference + "#" + service);
    }

    private Object getService(String ipAddr) {
        OSGiFrameworkImpl newSvc = new OSGiFrameworkImpl(ipAddr, monitorAdminServiceTracker);
        OSGiFrameworkImpl oldSvc = services.putIfAbsent(ipAddr, newSvc);
        return oldSvc == null ? newSvc : oldSvc;
    }
}
