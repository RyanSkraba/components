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
package ${package}.runtime.reader;

import java.io.IOException;

import org.apache.avro.Schema;
import org.talend.components.api.component.runtime.SourceOrSink;

/**
 * Represents runtime interface for schema discovery functionality.
 * guessSchema() is the main functionality of this method.
 * However, initialize() and validate() should be called before to
 * store Properties and validate them
 */
public interface SchemaDiscovery extends SourceOrSink {

    /**
     * Tries to guess schema from source and returns it
     * 
     * @return schema of source entity
     * @throws IOException
     */
    Schema guessSchema() throws IOException;
}
