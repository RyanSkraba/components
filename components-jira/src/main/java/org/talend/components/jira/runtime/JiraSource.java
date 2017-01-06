// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
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

import org.apache.avro.generic.IndexedRecord;
import org.talend.components.api.component.runtime.Reader;
import org.talend.components.api.component.runtime.Source;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.jira.Resource;
import org.talend.components.jira.runtime.reader.JiraProjectIdReader;
import org.talend.components.jira.runtime.reader.JiraProjectsReader;
import org.talend.components.jira.runtime.reader.JiraReader;
import org.talend.components.jira.runtime.reader.JiraSearchReader;
import org.talend.components.jira.tjirainput.TJiraInputProperties;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;

/**
 * Jira {@link Source} implementation
 */
public class JiraSource extends JiraSourceOrSink implements Source {

    private static final long serialVersionUID = 6087511765623929542L;

    /**
     * Jira REST API resource type. Could be issue or project
     */
    private Resource resourceType;

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
     * @param container {@link RuntimeContainer} instance
     * @param properties component properties
     */
    @Override
    public ValidationResult initialize(RuntimeContainer container, ComponentProperties properties) {
        ValidationResult validate = super.initialize(container, properties);
        if (validate.getStatus() == Result.ERROR) {
            return validate;
        }
        TJiraInputProperties inputProperties = (TJiraInputProperties) properties;
        this.jql = inputProperties.jql.getStringValue();
        this.batchSize = inputProperties.batchSize.getValue();
        this.projectId = inputProperties.projectId.getStringValue();
        this.resourceType = inputProperties.resource.getValue();
        return ValidationResult.OK;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Reader<IndexedRecord> createReader(RuntimeContainer container) {

        JiraReader reader = null;
        switch (resourceType) {
        case ISSUE: {
            reader = new JiraSearchReader(this);
            break;
        }
        case PROJECT: {
            if (projectId != null && !projectId.isEmpty()) {
                reader = new JiraProjectIdReader(this, projectId);
            } else {
                reader = new JiraProjectsReader(this);
            }
            break;
        }
        default: {
            reader = new JiraSearchReader(this);
            break;
        }
        }
        return reader;
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

    /**
     * Returns Jira project ID
     * 
     * @return Jira project ID
     */
    public String getProjectId() {
        return projectId;
    }
}
