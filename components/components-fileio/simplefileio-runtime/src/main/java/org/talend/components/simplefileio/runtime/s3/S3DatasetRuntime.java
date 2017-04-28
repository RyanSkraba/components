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
import org.talend.components.adapter.beam.coders.LazyAvroCoder;
import org.talend.components.adapter.beam.transform.DirectConsumerCollector;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.simplefileio.s3.S3DatasetProperties;
import org.talend.components.simplefileio.s3.S3Region;
import org.talend.components.simplefileio.s3.input.S3InputProperties;
import org.talend.components.simplefileio.s3.runtime.IS3DatasetRuntime;
import org.talend.daikon.java8.Consumer;
import org.talend.daikon.properties.ValidationResult;

import com.talend.shaded.com.amazonaws.services.s3.AmazonS3;
import com.talend.shaded.com.amazonaws.services.s3.model.Bucket;
import com.talend.shaded.com.amazonaws.services.s3.model.Region;

public class S3DatasetRuntime implements IS3DatasetRuntime {

    private static final Logger LOG = LoggerFactory.getLogger(S3DatasetRuntime.class);

    /**
     * The dataset instance that this runtime is configured for.
     */
    private S3DatasetProperties properties = null;

    @Override
    public Set<String> listBuckets() {
        AmazonS3 conn = S3Connection.createClient(properties.getDatastoreProperties());
        String region = properties.region.getValue().getValue();
        if (S3Region.OTHER.getValue().equals(region)) {
            region = properties.unknownRegion.getValue();
        }
        List<Bucket> buckets = conn.listBuckets();
        Set<String> bucketsName = new HashSet<>();
        for (Bucket bucket : buckets) {
            String bucketName = bucket.getName();
            try {
                String bucketLocation = conn.getBucketLocation(bucketName);
                if (Region.fromValue(bucketLocation).toAWSRegion().getName().equals(region)) {
                    bucketsName.add(bucketName);
                }
            } catch (Exception e) {
                // Ignore any exception when calling getBucketLocation, try next
                LOG.debug("Exception when check bucket location: {}", e.getMessage());
            }
        }
        return bucketsName;
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
        S3InputRuntime inputRuntime = new S3InputRuntime();
        S3InputProperties inputProperties = new S3InputProperties(null);
        inputProperties.limit.setValue(limit);
        inputProperties.init();
        inputProperties.setDatasetProperties(properties);
        inputRuntime.initialize(null, inputProperties);
        // Create a pipeline using the input component to get records.

        DirectOptions options = BeamLocalRunnerOption.getOptions();
        final Pipeline p = Pipeline.create(options);
        LazyAvroCoder.registerAsFallback(p);

        try (DirectConsumerCollector<IndexedRecord> collector = DirectConsumerCollector.of(consumer)) {
            // Collect a sample of the input records.
            p.apply(inputRuntime) //
                    .apply(Sample.<IndexedRecord> any(limit)) //
                    .apply(collector);
            p.run().waitUntilFinish();
        }
    }

    @Override
    public ValidationResult initialize(RuntimeContainer container, S3DatasetProperties properties) {
        this.properties = properties;
        return ValidationResult.OK;
    }
}
