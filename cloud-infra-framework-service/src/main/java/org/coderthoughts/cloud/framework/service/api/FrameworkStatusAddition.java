package org.coderthoughts.cloud.framework.service.api;

import org.apache.cxf.dosgi.dsw.ClientInfo;

public interface FrameworkStatusAddition {
    String[] getAdditionalPropertyKeys();
    Object getAdditionalProperty(String key);

    String[] getFrameworkVariableNames();
    String getFrameworkVariable(String name, ClientInfo client);

    String[] getServiceVariableNames();
    String getServiceVariable(long serviceID, String name, ClientInfo client);
}
