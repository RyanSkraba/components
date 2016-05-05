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
    private String inputResult;

    /**
     * A list of Jira {@link Entity} obtained from Jira server
     */
    private List<Entity> entities;

    /**
     * Index of current Jira entity
     */
    private int entityIndex = 0;
    
    /**
     * Number of Jira entities obtained
     */
    private int entityCounter = 0;
    
    /**
     * Jira paging http parameter, which defines page size 
     * (number of entities per request)
     */
    private int maxResults = 50;
    
    /**
     * Jira paging http parameter, which defines from which entity to start
     */
    private int startAt = 0;
    
    /**
     * Jira paging parameter, which defines total number of entities
     */
    private int total = UNDEFINED;

    /**
     * Stores http query parameters which are shared between requests
     */
    private Map<String, String> sharedParameters;
    
    /**
     * Data schema
     */
    private Schema schema;
    
    private final RuntimeContainer container;

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
        this.schema = schema;
        this.container = container;
        rest = new Rest(hostPort);
        rest.setCredentials(user, password);
        
        String maxRelultValue = sharedParameters.get("maxResults");
        if (maxRelultValue != null) {
            maxResults = Integer.parseInt(maxRelultValue);
        }
    }

    /**
     * TODO implement it
     * 
     * @return
     * @throws IOException
     */
    @Override
    public boolean start() throws IOException {
        if (!queryNextPage()) {
            return false;
        }
        Search search = new Search(inputResult);
        entities = search.getEntities();
        return true;
    }

    /**
     * TODO implement it
     */
    @Override
    public boolean advance() throws IOException {
        entityIndex++;

        if (entityIndex < entities.size()) {
            return true;
        }
        if (!queryNextPage()) {
            return false;
        }
        Search search = new Search(inputResult);
        entities = search.getEntities();
        entityIndex = 0;

        return true;
    }

    @Override
    public IndexedRecord getCurrent() throws NoSuchElementException {
        entityCounter++;
        Entity entity = entities.get(entityIndex);
        String json = entity.getJson();
        return getFactory().convertToAvro(json);
    }

    /**
     * TODO implement it (extend from BoundedReader)
     * 
     * @return
     * @throws NoSuchElementException
     */
    @Override
    public Instant getCurrentTimestamp() throws NoSuchElementException {
        // TODO Auto-generated method stub
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
     * Returns an instance of {@link IndexedRecordAdapterFactory}
     * 
     * @return {@link IssueAdapterFactory}
     */
    private IndexedRecordAdapterFactory<String, IssueIndexedRecord> getFactory() {
        if (factory == null) {
            factory = new IssueAdapterFactory();
            factory.setSchema(schema);
        }
        return factory;
    }

    /**
     * 
     * 
     * @return next response result
     * @throws IOException in case of exception during http connection
     */
    private boolean queryNextPage() throws IOException {
        
        // check next pages availability
        if (total != UNDEFINED && startAt >= total) {
            return false;
        }
        
        // generate parameters
        Map<String, String> parameters = new HashMap<>(sharedParameters);
        parameters.put("startAt", Integer.toString(startAt));
        
        // make request
        inputResult = rest.get(resource, parameters);
        
        // readTotal
        if (total == UNDEFINED) {
            if (resource.endsWith("search")) {
                Search search = new Search(inputResult);
                total = search.getTotal();
            }
            // /rest/api/2/project doesn't support paging, so total is set to 0 to be less than startAt
            if (resource.endsWith("project")) {
                total = 0;
            }
        }
        
        // iterate startAt
        startAt = startAt + maxResults;
        
        if (inputResult == null || inputResult.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

}
