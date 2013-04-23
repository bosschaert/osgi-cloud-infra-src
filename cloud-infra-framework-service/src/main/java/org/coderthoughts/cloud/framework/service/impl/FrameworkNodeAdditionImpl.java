package org.coderthoughts.cloud.framework.service.impl;

import org.apache.cxf.dosgi.dsw.ClientContext;
import org.coderthoughts.cloud.framework.service.api.FrameworkNodeAddition;
import org.coderthoughts.cloud.framework.service.api.FrameworkNodeStatus;

public class FrameworkNodeAdditionImpl implements FrameworkNodeAddition {
    @Override
    public String getFrameworkVariable(String name, ClientContext client) {
        System.out.println("*** Obtaining framework variable: " + name + " requester IP:" + client);
        if (FrameworkNodeStatus.FV_AVAILABLE_MEMORY.equals(name)) {
            return "" + Runtime.getRuntime().freeMemory();
        }
        throw new IllegalArgumentException(name);
    }
}
