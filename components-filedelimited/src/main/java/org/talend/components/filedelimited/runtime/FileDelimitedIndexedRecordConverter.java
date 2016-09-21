package org.talend.components.filedelimited.runtime;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.talend.components.common.ComponentConstants;
import org.talend.components.common.runtime.GenericIndexedRecordConverter;
import org.talend.daikon.avro.converter.AvroConverter;

public class FileDelimitedIndexedRecordConverter extends GenericIndexedRecordConverter {

    private String names[];

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
                names = new String[getSchema().getFields().size()];
                fieldConverter = new AvroConverter[names.length];
                String thousandsSeparator = getSchema().getProp(ComponentConstants.THOUSANDS_SEPARATOR);
                String decimalSeparator = getSchema().getProp(ComponentConstants.DECIMAL_SEPARATOR);
                String encoding = getSchema().getProp(ComponentConstants.FILE_ENCODING);
                for (int j = 0; j < names.length; j++) {
                    Schema.Field f = getSchema().getFields().get(j);
                    names[j] = f.name();
                    if (thousandsSeparator != null) {
                        f.addProp(ComponentConstants.THOUSANDS_SEPARATOR, thousandsSeparator);
                    }
                    if (decimalSeparator != null) {
                        f.addProp(ComponentConstants.DECIMAL_SEPARATOR, decimalSeparator);
                    }
                    if (encoding != null) {
                        f.addProp(ComponentConstants.FILE_ENCODING, encoding);
                    }
                    fieldConverter[j] = FileDelimitedAvroRegistry.get().getConverter(f);
                }
            }
            return fieldConverter[i].convertToDatum(value.get(getSchema().getField(names[i]).pos()));
        }
    }
}
