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
package org.talend.components.common.component.runtime;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.talend.components.common.avro.RootSchemaUtils;

/**
 * Root record is composite hierarchical record, which consists of 2 records: Main data record and Out of band data record.
 * Main data is data useful and meaningful for user
 * Out of band data is additional/technical data, which, for example, describes main data
 * Root record schema is Root schema. See {@link RootSchemaUtils}
 * 
 * <p>
 * This class provides means to create Root record and to check whether specific {@link IndexedRecord} is Root record
 */
public final class RootRecordUtils {

    private RootRecordUtils() {
        // Class provides static utility methods and shouldn't be instantiated
    }

    /**
     * Creates and returns root record
     * Use this method when only one Root record is required. In case of several Root records needed use
     * {@link RootRecordUtils#createRootRecord(Schema)} and reuse Root schema for performance purposes
     * 
     * @param mainSchema schema of Main data
     * @param outOfBandSchema schema of Out of band data
     * @return root record
     */
    public static IndexedRecord createRootRecord(Schema mainSchema, Schema outOfBandSchema) {
        if (mainSchema == null || outOfBandSchema == null) {
            throw new IllegalArgumentException("Input schemas should be not null");
        }
        Schema rootSchema = RootSchemaUtils.createRootSchema(mainSchema, outOfBandSchema);
        return createRootRecord(rootSchema);
    }

    /**
     * Creates and returns root record
     * For performance purposes, it is recommended to create Root schema only once and then use it to create Root record instances
     * 
     * @param rootSchema Root schema, see {@link RootSchemaUtils}
     * @return root record
     */
    public static IndexedRecord createRootRecord(Schema rootSchema) {
        if (!RootSchemaUtils.isRootSchema(rootSchema)) {
            throw new IllegalArgumentException("Input schema should be Root schema");
        }
        IndexedRecord rootRecord = new GenericData.Record(rootSchema);
        return rootRecord;
    }
    
    /**
     * Creates and returns root record
     * For performance purposes, it is recommended to create Root schema only once and then use it to create Root record instances
     * Sets <code>dataRecord</code> on position 0, and <code>outOfBandRecord</code> on position 1
     * 
     * @param rootSchema Root schema, see {@link RootSchemaUtils}
     * @param dataRecord main data record
     * @param outOfBandRecord out of band (additional) data record
     * @return root record
     */
    public static IndexedRecord createRootRecord(Schema rootSchema, IndexedRecord dataRecord, IndexedRecord outOfBandRecord) {
    	IndexedRecord rootRecord = createRootRecord(rootSchema);
    	rootRecord.put(0, dataRecord);
    	rootRecord.put(1, outOfBandRecord);
    	return rootRecord;
    }

    /**
     * Checks whether input {@link IndexedRecord} is Root record
     * It checks its {@link Schema}, it should be Root schema
     * 
     * @param data data to check
     * @return true if incoming {@link IndexedRecord} is Root record; false otherwise
     */
    public static boolean isRootRecord(Object data) {
        if (data == null) {
            return false;
        }
        if (!(data instanceof IndexedRecord)) {
            return false;
        }
        IndexedRecord record = (IndexedRecord) data;
        Schema recordSchema = record.getSchema();
        boolean result = RootSchemaUtils.isRootSchema(recordSchema);
        return result;
    }
}
