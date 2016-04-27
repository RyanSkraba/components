package org.talend.components.dataprep;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.talend.daikon.avro.IndexedRecordAdapterFactory;

public class DataPrepAdaptorFactory implements IndexedRecordAdapterFactory<String[],IndexedRecord> {
    private Schema schema;

    @Override
    public Schema getSchema() {
        return this.schema;
    }

    @Override
    public Class<String[]> getDatumClass() {
        return String[].class;
    }

    @Override
    public String[] convertToDatum(IndexedRecord indexedRecord) {
        int size = ((DataPrepIndexedRecord)indexedRecord).getSize();
        String[] datum = new String[size];
        for (int i = 0; i < ((DataPrepIndexedRecord)indexedRecord).getSize(); i++) {
            datum[i] = (String) indexedRecord.get(i);
        }
        return null;
    }

    @Override
    public IndexedRecord convertToAvro(String[] dataPrepDataSetRecord) {
        IndexedRecord indexRecord = new DataPrepIndexedRecord(dataPrepDataSetRecord.length);
        for (int i = 0; i < dataPrepDataSetRecord.length - 1; i++) {
            indexRecord.put(i, dataPrepDataSetRecord[i]);
        }
        return indexRecord;
    }

    @Override
    public void setSchema(Schema schema) {
        this.schema = schema;
    }

    private class DataPrepIndexedRecord implements IndexedRecord {
        private String[] data;
        private int size;
//        private Schema schema;

        DataPrepIndexedRecord(int size) {
            this.size = size;
            this.data = new String[size];
        }

        @Override
        public void put(int i, Object v) {
            data[i] = (String) v;
        }

        @Override
        public Object get(int i) {
            return data[i];
        }

        public int getSize() {
            return size;
        }

        public Schema getSchema() {
            return DataPrepAdaptorFactory.this.getSchema();
        }
    }
}
