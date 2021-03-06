package org.coderthoughts.cloud.framework.service.api;

import org.apache.cxf.dosgi.dsw.ClientContext;

public interface FrameworkNodeAddition {
    static final String ADD_PROPERTIES_KEY = "add.properties";
    static final String ADD_VARIABLES_KEY = "add.variables";

    String getFrameworkVariable(String name, ClientContext client);
}
