package org.talend.components.marketo.runtime.client.rest.type;

public class AuthenticationInfo {

    private String secretKey;

    private String clientAccessID;

    public AuthenticationInfo() {
    }

    public AuthenticationInfo(String secretKey, String clientAccessID) {
        this.secretKey = secretKey;
        this.clientAccessID = clientAccessID;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getClientAccessID() {
        return clientAccessID;
    }

    public void setClientAccessID(String clientAccessID) {
        this.clientAccessID = clientAccessID;
    }
}
