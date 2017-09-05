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
package org.talend.components.adapter.beam.coders;

import java.io.Serializable;

import org.apache.avro.Schema;

/**
 * Container for an indirect reference to an Avro Schema.
 *
 * This must be serializable to all of the nodes that will be using it to encode/decode data at runtime, but must not
 * rely on any non-serializable state.
 */
public interface AvroSchemaHolder extends Serializable {

    /**
     * @return A unique identifier that can be used to describe the schema. This must at least be unique for the schema
     * within the currently running job.
     */
    String getAvroSchemaId();

    /**
     * Store a schema during encoding.
     * 
     * @param s The schema to be used to encode all data.
     */
    void put(Schema s);

    /**
     * @return The schema that will be used to decode all data.
     */
    Schema get();
}
