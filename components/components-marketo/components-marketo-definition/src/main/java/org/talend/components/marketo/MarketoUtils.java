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
package org.talend.components.marketo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;

public class MarketoUtils {

    /**
     * Parse a string amongst date patterns allowed to give back the matching Date object
     * 
     * @param datetime string to parse
     * @return java.util.Date parsed
     * @throws ParseException
     */
    public static Date parseDateString(String datetime) throws ParseException {
        Date result;
        try {
            result = new SimpleDateFormat(MarketoConstants.DATETIME_PATTERN_PARAM).parse(datetime);
            return result;
        } catch (ParseException e) {
        }
        try {
            result = new SimpleDateFormat(MarketoConstants.DATETIME_PATTERN_PARAM_ALT).parse(datetime);
            return result;
        } catch (ParseException e) {
        }
        try {
            result = new SimpleDateFormat(MarketoConstants.DATETIME_PATTERN_PARAM_UTC).parse(datetime);
            return result;
        } catch (ParseException e) {
        }
        try {
            result = new SimpleDateFormat(MarketoConstants.DATETIME_PATTERN_REST).parse(datetime);
            return result;
        } catch (ParseException e) {
        }
        try {
            result = new SimpleDateFormat(MarketoConstants.DATETIME_PATTERN_SOAP).parse(datetime);
            return result;
        } catch (ParseException e) {
        }
        throw new ParseException(datetime + " don't use a pattern allowed.", 0);
    }

    public static Field generateNewField(Field origin) {
        Schema.Field field = new Schema.Field(origin.name(), origin.schema(), origin.doc(), origin.defaultVal(), origin.order());
        field.getObjectProps().putAll(origin.getObjectProps());
        for (Map.Entry<String, Object> entry : origin.getObjectProps().entrySet()) {
            field.addProp(entry.getKey(), entry.getValue());
        }
        return field;
    }

}
