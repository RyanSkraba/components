// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.filterrow.runtime;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.component.runtime.WriterWithFeedback;
import org.talend.components.filterrow.TFilterRowProperties;
import org.talend.components.filterrow.processing.ValueProcessor;
import org.talend.daikon.avro.AvroRegistry;
import org.talend.daikon.avro.converter.IndexedRecordConverter;

/**
 * created by dmytro.chmyga on Dec 19, 2016
 */
public class TFilterRowWriter implements WriterWithFeedback<Result, IndexedRecord, IndexedRecord> {

    private final WriteOperation<Result> writeOperation;

    private List<IndexedRecord> success;

    private List<IndexedRecord> reject;

    private int successCount;

    private int rejectCount;

    private int totalCount;

    private final Schema schemaFlow;

    private final Schema schemaReject;

    private final ValueProcessor valueProcessor;

    private IndexedRecordConverter<Object, ? extends IndexedRecord> factory;

    private String uid;

    public TFilterRowWriter(WriteOperation<Result> writeOperation, Schema schemaFlow, Schema schemaReject,
            ValueProcessor valueProcessor) {
        this.writeOperation = writeOperation;
        this.schemaFlow = schemaFlow;
        this.schemaReject = schemaReject;
        this.valueProcessor = valueProcessor;
    }

    @Override
    public Result close() throws IOException {
        return new Result(uid, totalCount, successCount, rejectCount);
    }

    @Override
    public WriteOperation<Result> getWriteOperation() {
        return writeOperation;
    }

    @Override
    public void open(String arg0) throws IOException {
        this.uid = arg0;
        this.success = new LinkedList<>();
        this.reject = new LinkedList<>();
    }

    @Override
    public void write(Object arg0) throws IOException {
        if (arg0 == null) {
            return;
        } // else handle the data.
        totalCount++;
        IndexedRecord input = getFactory(arg0).convertToAvro(arg0);

        Map<String, Object> columnValuesMap = prepareMap(input);
        if (valueProcessor.process(columnValuesMap)) {
            handleSuccess(input);
        } else {
            handleReject(input);
        }
    }

    private void handleReject(IndexedRecord input) {
        rejectCount++;
        Schema outSchema = schemaReject;
        if (outSchema == null || outSchema.getFields().size() == 0)
            return;
        if (input.getSchema().equals(outSchema)) {
            reject.add(input);
        } else {
            IndexedRecord reject = new GenericData.Record(outSchema);
            for (Schema.Field outField : reject.getSchema().getFields()) {
                Object outValue = null;
                Schema.Field inField = input.getSchema().getField(outField.name());
                if (inField != null)
                    outValue = input.get(inField.pos());
                else if (TFilterRowProperties.FIELD_ERROR_MESSAGE.equals(outField.name())) {
                    outValue = valueProcessor.getError();
                }
                reject.put(outField.pos(), outValue);
            }
            this.reject.add(reject);
        }
    }

    private void handleSuccess(IndexedRecord input) {
        successCount++;
        Schema outSchema = schemaFlow;
        if (outSchema == null || outSchema.getFields().size() == 0)
            return;
        if (input.getSchema().equals(outSchema)) {
            success.add(input);
        } else {
            IndexedRecord successful = new GenericData.Record(outSchema);
            for (Schema.Field outField : successful.getSchema().getFields()) {
                Object outValue = null;
                Schema.Field inField = input.getSchema().getField(outField.name());
                if (inField != null)
                    outValue = input.get(inField.pos());
                successful.put(outField.pos(), outValue);
            }
            success.add(successful);
        }
    }

    private Map<String, Object> prepareMap(IndexedRecord input) {
        Map<String, Object> valuesMap = new HashMap<>();
        for (Schema.Field f : input.getSchema().getFields()) {
            Object inputValue = input.get(f.pos());
            valuesMap.put(f.name(), inputValue);
        }
        return valuesMap;
    }

    @SuppressWarnings("unchecked")
    public IndexedRecordConverter<Object, ? extends IndexedRecord> getFactory(Object datum) {
        if (null == factory) {
            factory = (IndexedRecordConverter<Object, ? extends IndexedRecord>) new AvroRegistry()
                    .createIndexedRecordConverter(datum.getClass());
        }
        return factory;
    }

    @Override
    public Iterable<IndexedRecord> getRejectedWrites() {
        LinkedList<IndexedRecord> rejectsResult = new LinkedList<>(reject);
        reject.clear();
        return rejectsResult;
    }

    @Override
    public Iterable<IndexedRecord> getSuccessfulWrites() {
        LinkedList<IndexedRecord> successResult = new LinkedList<>(success);
        success.clear();
        return successResult;
    }

}
