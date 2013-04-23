package org.coderthoughts.cloud.framework.service.impl;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.cxf.dosgi.dsw.ClientContext;
import org.apache.cxf.dosgi.dsw.RemoteServiceInvocationHandler;
import org.coderthoughts.cloud.framework.service.api.FrameworkNodeStatus;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;


public class RemoteOSGiFrameworkServiceInvocationHandler implements RemoteServiceInvocationHandler<FrameworkNodeStatus> {
    private final BundleContext bundleContext;
//    private final ConcurrentMap<String, ServiceReference> variables = new ConcurrentHashMap<String, ServiceReference>();
//    private final ConcurrentMap<Tuple, ServiceReference> serviceVariables = new ConcurrentHashMap<Tuple, ServiceReference>();
    private final ConcurrentMap<Object, FrameworkNodeStatusImpl> services = new ConcurrentHashMap<Object, FrameworkNodeStatusImpl>();

    public RemoteOSGiFrameworkServiceInvocationHandler(BundleContext ctx) {
        bundleContext = ctx;
    }

    @Override
    public Object invoke(ClientContext client, ServiceReference reference, Method method, Object[] args) {
        System.out.println("*** Called invoke: " + client + "#" + reference + "#" + method + "#" + Arrays.toString(args));

        // I guess the following would do fine too. We don't need PrototypeServiceFactories here...
        FrameworkNodeStatusImpl newSvc = new FrameworkNodeStatusImpl(client, bundleContext);
        FrameworkNodeStatusImpl oldSvc = services.putIfAbsent(client, newSvc);
        FrameworkNodeStatusImpl svc = oldSvc == null ? newSvc : oldSvc;
        try {
            return method.invoke(svc, args);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /*
    @Override
    public FrameworkNodeStatus getService(ClientInfo client, ServiceReference reference, Method method, Object[] args) {
        System.out.println("*** Called getService: " + client + "#" + reference);

        // I guess the following would do fine too. We don't need PrototypeServiceFactories here...
        FrameworkNodeStatusImpl newSvc = new FrameworkNodeStatusImpl(client, bundleContext);
        FrameworkNodeStatusImpl oldSvc = services.putIfAbsent(client, newSvc);
        return oldSvc == null ? newSvc : oldSvc;
    }

    @Override
    public void ungetService(ClientInfo client, ServiceReference reference, FrameworkNodeStatus service, Method method, Object[] args, Object rv) {
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
    */
}
