package org.talend.components.filedelimited.runtime;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.apache.commons.lang3.StringUtils;
import org.talend.components.api.exception.DataRejectException;
import org.talend.components.filedelimited.FileDelimitedProperties;
import org.talend.components.filedelimited.tfileinputdelimited.TFileInputDelimitedProperties;
import org.talend.daikon.avro.converter.AvroConverter;
import org.talend.daikon.avro.converter.IndexedRecordConverter;

public class DelimitedAdaptorFactory implements IndexedRecordConverter<String[], IndexedRecord> {

    Schema schema;

    FileDelimitedProperties properties;

    private boolean trimValues[];

    private AvroConverter[] fieldConverter = null;

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

    public FileDelimitedProperties getProperties() {
        return properties;
    }

    public void setProperties(FileDelimitedProperties properties) {
        this.properties = properties;
    }

    private void initializeConverters() {
        if (properties == null) {
            throw new IllegalArgumentException("Runtime properties for converter is not be set!");
        }
        trimValues = new boolean[getSchema().getFields().size()];
        fieldConverter = new AvroConverter[trimValues.length];
        Object trimSelect = ((TFileInputDelimitedProperties) properties).trimColumns.trimTable.trim.getValue();
        boolean trimAll = ((TFileInputDelimitedProperties) properties).trimColumns.trimAll.getValue();
        for (int j = 0; j < trimValues.length; j++) {
            Schema.Field f = getSchema().getFields().get(j);
            fieldConverter[j] = new FileDelimitedAvroRegistry().getConverter(f, properties);
            if (trimAll) {
                trimValues[j] = true;
            } else if (trimSelect != null && (trimSelect instanceof List) && (j < ((List<Boolean>) trimSelect).size())) {
                trimValues[j] = ((List<Boolean>) trimSelect).get(j);
            }

        }
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

        @Override
        public Object get(int index) {
            // Lazy initialization of the cached converter objects.
            if (trimValues == null) {
                initializeConverters();
            }

            Object value = null;
            if (index < values.length) {
                try {
                    if (trimValues[index] && !StringUtils.isEmpty(values[index])) {
                        values[index] = values[index].trim();
                    }
                    value = fieldConverter[index].convertToAvro(values[index]);
                } catch (Exception e) {
                    if (((TFileInputDelimitedProperties) properties).dieOnError.getValue()) {
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
