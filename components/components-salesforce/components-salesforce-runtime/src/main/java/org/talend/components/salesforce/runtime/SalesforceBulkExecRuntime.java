// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
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

import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;

import org.slf4j.Logger;
import org.talend.components.api.component.runtime.ComponentDriverInitialization;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.salesforce.SalesforceConnectionModuleProperties;
import org.talend.components.salesforce.tsalesforcebulkexec.TSalesforceBulkExecProperties;
import org.talend.daikon.properties.ValidationResult;

import com.sforce.async.AsyncApiException;
import com.sforce.ws.ConnectionException;

public class SalesforceBulkExecRuntime extends SalesforceSourceOrSink
        implements ComponentDriverInitialization<ComponentProperties> {

    protected int dataCount;

    private int successCount;

    private int rejectCount;

    private transient static final Logger LOG = getLogger(SalesforceBulkExecRuntime.class);

    @Override
    public ValidationResult initialize(RuntimeContainer container, ComponentProperties properties) {
        this.properties = (SalesforceConnectionModuleProperties) properties;
        return ValidationResult.OK;
    }

    @Override
    public void runAtDriver(RuntimeContainer container) {
        bulkExecute(container);
        setReturnValues(container);
    }

    public void bulkExecute(RuntimeContainer container) {
        TSalesforceBulkExecProperties sprops = (TSalesforceBulkExecProperties) properties;
        try {
            SalesforceBulkRuntime bulkRuntime = new SalesforceBulkRuntime(connect(container).bulkConnection);
            bulkRuntime.setConcurrencyMode(sprops.bulkProperties.concurrencyMode.getValue());
            bulkRuntime.setAwaitTime(sprops.bulkProperties.waitTimeCheckBatchState.getValue());
            // We only support CSV file for bulk output
            bulkRuntime.executeBulk(sprops.module.moduleName.getStringValue(), sprops.outputAction.getValue(),
                    sprops.upsertKeyColumn.getStringValue(), "csv", sprops.bulkFilePath.getStringValue(),
                    sprops.bulkProperties.bytesToCommit.getValue(), sprops.bulkProperties.rowsToCommit.getValue());
            // count results
            for (int i = 0; i < bulkRuntime.getBatchCount(); i++) {
                for (BulkResult result : bulkRuntime.getBatchLog(i)) {
                    dataCount++;
                    if ("true".equalsIgnoreCase(String.valueOf(result.getValue("Success")))) {
                        successCount++;
                    } else {
                        rejectCount++;
                    }
                }
            }
            bulkRuntime.close();
        } catch (IOException | AsyncApiException | ConnectionException e) {
            throw new ComponentException(e);
        }
    }

    public void setReturnValues(RuntimeContainer container) {
        String componentId = container.getCurrentComponentId();
        container.setComponentData(componentId, "NB_LINE", dataCount);
        container.setComponentData(componentId, "NB_SUCCESS", successCount);
        container.setComponentData(componentId, "NB_REJECT", rejectCount);
    }

}
