package org.coderthoughts.cloud.framework.service.impl;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.cxf.dosgi.dsw.ClientInfo;
import org.apache.cxf.dosgi.dsw.RemoteServiceFactory;
import org.coderthoughts.cloud.framework.service.api.FrameworkStatus;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;


public class RemoteOSGiFrameworkFactoryService implements RemoteServiceFactory<FrameworkStatus> {
    private final BundleContext bundleContext;
    private final ConcurrentMap<String, ServiceReference> variables = new ConcurrentHashMap<String, ServiceReference>();
    private final ConcurrentMap<Tuple, ServiceReference> serviceVariables = new ConcurrentHashMap<Tuple, ServiceReference>();
    private final ConcurrentMap<Object, FrameworkStatusImpl> services = new ConcurrentHashMap<Object, FrameworkStatusImpl>();

    public RemoteOSGiFrameworkFactoryService(BundleContext ctx) {
        bundleContext = ctx;
    }

    @Override
    public FrameworkStatus getService(ClientInfo client, ServiceReference reference, Method method, Object[] args) {
        System.out.println("*** Called getService: " + client + "#" + reference);

        // I guess the following would do fine too. We don't need PrototypeServiceFactories here...
        FrameworkStatusImpl newSvc = new FrameworkStatusImpl(this, client, bundleContext);
        FrameworkStatusImpl oldSvc = services.putIfAbsent(client, newSvc);
        return oldSvc == null ? newSvc : oldSvc;
    }

    @Override
    public void ungetService(ClientInfo client, ServiceReference reference, FrameworkStatus service, Method method, Object[] args, Object rv) {
        System.out.println("*** Finished with the invocation of " + method + " with args " + Arrays.toString(args) + ". Return value: " + rv);
        // TODO clean out old services when things get too full.
    }

    Map<String, ServiceReference> getFrameworkVariables() {
        return variables;
    }

    Map<Tuple, ServiceReference> getServiceVariables() {
        return serviceVariables;
    }

    static class Tuple {
        final String serviceVariableName;
        final Collection<Long> serviceIDs;

        Tuple(String name, Collection<Long> ids) {
            serviceVariableName = name;
            serviceIDs = ids;
        }
    }
}
