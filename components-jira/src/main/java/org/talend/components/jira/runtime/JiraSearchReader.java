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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avro.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.jira.datum.Entity;
import org.talend.components.jira.datum.Search;

/**
 * 
 */
public class JiraSearchReader extends JiraReader {
    
    private static final Logger LOG = LoggerFactory.getLogger(JiraSearchReader.class);
    
    /**
     * Jira pagination parameter, which defines total number of entities
     */
    private int total = 0;
    
    /**
     * Jira pagination http parameter, which defines from which entity to start
     */
    private int startAt = 0;
    
    /**
     * Jira pagination http parameter, which defines page size 
     * (number of entities per request)
     */
    private int maxResults = 50;

    /**
     * {@inheritDoc}
     */
    public JiraSearchReader(JiraSource source, String hostPort, String resource, String user, String password,
            Map<String, Object> sharedParameters, Schema schema, RuntimeContainer container) {
        super(source, hostPort, resource, user, password, sharedParameters, schema, container);
        
        if (sharedParameters.containsKey("maxResults")) {
            Object maxResultsValue = sharedParameters.get("maxResults");
            if (maxResultsValue instanceof Integer) {
                maxResults = ((Integer) maxResultsValue).intValue();
            } else {
                LOG.debug("Wrong maxResult parameter type: {}", maxResultsValue.getClass().getName());
            }
        } else {
            LOG.debug("Shared parameters doesn't contain maxResults parameter");
        }
    }
    
    /**
     * Prepares and returns map with http parameters.
     * It includes startAt parameter, which is required for pagination 
     */
    @Override
    protected Map<String, Object> prepareParameters() {
        Map<String, Object> sharedParameters = super.prepareParameters();
        Map<String, Object> parameters = new HashMap<>(sharedParameters);
        parameters.put("startAt", startAt);
        return parameters;
    }
    
    /**
     * Process response. Updates total and startAt value.
     * Retrieves entities from response
     */
    protected List<Entity> processResponse(String response) {
        Search search = new Search(response);
        total = search.getTotal();
        startAt = startAt + maxResults;
        List<Entity> entities = search.getEntities();
        return entities;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void requestMoreRecords() throws IOException {
        if (startAt < total) {
            makeHttpRequest();
        }
    }

}
