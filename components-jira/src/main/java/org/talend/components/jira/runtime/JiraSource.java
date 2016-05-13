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
import java.util.List;

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
     * Optional Jira project ID property
     */
    private String projectId;
    
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
            this.projectId = inputProperties.projectId.getStringValue();
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

        JiraReader reader = null;
        switch (resourceType) {
        case TJiraInputProperties.ISSUE: {
            reader =  new JiraSearchReader(this, container);
            break;
        }
        case TJiraInputProperties.PROJECT: {
            if (projectId != null && !projectId.isEmpty()) {
                reader = new JiraProjectIdReader(this, container, projectId);
            } else {
                reader = new JiraProjectsReader(this, container);
            }
            break;
        }
        default: {
            reader = new JiraSearchReader(this, container);
            break;
        }
        }
        return reader;
    }
    
    /**
     * Returns hostPort
     * 
     * @return the hostPort
     */
    String getHostPort() {
        return hostPort;
    }

    /**
     * Returns userId
     * 
     * @return the userId
     */
    String getUserId() {
        return userId;
    }

    /**
     * Returns password
     * 
     * @return the password
     */
    String getPassword() {
        return password;
    }

    /**
     * Returns resourceType
     * 
     * @return the resourceType
     */
    String getResourceType() {
        return resourceType;
    }

    /**
     * Returns dataSchema
     * 
     * @return the dataSchema
     */
    Schema getDataSchema() {
        return dataSchema;
    }

    /**
     * Returns Jira search query
     * 
     * @return Jira search query
     */
    String getJql() {
        return jql;
    }
    
    /**
     * Returns batch size
     * 
     * @return the batch size
     */
    int getBatchSize() {
        return batchSize;
    }
    
    /**
     * Returns Jira project ID
     * 
     * @return Jira project ID
     */
    String getProjectId() {
        return projectId;
    }
}
