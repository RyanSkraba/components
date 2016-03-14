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
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;
import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.RuntimeHelper;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.salesforce.runtime.SalesforceBulkRuntime.BulkResultSet;
import org.talend.components.salesforce.tsalesforceinput.TSalesforceInputProperties;

import java.io.IOException;
import java.util.NoSuchElementException;

public class SalesforceBulkQuryInputReader extends SalesforceReader<IndexedRecord> {

    private static final Logger LOG = LoggerFactory.getLogger(SalesforceBulkQuryInputReader.class);

    protected RuntimeContainer adaptor;

    protected TSalesforceInputProperties properties;


    private transient Schema querySchema;

    private transient SObjectAdapterFactory factory;

    protected boolean isBulkQuery;

    protected BulkConnection bulkConnection;

    protected SalesforceBulkRuntime bulkUtil;

    protected BulkResultSet bulkResultSet;

    protected SObject currentRecord;

    public SalesforceBulkQuryInputReader(RuntimeContainer adaptor, SalesforceSource source, TSalesforceInputProperties props) {
        super(source);
        properties = props;
        this.adaptor = adaptor;
    }

    protected BulkConnection getBulkConnection() throws IOException {
        if (bulkConnection == null) {
            bulkConnection = ((SalesforceSource) getCurrentSource()).connect().bulkConnection;
        }
        return bulkConnection;
    }

    private Schema getSchema() throws IOException {
        if (null == querySchema) {
            querySchema = new Schema.Parser().parse(properties.module.schema.schema.getStringValue());
            //  querySchema = RuntimeHelper.resolveSchema(adaptor, getCurrentSource(), querySchema);
        }
        return querySchema;
    }

    private SObjectAdapterFactory getFactory() throws IOException {
        if (null == factory) {
            factory = new SObjectAdapterFactory();
            factory.setSchema(getSchema());
        }
        return factory;
    }

    @Override
    public boolean start() throws IOException {
        try {
            executeSalesforceBulkQuery();
            bulkResultSet = bulkUtil.getQueryResultSet( bulkUtil.nextResultId());
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
            String resultId = bulkUtil.nextResultId();
            if(resultId != null){
                try {
                    bulkResultSet = bulkUtil.getQueryResultSet(resultId);
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

    public SObject getCurrentRecord() throws NoSuchElementException {
        return currentRecord;
    }

    // FIXME some duplicate code
    protected void executeSalesforceBulkQuery() throws IOException, ConnectionException {
        String queryText;
        if (properties.manualQuery.getBooleanValue()) {
            queryText = properties.query.getStringValue();
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("select "); //$NON-NLS-1$
            int count = 0;
            for (Schema.Field se : getSchema().getFields()) {
                if (count++ > 0) {
                    sb.append(", "); //$NON-NLS-1$
                }
                sb.append(se.name());
            }
            sb.append(" from "); //$NON-NLS-1$
            sb.append(properties.module.moduleName.getStringValue());
            String condition = properties.condition.getStringValue();
            if(condition!=null && condition.trim().length()>0){
                sb.append(" where ");
                sb.append(condition);
            }
            queryText = sb.toString();
        }

        bulkUtil =new SalesforceBulkRuntime(getBulkConnection());
        try {
            bulkUtil.doBulkQuery(properties.module.moduleName.getStringValue(),queryText,30);
        } catch (AsyncApiException |InterruptedException | ConnectionException e) {
            throw new IOException(e);
        }
    }

    @Override
    public IndexedRecord getCurrent() {
        try {
            return getFactory().convertToAvro(getCurrentRecord());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws IOException {
        // No resources to close.
    }

}
