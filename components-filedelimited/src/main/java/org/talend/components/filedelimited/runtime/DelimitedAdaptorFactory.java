package org.talend.components.filedelimited.runtime;

import java.util.HashMap;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.apache.commons.lang3.StringUtils;
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

        private boolean trimValues[];

        AvroConverter[] fieldConverter = null;

        @Override
        public Object get(int index) {
            // Lazy initialization of the cached converter objects.
            Object value = null;
            if (trimValues == null) {
                trimValues = new boolean[getSchema().getFields().size()];
                fieldConverter = new AvroConverter[trimValues.length];
                for (int j = 0; j < trimValues.length; j++) {
                    Schema.Field f = getSchema().getFields().get(j);
                    fieldConverter[j] = new FileDelimitedAvroRegistry().getConverter(f);
                    trimValues[j] = Boolean.parseBoolean(f.getProp(ComponentConstants.TRIM_FIELD_VALUE));
                }
            }
            if (index < values.length) {
                try {
                    if (trimValues[index] && !StringUtils.isEmpty(values[index])) {
                        values[index] = values[index].trim();
                    }
                    value = fieldConverter[index].convertToAvro(values[index]);
                } catch (Exception e) {
                    if (Boolean.parseBoolean(getSchema().getProp(ComponentConstants.DIE_ON_ERROR))) {
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
