package org.coderthoughts.cloud.framework.service.api;

import java.util.Map;

public interface FrameworkNodeStatus {
    // Service Registration property keys
    static final String SERVICE_VARIABLES_KEY = "org.osgi.service.variables";
    static final String FRAMEWORK_VARIABLES_KEY = "org.osgi.framework.variables";

    static final String FV_AVAILABLE_MEMORY = "available.memory";

    static final String MONITORABLE_SERVICE_PID_PREFIX = "service."; // To be suffixed by the local service ID

    static final String SV_STATUS = "service.status"; // The external variable
    static final String SERVICE_STATUS_PREFIX = SV_STATUS + "."; // To be suffixed by the client IP
    static final String SERVICE_STATUS_OK = "OK"; // HTTP 200
    static final String SERVICE_STATUS_PAYMENT_NEEDED = "PAYMENT_NEEDED"; // HTTP 402
    static final String SERVICE_STATUS_UNAUTHORIZED = "UNAUTHORIZED"; // HTTP 401
    static final String SERVICE_STATUS_FORBIDDEN = "FORBIDDEN"; // HTTP 403
    static final String SERVICE_STATUS_NOT_FOUND = "NOT_FOUND"; // HTTP 404
    static final String SERVICE_STATUS_QUOTA_EXCEEDED = "QUOTA_EXCEEDED"; // HTTP 413
    static final String SERVICE_STATUS_SERVER_ERROR = "SERVER_ERROR"; // HTTP 500
    static final String SERVICE_STATUS_TEMPORARY_UNAVAILABLE = "TEMPORARY_UNAVAILABLE"; // HTTP 503

    // We could think of additional error codes that specify 'near error' situations:
    static final String SERVICE_STATUS_OK_QUOTA_ALMOST_EXCEEDED = "OK_QUOTA_ALMOST_EXCEEDED"; // Do we want to report how many invocations left?
    static final String SERVICE_STATUS_OK_PAYMENT_INFO_NEEDED_SOON = "OK_PAYMENT_INFO_NEEDED_SOON"; // Credit card about to expire, do we want to report when?

    // other status codes can be added by the user by using reverse dns prefix
    // e.g. org.acme.wrong.region

    // Returns an int. The lower the value the better
    static final String SERVICE_LOAD = "service.load";

    /**
     * Returns the list of available framework status names.
     * @return The list of available framework status names.
     */
    String[] listFrameworkVariableNames();

    /**
     * Obtain a framework variable value.
     *
     * @param name The framework variable to obtain.
     * @return The value of the framework variable.
     */
    String getFrameworkVariable(String name);

    /**
     * Obtain a number of framework variable names using regular expression pattern matching.
     *
     * @param filters The filters to match. Every filter is matched against all known variables.
     * @return A map representing all matching variables. The key is the variable name and the value
     * is the variable value.
     */
    Map<String, String> getFrameworkVariables(String ... filters);
}
