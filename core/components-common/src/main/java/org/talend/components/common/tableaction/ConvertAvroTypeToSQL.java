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
package org.talend.components.common.tableaction;

import org.apache.avro.LogicalType;
import org.apache.avro.LogicalTypes;
import org.apache.avro.Schema;
import org.apache.avro.reflect.AvroSchema;
import org.talend.daikon.avro.AvroRegistry;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.SchemaConstants;

import java.lang.reflect.Field;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ConvertAvroTypeToSQL {

    private final static Map<Integer, String> SQLTypesMap = new HashMap<>();

    static {
        // Get a Map with all type java.sql.Types with int id as key
        Field[] fields = java.sql.Types.class.getFields();
        for (int i = 0; i < fields.length; i++) {
            try {
                String name = fields[i].getName();
                Integer value = (Integer) fields[i].get(null);
                SQLTypesMap.put(value, name);
            } catch (IllegalAccessException e) {
            }
        }
    }

    private final static AvroRegistry avroRegistry = new AvroRegistry();

    private TableActionConfig config;

    public ConvertAvroTypeToSQL(TableActionConfig config){
        this.config = config;

        Set<Map.Entry<Integer, String>> entries = this.config.CUSTOMIZE_SQLTYPE_TYPENAME.entrySet();
        for(Map.Entry<Integer, String> e : entries){
            SQLTypesMap.put(e.getKey(), e.getValue());
        }
    }

    public String convertToSQLTypeString(Schema schema) {
        int sqlType = convertToSQLType(schema);
        String sType = SQLTypesMap.get(sqlType);
        if (sType == null) {
            throw new UnsupportedOperationException("Can't find " + sqlType + " sql type. You may add it into TableActionConfig.CUSTOMIZE_SQLTYPE_TYPENAME.");
        }

        return sType;
    }

    public int convertToSQLType(Schema schema) {
        Schema.Type type = schema.getType(); // The standard Avro Type
        LogicalType logicalType = schema.getLogicalType(); // The logical type for Data by example
        String javaType = schema.getProp(SchemaConstants.JAVA_CLASS_FLAG);  // And the Talend java type if standard Avro type is Union

        if (logicalType == null && type == Schema.Type.UNION) {
            for (Schema s : schema.getTypes()) {
                logicalType = null;
                if (s.getType() != Schema.Type.NULL) {
                    type = s.getType();
                    javaType = s.getProp(SchemaConstants.JAVA_CLASS_FLAG);
                    logicalType = s.getLogicalType();
                    if (javaType == null && logicalType == null) {
                        type = s.getType(); // Get Avro type if JAVA_CLASS_FLAG is not defined
                    }
                    break;
                }
            }
        }

        int sqlType = Types.NULL;
        if (logicalType != null) {
            sqlType = convertAvroLogicialType(logicalType);
        } else if (javaType == null) {
            sqlType = convertRawAvroType(type);
        } else {
            sqlType = convertTalendAvroType(javaType);
        }

        if(this.config.CONVERT_SQLTYPE_TO_ANOTHER_SQLTYPE.containsKey(sqlType)){
            sqlType = this.config.CONVERT_SQLTYPE_TO_ANOTHER_SQLTYPE.get(sqlType);
        }

        return sqlType;
    }

    private int convertRawAvroType(Schema.Type type) {
        Integer sqlType = this.config.CONVERT_AVROTYPE_TO_SQLTYPE.get(type);
        if(sqlType != null){
            return sqlType;
        }

        switch (type) {
        case STRING:
            sqlType = Types.VARCHAR;
            break;
        case BYTES:
            sqlType = Types.BLOB;
            break;
        case INT:
            sqlType = Types.INTEGER;
            break;
        case LONG:
            sqlType = Types.INTEGER;
            break;
        case FLOAT:
            sqlType = Types.NUMERIC;
            break;
        case DOUBLE:
            sqlType = Types.NUMERIC;
            break;
        case BOOLEAN:
            sqlType = Types.BOOLEAN;
            break;
        default:
            // ignored types ENUM, RECORD, MAP, FIXED, ARRAY, NULL
            throw new UnsupportedOperationException(type + " Avro type not supported");
        }

        return sqlType;
    }

    private int convertAvroLogicialType(LogicalType logicalType) {

        Integer sqlType = this.config.CONVERT_LOGICALTYPE_TO_SQLTYPE.get(logicalType);
        if(sqlType != null){
            return sqlType;
        }

        if (logicalType == LogicalTypes.timestampMillis()) {
            sqlType = Types.TIMESTAMP;
        } else if (logicalType instanceof LogicalTypes.Decimal) {
            sqlType = Types.NUMERIC;
        } else if (logicalType == LogicalTypes.date()) {
            sqlType = Types.DATE;
        } else if (logicalType == LogicalTypes.uuid()) {
            sqlType = Types.VARCHAR;
        } else if (logicalType == LogicalTypes.timestampMicros()) {
            sqlType = Types.TIMESTAMP;
        } else if (logicalType == LogicalTypes.timeMillis()) {
            sqlType = Types.TIME;
        } else if (logicalType == LogicalTypes.timeMicros()) {
            sqlType = Types.TIME;
        } else {
            // All logical type should be supported
            throw new UnsupportedOperationException("Logical type " + logicalType + " not supported");
        }

        return sqlType;
    }

    private int convertTalendAvroType(String javaType) {
        Integer sqlType = this.config.CONVERT_JAVATYPE_TO_SQLTYPE.get(javaType);
        if(sqlType != null){
            return sqlType;
        }

        switch (javaType) {
        case "java.lang.Byte":
            sqlType = Types.SMALLINT;
            break;
        case "java.lang.Short":
            sqlType = Types.SMALLINT;
            break;
        case "java.lang.Character":
            sqlType = Types.VARCHAR;
            break;
        case "java.util.Date":
            sqlType = Types.DATE;
            break;
        case "java.math.BigDecimal":
            sqlType = Types.NUMERIC;
            break;
        default:
            throw new UnsupportedOperationException(javaType + " class can't be converted to SQL type.");
        }

        return sqlType;
    }

}