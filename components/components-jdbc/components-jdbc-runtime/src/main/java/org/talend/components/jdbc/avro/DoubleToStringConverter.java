package org.talend.components.jdbc.avro;

import org.apache.avro.Schema;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.converter.AvroConverter;

/**
 * Converts datum Double to avro String
 */
public class DoubleToStringConverter implements AvroConverter<Double, String> {

    @Override
    public Schema getSchema() {
        return AvroUtils._string();
    }

    @Override
    public Class<Double> getDatumClass() {
        return Double.class;
    }

    @Override
    public Double convertToDatum(String value) {
        Double datumDouble = Double.parseDouble(value);
        return datumDouble;
    }

    @Override
    public String convertToAvro(Double value) {
        String avroString = value.toString();
        return avroString;
    }

}
