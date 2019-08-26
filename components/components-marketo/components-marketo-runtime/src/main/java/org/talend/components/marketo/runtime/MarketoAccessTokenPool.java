package org.talend.components.marketo.runtime;

import java.util.HashMap;
import java.util.Map;

public class MarketoAccessTokenPool {

    private static MarketoAccessTokenPool ourInstance = new MarketoAccessTokenPool();

    private Map<Integer, String> tokens;

    public static MarketoAccessTokenPool getInstance() {
        return ourInstance;
    }

    private MarketoAccessTokenPool() {
        tokens = new HashMap<>();
    }

    public void invalidateToken(Integer connectionHash) {
        tokens.remove(connectionHash);
    }

    public void setToken(Integer connectionHash, String token) {
        tokens.put(connectionHash, token);
    }

    public String getToken(Integer connectionHash) {
        return tokens.get(connectionHash);
    }

}
