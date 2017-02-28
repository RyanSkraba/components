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

import com.google.api.client.util.Base64;
import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.converter.AvroConverter;
import org.talend.daikon.avro.converter.IndexedRecordConverter;

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
            fieldConverters.put(field.name(), BigQueryAvroRegistry.get().getConverter(field.schema()));
        }
    }

    @Override
    public IndexedRecord convertToAvro(T row) {
        return new BigQueryIndexedRecord(row);
    }

    public class BigQueryIndexedRecord implements IndexedRecord {

        private Object[] values;

        public BigQueryIndexedRecord(T row) {
            values = new Object[schema.getFields().size()];
            for (Schema.Field field : schema.getFields()) {
                Object v = row.get(field.name());
                // Need to decode base64 for bytes type, use avro bytes type is ok as only one bigquery bytes type mapping for avro bytes type.
                // But can be better if there is db type in avro schema.
                if(AvroUtils.isSameType(AvroUtils.unwrapIfNullable(field.schema()), AvroUtils._bytes())) {
                    v = ByteBuffer.wrap(Base64.decodeBase64((String) v));
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
}
