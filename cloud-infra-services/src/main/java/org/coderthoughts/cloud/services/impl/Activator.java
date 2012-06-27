package org.coderthoughts.cloud.services.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.remoteserviceadmin.EndpointDescription;
import org.osgi.service.remoteserviceadmin.EndpointListener;
import org.osgi.service.remoteserviceadmin.ExportRegistration;
import org.osgi.service.remoteserviceadmin.RemoteServiceAdmin;
import org.osgi.util.tracker.ServiceTracker;

public class Activator implements BundleActivator {
    private Map<ServiceReference, EndpointListener> endpointListeners = new ConcurrentHashMap<ServiceReference, EndpointListener>();
    private Map<Long, Collection<ExportRegistration>> registrations = new ConcurrentHashMap<Long, Collection<ExportRegistration>>();
    private RemoteServiceAdmin rsa;
    private ServiceTracker cloudServiceTracker;
    private ServiceTracker endpointListenerServiceTracker;
    private ServiceTracker rsaServiceTracker;

    @Override
    public void start(final BundleContext context) throws Exception {
        rsaServiceTracker = new ServiceTracker(context, RemoteServiceAdmin.class.getName(), null);

        endpointListenerServiceTracker = new ServiceTracker(context, EndpointListener.class.getName(), null) {
            @Override
            public Object addingService(ServiceReference reference) {
                Object svc = super.addingService(reference);
                if (svc instanceof EndpointListener) {
                    endpointListeners.put(reference, (EndpointListener) svc);
                }
                return svc;
            }

            @Override
            public void removedService(ServiceReference reference, Object service) {
                endpointListeners.remove(service);
                super.removedService(reference, service);
            }
        };
        endpointListenerServiceTracker.open();

        Filter filter = context.createFilter(
                "(&(service.exported.interfaces=*)(service.exported.configs=org.coderthoughts.configtype.cloud))");
        cloudServiceTracker = new ServiceTracker(context, filter, null) {
            @Override
            public Object addingService(ServiceReference reference) {
                registerRemotedService(reference);
                return super.addingService(reference);
            }

            @Override
            public void removedService(ServiceReference reference, Object service) {
                unRegisterRemotedService(reference);
                super.removedService(reference, service);
            }
        };

        Runnable r = new Runnable() {
            @Override
            public void run() {
                rsaServiceTracker.open();

                try {
                    rsa = (RemoteServiceAdmin) rsaServiceTracker.waitForService(30000);
                    System.out.println("*** Found RSA: " + rsa);
                    if (rsa == null)
                        throw new RuntimeException("");

                    cloudServiceTracker.open();
                } catch (InterruptedException e) {
                }
            }
        };
        new Thread(r).start();
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        cloudServiceTracker.close();
        rsaServiceTracker.close();
        endpointListenerServiceTracker.close();
    }

    private void registerRemotedService(ServiceReference sr) {
        System.out.println("@@@ About to export: " + sr);
        Long serviceID = (Long) sr.getProperty(Constants.SERVICE_ID);
        if (registrations.containsKey(serviceID))
            return;

        Map<String, Object> props = new HashMap<String, Object>();
        props.put("service.exported.configs", "org.apache.cxf.ws");
        props.put("org.apache.cxf.ws.httpservice.context", "/" + serviceID);

        Collection<ExportRegistration> regs = rsa.exportService(sr, props);
        System.out.println("@@@ Exported: " + regs);
        registrations.put(serviceID, regs);

        // Call interested EndpointListeners (will publish in Discovery)
        // TODO can this piece perform what the zookeeper-plugin is doing?
        for (ServiceReference elSR : endpointListeners.keySet()) {
            for (String scope : getEndpointListenerScopes(elSR)) {
                try {
                    Filter filter = FrameworkUtil.createFilter(scope);
                    for (ExportRegistration reg : regs) {
                        EndpointDescription ed = reg.getExportReference().getExportedEndpoint();
                        Hashtable<String, Object> ht = new Hashtable<String, Object>(ed.getProperties());
                        if (filter.match(ht)) {
                            EndpointListener el = endpointListeners.get(elSR);
                            if (el != null)
                                el.endpointAdded(ed, scope);
                        }
                    }
                } catch (InvalidSyntaxException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> getEndpointListenerScopes(ServiceReference elSR) {
        Object scope = elSR.getProperty(EndpointListener.ENDPOINT_LISTENER_SCOPE);
        if (scope == null)
            return Collections.emptyList();

        if (scope instanceof String)
            return Collections.singletonList((String) scope);

        if (scope instanceof String [])
            return Arrays.asList((String []) scope);

        if (scope instanceof List)
            return (List<String>) scope;

        throw new IllegalArgumentException("Invalid scope: " + scope + "(" + elSR + ")");
    }

    private void unRegisterRemotedService(ServiceReference sr) {
        System.out.println("**** Unregistered matching service: " + sr);
        Long serviceID = (Long) sr.getProperty(Constants.SERVICE_ID);
        for(ExportRegistration reg : registrations.get(serviceID)) {
            reg.close();
        }
    }
}
