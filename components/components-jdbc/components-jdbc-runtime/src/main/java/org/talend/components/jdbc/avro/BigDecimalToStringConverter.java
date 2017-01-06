// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
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
