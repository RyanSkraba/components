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
package org.talend.components.dataprep.runtime;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.talend.components.dataprep.connection.DataPrepField;
import org.talend.daikon.avro.AvroConverter;
import org.talend.daikon.avro.IndexedRecordAdapterFactory;

public class DataPrepAdaptorFactory implements IndexedRecordAdapterFactory<DataPrepField[], IndexedRecord> {

    private Schema schema;

    @Override
    public Schema getSchema() {
        return this.schema;
    }

    @Override
    public Class<DataPrepField[]> getDatumClass() {
        return DataPrepField[].class;
    }

    @Override
    public DataPrepField[] convertToDatum(IndexedRecord indexedRecord) {
        throw new UnmodifiableAdapterException();
    }

    @Override
    public IndexedRecord convertToAvro(DataPrepField[] dataPrepDataSetRecord) {
        return new DataPrepIndexedRecord(dataPrepDataSetRecord);
    }

    @Override
    public void setSchema(Schema schema) {
        this.schema = schema;
    }

    private class DataPrepIndexedRecord implements IndexedRecord {

        private DataPrepField[] dataPrepFields;

        private AvroConverter[] fieldConverter;

        private String[] names;

        DataPrepIndexedRecord(DataPrepField[] dataPrepFields) {
            this.dataPrepFields = dataPrepFields;
        }

        @Override
        public void put(int i, Object v) {
            throw new UnmodifiableAdapterException();
        }

        @Override
        public Object get(int i) {
            if (names == null) {
                names = new String[getSchema().getFields().size()];
                fieldConverter = new AvroConverter[names.length];
                for (int j = 0; j < names.length; j++) {
                    Schema.Field f = getSchema().getFields().get(j);
                    names[j] = f.name();
                    fieldConverter[j] = DataPrepAvroRegistry.getDataPrepInstance().getConverterFromString(f);
                }
            }
            Object value = null;
            for (DataPrepField field : dataPrepFields) {

                if (field.getColumnName().equals(names[i])) {
                    value = fieldConverter[i].convertToAvro(field.getContent());
                }
            }
            return value;
        }

        @Override
        public Schema getSchema() {
            return DataPrepAdaptorFactory.this.getSchema();
        }
    }
}
