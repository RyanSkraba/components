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

import java.io.IOException;
import java.util.NoSuchElementException;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.RuntimeHelper;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.salesforce.tsalesforceinput.TSalesforceInputProperties;

import com.sforce.soap.partner.QueryResult;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;

public class SalesforceInputReader extends SalesforceReader<IndexedRecord> {

    private static final Logger LOG = LoggerFactory.getLogger(SalesforceInputReader.class);

    protected RuntimeContainer adaptor;

    protected TSalesforceInputProperties properties;

    private transient QueryResult inputResult;

    private transient SObject[] inputRecords;

    private transient int inputRecordsIndex;

    private transient Schema querySchema;

    private transient SObjectAdapterFactory factory;

    public SalesforceInputReader(RuntimeContainer adaptor, SalesforceSource source, TSalesforceInputProperties props) {
        super(source);
        properties = props;
        this.adaptor = adaptor;
    }

    private Schema getSchema() throws IOException {
        if (null == querySchema) {
            querySchema = new Schema.Parser().parse(properties.module.schema.schema.getStringValue());
            querySchema = RuntimeHelper.resolveSchema(adaptor, properties.module, getCurrentSource(), querySchema);
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
            inputResult = executeSalesforceQuery();
            if (inputResult.getSize() == 0) {
                return false;
            }
            inputRecords = inputResult.getRecords();
            inputRecordsIndex = 0;
            return inputRecords.length > 0;
        } catch (ConnectionException e) {
            // Wrap the exception in an IOException.
            throw new IOException(e);
        }
    }

    @Override
    public boolean advance() throws IOException {
        inputRecordsIndex++;

        // Fast return conditions.
        if (inputRecordsIndex < inputRecords.length) {
            return true;
        }
        if (inputResult.isDone()) {
            return false;
        }

        try {
            inputResult = getConnection().queryMore(inputResult.getQueryLocator());
            inputRecords = inputResult.getRecords();
            inputRecordsIndex = 0;
            return inputResult.getSize() > 0;
        } catch (ConnectionException e) {
            // Wrap the exception in an IOException.
            throw new IOException(e);
        }

    }

    public SObject getCurrentSObject() throws NoSuchElementException {
        return inputRecords[inputRecordsIndex];
    }

    protected QueryResult executeSalesforceQuery() throws IOException, ConnectionException {
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
            queryText = sb.toString();
        }

        getConnection().setQueryOptions(properties.batchSize.getIntValue());
        return getConnection().query(queryText);
    }

    @Override
    public IndexedRecord getCurrent() {
        try {
            return getFactory().convertToAvro(getCurrentSObject());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws IOException {
        // No resources to close.
    }

}
