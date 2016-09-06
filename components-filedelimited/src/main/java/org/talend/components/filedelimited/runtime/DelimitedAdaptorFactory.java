package org.talend.components.filedelimited.runtime;

import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.talend.daikon.avro.converter.AvroConverter;
import org.talend.daikon.avro.converter.IndexedRecordConverter;

public class DelimitedAdaptorFactory implements IndexedRecordConverter<List, IndexedRecord> {

    Schema schema;

    @Override
    public Class<List> getDatumClass() {
        return List.class;
    }

    @Override
    public List convertToDatum(IndexedRecord value) {
        return null;
    }

    @Override
    public DelimitedIndexedRecord convertToAvro(List values) {
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

        List<String> values;

        public DelimitedIndexedRecord(List<String> values) {
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
                    fieldConverter[j] = new FileDelimitedAvroRegistry().getConverterFromString(f);
                }
            }
            if (index < values.size()) {
                return fieldConverter[index].convertToAvro(values.get(index));
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
