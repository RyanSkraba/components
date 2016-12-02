package org.talend.components.jira.connection;

/**
 * Jira server response. It stores HTTP status code and body string
 */
public final class JiraResponse {
    
    /**
     * HTTP status code
     */
    private final int statusCode;
    
    /**
     * HTTP response body
     */
    private final String body;
    
    /**
     * Constructor sets status code and response body
     * 
     * @param statusCode status code
     * @param body response body; in case of null "" will be set
     */
    public JiraResponse(final int statusCode, final String body) {
        this.statusCode = statusCode;
        this.body = body == null ? "" : body;
    }
    
    /**
     * Returns response body
     * 
     * @return response body
     */
    public String getBody() {
        return body;
    }

    /**
     * Returns response status code
     * 
     * @return response status code
     */
    public int getStatusCode() {
        return statusCode;
    }

}
