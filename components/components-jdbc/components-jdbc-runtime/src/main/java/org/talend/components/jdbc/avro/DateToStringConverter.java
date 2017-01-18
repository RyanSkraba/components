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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.apache.avro.Schema;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.converter.AvroConverter;

/**
 * Converts datum Date to avro String
 */
public class DateToStringConverter implements AvroConverter<Date, String> {
    
    public static final String DEFAULT_PATTERN = "dd-MM-yyyy hh:mm:ss:SSS";
    
    /**
     * Used to format date string
     */
    private DateFormat dateFormat;
    
    public DateToStringConverter() {
        this(DEFAULT_PATTERN);
    }
    
    public DateToStringConverter(String datePattern) {
        dateFormat = new SimpleDateFormat(datePattern);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @Override
    public Schema getSchema() {
        return AvroUtils._string();
    }

    @Override
    public Class<Date> getDatumClass() {
        return Date.class;
    }

    /**
     * TODO implement it
     */
    @Override
    public Date convertToDatum(String value) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String convertToAvro(Date value) {
        String avroString = dateFormat.format(value);
        return avroString;
    }

}
