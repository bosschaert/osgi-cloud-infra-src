package org.coderthoughts.cloud.framework.service.impl;

import org.coderthoughts.cloud.framework.service.api.OSGiFramework;
import org.osgi.service.monitor.MonitorAdmin;
import org.osgi.util.tracker.ServiceTracker;

public class OSGiFrameworkImpl implements OSGiFramework {
    private String ipAddress;
    private ServiceTracker monitorAdminTracker;

    public OSGiFrameworkImpl(String ipAddr, ServiceTracker monitorAdminST) {
        ipAddress = ipAddr;
        monitorAdminTracker = monitorAdminST;
    }

    @Override
    public String getFrameworkVariable(String name) {
        System.out.println("*** Obtaining framework variable: " + name + " requester IP:" + ipAddress);
        if (FV_AVAILABLE_MEMORY.equals(name)) {
            return "" + Runtime.getRuntime().freeMemory();
        }
        throw new IllegalArgumentException(name);
    }

    @Override
    public String getServiceVariable(long serviceID, String name) {
        System.out.println("*** Obtaining info: " + name + " for " + serviceID + " requester IP:" + ipAddress);
        MonitorAdmin ma = (MonitorAdmin) monitorAdminTracker.getService();
        if (ma == null)
            return SERVICE_STATUS_SERVER_ERROR;

        String monitorableId = MONITORABLE_SERVICE_PID_PREFIX + serviceID;
        String variableName = name + "." + ipAddress;
        boolean variableFound = false;
        for (String var : ma.getStatusVariableNames(monitorableId)) {
            // A little awkward to have to go through all the names, but array traversal is quite fast
            if (variableName.equals(var)) {
                variableFound = true;
                break;
            }
        }
        if (!variableFound) {
            // the service is not yet tracked for the client, we assume that new clients can invoke.
            return SERVICE_STATUS_OK;
        }

        return ma.getStatusVariable(monitorableId + "/" + variableName).getString();
    }
}
