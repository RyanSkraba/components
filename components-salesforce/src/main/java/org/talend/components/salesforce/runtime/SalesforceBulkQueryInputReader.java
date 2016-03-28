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
package org.talend.components.salesforce.runtime;

import com.sforce.async.AsyncApiException;
import com.sforce.async.BulkConnection;
import com.sforce.ws.ConnectionException;
import org.apache.avro.generic.IndexedRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.salesforce.runtime.SalesforceBulkRuntime.BulkResult;
import org.talend.components.salesforce.runtime.SalesforceBulkRuntime.BulkResultSet;
import org.talend.components.salesforce.tsalesforceinput.TSalesforceInputProperties;

import java.io.IOException;
import java.util.NoSuchElementException;

public class SalesforceBulkQueryInputReader extends SalesforceReader<IndexedRecord> {

    private static final Logger LOG = LoggerFactory.getLogger(SalesforceBulkQueryInputReader.class);

    protected BulkConnection bulkConnection;

    protected SalesforceBulkRuntime bulkRuntime;

    protected BulkResultSet bulkResultSet;

    protected BulkResult currentRecord;

    public SalesforceBulkQueryInputReader(RuntimeContainer container, SalesforceSource source, TSalesforceInputProperties props) {
        super(container, source);
        properties = props;
        this.container = container;
    }

    protected BulkConnection getBulkConnection() throws IOException {
        if (bulkConnection == null) {
            bulkConnection = ((SalesforceSource) getCurrentSource()).connect(container).bulkConnection;
        }
        return bulkConnection;
    }

    @Override
    public boolean start() throws IOException {
        try {
            executeSalesforceBulkQuery();
            bulkResultSet = bulkRuntime.getQueryResultSet( bulkRuntime.nextResultId());
            currentRecord = bulkResultSet.next();
            return currentRecord !=null;
        } catch (ConnectionException|AsyncApiException e) {
            // Wrap the exception in an IOException.
            throw new IOException(e);
        }
    }

    @Override
    public boolean advance() throws IOException {
        currentRecord = bulkResultSet.next();
        if(currentRecord == null ){
            String resultId = bulkRuntime.nextResultId();
            if(resultId != null){
                try {
                    bulkResultSet = bulkRuntime.getQueryResultSet(resultId);
                    currentRecord = bulkResultSet.next();
                    return bulkResultSet.hasNext();
                } catch (AsyncApiException | ConnectionException e) {
                    throw new IOException(e);
                }
            }else{
                return false;
            }
        }
        return true;
    }

    public BulkResult getCurrentRecord() throws NoSuchElementException {
        return currentRecord;
    }

    // FIXME some duplicate code
    protected void executeSalesforceBulkQuery() throws IOException, ConnectionException {
        String queryText = getQueryString((TSalesforceInputProperties)properties);
        bulkRuntime =new SalesforceBulkRuntime((SalesforceSource) getCurrentSource(),container);
        try {
            bulkRuntime.doBulkQuery(properties.module.moduleName.getStringValue(), queryText, 30);
        } catch (AsyncApiException |InterruptedException | ConnectionException e) {
            throw new IOException(e);
        }
    }

    @Override
    public IndexedRecord getCurrent() {
        try {
            return ((BulkResultAdapterFactory)getFactory()).convertToAvro(getCurrentRecord());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
