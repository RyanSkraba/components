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
import org.talend.components.salesforce.SalesforceConnectionProperties;
import org.talend.components.salesforce.runtime.bulk.v2.BulkV2Connection;
import org.talend.components.salesforce.runtime.bulk.v2.SalesforceBulkV2Runtime;
import org.talend.components.salesforce.runtime.bulk.v2.error.BulkV2ClientException;
import org.talend.components.salesforce.tsalesforcebulkexec.TSalesforceBulkExecProperties;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;
import org.talend.daikon.properties.ValidationResult;

import com.sforce.async.AsyncApiException;
import com.sforce.async.BulkConnection;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;

public class SalesforceBulkExecRuntime extends SalesforceSourceOrSink
        implements ComponentDriverInitialization<ComponentProperties> {

    private transient static final Logger LOG = getLogger(SalesforceBulkExecRuntime.class);

    private static final I18nMessages MESSAGES =
            GlobalI18N.getI18nMessageProvider().getI18nMessages(SalesforceBulkExecRuntime.class);

    protected int dataCount;

    private int successCount;

    private int rejectCount;

    @Override
    public ValidationResult initialize(RuntimeContainer container, ComponentProperties properties) {
        this.properties = (SalesforceConnectionModuleProperties) properties;
        return ValidationResult.OK;
    }

    @Override
    public void runAtDriver(RuntimeContainer container) {
        TSalesforceBulkExecProperties sprops = (TSalesforceBulkExecProperties) properties;
        boolean useBulkApiV2 = SalesforceConnectionProperties.LoginType.OAuth.equals(
                sprops.getEffectiveConnProperties().loginType.getValue()) && sprops.bulkProperties.bulkApiV2.getValue();
        if (useBulkApiV2) {
            bulkV2Execute(container);
        } else {
            bulkExecute(container);
        }
        setReturnValues(container);
    }

    public void bulkExecute(RuntimeContainer container) {
        TSalesforceBulkExecProperties sprops = (TSalesforceBulkExecProperties) properties;
        try {
            SalesforceBulkRuntime bulkRuntime = new SalesforceBulkRuntime(connect(container).bulkConnection);
            bulkRuntime.setConcurrencyMode(sprops.bulkProperties.concurrencyMode.getValue());
            bulkRuntime.setAwaitTime(sprops.bulkProperties.waitTimeCheckBatchState.getValue());
            // We only support CSV file for bulk output
            bulkRuntime.executeBulk(sprops.module.moduleName.getStringValue(), sprops.outputAction.getValue(), sprops.hardDelete.getValue(),
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

    public void bulkV2Execute(RuntimeContainer container) {
        TSalesforceBulkExecProperties sprops = (TSalesforceBulkExecProperties) properties;
        try {
            BulkConnection bulkConnection = connect(container).bulkConnection;
            if (bulkConnection == null) {
                throw new BulkV2ClientException(MESSAGES.getMessage("error.bulk.config"));
            }
            ConnectorConfig bulkConfig = bulkConnection.getConfig();
            String apiVersion = sprops.getEffectiveConnProperties().apiVersion.getValue();
            BulkV2Connection bulkV2Conn = new BulkV2Connection(bulkConfig, apiVersion);
            SalesforceBulkV2Runtime bulkRuntime = new SalesforceBulkV2Runtime(bulkV2Conn, sprops);
            bulkRuntime.executeBulk();
            dataCount = bulkRuntime.getNumberRecordsProcessed();
            rejectCount = bulkRuntime.getNumberRecordsFailed();
            successCount = dataCount - rejectCount;
        } catch (InterruptedException | IOException e) {
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
