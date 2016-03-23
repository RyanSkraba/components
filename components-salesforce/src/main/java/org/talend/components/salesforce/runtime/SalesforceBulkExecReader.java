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
import com.sforce.async.BulkConnection;
import com.sforce.ws.ConnectionException;
import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.salesforce.runtime.SalesforceBulkRuntime.BulkResult;
import org.talend.components.salesforce.tsalesforcebulkexec.TSalesforceBulkExecProperties;

import java.io.IOException;
import java.util.List;
import java.util.Map;

final class SalesforceBulkExecReader extends SalesforceReader {


    private BulkConnection bulkConnection;

    protected SalesforceBulkRuntime bulkRuntime;

    private int batchIndex;

    private List<BulkResult> currentBatchResult;

    private int resultIndex;

    private String uId;

    private SalesforceSink sink;

    private RuntimeContainer adaptor;

    private transient BulkResultAdapterFactory factory;

    private TSalesforceBulkExecProperties sprops;

    private int dataCount;

    public static String ID_COLUMN="Id";

    private Schema schema;


    public SalesforceBulkExecReader(RuntimeContainer container, SalesforceSource source, TSalesforceBulkExecProperties props) {
        super(container, source);
        sprops = props;
    }

    protected BulkConnection getBulkConnection() throws IOException {
        if (bulkConnection == null) {
            bulkConnection = ((SalesforceSource) getCurrentSource()).connect(container).bulkConnection;
        }
        return bulkConnection;
    }

    private Schema getSchema() throws IOException {
        if (null == schema) {
            schema = new Schema.Parser().parse(sprops.module.schema.schema.getStringValue());
            //  querySchema = RuntimeHelper.resolveSchema(adaptor, getCurrentSource(), querySchema);
        }
        return schema;
    }

    @Override
    public boolean start() throws IOException {
        this.uId = uId;

        bulkRuntime = new SalesforceBulkRuntime(getBulkConnection());

        bulkRuntime.setConcurrencyMode(sprops.bulkProperties.concurrencyMode.getStringValue());
        bulkRuntime.setAwaitTime(sprops.bulkProperties.waitTimeCheckBatchState.getIntValue());

        try {
            // We only support CSV file for bulk output
            bulkRuntime.executeBulk(sprops.module.moduleName.getStringValue(), sprops.outputAction.getStringValue(),
                    sprops.upsertKeyColumn.getStringValue(), "csv", sprops.bulkFilePath.getStringValue(),
                    sprops.bulkProperties.bytesToCommit.getIntValue(), sprops.bulkProperties.rowsToCommit.getIntValue());
            if(bulkRuntime.getBatchCount()>0){
                batchIndex = 0;
                currentBatchResult = bulkRuntime.getBatchLog(0);
                resultIndex = 0;
                return currentBatchResult.size()>0;
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
            if(++batchIndex >= bulkRuntime.getBatchCount()){
                return false;
            }else {
                try {
                    currentBatchResult = bulkRuntime.getBatchLog(batchIndex);
                    resultIndex = 0;
                    return currentBatchResult.size()>0;
                } catch (AsyncApiException|ConnectionException e) {
                    throw new IOException(e);
                }
            }
        }
        return true;
    }

    @Override
    public IndexedRecord getCurrent() {
        // TODO need change after component REJECT line can be work.
        try {
            return getFactory().convertToAvro(currentBatchResult.get(resultIndex));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void close() throws IOException {
        bulkRuntime.close();
    }

    private BulkResultAdapterFactory getFactory() throws IOException {
        if (null == factory) {
            factory = new BulkResultAdapterFactory();
            factory.setSchema(getSchema());
        }
        return factory;
    }
}