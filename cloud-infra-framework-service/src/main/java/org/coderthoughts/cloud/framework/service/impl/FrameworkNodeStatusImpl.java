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
        // return frameworkService.getFrameworkVariables().keySet().toArray(new String[] {});
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

        /*
        for (String filter : filters) {
            // we only have one variable right now...
            if (FV_AVAILABLE_MEMORY.matches(filter)) {
                m.put(FV_AVAILABLE_MEMORY, "" + Runtime.getRuntime().freeMemory());
            }
        } */
        return m;
    }

    /*
    @Override
    public String[] listServiceVariableNames(long id) {
        List<String> names = new ArrayList<String>();
        for (Tuple t : frameworkService.getServiceVariables().keySet()) {
            if (t.serviceIDs.contains(new Long(id))) {
                names.add(t.serviceVariableName);
            }
        }
        return names.toArray(new String[] {});
    }

    @Override
    public String getServiceVariable(long serviceID, String name) {
        System.out.println("*** Obtaining info: " + name + " for " + serviceID + " requester IP:" + client);

        try {
            ServiceReference[] additionRefs =
                    bundleContext.getServiceReferences(FrameworkStatusAddition.class.getName(),
                    "(&(" + FrameworkStatusAddition.SERVICE_VARIABLES_KEY + "=" + name + ")" +
            		"(" + FrameworkStatusAddition.SERVICE_IDS_KEY + "=" + serviceID + "))");
            if (additionRefs != null) {
                sortServiceReferences(additionRefs);
                for (ServiceReference ref : additionRefs) {
                    FrameworkStatusAddition addition = (FrameworkStatusAddition) bundleContext.getService(ref);
                    return addition.getServiceVariable(serviceID, name, client);
                }
            }
            return FrameworkNodeStatus.SERVICE_STATUS_NOT_FOUND;
        } catch (InvalidSyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private void sortServiceReferences(ServiceReference[] additionRefs) {
        Arrays.sort(additionRefs, new Comparator<ServiceReference>() {
            @Override
            public int compare(ServiceReference o1, ServiceReference o2) {
                Object r1 = o1.getProperty(Constants.SERVICE_RANKING);
                Object r2 = o2.getProperty(Constants.SERVICE_RANKING);

                if (r1 instanceof Integer) {
                    if (r2 instanceof Integer) {
                        if (!r1.equals(r2)) {
                            return ((Integer) r2) - ((Integer) r1);
                        }
                    } else {
                        return -((Integer) r1);
                    }
                }

                Long id1 = (Long) o1.getProperty(Constants.SERVICE_ID);
                Long id2 = (Long) o2.getProperty(Constants.SERVICE_ID);
                return (id1 > id2 ? -1 : 1);
            }
        });
    }
    */
}
