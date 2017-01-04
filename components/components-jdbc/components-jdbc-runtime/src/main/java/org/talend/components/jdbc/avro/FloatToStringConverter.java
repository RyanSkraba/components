package org.talend.components.jdbc.avro;

import org.apache.avro.Schema;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.converter.AvroConverter;

/**
 * Converts datum Float to avro String
 */
public class FloatToStringConverter implements AvroConverter<Float, String> {

    @Override
    public Schema getSchema() {
        return AvroUtils._string();
    }

    @Override
    public Class<Float> getDatumClass() {
        return Float.class;
    }

    @Override
    public Float convertToDatum(String value) {
        Float datumFloat = Float.parseFloat(value);
        return datumFloat;
    }

    @Override
    public String convertToAvro(Float value) {
        String avroString = value.toString();
        return avroString;
    }

}
