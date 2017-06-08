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
package org.talend.components.salesforce.runtime.dataprep;

import java.io.IOException;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.talend.components.api.component.runtime.AbstractBoundedReader;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.salesforce.dataprep.SalesforceInputProperties;
import org.talend.components.salesforce.dataset.SalesforceDatasetProperties;
import org.talend.components.salesforce.dataset.SalesforceDatasetProperties.SourceType;
import org.talend.components.salesforce.runtime.BulkResult;
import org.talend.components.salesforce.runtime.BulkResultSet;
import org.talend.components.salesforce.runtime.SalesforceBulkRuntime;
import org.talend.components.salesforce.runtime.common.ConnectionHolder;
import org.talend.components.salesforce.soql.SoqlQuery;
import org.talend.daikon.avro.converter.IndexedRecordConverter;

import com.sforce.async.AsyncApiException;
import com.sforce.ws.ConnectionException;

public final class SalesforceBulkQueryReader extends AbstractBoundedReader<IndexedRecord> {

    private SalesforceBulkRuntime bulkRuntime;

    private BulkResultSet bulkResultSet;

    private BulkResult currentRecord;

    private RuntimeContainer container;

    private SalesforceInputProperties properties;

    private SalesforceDatasetProperties dataset;

    private transient IndexedRecordConverter<?, IndexedRecord> factory;

    protected transient Schema querySchema;

    private int dataCount;

    public SalesforceBulkQueryReader(RuntimeContainer container, SalesforceDataprepSource source,
            SalesforceInputProperties properties) {
        super(source);
        this.properties = properties;
        dataset = this.properties.getDatasetProperties();
        this.container = container;
    }

    @Override
    public boolean start() throws IOException {
        try {
            if (bulkRuntime == null) {
                ConnectionHolder connectionHolder = ((SalesforceDataprepSource) getCurrentSource()).getConnectionHolder();
                bulkRuntime = new SalesforceBulkRuntime(connectionHolder.bulkConnection);
            }
            executeSalesforceBulkQuery();
            bulkResultSet = bulkRuntime.getQueryResultSet(bulkRuntime.nextResultId());
            currentRecord = bulkResultSet.next();
            boolean start = currentRecord != null;
            if (start) {
                dataCount++;
            }
            return start;
        } catch (ConnectionException | AsyncApiException e) {
            // Wrap the exception in an IOException.
            throw new IOException(e);
        }
    }

    @Override
    public boolean advance() throws IOException {
        currentRecord = bulkResultSet.next();
        if (currentRecord == null) {
            String resultId = bulkRuntime.nextResultId();
            if (resultId != null) {
                try {
                    // Get a new result set
                    bulkResultSet = bulkRuntime.getQueryResultSet(resultId);
                    currentRecord = bulkResultSet.next();
                    boolean advance = currentRecord != null;
                    if (advance) {
                        // New result set available to retrieve
                        dataCount++;
                    }
                    return advance;
                } catch (AsyncApiException | ConnectionException e) {
                    throw new IOException(e);
                }
            } else {
                return false;
            }
        }
        dataCount++;
        return true;
    }

    public BulkResult getCurrentRecord() throws NoSuchElementException {
        return currentRecord;
    }

    protected void executeSalesforceBulkQuery() throws IOException, ConnectionException {
        String queryText = getQueryString();
        try {
            bulkRuntime.doBulkQuery(getModuleName(), queryText);
        } catch (AsyncApiException | InterruptedException | ConnectionException e) {
            throw new IOException(e);
        }
    }

    @Override
    public IndexedRecord getCurrent() {
        try {
            return ((BulkResultIndexedRecordConverter) getFactory()).convertToAvro(getCurrentRecord());
        } catch (IOException e) {
            throw new ComponentException(e);
        }
    }

    @Override
    public Map<String, Object> getReturnValues() {
        Result result = new Result();
        result.totalCount = dataCount;
        return result.toMap();
    }

    @Override
    public void close() throws IOException {
        // TODO no need to close it? we keep the same with di runtime only now
    }

    private String getQueryString() throws IOException {
        if (dataset.sourceType.getValue() == SourceType.MODULE_SELECTION) {
            final Schema schema = getSchema();
            if (schema == null) {
                throw new IllegalStateException("The schema must not be null");
            }

            StringBuilder sb = new StringBuilder();
            sb.append("select ");
            int count = 0;
            for (Schema.Field se : schema.getFields()) {
                if (count++ > 0) {
                    sb.append(", ");
                }
                sb.append(se.name());
            }
            sb.append(" from ");
            sb.append(dataset.moduleName.getValue());
            return sb.toString();
        } else {
            return dataset.query.getValue();
        }
    }

    private String getModuleName() {
        if (dataset.sourceType.getValue() == SourceType.MODULE_SELECTION) {
            return dataset.moduleName.getValue();
        } else {
            String query = dataset.query.getValue();
            if (query != null && !query.isEmpty()) {
                SoqlQuery soqlInstance = SoqlQuery.getInstance();
                soqlInstance.init(query);
                return soqlInstance.getDrivingEntityName();
            }
            return null;
        }
    }

    private IndexedRecordConverter<?, IndexedRecord> getFactory() throws IOException {
        if (null == factory) {
            factory = new BulkResultIndexedRecordConverter();
            factory.setSchema(getSchema());
        }
        return factory;
    }

    private Schema getSchema() throws IOException {
        if (querySchema == null) {
            SalesforceDataprepSource sds = (SalesforceDataprepSource) getCurrentSource();
            return SalesforceSchemaUtils.getSchema(dataset, sds, container);
        }
        return querySchema;
    }

}
