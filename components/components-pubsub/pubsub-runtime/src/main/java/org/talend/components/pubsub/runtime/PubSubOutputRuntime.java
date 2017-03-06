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

import java.nio.charset.Charset;

import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.sdk.coders.ByteArrayCoder;
import org.apache.beam.sdk.io.PubsubIO;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.SerializableFunction;
import org.apache.beam.sdk.transforms.SimpleFunction;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PDone;
import org.talend.components.adapter.beam.coders.LazyAvroCoder;
import org.talend.components.adapter.beam.gcp.GcpServiceAccountOptions;
import org.talend.components.adapter.beam.gcp.ServiceAccountCredentialFactory;
import org.talend.components.api.component.runtime.RuntimableRuntime;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.pubsub.PubSubDatasetProperties;
import org.talend.components.pubsub.PubSubDatastoreProperties;
import org.talend.components.pubsub.output.PubSubOutputProperties;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.exception.error.CommonErrorCodes;
import org.talend.daikon.properties.ValidationResult;

import com.google.cloud.pubsub.PubSub;
import com.google.cloud.pubsub.PubSubException;
import com.google.cloud.pubsub.SubscriptionInfo;
import com.google.cloud.pubsub.TopicInfo;

public class PubSubOutputRuntime extends PTransform<PCollection<IndexedRecord>, PDone>
        implements RuntimableRuntime<PubSubOutputProperties> {

    /**
     * The component instance that this runtime is configured for.
     */
    private PubSubOutputProperties properties;

    @Override
    public ValidationResult initialize(RuntimeContainer container, PubSubOutputProperties properties) {
        this.properties = properties;
        return ValidationResult.OK;
    }

    @Override
    public PDone expand(PCollection<IndexedRecord> in) {
        PubSubDatasetProperties dataset = properties.getDatasetProperties();
        PubSubDatastoreProperties datastore = dataset.getDatastoreProperties();

        GcpServiceAccountOptions gcpOptions = in.getPipeline().getOptions().as(GcpServiceAccountOptions.class);
        gcpOptions.setProject(datastore.projectName.getValue());
        if (datastore.serviceAccountFile.getValue() != null) {
            gcpOptions.setCredentialFactoryClass(ServiceAccountCredentialFactory.class);
            gcpOptions.setServiceAccountFile(datastore.serviceAccountFile.getValue());
            gcpOptions.setGcpCredential(PubSubConnection.createCredentials(datastore));
        }

        createTopicSubscriptionIfNeeded(properties);

        PubsubIO.Write<byte[]> pubsubWrite = PubsubIO.<byte[]> write()
                .topic(String.format("projects/%s/topics/%s", datastore.projectName.getValue(), dataset.topic.getValue()));

        if (properties.idLabel.getValue() != null && !"".equals(properties.idLabel.getValue())) {
            pubsubWrite.idLabel(properties.idLabel.getValue());
        }
        if (properties.timestampLabel.getValue() != null && !"".equals(properties.timestampLabel.getValue())) {
            pubsubWrite.timestampLabel(properties.timestampLabel.getValue());
        }

        switch (dataset.valueFormat.getValue()) {
        case CSV: {
            return (PDone) in.apply(MapElements.via(new FormatCsv(dataset.fieldDelimiter.getValue())))
                    .apply(pubsubWrite.withCoder(ByteArrayCoder.of()));
        }
        case AVRO: {
            return (PDone) in.apply(pubsubWrite.withCoder(LazyAvroCoder.of()));
        }
        default:
            throw new RuntimeException("To be implemented: " + dataset.valueFormat.getValue());
        }

    }

    private void createTopicSubscriptionIfNeeded(PubSubOutputProperties properties) {
        PubSubOutputProperties.TopicOperation topicOperation = properties.topicOperation.getValue();
        if (topicOperation == PubSubOutputProperties.TopicOperation.NONE) {
            return;
        }
        PubSubDatasetProperties dataset = properties.getDatasetProperties();

        validateForCreateTopic(dataset);

        PubSub client = PubSubConnection.createClient(dataset.getDatastoreProperties());

        if (topicOperation == PubSubOutputProperties.TopicOperation.DROP_IF_EXISTS_AND_CREATE) {
            dropTopic(client, dataset);
        }

        createTopic(client, dataset);
    }

    private void createTopic(PubSub client, PubSubDatasetProperties dataset) {
        try {
            client.create(TopicInfo.of(dataset.topic.getValue()));
        } catch (PubSubException e) {
            // ignore. no check before create, so the topic may exists
        }
        client.create(SubscriptionInfo.of(dataset.topic.getValue(), dataset.subscription.getValue()));
    }

    private void dropTopic(PubSub client, PubSubDatasetProperties dataset) {
        client.deleteSubscription(dataset.subscription.getValue());
        client.deleteTopic(dataset.topic.getValue());
    }

    private void validateForCreateTopic(PubSubDatasetProperties dataset) {
        if (dataset.subscription.getValue() == null || "".equals(dataset.subscription.getValue())) {
            TalendRuntimeException.build(CommonErrorCodes.UNEXPECTED_EXCEPTION)
                    .setAndThrow("Subscription required when create topic");
        }
        if (dataset.topic.getValue() == null || "".equals(dataset.topic.getValue())) {
            TalendRuntimeException.build(CommonErrorCodes.UNEXPECTED_EXCEPTION).setAndThrow("Topic required when create topic");
        }
    }

    public static class FormatCsvFunction implements SerializableFunction<IndexedRecord, byte[]> {

        public final String fieldDelimiter;

        private StringBuilder sb = new StringBuilder();

        public FormatCsvFunction(String fieldDelimiter) {
            this.fieldDelimiter = fieldDelimiter;
        }

        @Override
        public byte[] apply(IndexedRecord input) {
            int size = input.getSchema().getFields().size();
            for (int i = 0; i < size; i++) {
                if (sb.length() != 0)
                    sb.append(fieldDelimiter);
                sb.append(input.get(i));
            }
            byte[] bytes = sb.toString().getBytes(Charset.forName("UTF-8"));
            sb.setLength(0);
            return bytes;
        }
    }

    public static class FormatCsv extends SimpleFunction<IndexedRecord, byte[]> {

        public final FormatCsvFunction function;

        public FormatCsv(String fieldDelimiter) {
            function = new FormatCsvFunction(fieldDelimiter);
        }

        @Override
        public byte[] apply(IndexedRecord input) {
            return function.apply(input);
        }
    }
}
