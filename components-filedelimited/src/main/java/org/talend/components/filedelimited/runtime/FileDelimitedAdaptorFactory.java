package org.talend.components.filedelimited.runtime;

import org.apache.avro.Schema;
import org.talend.daikon.avro.converter.IndexedRecordConverter;

public class FileDelimitedAdaptorFactory implements IndexedRecordConverter<String, FileDelimitedIndexedRecord> {

    Schema schema;

    @Override
    public Class<String> getDatumClass() {
        return String.class;
    }

    @Override
    public String convertToDatum(FileDelimitedIndexedRecord value) {
        return null;
    }

    @Override
    public FileDelimitedIndexedRecord convertToAvro(String values) {
        return new FileDelimitedIndexedRecord(values, schema);
    }

    @Override
    public Schema getSchema() {
        return schema;
    }

    @Override
    public void setSchema(Schema schema) {
        this.schema = schema;
    }

}
