// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.exception.DataRejectException;
import org.talend.components.salesforce.runtime.SalesforceBulkRuntime.BulkResult;
import org.talend.components.salesforce.tsalesforcebulkexec.TSalesforceBulkExecDefinition;
import org.talend.components.salesforce.tsalesforcebulkexec.TSalesforceBulkExecProperties;

import com.sforce.async.AsyncApiException;
import com.sforce.ws.ConnectionException;

final class SalesforceBulkExecReader extends SalesforceReader {

    protected SalesforceBulkRuntime bulkRuntime;

    private int batchIndex;

    private List<BulkResult> currentBatchResult;

    private int resultIndex;

    private int successCount;

    private int rejectCount;

    public SalesforceBulkExecReader(RuntimeContainer container, SalesforceSource source,
                                    TSalesforceBulkExecProperties props) {
        super(container, source);
        properties = props;
    }

    @Override
    public boolean start() throws IOException {

        TSalesforceBulkExecProperties sprops = (TSalesforceBulkExecProperties) properties;
        bulkRuntime = new SalesforceBulkRuntime((SalesforceSource) getCurrentSource(), container);
        bulkRuntime.setConcurrencyMode(sprops.bulkProperties.concurrencyMode.getStringValue());
        bulkRuntime.setAwaitTime(sprops.bulkProperties.waitTimeCheckBatchState.getIntValue());

        try {
            // We only support CSV file for bulk output
            bulkRuntime.executeBulk(sprops.module.moduleName.getStringValue(), sprops.outputAction.getStringValue(),
                    sprops.upsertKeyColumn.getStringValue(), "csv", sprops.bulkFilePath.getStringValue(),
                    sprops.bulkProperties.bytesToCommit.getIntValue(),
                    sprops.bulkProperties.rowsToCommit.getIntValue());
            if (bulkRuntime.getBatchCount() > 0) {
                batchIndex = 0;
                currentBatchResult = bulkRuntime.getBatchLog(0);
                resultIndex = 0;
                boolean startable = currentBatchResult.size() > 0;
                if (startable) {
                    countData();
                }
                return startable;
            }
            return false;
        } catch (AsyncApiException | ConnectionException e) {
            throw new IOException(e);
        }
    }

    protected Map<String, String> getResult() {
        return null;
    }

    @Override
    public boolean advance() throws IOException {
        if (++resultIndex >= currentBatchResult.size()) {
            if (++batchIndex >= bulkRuntime.getBatchCount()) {
                return false;
            } else {
                try {
                    currentBatchResult = bulkRuntime.getBatchLog(batchIndex);
                    resultIndex = 0;
                    boolean isAdvanced = currentBatchResult.size() > 0;
                    if (isAdvanced) {
                        countData();
                    }
                    return isAdvanced;
                } catch (AsyncApiException | ConnectionException e) {
                    throw new IOException(e);
                }
            }
        }
        countData();
        return true;
    }

    @Override
    public IndexedRecord getCurrent() {
        BulkResult result = currentBatchResult.get(resultIndex);
        IndexedRecord record = null;
        try {
            record = ((BulkResultAdapterFactory) getFactory()).convertToAvro(result);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if ("true".equalsIgnoreCase((String) result.getValue("Success"))) {
            return record;
        } else {
            Map<String, Object> resultMessage = new HashMap<String, Object>();
            String error = (String) result.getValue("Error");
            resultMessage.put("error", error);
            resultMessage.put("talend_record", record);
            throw new DataRejectException(resultMessage);
        }

    }

    @Override
    protected Schema getSchema() throws IOException {
        if (querySchema == null) {
            TSalesforceBulkExecProperties sprops = (TSalesforceBulkExecProperties) properties;
            //TODO check the assert : the output schema have values even when no output connector
            querySchema = (Schema) sprops.schemaFlow.schema.getValue();

            //tsalesforcebulkexec don't support dynamic in fact, only tsalesforceoutputbulk support.

        	/*
            BulkResult currentRow = currentBatchResult.get(resultIndex);
        	Set<String> keys = currentRow.values.keySet();
        	
            List<Schema.Field> columnlist = new ArrayList<>();
            for (Schema.Field se : querySchema.getFields()) {
                if (keys.contains(se.name())) {
                	columnlist.add(new Schema.Field(se.name(), se.schema(), se.doc(), se.defaultVal()));
                }
            }
            
            Map<String, Object> objectProps = querySchema.getObjectProps();
            querySchema = Schema.createRecord(querySchema.getName(), querySchema.getDoc(), querySchema.getNamespace(),
                    querySchema.isError());
            querySchema.getObjectProps().putAll(objectProps);
            querySchema.setFields(columnlist);
            */
        }
        return querySchema;
    }

    @Override
    public void close() throws IOException {
        if (container != null) {
            String currentComponent = container.getCurrentComponentId()
                    .replace("_" + TSalesforceBulkExecDefinition.COMPONENT_NAME, "");
            container.setComponentData(currentComponent, TSalesforceBulkExecProperties.NB_LINE_NAME, dataCount);
            container.setComponentData(currentComponent, TSalesforceBulkExecProperties.NB_SUCCESS_NAME, successCount);
            container.setComponentData(currentComponent, TSalesforceBulkExecProperties.NB_REJECT_NAME, rejectCount);
        }
        bulkRuntime.close();
    }

    protected void countData() {
        dataCount++;
        BulkResult result = currentBatchResult.get(resultIndex);
        if ("true".equalsIgnoreCase(String.valueOf(result.getValue("Success")))) {
            successCount++;
        } else {
            rejectCount++;
        }
    }
}