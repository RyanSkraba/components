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

package org.talend.components.kinesis.runtime;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.runners.direct.DirectOptions;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.transforms.Sample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.adapter.beam.BeamLocalRunnerOption;
import org.talend.components.adapter.beam.transform.DirectConsumerCollector;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.kinesis.KinesisDatasetProperties;
import org.talend.components.kinesis.input.KinesisInputProperties;
import org.talend.daikon.java8.Consumer;
import org.talend.daikon.properties.ValidationResult;

import com.amazonaws.services.kinesis.AmazonKinesis;
import com.amazonaws.services.kinesis.model.ListStreamsResult;

public class KinesisDatasetRuntime implements IKinesisDatasetRuntime {

    private static final Logger LOG = LoggerFactory.getLogger(KinesisDatasetRuntime.class);

    /**
     * The dataset instance that this runtime is configured for.
     */
    private KinesisDatasetProperties properties = null;

    @Override
    public ValidationResult initialize(RuntimeContainer container, KinesisDatasetProperties properties) {
        this.properties = properties;
        return ValidationResult.OK;
    }

    @Override
    public Schema getSchema() {
        // Simple schema container.
        final Schema[] s = new Schema[1];
        // Try to get one record and determine its schema in a callback.
        getSample(1, new Consumer<IndexedRecord>() {

            @Override
            public void accept(IndexedRecord in) {
                s[0] = in.getSchema();
            }
        });
        // Return the discovered schema.
        return s[0];
    }

    @Override
    public void getSample(int limit, Consumer<IndexedRecord> consumer) {
        // Create an input runtime based on the properties.
        KinesisInputRuntime inputRuntime = new KinesisInputRuntime();
        KinesisInputProperties inputProperties = new KinesisInputProperties(null);
        inputProperties.init();
        inputProperties.setDatasetProperties(properties);
        inputProperties.useMaxNumRecords.setValue(true);
        inputProperties.maxNumRecords.setValue(limit);
        inputProperties.useMaxReadTime.setValue(true);
        inputProperties.maxReadTime.setValue(10000l);
        inputProperties.position.setValue(KinesisInputProperties.OffsetType.EARLIEST);
        inputRuntime.initialize(null, inputProperties);

        // Create a pipeline using the input component to get records.
        DirectOptions options = BeamLocalRunnerOption.getOptions();
        final Pipeline p = Pipeline.create(options);

        try (DirectConsumerCollector<IndexedRecord> collector = DirectConsumerCollector.of(consumer)) {
            // Collect a sample of the input records.
            p
                    .apply(inputRuntime) //
                    .apply(Sample.<IndexedRecord> any(limit))
                    .apply(collector);
            p.run().waitUntilFinish();
        }
    }

    @Override
    public Set<String> listStreams() {
        AmazonKinesis amazonKinesis = KinesisClient.create(properties);
        ListStreamsResult listStreamsResult = amazonKinesis.listStreams();
        List<String> streamNames = listStreamsResult.getStreamNames();
        Set<String> streamNamesCollection = new HashSet(streamNames);
        while (listStreamsResult.isHasMoreStreams() && !streamNames.isEmpty()) {
            listStreamsResult = amazonKinesis.listStreams(streamNames.get(streamNames.size() - 1));
            streamNames = listStreamsResult.getStreamNames();
            streamNamesCollection.addAll(streamNames);
        }
        return streamNamesCollection;
    }
}
