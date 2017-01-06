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
package org.talend.components.localio.runtime.fixedflowinput;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.Coder;
import org.apache.beam.sdk.io.BoundedSource;
import org.apache.beam.sdk.options.PipelineOptions;

public class FixedFlowInputBoundedSource extends BoundedSource<IndexedRecord> {

    private String schemaString = "";

    private String values = "";

    private int nbRows = 1;

    @Override
    public List<? extends BoundedSource<IndexedRecord>> splitIntoBundles(long desiredBundleSizeBytes, PipelineOptions options)
            throws Exception {
        return Arrays.asList(this);
    }

    @Override
    public long getEstimatedSizeBytes(PipelineOptions options) throws Exception {
        return 0;
    }

    @Override
    public boolean producesSortedKeys(PipelineOptions options) throws Exception {
        return false;
    }

    @Override
    public org.apache.beam.sdk.io.BoundedSource.BoundedReader<IndexedRecord> createReader(PipelineOptions options)
            throws IOException {
        return new FixedFlowInputBoundedReader(this)//
                .withSchema(schemaString) //
                .withValues(values) //
                .withNbRows(nbRows);
    }

    @Override
    public void validate() {
    }

    @Override
    public Coder<IndexedRecord> getDefaultOutputCoder() {
        Schema.Parser parser = new Schema.Parser();
        return AvroCoder.of(IndexedRecord.class, parser.parse(schemaString));
    }

    public FixedFlowInputBoundedSource withSchema(Schema schema) {
        this.schemaString = schema.toString();
        return this;
    }

    public FixedFlowInputBoundedSource withValues(String values) {
        this.values = values;
        return this;
    }

    public FixedFlowInputBoundedSource withNbRows(int nbRows) {
        this.nbRows = nbRows;
        return this;
    }

}
