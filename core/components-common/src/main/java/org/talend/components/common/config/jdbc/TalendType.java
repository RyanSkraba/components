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
package org.talend.components.common.config.jdbc;

import java.util.HashMap;
import java.util.Map;

import org.apache.avro.Schema;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.LogicalTypeUtils;
import org.talend.daikon.avro.SchemaConstants;

/**
 * Talend data type
 */
public enum TalendType {
    LIST("id_List"),
    BOOLEAN("id_Boolean"),
    BYTE("id_Byte"),
    BYTES("id_byte[]"),
    CHARACTER("id_Character"),
    DATE("id_Date"),
    BIG_DECIMAL("id_BigDecimal"),
    DOUBLE("id_Double"),
    FLOAT("id_Float"),
    INTEGER("id_Integer"),
    LONG("id_Long"),
    OBJECT("id_Object"),
    SHORT("id_Short"),
    STRING("id_String");

    private static final Map<String, TalendType> talendTypes = new HashMap<>();

    static {
        for (TalendType talendType : values()) {
            talendTypes.put(talendType.typeName, talendType);
        }
    }

    private final String typeName;

    private TalendType(String typeName) {
        this.typeName = typeName;
    }

    public String getName() {
        return typeName;
    }

    /**
     * Provides {@link TalendType} by its name
     * 
     * @param typeName {@link TalendType} name
     * @return {@link TalendType}
     */
    public static TalendType get(String typeName) {
        TalendType talendType = talendTypes.get(typeName);
        if (talendType == null) {
            throw new IllegalArgumentException(String.format("Invalid value %s, it should be one of %s", typeName, talendTypes));
        }
        return talendType;
    }

    /**
     * Converts Avro type to Talend type
     * Conversion strategy is following:
     * 1. check Avro logical type
     * 2. check java-class property
     * 3. if above things are null, convert it according schema type
     * Avro type doesn't uniquely identify Talend type. Several Talend types may correspond to the same Avro type.
     * Thus, logical type and java-class are checked first as they uniquely identify DI type
     * 
     * @param avroType Avro field schema
     * @return corresponding Talend type
     */
    public static TalendType convertFromAvro(Schema avroType) {
        Schema type = AvroUtils.unwrapIfNullable(avroType);
        String logicalType = LogicalTypeUtils.getLogicalTypeName(type);

        if (logicalType != null) {
            return getTalendByLogicalType(logicalType);
        }

        String javaClass = type.getProp(SchemaConstants.JAVA_CLASS_FLAG);
        if (javaClass != null) {
            return getTalendByJavaClass(javaClass);
        }

        return getTalendByAvroType(type.getType());
    }

    /**
     * Returns Talend metadata type which corresponds to Avro logical type
     * 
     * @param logicalType Avro logical type
     * @return Talend type
     */
    private static TalendType getTalendByLogicalType(String logicalType) {
        switch (logicalType) {
        case LogicalTypeUtils.DATE:
            return DATE;
        case LogicalTypeUtils.TIME_MICROS:
            return LONG;
        case LogicalTypeUtils.TIME_MILLIS:
            return INTEGER;
        case LogicalTypeUtils.TIMESTAMP_MICROS:
            return DATE;
        case LogicalTypeUtils.TIMESTAMP_MILLIS:
            return DATE;
        default:
            throw new UnsupportedOperationException("Unrecognized type " + logicalType);
        }
    }

    /**
     * Returns Talend metadata type which corresponds to java-class property flag
     * 
     * @param javaClass java-class property value
     * @return Talend type
     */
    private static TalendType getTalendByJavaClass(String javaClass) {
        switch (javaClass) {
        case "java.math.BigDecimal":
            return BIG_DECIMAL;
        case "java.lang.Byte":
            return BYTE;
        case "java.lang.Character":
            return CHARACTER;
        case "java.lang.Short":
            return SHORT;
        case "java.util.Date":
            return DATE;
        default:
            throw new UnsupportedOperationException("Unrecognized java class " + javaClass);
        }
    }

    /**
     * Returns Talend metadata type which corresponds to avro type
     * 
     * @param type avro schema type
     * @return Talend type
     */
    private static TalendType getTalendByAvroType(Schema.Type type) {
        switch (type) {
        case ARRAY:
            return LIST;
        case BYTES:
            return BYTES;
        case INT:
            return INTEGER;
        case LONG:
            return LONG;
        case FLOAT:
            return FLOAT;
        case DOUBLE:
            return DOUBLE;
        case BOOLEAN:
            return BOOLEAN;
        case STRING:
            return STRING;
        default:
            throw new UnsupportedOperationException("Unsupported avro type " + type);
        }
    }

}
