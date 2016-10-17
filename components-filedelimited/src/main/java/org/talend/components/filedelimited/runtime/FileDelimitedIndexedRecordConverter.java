package org.talend.components.filedelimited.runtime;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.talend.components.common.runtime.GenericIndexedRecordConverter;
import org.talend.components.filedelimited.FileDelimitedProperties;
import org.talend.daikon.avro.converter.AvroConverter;

public class FileDelimitedIndexedRecordConverter extends GenericIndexedRecordConverter {

    private String names[];

    private FileDelimitedProperties properties;

    public FileDelimitedProperties getProperties() {
        return properties;
    }

    public void setProperties(FileDelimitedProperties properties) {
        this.properties = properties;
    }

    @Override
    public IndexedRecord convertToAvro(IndexedRecord value) {
        return new FileDelimitedIndexedRecord(value);
    }

    private class FileDelimitedIndexedRecord implements IndexedRecord {

        private final IndexedRecord value;

        public FileDelimitedIndexedRecord(IndexedRecord value) {
            this.value = value;
        }

        @Override
        public Schema getSchema() {
            return FileDelimitedIndexedRecordConverter.this.getSchema();
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
                if (properties == null) {
                    throw new IllegalArgumentException("Runtime properties for converter is not be set!");
                }
                names = new String[getSchema().getFields().size()];
                fieldConverter = new AvroConverter[names.length];
                for (int j = 0; j < names.length; j++) {
                    Schema.Field f = getSchema().getFields().get(j);
                    names[j] = f.name();
                    fieldConverter[j] = FileDelimitedAvroRegistry.get().getConverter(f, properties);
                }
            }
            return fieldConverter[i].convertToDatum(value.get(getSchema().getField(names[i]).pos()));
        }
    }
}
