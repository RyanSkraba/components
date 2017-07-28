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
package org.talend.components.marketo.runtime.client;

import static org.talend.components.marketo.MarketoConstants.DATETIME_PATTERN_PARAM_UTC;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.daikon.avro.SchemaConstants;

public class MarketoClientUtils {

    private static final Logger LOG = LoggerFactory.getLogger(MarketoClientUtils.class);

    /**
     * Get The Avro field type from its schema
     * 
     * @param field
     * @return
     */
    public static Type getFieldType(Field field) {
        Schema convSchema = field.schema();
        Type type = field.schema().getType();
        if (convSchema.getType().equals(Type.UNION)) {
            for (Schema s : field.schema().getTypes()) {
                if (s.getType() != Type.NULL) {
                    type = s.getType();
                    break;
                }
            }
        }
        return type;
    }

    /**
     * Check if the Avro field is of Date type
     *
     * @param field
     * @return
     */
    public static boolean isDateTypeField(Field field) {
        if (field == null) {
            return false;
        }
        if (!Type.LONG.equals(getFieldType(field))) {
            return false;
        }
        String clazz = field.getProp(SchemaConstants.JAVA_CLASS_FLAG);
        String pattr = field.getProp(SchemaConstants.TALEND_COLUMN_PATTERN);
        return (clazz != null && clazz.equals(Date.class.getCanonicalName())) || !StringUtils.isEmpty(pattr);
    }

    /**
     * Format a long value to an UTC ISO8601 date format
     * 
     * @param datetime
     * @return
     */
    public static String formatLongToDateString(Long datetime) {
        if (datetime == null) {
            return null;
        }
        try {
            Date dt = null;
            // Mkto returns datetime in UTC and Follows W3C format (ISO 8601).
            dt = new DateTime(datetime, DateTimeZone.forID("UTC")).toDate();
            SimpleDateFormat sdf = new SimpleDateFormat(DATETIME_PATTERN_PARAM_UTC);
            return sdf.format(dt);
        } catch (Exception e) {
            LOG.error("Error while parsing date : {}.", e.getMessage());
        }
        return null;
    }
}
