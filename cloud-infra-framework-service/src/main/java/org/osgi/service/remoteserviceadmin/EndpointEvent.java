package org.osgi.service.remoteserviceadmin;

import java.util.EventObject;

public class EndpointEvent extends EventObject {
    private static final long serialVersionUID = -3530133442602636710L;

    public static final int ADDED = 0x00000001;
    public static final int REMOVED = 0x00000002;
    public static final int MODIFIED = 0x00000004;
    public static final int MODIFIED_ENDMATCH = 0x00000008;

    private final EndpointDescription endpoint;
    private final String matchedFilter;
    private final int type;

    public EndpointEvent(int type, EndpointDescription endpoint, String matchedFilter) {
        super(endpoint);
        this.endpoint = endpoint;
        this.matchedFilter = matchedFilter;
        this.type = type;
    }

    public EndpointDescription getEndpoint() {
        return endpoint;
    }

    public String getMatchedFilter() {
        return matchedFilter;
    }

    public int getType() {
        return type;
    }
}
