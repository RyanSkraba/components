// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;

public class MarketoUtils {

    private static final List<SimpleDateFormat> allowedDateFormats = Arrays.asList(
            new SimpleDateFormat(MarketoConstants.DATETIME_PATTERN_BASE),
            new SimpleDateFormat(MarketoConstants.DATETIME_PATTERN_PARAM),
            new SimpleDateFormat(MarketoConstants.DATETIME_PATTERN_PARAM_ALT),
            new SimpleDateFormat(MarketoConstants.DATETIME_PATTERN_PARAM_UTC),
            new SimpleDateFormat(MarketoConstants.DATETIME_PATTERN_REST),
            new SimpleDateFormat(MarketoConstants.DATETIME_PATTERN_SOAP));

    /**
     * Parse a string amongst date patterns allowed to give back the matching Date object
     *
     * @param datetime string to parse
     * @return java.util.Date parsed
     * @throws ParseException
     */
    public static Date parseDateString(String datetime) throws ParseException {
        Date result = null;
        for (SimpleDateFormat sdf : allowedDateFormats) {
            try {
                result = sdf.parse(datetime);
                break;
            } catch (ParseException e) {
                // nothing to do
            }
        }
        if (result == null) {
            throw new ParseException(datetime + " don't use a pattern allowed.", 0);
        }

        return result;
    }

    public static Field generateNewField(Field origin) {
        Schema.Field field = new Schema.Field(origin.name(), origin.schema(), origin.doc(), origin.defaultVal(), origin.order());
        field.getObjectProps().putAll(origin.getObjectProps());
        for (Map.Entry<String, Object> entry : origin.getObjectProps().entrySet()) {
            field.addProp(entry.getKey(), entry.getValue());
        }
        return field;
    }

    public static List<String> getSchemaFields(Schema schema) {
        List<String> fieldNames = new ArrayList<>();
        for (Schema.Field f : schema.getFields()) {
            fieldNames.add(f.name());
        }
        return fieldNames;
    }

    public static Schema newSchema(Schema metadataSchema, String newSchemaName, List<Schema.Field> moreFields) {
        Schema newSchema = Schema.createRecord(newSchemaName, metadataSchema.getDoc(), metadataSchema.getNamespace(),
                metadataSchema.isError());

        List<Schema.Field> copyFieldList = new ArrayList<>();
        for (Schema.Field se : metadataSchema.getFields()) {
            Schema.Field field = new Schema.Field(se.name(), se.schema(), se.doc(), se.defaultVal(), se.order());
            field.getObjectProps().putAll(se.getObjectProps());
            for (Map.Entry<String, Object> entry : se.getObjectProps().entrySet()) {
                field.addProp(entry.getKey(), entry.getValue());
            }
            copyFieldList.add(field);
        }
        copyFieldList.addAll(moreFields);

        newSchema.setFields(copyFieldList);
        for (Map.Entry<String, Object> entry : metadataSchema.getObjectProps().entrySet()) {
            newSchema.addProp(entry.getKey(), entry.getValue());
        }

        return newSchema;
    }

    /**
     * Get easyly the Avro field type from its schema
     *
     * @param field concerned
     * @return Avro.Type
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
     * Modify some fields in the provided schema with new fields definitions
     *
     * @param schema original schema
     * @param changedFields fields to change
     * @return modified schema
     */
    public static Schema modifySchemaFields(Schema schema, List<Schema.Field> changedFields) {
        Schema newSchema = Schema.createRecord(schema.getName(), schema.getDoc(), schema.getNamespace(), schema.isError());
        List<Schema.Field> fields = new ArrayList<>();
        for (Schema.Field se : schema.getFields()) {
            Schema.Field field = null;
            for (Field cf : changedFields) {
                if (cf.name().equals(se.name())) {
                    field = cf;
                    break;
                }
            }
            if (field == null) {
                field = new Schema.Field(se.name(), se.schema(), se.doc(), se.defaultVal(), se.order());
                for (Map.Entry<String, Object> entry : se.getObjectProps().entrySet()) {
                    field.addProp(entry.getKey(), entry.getValue());
                }
            }
            fields.add(field);
        }
        newSchema.setFields(fields);
        for (Map.Entry<String, Object> entry : schema.getObjectProps().entrySet()) {
            newSchema.addProp(entry.getKey(), entry.getValue());
        }

        return newSchema;
    }

}
