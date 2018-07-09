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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.IndexedRecord;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubIO;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubMessage;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.SerializableFunction;
import org.apache.beam.sdk.transforms.SimpleFunction;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PDone;
import org.talend.components.adapter.beam.BeamJobRuntimeContainer;
import org.talend.components.adapter.beam.gcp.GcpServiceAccountOptions;
import org.talend.components.adapter.beam.gcp.ServiceAccountCredentialFactory;
import org.talend.components.api.component.runtime.RuntimableRuntime;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.exception.error.ComponentsErrorCode;
import org.talend.components.pubsub.PubSubDatasetProperties;
import org.talend.components.pubsub.PubSubDatastoreProperties;
import org.talend.components.pubsub.output.PubSubOutputProperties;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.exception.error.CommonErrorCodes;
import org.talend.daikon.properties.ValidationResult;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.common.collect.ImmutableMap;

public class PubSubOutputRuntime extends PTransform<PCollection<IndexedRecord>, PDone>
        implements RuntimableRuntime<PubSubOutputProperties> {

    /**
     * The component instance that this runtime is configured for.
     */
    private PubSubOutputProperties properties;

    private PubSubDatasetProperties dataset = null;

    private PubSubDatastoreProperties datastore = null;

    private boolean runOnDataflow = false;

    @Override
    public ValidationResult initialize(RuntimeContainer container, PubSubOutputProperties properties) {
        this.properties = properties;
        this.dataset = properties.getDatasetProperties();
        this.datastore = dataset.getDatastoreProperties();

        Object pipelineOptionsObj = container.getGlobalData(BeamJobRuntimeContainer.PIPELINE_OPTIONS);
        if (pipelineOptionsObj != null) {
            PipelineOptions pipelineOptions = (PipelineOptions) pipelineOptionsObj;
            GcpServiceAccountOptions gcpOptions = pipelineOptions.as(GcpServiceAccountOptions.class);
            runOnDataflow = "DataflowRunner".equals(gcpOptions.getRunner().getSimpleName());
            if (!runOnDataflow) {
                gcpOptions.setProject(datastore.projectName.getValue());
                if (datastore.serviceAccountFile.getValue() != null) {
                    gcpOptions.setCredentialFactoryClass(ServiceAccountCredentialFactory.class);
                    gcpOptions.setServiceAccountFile(datastore.serviceAccountFile.getValue());
                    gcpOptions.setGcpCredential(PubSubConnection.createCredentials(datastore));
                }
            }
        }

        return ValidationResult.OK;
    }

    @Override
    public PDone expand(PCollection<IndexedRecord> in) {
        PubSubDatasetProperties dataset = properties.getDatasetProperties();
        PubSubDatastoreProperties datastore = dataset.getDatastoreProperties();

        prepareTopicSubscription(properties);

        PubsubIO.Write<PubsubMessage> pubsubWrite = PubsubIO.writeMessages().to(
                String.format("projects/%s/topics/%s", datastore.projectName.getValue(), dataset.topic.getValue()));

        if (properties.idLabel.getValue() != null && !"".equals(properties.idLabel.getValue())) {
            pubsubWrite.withIdAttribute(properties.idLabel.getValue());
        }
        if (properties.timestampLabel.getValue() != null && !"".equals(properties.timestampLabel.getValue())) {
            pubsubWrite.withTimestampAttribute(properties.timestampLabel.getValue());
        }

        switch (dataset.valueFormat.getValue()) {
        case CSV: {
            return in.apply(MapElements.via(new FormatCsv(dataset.fieldDelimiter.getValue()))).apply(pubsubWrite);
        }
        case AVRO: {
            return in.apply(MapElements.via(new FormatAvro())).apply(pubsubWrite);
        }
        default:
            throw new RuntimeException("To be implemented: " + dataset.valueFormat.getValue());
        }

    }

    private void prepareTopicSubscription(PubSubOutputProperties properties) {
        PubSubOutputProperties.TopicOperation topicOperation = properties.topicOperation.getValue();
        if (topicOperation == PubSubOutputProperties.TopicOperation.NONE) {
            // no check exist method provided, if the topic do not exist, the exception will thrown by pipeline
            return;
        }
        validateForCreateTopic();

        PubSubClient client = PubSubConnection.createClient(datastore, runOnDataflow);

        if (topicOperation == PubSubOutputProperties.TopicOperation.DROP_IF_EXISTS_AND_CREATE) {
            dropTopic(client);
        }

        createTopic(client);
    }

    private void createTopic(PubSubClient client) {
        try {
            client.createTopic(dataset.topic.getValue());
        } catch (Exception e) {
            if (((GoogleJsonResponseException) e).getStatusCode() == 409) {
                // Ignore ALREADY_EXISTS, but throw others
            } else {
                TalendRuntimeException.build(ComponentsErrorCode.IO_EXCEPTION, e).throwIt();
            }
        }
        try {
            client.createSubscription(dataset.topic.getValue(), dataset.subscription.getValue());
        } catch (Exception e) {
            if (((GoogleJsonResponseException) e).getStatusCode() == 409) {
                // Ignore ALREADY_EXISTS, but throw others
            } else {
                TalendRuntimeException.build(ComponentsErrorCode.IO_EXCEPTION, e).throwIt();
            }
        }
    }

    private void dropTopic(PubSubClient client) {
        try {
            client.deleteSubscription(dataset.subscription.getValue());
        } catch (Exception e) {
            if (((GoogleJsonResponseException) e).getStatusCode() == 404) {
                // Ignore Not Found, but throw others
            } else {
                TalendRuntimeException.build(ComponentsErrorCode.IO_EXCEPTION, e).throwIt();
            }
        }
        try {
            client.deleteTopic(dataset.topic.getValue());
        } catch (Exception e) {
            if (((GoogleJsonResponseException) e).getStatusCode() == 404) {
                // Ignore Not Found, but throw others
            } else {
                TalendRuntimeException.build(ComponentsErrorCode.IO_EXCEPTION, e).throwIt();
            }
        }
    }

    private void validateForCreateTopic() {
        if (dataset.subscription.getValue() == null || "".equals(dataset.subscription.getValue())) {
            TalendRuntimeException.build(CommonErrorCodes.UNEXPECTED_EXCEPTION).setAndThrow(
                    "Subscription required when create topic");
        }
        if (dataset.topic.getValue() == null || "".equals(dataset.topic.getValue())) {
            TalendRuntimeException.build(CommonErrorCodes.UNEXPECTED_EXCEPTION).setAndThrow(
                    "Topic required when create topic");
        }
    }

    public static class FormatCsvFunction implements SerializableFunction<IndexedRecord, PubsubMessage> {

        public final String fieldDelimiter;

        private StringBuilder sb = new StringBuilder();

        public FormatCsvFunction(String fieldDelimiter) {
            this.fieldDelimiter = fieldDelimiter;
        }

        @Override
        public PubsubMessage apply(IndexedRecord input) {
            int size = input.getSchema().getFields().size();
            for (int i = 0; i < size; i++) {
                if (sb.length() != 0)
                    sb.append(fieldDelimiter);
                sb.append(input.get(i));
            }
            byte[] bytes = sb.toString().getBytes(Charset.forName("UTF-8"));
            sb.setLength(0);
            return new PubsubMessage(bytes, ImmutableMap.<String, String> of());
        }
    }

    public static class FormatCsv extends SimpleFunction<IndexedRecord, PubsubMessage> {

        public final FormatCsvFunction function;

        public FormatCsv(String fieldDelimiter) {
            function = new FormatCsvFunction(fieldDelimiter);
        }

        @Override
        public PubsubMessage apply(IndexedRecord input) {
            return function.apply(input);
        }
    }

    public static class FormatAvro extends SimpleFunction<IndexedRecord, PubsubMessage> {

        @Override
        public PubsubMessage apply(IndexedRecord input) {
            try {
                DatumWriter<IndexedRecord> datumWriter = new GenericDatumWriter(input.getSchema());
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(out, null);
                datumWriter.write(input, encoder);
                encoder.flush();
                byte[] result = out.toByteArray();
                out.close();
                return new PubsubMessage(result, ImmutableMap.<String, String> of());
            } catch (IOException e) {
                throw TalendRuntimeException.createUnexpectedException(e);
            }
        }
    }
}
