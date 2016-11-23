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
package org.talend.components.common.runtime;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.talend.daikon.avro.converter.AvroConverter;
import org.talend.daikon.avro.converter.IndexedRecordConverter;

public class GenericIndexedRecordConverter implements IndexedRecordConverter<IndexedRecord, IndexedRecord> {

    private Schema schema;

    private String names[];

    /**
     * The cached AvroConverter objects for the fields of this record.
     */
    @SuppressWarnings("rawtypes")
    protected transient AvroConverter[] fieldConverter;

    @Override
    public Schema getSchema() {
        return schema;
    }

    @Override
    public void setSchema(Schema schema) {
        this.schema = schema;
    }

    @Override
    public Class<IndexedRecord> getDatumClass() {
        return IndexedRecord.class;
    }

    @Override
    public IndexedRecord convertToDatum(IndexedRecord value) {
        throw new UnmodifiableAdapterException();
    }

    @Override
    public IndexedRecord convertToAvro(IndexedRecord value) {
        return new GenericIndexedRecord(value);
    }

    private class GenericIndexedRecord implements IndexedRecord {

        private final IndexedRecord value;

        public GenericIndexedRecord(IndexedRecord value) {
            this.value = value;
        }

        @Override
        public Schema getSchema() {
            return GenericIndexedRecordConverter.this.getSchema();
        }

        @Override
        public void put(int i, Object v) {
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
                    Schema.Field f = getSchema().getFields().get(j);
                    names[j] = f.name();
                    fieldConverter[j] = GenericAvroRegistry.get().convertToString(f);
                }
            }
            return fieldConverter[i].convertToDatum(value.get(getSchema().getField(names[i]).pos()));
        }
    }
}
