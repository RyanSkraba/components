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

import java.util.Arrays;

import org.apache.avro.Schema;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.converter.AvroConverter;

/**
 * Converts datum byte[] to avro String in following format:
 * "[1, 2, 3, 4]"
 * where 1, 2, 3, 4 are byte element values
 */
public class BytesToStringConverter implements AvroConverter<byte[], String> {

    @Override
    public Schema getSchema() {
        return AvroUtils._string();
    }

    @Override
    public Class<byte[]> getDatumClass() {
        return byte[].class;
    }

    /**
     * TODO
     * It wasn't implemented due to lack of time
     * 1. Incoming should be checked whether is matches format "[1, 2, 3, 4]"
     * 2. It should be splitted in substrings containing byte values
     * 3. byte[] should be created and filled with byte values converted from substrings 
     */
    @Override
    public byte[] convertToDatum(String value) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String convertToAvro(byte[] value) {
        String avroString = Arrays.toString(value);
        return avroString;
    }

}
