// ============================================================================
//
// Copyright (C) 2006-2020 Talend Inc. - www.talend.com
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

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.salesforce.runtime.bulk.v2.BulkV2Connection;
import org.talend.components.salesforce.runtime.bulk.v2.SalesforceBulkQueryV2Runtime;
import org.talend.components.salesforce.runtime.bulk.v2.error.BulkV2ClientException;
import org.talend.components.salesforce.tsalesforceinput.TSalesforceInputProperties;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;

import com.sforce.async.BulkConnection;
import com.sforce.ws.ConnectorConfig;

final class SalesforceBulkQueryV2Reader extends SalesforceReader {

    private static final I18nMessages MESSAGES =
            GlobalI18N.getI18nMessageProvider().getI18nMessages(SalesforceBulkQueryV2Reader.class);

    protected SalesforceBulkQueryV2Runtime bulkRuntime;

    protected BulkResult currentRecord;


    private BulkResultSet bulkResultSet;

    public SalesforceBulkQueryV2Reader(RuntimeContainer container, SalesforceSource source,
            TSalesforceInputProperties props) {
        super(container, source);
        properties = props;
    }

    @Override
    public boolean start() throws IOException {

        TSalesforceInputProperties sprops = (TSalesforceInputProperties) properties;
        BulkConnection bulkConnection = ((SalesforceSource) getCurrentSource()).connect(container).bulkConnection;
        if (bulkConnection == null) {
            throw new BulkV2ClientException(MESSAGES.getMessage("error.bulk.config"));
        }
        ConnectorConfig bulkConfig = bulkConnection.getConfig();
        // Replace rest endpoint with bulk v2 rest one.
        BulkV2Connection bulkV2Conn = new BulkV2Connection(bulkConfig, BulkV2Connection.OperationType.QUERY);
        bulkRuntime = new SalesforceBulkQueryV2Runtime(bulkV2Conn, sprops);
        bulkRuntime.setQuery(getQueryString(properties));
        bulkRuntime.setSafetySwitch(sprops.safetySwitch.getValue());
        bulkRuntime.setJobTimeout(sprops);
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
        dataCount++;
        return true;
    }

    @Override
    public void close() {
        // do nothing
    }

    private boolean retrieveResultSet() throws IOException {
        bulkResultSet = bulkRuntime.getResultSet();
        if(bulkResultSet == null){
            return false;
        }
        currentRecord = bulkResultSet.next();
        if (currentRecord == null) {
            return retrieveResultSet();
        } else {
            dataCount++;
            return true;
        }
    }

    @Override
    public IndexedRecord getCurrent() {
        try {
            return ((BulkResultAdapterFactory) getFactory()).convertToAvro(currentRecord);
        } catch (IOException e) {
            throw new ComponentException(e);
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
