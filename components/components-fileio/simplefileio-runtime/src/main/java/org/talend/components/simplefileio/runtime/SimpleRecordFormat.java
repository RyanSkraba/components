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
package org.talend.components.simplefileio.runtime;

import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.sdk.values.PBegin;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PDone;

/**
 * Record formats that are hard-coded into the {@link SimpleFileIOInputRuntime} and {@link SimpleFileIOOutputRuntime}.
 */
public interface SimpleRecordFormat {

    /**
     * Implementation for reading records at the start of a pipeline.
     */
    PCollection<IndexedRecord> read(PBegin in);

    /**
     * Implementation for writing records at the end of a pipeline.
     */
    PDone write(PCollection<IndexedRecord> in);
}
