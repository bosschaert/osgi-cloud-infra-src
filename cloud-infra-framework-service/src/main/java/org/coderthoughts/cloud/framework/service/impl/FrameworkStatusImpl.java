package org.coderthoughts.cloud.framework.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.apache.cxf.dosgi.dsw.ClientInfo;
import org.coderthoughts.cloud.framework.service.api.FrameworkStatus;
import org.coderthoughts.cloud.framework.service.api.FrameworkStatusAddition;
import org.coderthoughts.cloud.framework.service.impl.RemoteOSGiFrameworkFactoryService.Tuple;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

public class FrameworkStatusImpl implements FrameworkStatus {
    private final RemoteOSGiFrameworkFactoryService frameworkService;
    private final ClientInfo client;
    private final BundleContext bundleContext;

    public FrameworkStatusImpl(RemoteOSGiFrameworkFactoryService fs, ClientInfo client, BundleContext bc) {
        this.frameworkService = fs;
        this.client = client;
        this.bundleContext = bc;
    }

    @Override
    public String[] listFrameworkVariableNames() {
        return frameworkService.getFrameworkVariables().keySet().toArray(new String[] {});
    }

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
    public String getFrameworkVariable(String name) {
        System.out.println("*** Obtaining framework variable: " + name + " requester IP:" + client);
        if (FV_AVAILABLE_MEMORY.equals(name)) {
            return "" + Runtime.getRuntime().freeMemory();
        }
        throw new IllegalArgumentException(name);
    }


    @Override
    public String getServiceVariable(long serviceID, String name) {
        System.out.println("*** Obtaining info: " + name + " for " + serviceID + " requester IP:" + client);

        try {
            ServiceReference[] additionRefs =
                    bundleContext.getServiceReferences(FrameworkStatusAddition.class.getName(),
                    "(&(service.variables=" + name + ")(service.ids=" + serviceID + ")");
            sortServiceReferences(additionRefs);
            for (ServiceReference ref : additionRefs) {
                FrameworkStatusAddition addition = (FrameworkStatusAddition) bundleContext.getService(ref);
                return addition.getServiceVariable(serviceID, name, client);
            }
            return FrameworkStatus.SERVICE_STATUS_NOT_FOUND;
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
}
