package org.talend.components.fileinput.runtime;

import org.apache.avro.Schema;
import org.talend.daikon.avro.converter.IndexedRecordConverter;

public class FileInputAdaptorFactory implements IndexedRecordConverter<String, FileInputIndexedRecord> {

    Schema schema;

    @Override
    public Class<String> getDatumClass() {
        // TODO Auto-generated method stub
        return String.class;
    }

    @Override
    public String convertToDatum(FileInputIndexedRecord value) {
        return null;
    }

    @Override
    public FileInputIndexedRecord convertToAvro(String values) {

        return new FileInputIndexedRecord(values, schema);
    }

    @Override
    public Schema getSchema() {
        // TODO Auto-generated method stub
        return schema;
    }

    @Override
    public void setSchema(Schema schema) {
        // TODO Auto-generated method stub
        this.schema = schema;
    }

}
