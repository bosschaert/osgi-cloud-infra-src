package org.coderthoughts.cloud.framework.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.cxf.dosgi.dsw.ClientInfo;
import org.coderthoughts.cloud.framework.service.api.FrameworkNodeStatus;
import org.osgi.framework.BundleContext;

public class FrameworkNodeStatusImpl implements FrameworkNodeStatus {
    private final RemoteOSGiFrameworkFactoryService frameworkService;
    private final ClientInfo client;
    private final BundleContext bundleContext;

    public FrameworkNodeStatusImpl(RemoteOSGiFrameworkFactoryService fs, ClientInfo client, BundleContext bc) {
        this.frameworkService = fs;
        this.client = client;
        this.bundleContext = bc;
    }

    @Override
    public String[] listFrameworkVariableNames() {
        return frameworkService.getFrameworkVariables().keySet().toArray(new String[] {});
    }

    @Override
    public String getFrameworkVariable(String name) {
        System.out.println("*** Obtaining framework variable: " + name + " requester IP:" + client);
        if (FV_AVAILABLE_MEMORY.equals(name)) {
            return "" + Runtime.getRuntime().freeMemory();
        }
        throw new IllegalArgumentException(name);
    }

    @Override
    public Map<String, String> getFrameworkVariables(String... filters) {
        Map<String, String> m = new HashMap<String, String>();
        for (String filter : filters) {
            // we only have one variable right now...
            if (FV_AVAILABLE_MEMORY.matches(filter)) {
                m.put(FV_AVAILABLE_MEMORY, "" + Runtime.getRuntime().freeMemory());
            }
        }
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