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
package org.talend.components.jira.runtime.reader;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.jira.datum.Entity;
import org.talend.components.jira.datum.Search;
import org.talend.components.jira.runtime.JiraSource;

/**
 * {@link JiraReader} for rest/api/2/search Jira REST API resource
 */
public class JiraSearchReader extends JiraReader {
    
    private static final Logger LOG = LoggerFactory.getLogger(JiraSearchReader.class);
    
    private static final String REST_RESOURCE = "rest/api/2/search";
    
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
    public JiraSearchReader(JiraSource source, RuntimeContainer container) {
        super(source, REST_RESOURCE, container);
        //TODO check if user specify blank batch size
        maxResults = source.getBatchSize();
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
     * 
     * @param response http response
     * @return {@link List} of entities retrieved from response
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
