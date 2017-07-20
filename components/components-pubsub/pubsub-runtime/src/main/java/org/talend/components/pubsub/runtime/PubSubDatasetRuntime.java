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

package org.talend.components.pubsub.runtime;

import java.util.*;

import com.google.api.services.pubsub.Pubsub;
import com.google.api.services.pubsub.model.ListTopicSubscriptionsResponse;
import com.google.api.services.pubsub.model.ListTopicsResponse;
import com.google.api.services.pubsub.model.Topic;
import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.runners.direct.DirectOptions;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.transforms.Sample;
import org.talend.components.adapter.beam.BeamLocalRunnerOption;
import org.talend.components.adapter.beam.transform.DirectConsumerCollector;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.pubsub.PubSubDatasetProperties;
import org.talend.components.pubsub.PubSubDatastoreProperties;
import org.talend.components.pubsub.input.PubSubInputProperties;
import org.talend.daikon.java8.Consumer;
import org.talend.daikon.properties.ValidationResult;


// import org.apache.beam.runners.direct.DirectRunner;

public class PubSubDatasetRuntime implements IPubSubDatasetRuntime {

    /**
     * The dataset instance that this runtime is configured for.
     */
    private PubSubDatasetProperties properties = null;

    @Override
    public ValidationResult initialize(RuntimeContainer container, PubSubDatasetProperties properties) {
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
        // Because PubSub do not have offset, and the message will be deleted after
        // read, so have to create a dumy reader which do not call ack after read

        // Create an input runtime based on the properties.
        PubSubInputRuntime inputRuntime = new PubSubInputRuntime();
        PubSubInputProperties inputProperties = new PubSubInputProperties(null);
        inputProperties.init();
        inputProperties.setDatasetProperties(properties);
        inputProperties.useMaxNumRecords.setValue(true);
        inputProperties.maxNumRecords.setValue(limit);
        inputProperties.useMaxReadTime.setValue(true);
        // 10s, the value is better to depends on ack deadline for small dataset
        inputProperties.maxReadTime.setValue(10000l);
        inputProperties.noACK.setValue(true);
        inputRuntime.initialize(null, inputProperties);

        DirectOptions options = BeamLocalRunnerOption.getOptions();
        final Pipeline p = Pipeline.create(options);

        try (DirectConsumerCollector<IndexedRecord> collector = DirectConsumerCollector.of(consumer)) {
            // Collect a sample of the input records.
            p.apply(inputRuntime) //
                    .apply(Sample.<IndexedRecord> any(limit)).apply(collector);
            p.run().waitUntilFinish();
        }
    }

    @Override
    public Set<String> listTopics() throws Exception {
        PubSubDatastoreProperties datastore = properties.getDatastoreProperties();
        PubSubClient client = PubSubConnection.createClient(datastore);
        return client.listTopics();
    }

    @Override
    public Set<String> listSubscriptions() throws Exception {
        PubSubDatastoreProperties datastore = properties.getDatastoreProperties();
        PubSubClient client = PubSubConnection.createClient(datastore);
        return client.listSubscriptions(properties.topic.getValue());
    }
}
