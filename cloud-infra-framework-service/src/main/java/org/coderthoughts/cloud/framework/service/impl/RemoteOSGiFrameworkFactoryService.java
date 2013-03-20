package org.coderthoughts.cloud.framework.service.impl;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.cxf.dosgi.dsw.ClientInfo;
import org.apache.cxf.dosgi.dsw.RemoteServiceFactory;
import org.coderthoughts.cloud.framework.service.api.FrameworkStatus;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;


public class RemoteOSGiFrameworkFactoryService implements RemoteServiceFactory<FrameworkStatus> {
    private final BundleContext bundleContext;
    private final ServiceTracker monitorAdminServiceTracker;
    private final ConcurrentMap<Object, FrameworkStatusImpl> services = new ConcurrentHashMap<Object, FrameworkStatusImpl>();

    public RemoteOSGiFrameworkFactoryService(BundleContext ctx, ServiceTracker monitorAdminST) {
        bundleContext = ctx;
        monitorAdminServiceTracker = monitorAdminST;
    }

    @Override
    public FrameworkStatus getService(ClientInfo client, ServiceReference reference) {
        System.out.println("*** Called getService: " + client + "#" + reference);

        // I guess the following would do fine too. We don't need PrototypeServiceFactories here...
        FrameworkStatusImpl newSvc = new FrameworkStatusImpl(client, monitorAdminServiceTracker);
        FrameworkStatusImpl oldSvc = services.putIfAbsent(client, newSvc);
        return oldSvc == null ? newSvc : oldSvc;
    }

    @Override
    public void ungetService(ClientInfo client, ServiceReference reference, FrameworkStatus service) {
    }
}
