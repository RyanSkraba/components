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

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.Reader;
import org.talend.components.api.component.runtime.Source;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.jira.tjirainput.TJiraInputProperties;
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.ValidationResult;

/**
 * Jira source implementation
 */
public class JiraSource implements Source {

    private static final Logger LOG = LoggerFactory.getLogger(JiraSource.class);
    
    private static final long serialVersionUID = 1L;

    /**
     * Jira REST API version. It is a part of REST URL
     */
    private static final String REST_VERSION = "/rest/api/2/";
    
    /**
     * Host and port number of Jira server
     */
    private String hostPort;
    
    /**
     * Jira user ID
     */
    private String userId;
    
    /**
     * Jira user password
     */
    private String password;
    
    /**
     * Jira REST API resource type.
     * Could be issue or project
     */
    private String resourceType;
    
    /**
     * Schema of data to be retrieved
     */
    private Schema dataSchema;
    
    /**
     * Optional Jira search query property
     */
    private String jql;
    
    /**
     * Optional Jira batch size property
     */
    private int batchSize;

    /**
     * Saves component properties in this object
     * 
     * @param container runtime container
     * @param properties component properties
     */
    @Override
    public void initialize(RuntimeContainer container, ComponentProperties properties) {

        if (properties instanceof TJiraInputProperties) {
            TJiraInputProperties inputProperties = (TJiraInputProperties) properties;
            this.hostPort = inputProperties.host.getStringValue();
            this.userId = inputProperties.userPassword.userId.getStringValue();
            this.password = inputProperties.userPassword.password.getStringValue();
            this.resourceType = inputProperties.resource.getStringValue();
            this.dataSchema = (Schema) inputProperties.schema.schema.getValue();
            this.jql = inputProperties.jql.getStringValue();
            this.batchSize = inputProperties.batchSize.getIntValue();
        } else {
            LOG.error("Wrong properties typs: {}", properties.getClass().getName());
        }
    }

    /**
     * TODO implement it
     */
    @Override
    public ValidationResult validate(RuntimeContainer container) {
        return ValidationResult.OK;
    }

    /**
     * {@inheritDoc}
     * 
     * Component doesn't retrieve schemas from {@link Source}. Static schema is used in UI.
     */
    @Override
    public List<NamedThing> getSchemaNames(RuntimeContainer container) throws IOException {
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * Component doesn't retrieve schemas from {@link Source}. Static schema is used in UI
     */
    @Override
    public Schema getSchema(RuntimeContainer container, String schemaName) throws IOException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Reader<IndexedRecord> createReader(RuntimeContainer container) {
        String resourcePath = getResourcePath();
        Map<String, Object> sharedParameters = getSharedParameters();
       
        JiraReader reader = null;
        switch (resourceType) {
        case TJiraInputProperties.ISSUE: {
            reader =  new JiraSearchReader(this, hostPort, resourcePath, userId, password, sharedParameters, dataSchema, container);
            break;
        }
        case TJiraInputProperties.PROJECT: {
            reader = new JiraProjectsReader(this, hostPort, resourcePath, userId, password, sharedParameters, dataSchema, container);
            break;
        }
        default: {
            reader = new JiraSearchReader(this, hostPort, resourcePath, userId, password, sharedParameters, dataSchema, container);
            break;
        }
        }
        return reader;
    }
    
    /**
     * Builds and returns resource path
     * 
     * @return resource path
     */
    String getResourcePath() {
        String resourcePath = null;
        switch (resourceType) {
        case TJiraInputProperties.ISSUE: {
            resourcePath = REST_VERSION + "search";
            break;
        }
        case TJiraInputProperties.PROJECT: {
            resourcePath = REST_VERSION + "project";
            // TODO Add project id /{projectIdOrKey}
            break;
        }
        default: {
            resourcePath = REST_VERSION + "search";
            break;
        }
        }
        return resourcePath;
    }

    /**
     * Creates and returns map with shared http query parameters
     * 
     * @return shared http parametes
     */
    Map<String, Object> getSharedParameters() {
        Map<String, Object> sharedParameters = new HashMap<>();
        if (jql != null && !jql.isEmpty()) {
            String jqlKey = "jql";
            sharedParameters.put(jqlKey, jql);
        }
        String maxResultsKey = "maxResults";
        sharedParameters.put(maxResultsKey, batchSize);
        return Collections.unmodifiableMap(sharedParameters);
    }
    
    /**
     * Returns hostPort
     * 
     * @return the hostPort
     */
    public String getHostPort() {
        return hostPort;
    }

    /**
     * Returns userId
     * 
     * @return the userId
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Returns password
     * 
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Returns resourceType
     * 
     * @return the resourceType
     */
    public String getResourceType() {
        return resourceType;
    }

    /**
     * Returns dataSchema
     * 
     * @return the dataSchema
     */
    public Schema getDataSchema() {
        return dataSchema;
    }

    /**
     * Returns Jira search query
     * 
     * @return Jira search query
     */
    public String getJql() {
        return jql;
    }
    
    /**
     * Returns batch size
     * 
     * @return the batch size
     */
    public int getBatchSize() {
        return batchSize;
    }
}
