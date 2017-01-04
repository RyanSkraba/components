package org.talend.components.jdbc.avro;

import java.math.BigDecimal;

import org.apache.avro.Schema;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.converter.AvroConverter;

/**
 * Converts datum BigDecimal to avro String
 */
public class BigDecimalToStringConverter implements AvroConverter<BigDecimal, String> {

    @Override
    public Schema getSchema() {
        return AvroUtils._string();
    }

    @Override
    public Class<BigDecimal> getDatumClass() {
        return BigDecimal.class;
    }

    @Override
    public BigDecimal convertToDatum(String value) {
        BigDecimal datumBigDecimal = new BigDecimal(value);
        return datumBigDecimal;
    }

    @Override
    public String convertToAvro(BigDecimal value) {
        String avroString = value.toString();
        return avroString;
    }

}
