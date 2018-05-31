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
package org.talend.components.snowflake.runtime.utils;

import java.io.IOException;
import java.sql.SQLException;

import org.apache.avro.Schema;

/**
 * It's used to specify correct implementation of get runtime schema for SourceOrSink.
 *
 */
public interface SchemaResolver {

    /**
     * Retrieves runtime schema from data source.
     * @return runtime schema.
     * @throws IOException if any {@link SQLException} happened.
     */
    Schema getSchema() throws IOException;
}
