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
package org.talend.components.salesforce.runtime;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.generic.IndexedRecord;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.avro.converter.AvroConverter;
import org.talend.daikon.avro.converter.IndexedRecordConverter;

public class BulkResultAdapterFactory implements IndexedRecordConverter<BulkResult, IndexedRecord> {

    private Schema schema;

    private String names[];

    private boolean returnNullForEmpty;

    /** The cached AvroConverter objects for the fields of this record. */
    @SuppressWarnings("rawtypes")
    protected transient AvroConverter[] fieldConverter;

    @Override
    public Schema getSchema() {
        return schema;
    }

    @Override
    public Class<BulkResult> getDatumClass() {
        return BulkResult.class;
    }

    @Override
    public BulkResult convertToDatum(IndexedRecord indexedRecord) {
        throw new UnmodifiableAdapterException();
    }

    @Override
    public IndexedRecord convertToAvro(BulkResult result) {
        if (AvroUtils.isIncludeAllFields(schema)) {
            List<Field> fields = new ArrayList<>();
            Set<String> resultFieldNames = result.values.keySet();
            for (String fieldName : resultFieldNames) {
                // skip field which set in design schema or duplicate value
                if (schema.getField(fieldName) != null || "Created".equals(fieldName) || "Error".equals(fieldName)
                        || "Id".equals(fieldName) || "Success".equals(fieldName)) {
                    continue;
                }
                // only can set String type, as we can't guess type here.
                Field field = new Schema.Field(fieldName, Schema.create(Schema.Type.STRING), null, (Object) null);
                field.addProp(SchemaConstants.TALEND_IS_LOCKED, "false");
                field.addProp(SchemaConstants.TALEND_FIELD_GENERATED, "true");
                field.addProp(SchemaConstants.TALEND_COLUMN_DB_LENGTH, "255");
                fields.add(field);
            }
            for (Schema.Field se : schema.getFields()) {
                Schema.Field field = new Schema.Field(se.name(), se.schema(), se.doc(), se.defaultVal(), se.order());
                field.getObjectProps().putAll(se.getObjectProps());
                for (Map.Entry<String, Object> entry : se.getObjectProps().entrySet()) {
                    field.addProp(entry.getKey(), entry.getValue());
                }
                fields.add(field);
            }
            // update runtime schema to correct one
            schema = Schema.createRecord(schema.getName(), schema.getDoc(), schema.getNamespace(), schema.isError());
            schema.setFields(fields);
        }
        return new ResultIndexedRecord(result);
    }

    @Override
    public void setSchema(Schema schema) {
        this.schema = schema;
        Object propValue = schema.getObjectProp(SalesforceSchemaConstants.RETURN_NULL_FOR_EMPTY);
        this.returnNullForEmpty = (propValue == null) ? false : (Boolean) propValue;
    }

    private class ResultIndexedRecord implements IndexedRecord {

        private final BulkResult value;

        public ResultIndexedRecord(BulkResult value) {
            this.value = value;
        }

        @Override
        public void put(int i, Object o) {
            throw new UnmodifiableAdapterException();
        }

        @SuppressWarnings("unchecked")
        @Override
        public Object get(int i) {
            // Lazy initialization of the cached converter objects.
            if (names == null) {
                names = new String[getSchema().getFields().size()];
                fieldConverter = new AvroConverter[names.length];
                for (int j = 0; j < names.length; j++) {
                    Field f = getSchema().getFields().get(j);
                    names[j] = f.name();
                    fieldConverter[j] = SalesforceAvroRegistry.get().getConverterFromString(f);
                }
            }
            Object resultValue = value.getValue(names[i]);
            if (resultValue == null) {
                String columnName = names[i].substring(names[i].indexOf("_") + 1);
                resultValue = value.getValue(columnName);
            }
            if (returnNullForEmpty && resultValue != null && "".equals(resultValue)) {
                resultValue = null;
            }
            return fieldConverter[i].convertToAvro(resultValue);
        }

        @Override
        public Schema getSchema() {
            return BulkResultAdapterFactory.this.getSchema();
        }
    }
}
