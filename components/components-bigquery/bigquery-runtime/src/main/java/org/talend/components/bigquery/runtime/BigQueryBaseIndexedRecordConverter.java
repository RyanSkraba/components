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

package org.talend.components.bigquery.runtime;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.talend.daikon.avro.AvroRegistry;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.converter.AvroConverter;
import org.talend.daikon.avro.converter.ComparableIndexedRecordBase;
import org.talend.daikon.avro.converter.ConvertAvroList;
import org.talend.daikon.avro.converter.IndexedRecordConverter;

import com.google.api.client.util.Base64;
import com.google.api.services.bigquery.model.TableRow;

/**
 * Converter for convert {@link TableRow} to {@link IndexedRecord} {@link TableRow} does not contain predefined schema,
 * so need to set schema before calling convertToAvro/convertToDatum
 */
public abstract class BigQueryBaseIndexedRecordConverter<T extends Map> implements IndexedRecordConverter<T, IndexedRecord> {

    protected Map<String, AvroConverter> fieldConverters;

    protected Schema schema;

    @Override
    public Schema getSchema() {
        return schema;
    }

    @Override
    public void setSchema(Schema schema) {
        this.schema = schema;
        initFieldConverters();
    }

    /**
     * Construct all field's converter for read or write Ensure to do that before read and write operation
     */
    protected void initFieldConverters() {
        List<Schema.Field> fields = schema.getFields();
        fieldConverters = new HashMap<>();
        for (Schema.Field field : fields) {
            fieldConverters.put(field.name(), getConverter(field.schema()));
        }
    }

    @Override
    public IndexedRecord convertToAvro(T row) {
        return new BigQueryIndexedRecord(row);
    }

    public class BigQueryIndexedRecord extends ComparableIndexedRecordBase {

        private Object[] values;

        public BigQueryIndexedRecord(T row) {
            values = new Object[schema.getFields().size()];
            for (Schema.Field field : schema.getFields()) {
                Object v = row.get(field.name());
                // Need to decode base64 for bytes type, use avro bytes type is ok as only one bigquery bytes type mapping for avro bytes type.
                // But can be better if there is db type in avro schema.
                if (AvroUtils.isSameType(AvroUtils.unwrapIfNullable(field.schema()), AvroUtils._bytes())) {
                    v = v == null ? null : ByteBuffer.wrap(Base64.decodeBase64((String) v));
                }
                values[field.pos()] = fieldConverters.get(field.name()).convertToAvro(v);
            }
        }

        @Override
        public void put(int i, Object v) {
            throw new UnsupportedOperationException("Should not write to a read-only item.");
        }

        @Override
        public Object get(int i) {
            return values[i];
        }

        @Override
        public Schema getSchema() {
            return schema;
        }
    }

    abstract BigQueryBaseIndexedRecordConverter createRecordConverter();

    /**
     *
     * @param fieldSchema
     * @param <T>
     * @return
     */
    public <T> AvroConverter<? super T, ?> getConverter(org.apache.avro.Schema fieldSchema) {
        fieldSchema = AvroUtils.unwrapIfNullable(fieldSchema);
        if (AvroUtils.isSameType(fieldSchema, AvroUtils._boolean())) {
            return (AvroConverter<? super T, ?>) BigQueryAvroRegistry.get().getConverter(Boolean.class);
        }
        if (AvroUtils.isSameType(fieldSchema, AvroUtils._double())) {
            return (AvroConverter<? super T, ?>) new AvroConverter<Number, Double>() {

                @Override
                public org.apache.avro.Schema getSchema() {
                    return AvroUtils._double();
                }

                @Override
                public Class<Number> getDatumClass() {
                    return Number.class;
                }

                @Override
                public Number convertToDatum(Double aDouble) {
                    return aDouble;
                }

                @Override
                public Double convertToAvro(Number number) {
                    return number == null ? null : number.doubleValue();
                }
            };
        }
        if (AvroUtils.isSameType(fieldSchema, AvroUtils._long())) {
            return (AvroConverter<? super T, ?>) new AvroConverter<Object, Long>() {

                @Override
                public org.apache.avro.Schema getSchema() {
                    return AvroUtils._long();
                }

                @Override
                public Class<Object> getDatumClass() {
                    return Object.class;
                }

                @Override
                public Number convertToDatum(Long aLong) {
                    return aLong;
                }

                @Override
                public Long convertToAvro(Object number) {
                    if (number instanceof Number)
                        return ((Number) number).longValue();
                    return number == null ? null : Long.valueOf(String.valueOf(number));
                }
            };
        }
        if (AvroUtils.isSameType(fieldSchema, AvroUtils._bytes())) {
            return new AvroRegistry.Unconverted(ByteBuffer.class, AvroUtils._bytes());
        }
        if (fieldSchema.getType() == org.apache.avro.Schema.Type.RECORD) {
            BigQueryBaseIndexedRecordConverter recordTypeIndexedRecordConverter = createRecordConverter();
            recordTypeIndexedRecordConverter.setSchema(fieldSchema);
            return (AvroConverter<? super T, ?>) recordTypeIndexedRecordConverter;
        }
        if (fieldSchema.getType() == org.apache.avro.Schema.Type.ARRAY) {
            org.apache.avro.Schema elementSchema = AvroUtils.unwrapIfNullable(fieldSchema.getElementType());
            // List.class is enough here, ConvertAvroList do not use it actually
            return new ConvertAvroList(List.class, elementSchema, getConverter(elementSchema));
        }
        // When construct BigQuery TableRow object, the value's java type is String for the rest of LegacySQLTypeName
        // And after AvroCoder, the type of String changes to Utf8, so need to convert it to String too
        return new AvroConverter<Object, Object>() {

            @Override
            public org.apache.avro.Schema getSchema() {
                return org.apache.avro.Schema.create(org.apache.avro.Schema.Type.STRING);
            }

            @Override
            public Class getDatumClass() {
                return Object.class;
            }

            @Override
            public Object convertToDatum(Object o) {
                return o == null ? null : String.valueOf(o);
            }

            @Override
            public Object convertToAvro(Object o) {
                return o == null ? null : String.valueOf(o);
            }
        };
    }
}
