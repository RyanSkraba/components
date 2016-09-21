package org.talend.components.filedelimited.runtime;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.talend.daikon.avro.converter.AvroConverter;
import org.talend.daikon.avro.converter.IndexedRecordConverter;

public class DelimitedAdaptorFactory implements IndexedRecordConverter<String[], IndexedRecord> {

    Schema schema;

    @Override
    public Class<String[]> getDatumClass() {
        return String[].class;
    }

    @Override
    public String[] convertToDatum(IndexedRecord value) {
        return null;
    }

    @Override
    public DelimitedIndexedRecord convertToAvro(String[] values) {
        return new DelimitedIndexedRecord(values);
    }

    @Override
    public Schema getSchema() {
        return schema;
    }

    @Override
    public void setSchema(Schema schema) {
        this.schema = schema;
    }

    private class DelimitedIndexedRecord implements IndexedRecord {

        String[] values;

        public DelimitedIndexedRecord(String[] values) {
            this.values = values;
        }

        @Override
        public void put(int index, Object json) {
            throw new UnsupportedOperationException();
        }

        private String names[];

        AvroConverter[] fieldConverter = null;

        @Override
        public Object get(int index) {
            // Lazy initialization of the cached converter objects.
            if (names == null) {
                names = new String[getSchema().getFields().size()];
                fieldConverter = new AvroConverter[names.length];
                for (int j = 0; j < names.length; j++) {
                    Schema.Field f = getSchema().getFields().get(j);
                    names[j] = f.name();
                    fieldConverter[j] = new FileDelimitedAvroRegistry().getConverter(f);
                }
            }
            if (index < values.length) {
                return fieldConverter[index].convertToAvro(values[index]);
            } else {
                return null;
            }
        }

        @Override
        public Schema getSchema() {
            return DelimitedAdaptorFactory.this.getSchema();
        }

    }

}
