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
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PDone;
import org.talend.components.api.component.runtime.RuntimableRuntime;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.simplefileio.SimpleFileIOErrorCode;
import org.talend.components.simplefileio.output.SimpleFileIOOutputProperties;
import org.talend.components.simplefileio.runtime.ugi.UgiDoAs;
import org.talend.components.simplefileio.runtime.ugi.UgiExceptionHandler;
import org.talend.daikon.properties.ValidationResult;

public class SimpleFileIOOutputRuntime extends PTransform<PCollection<IndexedRecord>, PDone> implements
        RuntimableRuntime<SimpleFileIOOutputProperties> {

    static {
        // Ensure that the singleton for the SimpleFileIOAvroRegistry is created.
        SimpleFileIOAvroRegistry.get();
    }

    /**
     * The component instance that this runtime is configured for.
     */
    private SimpleFileIOOutputProperties properties;

    @Override
    public ValidationResult initialize(RuntimeContainer container, SimpleFileIOOutputProperties properties) {
        this.properties = properties;
        return ValidationResult.OK;
    }

    @Override
    public PDone expand(PCollection<IndexedRecord> in) {
        // Controls the access security on the cluster.
        UgiDoAs doAs = SimpleFileIODatasetRuntime.getReadWriteUgiDoAs(properties.getDatasetProperties(),
                UgiExceptionHandler.AccessType.Write);
        String path = properties.getDatasetProperties().path.getValue();
        int limit = -1;

        SimpleRecordFormat rf = null;
        switch (properties.getDatasetProperties().format.getValue()) {

        case AVRO:
            rf = new SimpleRecordFormatAvroIO(doAs, path, limit);
            break;

        case CSV:
            rf = new SimpleRecordFormatCsvIO(doAs, path, limit, properties.getDatasetProperties().getRecordDelimiter(),
                    properties.getDatasetProperties().getFieldDelimiter());
            break;

        case PARQUET:
            rf = new SimpleRecordFormatParquetIO(doAs, path, limit);
            break;
        }

        if (rf == null) {
            throw new RuntimeException("To be implemented: " + properties.getDatasetProperties().format.getValue());
        }

        try {
            return rf.write(in);
        } catch (IllegalStateException rte) {
            // Unable to overwrite exceptions are handled here.
            if (rte.getMessage().startsWith("Output path") && rte.getMessage().endsWith("already exists")) {
                throw SimpleFileIOErrorCode.createOutputAlreadyExistsException(rte, path);
            } else {
                throw rte;
            }
        }
    }
}
