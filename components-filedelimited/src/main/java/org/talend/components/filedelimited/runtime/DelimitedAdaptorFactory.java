package org.talend.components.filedelimited.runtime;

import java.io.IOException;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.talend.daikon.avro.converter.AvroConverter;
import org.talend.daikon.avro.converter.IndexedRecordConverter;
import org.talend.fileprocess.FileInputDelimited;

public class DelimitedAdaptorFactory implements IndexedRecordConverter<FileInputDelimited, IndexedRecord> {

    Schema schema;

    @Override
    public Class<FileInputDelimited> getDatumClass() {
        return FileInputDelimited.class;
    }

    @Override
    public FileInputDelimited convertToDatum(IndexedRecord value) {
        return null;
    }

    @Override
    public DelimitedIndexedRecord convertToAvro(FileInputDelimited fid) {
        return new DelimitedIndexedRecord(fid);
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

        private FileInputDelimited fid;

        public DelimitedIndexedRecord(FileInputDelimited fid) {
            this.fid = fid;
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
            try {
                return fieldConverter[index].convertToAvro(fid.get(index));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public Schema getSchema() {
            return DelimitedAdaptorFactory.this.getSchema();
        }

    }

}
