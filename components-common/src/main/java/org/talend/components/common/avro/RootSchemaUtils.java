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
package org.talend.components.common.avro;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;
import org.apache.avro.SchemaBuilder;

/**
 * Root schema is a record schema with 2 fields of type record: Main and Out of band (data)
 * Main field is supposed to store main data (i.e. data useful and meaningful for user)
 * Out of band field is supposed to store additional/technical data (e.g. data which describes main data)
 * 
 * Components should output Root records. Root schema describes Root records
 * 
 * This class provides methods for Root schema handling
 */
public final class RootSchemaUtils {

    private static final String MAIN_FIELD_NAME = "Main";

    private static final String OUTOFBAND_FIELD_NAME = "OutOfBand";

    private RootSchemaUtils() {
        // Class provides static utility methods and shouldn't be instantiated
    }

    /**
     * Creates root record schema, which has 2 fields of type record: Main and OutOfBand with name "Root"
     * 
     * @param mainSchema schema of Main data
     * @param outOfBandSchema schema of Out of band data
     * @return root schema
     */
    public static Schema createRootSchema(final Schema mainSchema, final Schema outOfBandSchema) {
        if (mainSchema == null || outOfBandSchema == null) {
            throw new IllegalArgumentException("Input schemas should be not null");
        }
        Schema rootSchema = SchemaBuilder.record("Root").fields() //$NON-NLS-1$
                .name(MAIN_FIELD_NAME).type(mainSchema).noDefault() // $NON-NLS-1$
                .name(OUTOFBAND_FIELD_NAME).type(outOfBandSchema).noDefault() // $NON-NLS-1$
                .endRecord(); //

        return rootSchema;
    }

    /**
     * Checks whether given schema is Root schema
     * It checks names of this schema and its 2 child fields
     * Also it checks schema type
     * This schema name is supposed to be "Root"
     * Field name are supposed to be "Main" and "OutOfBand"
     * However this method doesn't check field schemas types
     * 
     * @param avro record schema
     * @return true is given schema is Root; false otherwise
     */
    public static boolean isRootSchema(final Schema schema) {
        if (schema == null) {
            return false;
        }

        String schemaName = schema.getName();
        if (!"Root".equals(schemaName)) {
            return false;
        }
        if (Type.RECORD != schema.getType()) {
            return false;
        }
        Field main = schema.getField(MAIN_FIELD_NAME);
        Field outOfBand = schema.getField(OUTOFBAND_FIELD_NAME);
        if (main == null || outOfBand == null) {
            return false;
        }
        return true;
    }

    /**
     * Retrieves and returns Main data schema from Root schema.
     * If incoming schema is not Root schema throws an {@link IllegalArgumentException}
     * 
     * @param rootSchema Root schema
     * @return Main data schema
     */
    public static Schema getMainSchema(Schema rootSchema) {
        if (!isRootSchema(rootSchema)) {
            throw new IllegalArgumentException("Input schema should be Root schema");
        }
        Schema mainSchema = rootSchema.getField(MAIN_FIELD_NAME).schema();
        return mainSchema;
    }

    /**
     * Retrieves and returns Out of band data schema from Root schema.
     * If incoming schema is not Root schema throws an {@link IllegalArgumentException}
     * 
     * @param rootSchema Root schema
     * @return Main data schema
     */
    public static Schema getOutOfBandSchema(Schema rootSchema) {
        if (!isRootSchema(rootSchema)) {
            throw new IllegalArgumentException("Input schema should be Root schema");
        }
        Schema outOfBandSchema = rootSchema.getField(OUTOFBAND_FIELD_NAME).schema();
        return outOfBandSchema;
    }
}
