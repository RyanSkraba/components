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
import java.util.NoSuchElementException;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.joda.time.Instant;
import org.talend.components.api.component.runtime.Reader;
import org.talend.components.api.component.runtime.Source;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.jira.avro.IssueAdapterFactory;
import org.talend.components.jira.avro.IssueIndexedRecord;
import org.talend.components.jira.connection.Rest;
import org.talend.components.jira.datum.Entity;
import org.talend.components.jira.datum.Search;
import org.talend.daikon.avro.IndexedRecordAdapterFactory;

/**
 * Jira reader implementation
 */
public class JiraReader implements Reader<IndexedRecord> {
    
    /**
     * Specifies some integer value is undefined
     */
    private static final int UNDEFINED = -1;

    /**
     * {@link Source} instance, which had created this {@link Reader}
     */
    private JiraSource source;

    /**
     * Apache Http components library wrapper, which provides connection methods
     */
    private Rest rest;

    /**
     * Issue adaptor factory
     */
    private IndexedRecordAdapterFactory<String, IssueIndexedRecord> factory;

    /**
     * Jira resource to get
     */
    private String resource;

    /**
     * JSON string, which represents result obtained from Jira server
     */
    private String response;

    /**
     * A list of Jira {@link Entity} obtained from Jira server
     */
    private List<Entity> entities;

    /**
     * Index of current Jira entity
     */
    private int entityIndex = 0;
    
    /**
     * Number of Jira entities read
     */
    private int entityCounter = 0;
    
    /**
     * Jira pagination http parameter, which defines page size 
     * (number of entities per request)
     */
    private int maxResults = 50;
    
    /**
     * Jira pagination http parameter, which defines from which entity to start
     */
    private int startAt = 0;
    
    /**
     * Jira pagination parameter, which defines total number of entities
     */
    private int total = UNDEFINED;

    /**
     * Stores http query parameters which are shared between requests
     */
    private Map<String, String> sharedParameters;
    
    /**
     * Runtime container
     */
    private final RuntimeContainer container;
    
    /**
     * Denotes this {@link Reader} was started
     */
    private boolean started;
    
    /**
     * Denotes this {@link Reader} has more records
     */
    private boolean hasMoreRecords;

    /**
     * Constructor sets required properties for http connection
     * 
     * @param source instance of {@link Source}, which had created this {@link Reader}
     * @param hostPort url of Jira instance
     * @param resource REST resource to communicate
     * @param user Basic authorization user id
     * @param password Basic authorizatiion password
     * @param sharedParameters map with http parameter which are shared between requests. It could include maxResult
     * @param Schema data schema
     * parameter
     */
    public JiraReader(JiraSource source, String hostPort, String resource, String user, String password,
            Map<String, String> sharedParameters, Schema schema, RuntimeContainer container) {
        this.source = source;
        this.resource = resource;
        this.sharedParameters = sharedParameters;
        this.container = container;
        rest = new Rest(hostPort);
        rest.setCredentials(user, password);
        
        String maxRelultValue = sharedParameters.get("maxResults");
        if (maxRelultValue != null) {
            maxResults = Integer.parseInt(maxRelultValue);
        }
        
        factory = new IssueAdapterFactory();
        factory.setSchema(schema);
    }

    /**
     * {@inheritDoc}
     * @throws IOException in case of exception during http connection 
     */
    @Override
    public boolean start() throws IOException {
        started = true;
        makeHttpRequest();
        return hasMoreRecords;
    }

    /**
     * {@inheritDoc}
     * @throws IOException in case {@link JiraReader#start()} wasn't invoked or
     * in case of exception during http connection
     */
    @Override
    public boolean advance() throws IOException {
        if (!started) {
            throw new IOException("Reader wasn't started");
        }
        entityIndex++;
        entityCounter++;

        if (entityIndex < entities.size()) {
            return true;
        } else {
            hasMoreRecords = false;
            // try to get more
            if (startAt < total) {
                makeHttpRequest();
            }
        }
        return hasMoreRecords;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IndexedRecord getCurrent() throws NoSuchElementException {
        if (!started) {
            throw new NoSuchElementException("Reader wasn't started");
        }
        if (!hasMoreRecords) {
            throw new NoSuchElementException("No records available");
        }
        
        Entity entity = entities.get(entityIndex);
        String json = entity.getJson();
        return factory.convertToAvro(json);
    }

    /**
     * @return null
     * @throws NoSuchElementException in case {@link JiraReader} wasn't started or 
     * there is no more records available
     */
    @Override
    public Instant getCurrentTimestamp() throws NoSuchElementException {
        if (!started) {
            throw new NoSuchElementException("Reader wasn't started");
        }
        if (!hasMoreRecords) {
            throw new NoSuchElementException("No records available");
        }
        return null;
    }

    @Override
    public void close() throws IOException {
        container.setComponentData(container.getCurrentComponentId(), "_numberOfRecords",
                entityCounter);
    }

    /**
     * Returns {@link Source} instance
     * 
     * @return {@link Source} instance, which had created this {@link Reader}
     */
    @Override
    public Source getCurrentSource() {
        return source;
    }

    /**
     * Makes http request to the server and process its response
     * 
     * @throws IOException in case of exception during http connection
     */
    protected void makeHttpRequest() throws IOException {
        Map<String, String> parameters = prepareParameters();
        response = rest.get(resource, parameters);
        processResponse();
    }

    /**
     * Prepares and returns map with http parameters suitable for current REST API resource.
     * 
     * @return map with http parameters
     */
    protected Map<String, String> prepareParameters() {
        Map<String, String> parameters = new HashMap<>(sharedParameters);
        parameters.put("startAt", Integer.toString(startAt));
        return parameters;
    }

    /**
     * Process response. Updates total and startAt value.
     * Retrieves entities from response
     */
    protected void processResponse() {
        Search search = new Search(response);
        // FIXME
        if (resource.endsWith("search")) {
            total = search.getTotal();
        }
        // /rest/api/2/project doesn't support paging, so total is set to 0 to be less than startAt
        if (resource.endsWith("project")) {
            total = 0;
        }
        startAt = startAt + maxResults;
        entities = search.getEntities();
        entityIndex = 0;
        if (!entities.isEmpty()) {
            hasMoreRecords = true;
            entityCounter++;
        }
    }

}
