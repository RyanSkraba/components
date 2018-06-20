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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.api.exception.DataRejectException;
import org.talend.components.salesforce.runtime.bulk.v2.BulkV2Connection;
import org.talend.components.salesforce.runtime.bulk.v2.SalesforceBulkV2Runtime;
import org.talend.components.salesforce.runtime.bulk.v2.error.BulkV2ClientException;
import org.talend.components.salesforce.tsalesforcebulkexec.TSalesforceBulkExecProperties;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;

import com.sforce.async.BulkConnection;
import com.sforce.ws.ConnectorConfig;

final class SalesforceBulkV2ExecReader extends SalesforceReader {

    private static final I18nMessages MESSAGES =
            GlobalI18N.getI18nMessageProvider().getI18nMessages(SalesforceBulkV2ExecReader.class);

    protected SalesforceBulkV2Runtime bulkRuntime;

    protected BulkResult currentRecord;

    private boolean successResultRetrieved;

    private boolean failedResultRetrieved;

    private BulkResultSet bulkResultSet;

    private int successCount;

    private int rejectCount;

    public SalesforceBulkV2ExecReader(RuntimeContainer container, SalesforceSource source,
            TSalesforceBulkExecProperties props) {
        super(container, source);
        properties = props;
    }

    @Override
    public boolean start() throws IOException {

        TSalesforceBulkExecProperties sprops = (TSalesforceBulkExecProperties) properties;
        BulkConnection bulkConnection = ((SalesforceSource) getCurrentSource()).connect(container).bulkConnection;
        if (bulkConnection == null) {
            throw new BulkV2ClientException(MESSAGES.getMessage("error.bulk.config"));
        }
        ConnectorConfig bulkConfig = bulkConnection.getConfig();
        // Replace rest endpoint with bulk v2 rest one.
        String apiVersion = sprops.getEffectiveConnProperties().apiVersion.getValue();
        BulkV2Connection bulkV2Conn = new BulkV2Connection(bulkConfig, apiVersion);
        bulkRuntime = new SalesforceBulkV2Runtime(bulkV2Conn, sprops);
        try {
            bulkRuntime.executeBulk();
            return retrieveResultSet();
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }

    @Override
    public boolean advance() throws IOException {
        currentRecord = bulkResultSet.next();
        if (currentRecord == null) {
            return retrieveResultSet();
        }
        countData();
        return true;
    }

    @Override
    public IndexedRecord getCurrent() {

        try {
            IndexedRecord record = ((BulkResultAdapterFactory) getFactory()).convertToAvro(currentRecord);
            if (currentRecord.getValue("salesforce_created") != null) {
                return record;
            } else {
                Map<String, Object> resultMessage = new HashMap<String, Object>();
                String error = (String) currentRecord.getValue("sf__Error");
                resultMessage.put("error", error);
                resultMessage.put("talend_record", record);
                throw new DataRejectException(resultMessage);
            }
        } catch (IOException e) {
            throw new ComponentException(e);
        }

    }

    @Override
    protected Schema getSchema() {
        if (querySchema == null) {
            TSalesforceBulkExecProperties sprops = (TSalesforceBulkExecProperties) properties;
            querySchema = sprops.schemaFlow.schema.getValue();
        }
        return querySchema;
    }

    @Override
    public void close() {
        // Do nothing
    }

    @Override
    public Map<String, Object> getReturnValues() {
        Result result = new Result();
        result.totalCount = dataCount;
        result.successCount = successCount;
        result.rejectCount = rejectCount;
        return result.toMap();
    }

    protected void countData() {
        dataCount++;
        if (currentRecord.getValue("salesforce_created") != null) {
            successCount++;
        } else {
            rejectCount++;
        }
    }

    private boolean retrieveResultSet() throws IOException {
        if (successResultRetrieved && failedResultRetrieved) {
            return false;
        }
        if (!successResultRetrieved) {
            bulkResultSet = bulkRuntime.getSuccessResultSet();
            successResultRetrieved = true;
            currentRecord = bulkResultSet.next();
            if (currentRecord == null) {
                return retrieveResultSet();
            } else {
                countData();
                return true;
            }
        }
        if (!failedResultRetrieved) {
            bulkResultSet = bulkRuntime.getFailedResultSet();
            failedResultRetrieved = true;
            currentRecord = bulkResultSet.next();
            if (currentRecord == null) {
                return retrieveResultSet();
            } else {
                countData();
                return true;
            }
        }

        return false;
    }
}
