package org.talend.components.jdbc.avro;

import org.apache.avro.Schema;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.converter.AvroConverter;

/**
 * Converts from datum Boolean to avro String
 */
public class BooleanToStringConverter implements AvroConverter<Boolean, String> {

    @Override
    public Schema getSchema() {
        return AvroUtils._string();
    }

    @Override
    public Class<Boolean> getDatumClass() {
        return Boolean.class;
    }

    @Override
    public Boolean convertToDatum(String value) {
        Boolean datumBoolean = Boolean.parseBoolean(value);
        return datumBoolean;
    }

    @Override
    public String convertToAvro(Boolean value) {
        String avroString = value.toString();
        return avroString;
    }

}
