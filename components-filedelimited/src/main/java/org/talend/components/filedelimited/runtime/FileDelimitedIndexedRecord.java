package org.talend.components.filedelimited.runtime;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.generic.IndexedRecord;
import org.talend.daikon.avro.converter.AvroConverter;

public class FileDelimitedIndexedRecord implements IndexedRecord {

    private Schema schema;

    private String values;

    public FileDelimitedIndexedRecord(String values, Schema schema) {
        this.values = values;
        this.schema = schema;
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
                Field f = getSchema().getFields().get(j);
                names[j] = f.name();
                fieldConverter[j] = new FileDelimitedAvroRegistry().getConverterFromString(f);
            }
        }
        return fieldConverter[index].convertToAvro(values);
    }

    @Override
    public Schema getSchema() {
        return schema;
    }

}
