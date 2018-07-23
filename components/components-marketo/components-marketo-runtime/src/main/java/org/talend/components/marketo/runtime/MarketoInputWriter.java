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
package org.talend.components.marketo.runtime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.generic.IndexedRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.marketo.runtime.client.type.MarketoRecordResult;
import org.talend.components.marketo.tmarketoinput.TMarketoInputProperties;
import org.talend.daikon.avro.AvroUtils;

public class MarketoInputWriter extends MarketoWriter {

    TMarketoInputProperties properties;

    Boolean isDynamic = Boolean.FALSE;

    String leadKeyColumn;

    private transient static final Logger LOG = LoggerFactory.getLogger(MarketoInputWriter.class);

    public MarketoInputWriter(MarketoWriteOperation writeOperation, RuntimeContainer container) {
        super(writeOperation, container);
    }

    public void adaptSchemaToDynamic() throws IOException {
        Schema design = this.properties.schemaInput.schema.getValue();
        if (!isDynamic) {
            return;
        }
        try {
            Schema runtimeSchema;
            runtimeSchema = sink.getDynamicSchema("", design);
            // preserve mappings to re-apply them after
            Map<String, String> mappings = properties.mappingInput.getNameMappingsForMarketo();
            List<String> columnNames = new ArrayList<>();
            List<String> mktoNames = new ArrayList<>();
            for (Field f : runtimeSchema.getFields()) {
                columnNames.add(f.name());
                if (mappings.get(f.name()) != null) {
                    mktoNames.add(mappings.get(f.name()));
                } else {
                    mktoNames.add("");
                }
            }
            properties.mappingInput.columnName.setValue(columnNames);
            properties.mappingInput.marketoColumnName.setValue(mktoNames);
            properties.schemaInput.schema.setValue(runtimeSchema);
        } catch (IOException e) {
            LOG.error(e.getMessage());
            throw e;
        }
    }

    @Override
    public void open(String uId) throws IOException {
        super.open(uId);
        properties = (TMarketoInputProperties) sink.getProperties();
        flowSchema = properties.schemaFlow.schema.getValue();
        dieOnError = properties.dieOnError.getValue();
        isDynamic = AvroUtils.isIncludeAllFields(this.properties.schemaInput.schema.getValue());
        leadKeyColumn = properties.leadKeyValues.getValue();
    }

    @Override
    public void write(Object object) throws IOException {
        if (object == null) {
            return;
        }
        cleanWrites();
        //
        inputRecord = (IndexedRecord) object;
        result.totalCount++;
        // This for dynamic which would get schema from the first record
        if (inputSchema == null) {
            inputSchema = inputRecord.getSchema();
            if (isDynamic) {
                adaptSchemaToDynamic();
            }
        }
        // switch between column name in design and column value for runtime
        properties.leadKeyValues.setValue(String.valueOf(inputRecord.get(inputSchema.getField(leadKeyColumn).pos())));
        //
        for (int i = 0; i < getRetryAttemps(); i++) {
            result.apiCalls++;
            MarketoRecordResult mktoResult = client.getMultipleLeads(properties, null);
            //
            if (!mktoResult.isSuccess()) {
                if (dieOnError) {
                    throw new IOException(mktoResult.getErrorsString());
                }
                // is recoverable error
                if (client.isErrorRecoverable(mktoResult.getErrors())) {
                    LOG.debug("Recoverable error during operation : `{}`. Retrying...", mktoResult.getErrorsString());
                    waitForRetryAttempInterval();
                    continue;
                } else {
                    LOG.error("Unrecoverable error : `{}`.", mktoResult.getErrorsString());
                    break;
                }
            } else {
                for (IndexedRecord record : mktoResult.getRecords()) {
                    if (record != null) {
                        this.result.successCount++;
                        successfulWrites.add(record);
                    }
                }
                break;
            }
        }
    }

    @Override
    protected void flush() {
        // nop, no batch mode.
    }
}
