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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field.Order;
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

    private static final long serialVersionUID = 1L;

    /**
     * Jira REST API version. It is a part of REST URL
     */
    private static final String REST_VERSION = "/rest/api/2/";

    /**
     * Jira component properties
     */
    private TJiraInputProperties properties;

    /**
     * Stores component properties in this object
     * 
     * @param container runtime container
     * @param properties
     */
    @Override
    public void initialize(RuntimeContainer container, ComponentProperties properties) {
        // FIXME could it throw cast exception?
        this.properties = (TJiraInputProperties) properties;
    }

    /**
     * What should I validate here? validate connection to Jira here TODO implement it
     * 
     */
    @Override
    public ValidationResult validate(RuntimeContainer container) {
        return ValidationResult.OK;
    }

    /**
     * TODO implement it
     */
    @Override
    public List<NamedThing> getSchemaNames(RuntimeContainer container) throws IOException {
        List<NamedThing> schemaNames = new ArrayList<>();
        return schemaNames;
    }

    /**
     * TODO implement it
     */
    @Override
    public Schema getSchema(RuntimeContainer container, String schemaName) throws IOException {
        Schema.Field jsonField = new Schema.Field("json", Schema.create(Schema.Type.STRING), null, null, Order.ASCENDING);
        return Schema.createRecord("issue", null, null, false, Collections.singletonList(jsonField));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Reader createReader(RuntimeContainer container) {
        String hostPort = properties.host.getStringValue();
        String userId = properties.userPassword.userId.getStringValue();
        String password = properties.userPassword.password.getStringValue();
        String resourcePath = getResourcePath();
        Map<String, String> sharedParameters = getSharedParameters();
        return new JiraReader(this, hostPort, resourcePath, userId, password, sharedParameters);
    }
    
    /**
     * Builds and returns resource path
     * 
     * @return resource path
     */
    String getResourcePath() {
        String resourceType = properties.resource.getStringValue();
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
    Map<String, String> getSharedParameters() {
        Map<String, String> sharedParameters = new HashMap<>();

        String jqlValue = properties.jql.getStringValue();
        if (jqlValue != null && !jqlValue.isEmpty()) {
            String jqlKey = "jql";
            sharedParameters.put(jqlKey, jqlValue);
        }

        String maxResultsValue = properties.batchSize.getStringValue();
        if (maxResultsValue != null && !maxResultsValue.isEmpty()) {
            String maxResultsKey = "maxResults";
            sharedParameters.put(maxResultsKey, maxResultsValue);
        }

        return Collections.unmodifiableMap(sharedParameters);
    }

}
