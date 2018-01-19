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

import org.apache.avro.Schema;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.LogicalTypeUtils;

/**
 * Utility class to map Avro type to Talend type
 */
public final class AvroTypeConverter {

    private AvroTypeConverter() {
        // no-op
    }

    /**
     * Converts Talend type to Avro type schema
     * 
     * @param talendType data integration native type
     * @param logicalType avro logical type
     * @return field schema
     * @throws {@link UnsupportedOperationException} in case of unsupported Talend type or logical type
     */
    public static Schema convertToAvro(TalendType talendType, String logicalType) {
        Schema fieldSchema = LogicalTypeUtils.getSchemaByLogicalType(logicalType);
        if (fieldSchema != null) {
            return fieldSchema;
        }

        switch (talendType) {
        case STRING:
            return Schema.create(Schema.Type.STRING);
        case BOOLEAN:
            return Schema.create(Schema.Type.BOOLEAN);
        case INTEGER:
            return Schema.create(Schema.Type.INT);
        case LONG:
            return Schema.create(Schema.Type.LONG);
        case DOUBLE:
            return Schema.create(Schema.Type.DOUBLE);
        case FLOAT:
            return Schema.create(Schema.Type.FLOAT);
        case BYTE:
            return AvroUtils._byte();
        case SHORT:
            return AvroUtils._short();
        case CHARACTER:
            return AvroUtils._character();
        case BIG_DECIMAL:
            return AvroUtils._decimal();
        case DATE:
            return AvroUtils._logicalTimestamp();
        default:
            throw new UnsupportedOperationException("Unrecognized type " + talendType);
        }
    }
}
