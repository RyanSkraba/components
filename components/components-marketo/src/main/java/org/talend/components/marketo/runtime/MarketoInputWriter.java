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
package org.talend.components.marketo.runtime;

import java.io.IOException;

import org.apache.avro.generic.IndexedRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.common.runtime.GenericAvroRegistry;
import org.talend.components.marketo.runtime.client.type.MarketoRecordResult;
import org.talend.components.marketo.tmarketoinput.TMarketoInputProperties;
import org.talend.daikon.avro.converter.IndexedRecordConverter;

public class MarketoInputWriter extends MarketoWriter {

    TMarketoInputProperties properties;

    private transient static final Logger LOG = LoggerFactory.getLogger(MarketoInputWriter.class);

    public MarketoInputWriter(MarketoWriteOperation writeOperation, RuntimeContainer container) {
        super(writeOperation, container);
    }

    @Override
    public void open(String uId) throws IOException {
        super.open(uId);
        properties = (TMarketoInputProperties) sink.properties;
        flowSchema = properties.schemaFlow.schema.getValue();

    }

    @Override
    public void write(Object object) throws IOException {
        if (object == null) {
            return;
        }

        if (null == factory) {
            factory = (IndexedRecordConverter<Object, ? extends IndexedRecord>) GenericAvroRegistry.get()
                    .createIndexedRecordConverter(object.getClass());
        }
        inputRecord = factory.convertToAvro(object);
        result.totalCount++;
        result.apiCalls++;
        // This for dynamic which would get schema from the first record
        if (inputSchema == null) {
            inputSchema = ((IndexedRecord) object).getSchema();
        }
        //
        Object lkv = inputRecord.get(inputSchema.getField(properties.leadKeyValues.getValue()).pos());
        LOG.debug("lkv = {}.", lkv);
        // switch between column name in design and column value for runtime
        properties.leadKeyValues.setValue(lkv.toString());

        MarketoRecordResult result = client.getMultipleLeads(properties, null);
        if (!result.isSuccess()) {
            LOG.error(result.getErrors().toString());
        }
        IndexedRecord record = result.getRecords().get(0);
        successfulWrites.clear();
        if (record != null) {
            this.result.successCount++;
            successfulWrites.add(record);
        }
    }
}
