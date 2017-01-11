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

import org.apache.avro.Schema;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.converter.AvroConverter;

/**
 * Converts datum Integer to avro String
 */
public class IntegerToStringConverter implements AvroConverter<Integer, String> {

    @Override
    public Schema getSchema() {
        return AvroUtils._string();
    }

    @Override
    public Class<Integer> getDatumClass() {
        return Integer.class;
    }

    @Override
    public Integer convertToDatum(String value) {
        int datumInt = Integer.parseInt(value);
        return datumInt;
    }

    @Override
    public String convertToAvro(Integer value) {
        String avroString = value.toString();
        return avroString;
    }

}
