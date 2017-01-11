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
 * Converts datum String to avro String
 */
public class StringToStringConverter implements AvroConverter<String, String> {
    
    /**
     * Returns schema of avro data
     */
    @Override
    public Schema getSchema() {
        return AvroUtils._string();
    }

    @Override
    public Class<String> getDatumClass() {
        return String.class;
    }

    /**
     * Remain value unchanged
     */
    @Override
    public String convertToDatum(String value) {
        return value;
    }

    /**
     * Remain value unchanged
     */
    @Override
    public String convertToAvro(String value) {
        return value;
    }

}
