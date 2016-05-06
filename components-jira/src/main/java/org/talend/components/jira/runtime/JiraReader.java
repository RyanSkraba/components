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
import org.talend.daikon.avro.IndexedRecordAdapterFactory;

/**
 * Jira reader implementation
 */
public abstract class JiraReader implements Reader<IndexedRecord> {

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
     * Stores http parameters which are shared between requests
     */
    private final Map<String, String> sharedParameters;
    
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
     * parameter
     * @param Schema data schema
     * @param container runtime container
     */
    public JiraReader(JiraSource source, String hostPort, String resource, String user, String password,
            Map<String, String> sharedParameters, Schema schema, RuntimeContainer container) {
        this.source = source;
        this.resource = resource;
        this.sharedParameters = sharedParameters;
        this.container = container;
        rest = new Rest(hostPort);
        rest.setCredentials(user, password);
        
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
            requestMoreRecords();
        }
        return hasMoreRecords;
    }
    
    /**
     * Tries to get more records
     * 
     * @throws IOException in case of exception during http connection
     */
    protected abstract void requestMoreRecords() throws IOException;

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
        entities = processResponse(response);
        if(!entities.isEmpty()) {
            hasMoreRecords = true;
            entityIndex = 0;
            entityCounter++;
        }
    }

    /**
     * Prepares and returns map with http parameters suitable for current REST API resource.
     * Returns shared parameters by default
     * 
     * @return map with shared parameters
     */
    protected Map<String, String> prepareParameters() {
        return sharedParameters;
    }

    /**
     * Process response. Updates http parameters for next request if needed.
     * Retrieves entities from response
     */
    protected abstract List<Entity> processResponse(String response); 

}
