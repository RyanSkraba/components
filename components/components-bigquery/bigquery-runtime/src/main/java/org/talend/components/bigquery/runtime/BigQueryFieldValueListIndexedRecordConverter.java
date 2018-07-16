package org.talend.components.bigquery.runtime;

import java.util.HashMap;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.exception.error.CommonErrorCodes;

public class BigQueryFieldValueListIndexedRecordConverter
        extends BigQueryBaseIndexedRecordConverter<Map<String, Object>> {

    @Override
    public Class<Map<String, Object>> getDatumClass() {
        throw TalendRuntimeException.build(CommonErrorCodes.UNEXPECTED_EXCEPTION).create();
    }

    @Override
    public Map<String, Object> convertToDatum(IndexedRecord indexedRecord) {
        // When BigQueryOutput do not specify schema, so read it from the incoming data
        if (schema == null) {
            schema = indexedRecord.getSchema();
            initFieldConverters();
        }

        Map<String, Object> row = new HashMap<>();
        for (Schema.Field field : schema.getFields()) {
            Object v = indexedRecord.get(field.pos());
            if (v != null) {
                row.put(field.name(), fieldConverters.get(field.name()).convertToDatum(v));
            }
        }
        return row;
    }

    @Override
    BigQueryBaseIndexedRecordConverter createRecordConverter() {
        return new BigQueryFieldValueListIndexedRecordConverter();
    }
}
