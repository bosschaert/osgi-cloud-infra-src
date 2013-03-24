package org.coderthoughts.cloud.framework.service.api;

import org.apache.cxf.dosgi.dsw.ClientInfo;

public interface FrameworkStatusAddition {
    static final String ADD_PROPERTIES_KEY = "add.properties";
    static final String ADD_VARIABLES_KEY = "add.variables";
    static final String SERVICE_VARIABLES_KEY = "service.variables";
    static final String SERVICE_IDS_KEY = "service.ids";

    String getFrameworkVariable(String name, ClientInfo client);
    String getServiceVariable(long id, String name, ClientInfo client);
}
