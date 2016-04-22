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

import com.sforce.async.AsyncApiException;
import com.sforce.ws.ConnectionException;
import org.apache.avro.generic.IndexedRecord;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.salesforce.SalesforceOutputProperties;
import org.talend.components.salesforce.runtime.SalesforceBulkRuntime.BulkResult;
import org.talend.components.salesforce.tsalesforcebulkexec.TSalesforceBulkExecDefinition;
import org.talend.components.salesforce.tsalesforcebulkexec.TSalesforceBulkExecProperties;

import java.io.IOException;
import java.util.List;
import java.util.Map;

final class SalesforceBulkExecReader extends SalesforceReader {


    protected SalesforceBulkRuntime bulkRuntime;

    private int batchIndex;

    private List<BulkResult> currentBatchResult;

    private int resultIndex;

    private int successCount;

    private int rejectCount;

    public SalesforceBulkExecReader(RuntimeContainer container, SalesforceSource source, TSalesforceBulkExecProperties props) {
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
                    sprops.bulkProperties.bytesToCommit.getIntValue(), sprops.bulkProperties.rowsToCommit.getIntValue());
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
        // TODO need change after component REJECT line can be work.
        try {
            return ((BulkResultAdapterFactory) getFactory()).convertToAvro(currentBatchResult.get(resultIndex));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws IOException {
        if (container != null) {
            String currentComponent = container.getCurrentComponentId().replace("_" + TSalesforceBulkExecDefinition.COMPONENT_NAME, "");
            container.setComponentData(currentComponent, SalesforceOutputProperties.NB_LINE, dataCount);
            container.setComponentData(currentComponent, SalesforceOutputProperties.NB_SUCCESS, successCount);
            container.setComponentData(currentComponent, SalesforceOutputProperties.NB_REJECT, rejectCount);
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