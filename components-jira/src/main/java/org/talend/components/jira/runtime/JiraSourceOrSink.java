// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.jira.runtime;

import static javax.servlet.http.HttpServletResponse.SC_OK;

import java.io.IOException;
import java.util.List;

import org.apache.avro.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.SourceOrSink;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.jira.JiraProperties;
import org.talend.components.jira.Resource;
import org.talend.components.jira.connection.Rest;
import org.talend.daikon.NamedThing;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;

/**
 * Jira Server {@link SourceOrSink}
 */
public class JiraSourceOrSink implements SourceOrSink {

    private static final long serialVersionUID = -3064243115746389073L;

    private static final Logger LOG = LoggerFactory.getLogger(JiraSourceOrSink.class);

    private static final I18nMessages messages = GlobalI18N.getI18nMessageProvider().getI18nMessages(JiraSourceOrSink.class);

    /**
     * Data schema
     */
    private Schema schema;

    /**
     * Jira REST resource path. It is composed of 2 parts: <br>
     * 1. REST API part, which also contains API version. E.g. rest/api/2 <br>
     * 2. resource part. Supported resources are: issue, project <br>
     * Example of full resource: rest/api/2/issue
     */
    private Resource resource;

    /**
     * Host and port of Jira server. E.g. "http://localhost:8080"
     */
    private String hostPort;

    /**
     * Jira user ID, which is used in requests
     */
    private String userId;

    /**
     * Jira user password, which is used in requests
     */
    private String userPassword;

    @Override
    public Schema getEndpointSchema(RuntimeContainer arg0, String arg1) throws IOException {
        return null;
    }

    @Override
    public List<NamedThing> getSchemaNames(RuntimeContainer arg0) throws IOException {
        return null;
    }

    /**
     * Initializes this {@link SourceOrSink} with user specified properties
     * 
     * @param container {@link RuntimeContainer} instance
     * @param properties user specified properties
     */
    @Override
    public ValidationResult initialize(RuntimeContainer container, ComponentProperties properties) {
        JiraProperties jiraProperties = (JiraProperties) properties;
        hostPort = jiraProperties.connection.hostUrl.getValue();
        userId = jiraProperties.connection.basicAuthentication.userId.getValue();
        userPassword = jiraProperties.connection.basicAuthentication.password.getValue();
        resource = jiraProperties.resource.getValue();
        schema = jiraProperties.schema.schema.getValue();
        return ValidationResult.OK;
    }

    /**
     * Validates connection to the Host
     * 
     * @param container Runtime container
     * @return {@link Result#OK} if connection was established and {@link Result#ERROR} otherwise
     */
    @Override
    public ValidationResult validate(RuntimeContainer container) {
        Rest rest = new Rest(hostPort);
        String errorMessage;
        try {
            int statusCode = rest.checkConnection();
            if (statusCode == SC_OK) {
                return ValidationResult.OK;
            } else {
                errorMessage = messages.getMessage("error.wrongStatusCode", statusCode);
                LOG.debug(errorMessage);
            }
        } catch (IOException e) {
            errorMessage = messages.getMessage("error.connectionException", e);
            LOG.debug(errorMessage);
        }
        String validationFailed = messages.getMessage("error.hostNotValidated", hostPort);
        StringBuilder sb = new StringBuilder(validationFailed);
        sb.append(System.lineSeparator());
        sb.append(errorMessage);
        ValidationResult validationResult = new ValidationResult();
        validationResult.setStatus(Result.ERROR);
        validationResult.setMessage(sb.toString());
        return validationResult;
    }

    /**
     * Returns data schema
     * 
     * @return data schema
     */
    public Schema getSchema() {
        return schema;
    }

    /**
     * Returns Jira REST resource
     * 
     * @return Jira REST resource
     */
    public String getResource() {
        return resource.getUrl();
    }

    /**
     * Returns Jira server host and port value resource
     * 
     * @return host and port value
     */
    public String getHostPort() {
        return hostPort;
    }

    /**
     * Returns Jira server user ID
     * 
     * @return Jira server user ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Returns Jira server user password
     * 
     * @return Jira server user password
     */
    public String getUserPassword() {
        return userPassword;
    }

}
