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
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.talend.components.api.component.runtime.ComponentDriverInitialization;
import org.talend.components.api.component.runtime.RuntimableRuntime;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.simplefileio.SimpleFileIOErrorCode;
import org.talend.components.simplefileio.output.SimpleFileIOOutputProperties;
import org.talend.components.simplefileio.runtime.ugi.UgiDoAs;
import org.talend.components.simplefileio.runtime.ugi.UgiExceptionHandler;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.properties.ValidationResult;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;

public class SimpleFileIOOutputRuntime extends PTransform<PCollection<IndexedRecord>, PDone>
        implements RuntimableRuntime<SimpleFileIOOutputProperties>, ComponentDriverInitialization<SimpleFileIOOutputProperties> {

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
        boolean overwrite = properties.overwrite.getValue();
        int limit = -1; // limit is ignored for sinks

        SimpleRecordFormat rf = null;
        switch (properties.getDatasetProperties().format.getValue()) {

        case AVRO:
            rf = new SimpleRecordFormatAvroIO(doAs, path, overwrite, limit);
            break;

        case CSV:
            rf = new SimpleRecordFormatCsvIO(doAs, path, overwrite, limit, properties.getDatasetProperties().getRecordDelimiter(),
                    properties.getDatasetProperties().getFieldDelimiter());
            break;

        case PARQUET:
            rf = new SimpleRecordFormatParquetIO(doAs, path, overwrite, limit);
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

    /**
     * If overwriting a file, causes any any files at the existing location to be deleted.
     *
     * This action occurs before running the job.
     */
    @Override
    public void runAtDriver(RuntimeContainer container) {
        if (properties.overwrite.getValue()) {
            UgiDoAs doAs = SimpleFileIODatasetRuntime.getReadWriteUgiDoAs(properties.getDatasetProperties(),
                    UgiExceptionHandler.AccessType.Write);
            try {
                doAs.doAs(new PrivilegedExceptionAction<Void>() {

                    @Override
                    public Void run() throws IOException, URISyntaxException {
                        Path p = new Path(properties.getDatasetProperties().path.getValue());
                        FileSystem fs = p.getFileSystem(new Configuration());
                        if (fs.exists(p)) {
                            boolean deleted = fs.delete(p, true);
                            if (!deleted)
                                throw SimpleFileIOErrorCode.createOutputNotAuthorized(null, null,
                                        properties.getDatasetProperties().path.getValue());
                        }
                        return null;
                    }
                });
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw TalendRuntimeException.createUnexpectedException(e);
            }
        }
    }

}
