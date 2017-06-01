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
package org.talend.components.salesforce.runtime;

import java.io.IOException;
import java.util.NoSuchElementException;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.salesforce.soql.SoqlQuery;
import org.talend.components.salesforce.tsalesforceinput.TSalesforceInputProperties;
import org.talend.daikon.avro.AvroUtils;

import com.sforce.async.AsyncApiException;
import com.sforce.ws.ConnectionException;

public class SalesforceBulkQueryInputReader extends SalesforceReader<IndexedRecord> {

    private static final Logger LOG = LoggerFactory.getLogger(SalesforceBulkQueryInputReader.class);

    protected SalesforceBulkRuntime bulkRuntime;

    protected BulkResultSet bulkResultSet;

    protected BulkResult currentRecord;

    public SalesforceBulkQueryInputReader(RuntimeContainer container, SalesforceSource source, TSalesforceInputProperties props) {
        super(container, source);
        properties = props;
        this.container = container;
    }

    @Override
    public boolean start() throws IOException {
        if (bulkRuntime == null) {
            bulkRuntime = new SalesforceBulkRuntime(((SalesforceSource) getCurrentSource()).connect(container).bulkConnection);
            if (((TSalesforceInputProperties) properties).pkChunking.getValue()) {
                bulkRuntime.setChunkProperties((TSalesforceInputProperties) properties);
            }
        }
        try {
            executeSalesforceBulkQuery();
        } catch (ConnectionException e) {
            // Wrap the exception in an IOException.
            throw new IOException(e);
        }

        return retrieveNextResultSet();
    }

    @Override
    public boolean advance() throws IOException {
        currentRecord = bulkResultSet.next();
        if (currentRecord == null) {
            return retrieveNextResultSet();
        }
        dataCount++;
        return true;
    }

    private boolean retrieveNextResultSet() throws IOException {
        while (bulkRuntime.hasNextResultId()) {
            String resultId = bulkRuntime.nextResultId();
            if (null != resultId) {
                try {
                    // Get a new result set
                    bulkResultSet = bulkRuntime.getQueryResultSet(resultId);
                } catch (AsyncApiException | ConnectionException e) {
                    throw new IOException(e);
                }

                currentRecord = bulkResultSet.next();
                // If currentRecord is null, we need to check if resultId set has more entries.
                if (null != currentRecord) {
                    // New result set available to retrieve
                    dataCount++;
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void close() throws IOException {
        try {
            bulkRuntime.closeJob();
        } catch (AsyncApiException | ConnectionException e) {
            throw new IOException(e);
        }
    }

    public BulkResult getCurrentRecord() throws NoSuchElementException {
        return currentRecord;
    }

    protected void executeSalesforceBulkQuery() throws IOException, ConnectionException {
        String queryText = getQueryString(properties);
        try {
            bulkRuntime.doBulkQuery(getModuleName(), queryText);
        } catch (AsyncApiException | InterruptedException | ConnectionException e) {
            throw new IOException(e);
        }
    }

    @Override
    public IndexedRecord getCurrent() {
        try {
            return ((BulkResultAdapterFactory) getFactory()).convertToAvro(getCurrentRecord());
        } catch (IOException e) {
            throw new ComponentException(e);
        }
    }

    private String getModuleName() {
        TSalesforceInputProperties inProperties = (TSalesforceInputProperties) properties;
        if (inProperties.manualQuery.getValue()) {
            SoqlQuery query = SoqlQuery.getInstance();
            query.init(inProperties.query.getValue());
            return query.getDrivingEntityName();
        } else {
            return properties.module.moduleName.getValue();
        }
    }

    @Override
    protected Schema getSchema() throws IOException {
        if (querySchema == null && AvroUtils.isIncludeAllFields(properties.module.main.schema.getValue())) {
            // This for bulk manual query dynamic to generate schema based on soql
            if (properties instanceof TSalesforceInputProperties) {
                TSalesforceInputProperties inProperties = (TSalesforceInputProperties) properties;
                if (inProperties.manualQuery.getValue()) {
                    querySchema = ((SalesforceSource) getCurrentSource()).guessSchema(inProperties.query.getValue());
                    return querySchema;
                }
            }
        }
        return super.getSchema();
    }
}
