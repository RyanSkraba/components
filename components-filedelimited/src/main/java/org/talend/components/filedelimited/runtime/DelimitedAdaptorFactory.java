package org.talend.components.filedelimited.runtime;

import java.util.HashMap;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.talend.components.api.exception.DataRejectException;
import org.talend.components.common.ComponentConstants;
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
            Object value = null;
            if (names == null) {
                names = new String[getSchema().getFields().size()];
                fieldConverter = new AvroConverter[names.length];
                for (int j = 0; j < names.length; j++) {
                    Schema.Field f = getSchema().getFields().get(j);
                    names[j] = f.name();
                    f.addProp(ComponentConstants.CHECK_DATE, getSchema().getProp(ComponentConstants.CHECK_DATE));
                    fieldConverter[j] = new FileDelimitedAvroRegistry().getConverter(f);
                }
            }
            if (index < values.length) {
                try {
                    value = fieldConverter[index].convertToAvro(values[index]);
                } catch (Exception e) {
                    String propValue = getSchema().getProp(ComponentConstants.DIE_ON_ERROR);
                    if (propValue != null && Boolean.valueOf(propValue)) {
                        throw e;
                    } else {
                        Map<String, Object> resultMessage = new HashMap<String, Object>();
                        resultMessage.put("errorMessage", e.getMessage());
                        resultMessage.put("talend_record", this);
                        throw new DataRejectException(resultMessage);
                    }
                }
            }
            return value;
        }

        @Override
        public Schema getSchema() {
            return DelimitedAdaptorFactory.this.getSchema();
        }

    }

}
