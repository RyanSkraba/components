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
package org.talend.components.simplefileio.runtime.s3;

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
import org.talend.components.simplefileio.runtime.ExtraHadoopConfiguration;
import org.talend.components.simplefileio.runtime.SimpleFileIOAvroRegistry;
import org.talend.components.simplefileio.runtime.SimpleRecordFormatAvroIO;
import org.talend.components.simplefileio.runtime.SimpleRecordFormatBase;
import org.talend.components.simplefileio.runtime.SimpleRecordFormatCsvIO;
import org.talend.components.simplefileio.runtime.SimpleRecordFormatParquetIO;
import org.talend.components.simplefileio.runtime.ugi.UgiDoAs;
import org.talend.components.simplefileio.s3.output.S3OutputProperties;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.properties.ValidationResult;

public class S3OutputRuntime extends PTransform<PCollection<IndexedRecord>, PDone>
        implements RuntimableRuntime<S3OutputProperties>, ComponentDriverInitialization<S3OutputProperties> {

    static {
        // Ensure that the singleton for the SimpleFileIOAvroRegistry is created.
        SimpleFileIOAvroRegistry.get();
    }

    /**
     * The component instance that this runtime is configured for.
     */
    private S3OutputProperties properties;

    @Override
    public ValidationResult initialize(RuntimeContainer container, S3OutputProperties properties) {
        this.properties = properties;
        return ValidationResult.OK;
    }

    @Override
    public PDone expand(PCollection<IndexedRecord> in) {
        // The UGI does not control security for S3.
        UgiDoAs doAs = UgiDoAs.ofNone();
        String path = S3Connection.getUriPath(properties.getDatasetProperties());
        boolean overwrite = properties.overwrite.getValue();
        int limit = -1; // limit is ignored for sinks
        boolean mergeOutput = properties.mergeOutput.getValue();

        SimpleRecordFormatBase rf = null;
        switch (properties.getDatasetProperties().format.getValue()) {

        case AVRO:
            rf = new SimpleRecordFormatAvroIO(doAs, path, overwrite, limit, mergeOutput);
            break;

        case CSV:
            rf = new SimpleRecordFormatCsvIO(doAs, path, overwrite, limit,
                    properties.getDatasetProperties().getRecordDelimiter(),
                    properties.getDatasetProperties().getMaxRowSize(),
                    properties.getDatasetProperties().getFieldDelimiter(), mergeOutput);
            break;

        case PARQUET:
            rf = new SimpleRecordFormatParquetIO(doAs, path, overwrite, limit, mergeOutput);
            break;
        }

        if (rf == null) {
            throw new RuntimeException("To be implemented: " + properties.getDatasetProperties().format.getValue());
        }

        S3Connection.setS3Configuration(rf.getExtraHadoopConfiguration(), properties.getDatasetProperties());
        return rf.write(in);
    }

    /**
     * If overwriting a file, causes any any files at the existing location to be deleted.
     *
     * This action occurs before running the job.
     */
    @Override
    public void runAtDriver(RuntimeContainer container) {
        if (properties.overwrite.getValue()) {
            try {
                Path p = new Path(S3Connection.getUriPath(properties.getDatasetProperties()));

                // Add the AWS configuration to the Hadoop filesystem.
                ExtraHadoopConfiguration awsConf = new ExtraHadoopConfiguration();
                S3Connection.setS3Configuration(awsConf, properties.getDatasetProperties());
                Configuration hadoopConf = new Configuration();
                awsConf.addTo(hadoopConf);
                FileSystem fs = p.getFileSystem(hadoopConf);

                if (fs.exists(p)) {
                    boolean deleted = fs.delete(p, true);
                    if (!deleted)
                        throw SimpleFileIOErrorCode.createOutputNotAuthorized(null, null,
                                p.toString());
                }
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw TalendRuntimeException.createUnexpectedException(e);
            }
        }
    }
}
