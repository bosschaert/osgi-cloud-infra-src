package org.coderthoughts.cloud.framework.service.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.cxf.dosgi.dsw.ClientContext;
import org.coderthoughts.cloud.framework.service.api.FrameworkNodeAddition;
import org.coderthoughts.cloud.framework.service.api.FrameworkNodeStatus;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

class FrameworkNodeStatusImpl implements FrameworkNodeStatus {
    private final ClientContext client;
    private final BundleContext bundleContext;

    public FrameworkNodeStatusImpl(ClientContext client, BundleContext bc) {
        this.client = client;
        this.bundleContext = bc;
    }

    @Override
    public String[] listFrameworkVariableNames() {
        try {
            Set<String> names = new HashSet<String>();
            ServiceReference[] refs = bundleContext.getServiceReferences(FrameworkNodeAddition.class.getName(), null);
            if (refs != null) {
                for (ServiceReference ref : refs) {
                    names.addAll(Activator.getStringPlusProperty(ref.getProperty(FrameworkNodeAddition.ADD_VARIABLES_KEY)));
                }
            }
            return names.toArray(new String [] {});
        } catch (InvalidSyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getFrameworkVariable(String name) {
        try {
            ServiceReference[] refs = bundleContext.getServiceReferences(FrameworkNodeAddition.class.getName(),
                    "(" + FrameworkNodeAddition.ADD_VARIABLES_KEY + "=" + name + ")");
            if (refs != null && refs.length > 0) {
                Object svc = bundleContext.getService(refs[0]);
                if (svc instanceof FrameworkNodeAddition) {
                    return ((FrameworkNodeAddition) svc).getFrameworkVariable(name, client);
                }
            }
        } catch (InvalidSyntaxException e) {
            throw new RuntimeException(e);
        }
        throw new IllegalArgumentException(name);
    }

    @Override
    public Map<String, String> getFrameworkVariables(String... filters) {
        Map<String, String> m = new HashMap<String, String>();
        try {
            ServiceReference[] refs = bundleContext.getServiceReferences(FrameworkNodeAddition.class.getName(), null);
            if (refs == null)
                return m;

            for (ServiceReference ref : refs) {
                for (String var : Activator.getStringPlusProperty(ref.getProperty(FrameworkNodeAddition.ADD_VARIABLES_KEY))) {
                    for (String filter : filters) {
                        if (var.matches(filter)) {
                            try {
                                m.put(var, ((FrameworkNodeAddition) bundleContext.getService(ref)).getFrameworkVariable(var, client));
                            } catch (Throwable th) {
                                m.put(var, th.getMessage());
                            }
                        }
                    }
                }
            }
        } catch (InvalidSyntaxException e) {
            throw new RuntimeException(e);
        }

        return m;
    }
}
