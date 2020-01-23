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
import org.apache.beam.sdk.values.PBegin;
import org.apache.beam.sdk.values.PCollection;
import org.talend.components.api.component.runtime.RuntimableRuntime;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.simplefileio.SimpleFileIODatasetProperties;
import org.talend.components.simplefileio.input.SimpleFileIOInputProperties;
import org.talend.components.simplefileio.runtime.ugi.UgiDoAs;
import org.talend.components.simplefileio.runtime.ugi.UgiExceptionHandler;
import org.talend.components.simplefileio.s3.S3DatasetProperties;
import org.talend.daikon.properties.ValidationResult;

public class SimpleFileIOInputRuntime extends PTransform<PBegin, PCollection<IndexedRecord>>
        implements RuntimableRuntime<SimpleFileIOInputProperties> {

    static {
        // Ensure that the singleton for the SimpleFileIOAvroRegistry is created.
        SimpleFileIOAvroRegistry.get();
    }

    /**
     * The component instance that this runtime is configured for.
     */
    private SimpleFileIOInputProperties properties = null;

    @Override
    public ValidationResult initialize(RuntimeContainer container, SimpleFileIOInputProperties properties) {
        this.properties = properties;
        return ValidationResult.OK;
    }

    @Override
    public PCollection<IndexedRecord> expand(PBegin in) {
        // Controls the access security on the cluster.
        UgiDoAs doAs = SimpleFileIODatasetRuntime.getReadWriteUgiDoAs(properties.getDatasetProperties(),
                UgiExceptionHandler.AccessType.Read);
        String path = properties.getDatasetProperties().path.getValue();
        boolean overwrite = false; // overwrite is ignored for reads.
        int limit = properties.limit.getValue();
        boolean mergeOutput = false; // mergeOutput is ignored for reads.

        SimpleRecordFormat rf = null;
        switch (properties.getDatasetProperties().format.getValue()) {

        case AVRO:
            rf = new SimpleRecordFormatAvroIO(doAs, path, overwrite, limit, mergeOutput);
            break;

        case CSV:
            SimpleFileIODatasetProperties dataset = properties.getDatasetProperties();
            rf = new SimpleRecordFormatCsvIO(doAs, path, limit, dataset.getRecordDelimiter(),
                dataset.getFieldDelimiter(), dataset.getEncoding(), 
                dataset.getHeaderLine(), dataset.getTextEnclosureCharacter(), dataset.getEscapeCharacter());
            break;

        case PARQUET:
            rf = new SimpleRecordFormatParquetIO(doAs, path, overwrite, limit, mergeOutput);
            break;
            
        case EXCEL:
            SimpleFileIODatasetProperties ds = properties.getDatasetProperties();
            rf = new SimpleRecordFormatExcelIO(doAs, path, overwrite, limit, mergeOutput, ds.getEncoding(), ds.getSheetName(), ds.getHeaderLine(), ds.getFooterLine(), ds.getExcelFormat());
            break;
        }

        if (rf == null) {
            throw new RuntimeException("To be implemented: " + properties.getDatasetProperties().format.getValue());
        }

        return rf.read(in);
    }
    
}
