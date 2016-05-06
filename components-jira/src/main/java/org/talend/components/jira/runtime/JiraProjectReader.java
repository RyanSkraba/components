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
import java.util.Map;

import org.apache.avro.Schema;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.jira.datum.Entity;
import org.talend.components.jira.datum.Project;

/**
 * 
 */
public class JiraProjectReader extends JiraReader {
    
    /**
     * {@inheritDoc}
     */
    public JiraProjectReader(JiraSource source, String hostPort, String resource, String user, String password,
            Map<String, String> sharedParameters, Schema schema, RuntimeContainer container) {
        super(source, hostPort, resource, user, password, sharedParameters, schema, container);
    }
    
    /**
     * Process response. Updates total and startAt value.
     * Retrieves entities from response
     */
    protected List<Entity> processResponse(String response) {
        Project project = new Project(response);
        List<Entity> entities = project.getEntities();
        return entities;
    }
    
    /**
     * Does nothing, because project REST API resource doesn't support pagination
     * and only 1 request is possible
     * 
     * @throws IOException doens't throw exception
     */
    @Override
    protected void requestMoreRecords() throws IOException {
        // nothing to do
    }
}
