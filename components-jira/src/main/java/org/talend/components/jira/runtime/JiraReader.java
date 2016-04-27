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
import java.util.NoSuchElementException;

import org.apache.avro.generic.IndexedRecord;
import org.joda.time.Instant;
import org.talend.components.api.component.runtime.Reader;
import org.talend.components.api.component.runtime.Source;
import org.talend.components.jira.avro.IssueAdapterFactory;
import org.talend.components.jira.avro.IssueIndexedRecord;
import org.talend.components.jira.connection.Rest;
import org.talend.daikon.avro.IndexedRecordAdapterFactory;

/**
 * Jira reader implementation
 */
public class JiraReader implements Reader<IndexedRecord> {

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
    private String jsonResult;

    /**
     * Constructor sets required properties for http connection
     * 
     * @param source instance of {@link Source}, which had created this {@link Reader}
     * @param url url of Jira instance
     * @param resource REST resource to communicate
     * @param user Basic authorization user id
     * @param password Basic authorizatiion password
     */
    public JiraReader(JiraSource source, String url, String resource, String user, String password) {
        this.source = source;
        this.resource = resource;
        rest = new Rest(url);
        rest.setCredentials(user, password);
    }

    /**
     * TODO implement it
     * 
     * @return
     * @throws IOException
     */
    @Override
    public boolean start() throws IOException {
        jsonResult = rest.get(resource);
        if (jsonResult != null && !jsonResult.isEmpty()) {
            return true;
        }
        return false;
    }

    /**
     * TODO implement it
     */
    @Override
    public boolean advance() throws IOException {
        return false;
    }

    @Override
    public IndexedRecord getCurrent() throws NoSuchElementException {
        return getFactory().convertToAvro(jsonResult);
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
        // nothing to do
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
        }
        return factory;
    }

}
