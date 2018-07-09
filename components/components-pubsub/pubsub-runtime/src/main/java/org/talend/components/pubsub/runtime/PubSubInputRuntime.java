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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.IndexedRecord;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DecoderFactory;
import org.apache.beam.sdk.coders.Coder;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubIO;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubMessage;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PBegin;
import org.apache.beam.sdk.values.PCollection;
import org.joda.time.Instant;
import org.talend.components.adapter.beam.BeamJobRuntimeContainer;
import org.talend.components.adapter.beam.coders.LazyAvroCoder;
import org.talend.components.adapter.beam.gcp.GcpServiceAccountOptions;
import org.talend.components.adapter.beam.gcp.ServiceAccountCredentialFactory;
import org.talend.components.adapter.beam.transform.ConvertToIndexedRecord;
import org.talend.components.api.component.runtime.RuntimableRuntime;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.pubsub.PubSubDatasetProperties;
import org.talend.components.pubsub.PubSubDatastoreProperties;
import org.talend.components.pubsub.input.PubSubInputProperties;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.properties.ValidationResult;

import com.google.api.services.pubsub.model.ReceivedMessage;
import com.google.common.collect.ImmutableMap;

public class PubSubInputRuntime extends PTransform<PBegin, PCollection<IndexedRecord>>
        implements RuntimableRuntime<PubSubInputProperties> {

    /**
     * The component instance that this runtime is configured for.
     */
    private PubSubInputProperties properties = null;

    private PubSubDatasetProperties dataset = null;

    private PubSubDatastoreProperties datastore = null;

    protected boolean runOnDataflow = false;

    @Override
    public ValidationResult initialize(RuntimeContainer container, PubSubInputProperties properties) {
        this.properties = properties;
        this.dataset = properties.getDatasetProperties();
        this.datastore = dataset.getDatastoreProperties();

        if (container != null) {
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
        }
        return ValidationResult.OK;
    }

    @Override
    public PCollection<IndexedRecord> expand(PBegin in) {
        PCollection<PubsubMessage> pubsubMessages = null;
        if (properties.useMaxNumRecords.getValue() || properties.useMaxReadTime.getValue()) {
            pubsubMessages = in.apply(Create.of(dataset.subscription.getValue())).apply(
                    ParDo.of(new BoundedReaderFn(properties, runOnDataflow)));
        } else {// normal
            PubsubIO.Read<PubsubMessage> pubsubRead =
                    PubsubIO.readMessages().fromSubscription(String.format("projects/%s/subscriptions/%s",
                            datastore.projectName.getValue(), dataset.subscription.getValue()));
            if (properties.idLabel.getValue() != null && !"".equals(properties.idLabel.getValue())) {
                pubsubRead.withIdAttribute(properties.idLabel.getValue());
            }
            if (properties.timestampLabel.getValue() != null && !"".equals(properties.timestampLabel.getValue())) {
                pubsubRead.withTimestampAttribute(properties.timestampLabel.getValue());
            }

            pubsubMessages = in.apply(pubsubRead);
        }

        switch (dataset.valueFormat.getValue()) {
        case AVRO: {
            Schema schema = new Schema.Parser().parse(dataset.avroSchema.getValue());
            return pubsubMessages.apply(ParDo.of(new ConvertToAvro(schema.toString()))).setCoder(
                    getDefaultOutputCoder());
        }
        case CSV: {
            return (PCollection<IndexedRecord>) pubsubMessages
                    .apply(ParDo.of(new ExtractCsvSplit(dataset.fieldDelimiter.getValue())))
                    .apply((PTransform) ConvertToIndexedRecord.of());
        }
        default:
            throw new RuntimeException("To be implemented: " + dataset.valueFormat.getValue());

        }
    }

    @Override
    public Coder getDefaultOutputCoder() {
        return LazyAvroCoder.of();
    }

    public static class ConvertToAvro extends DoFn<PubsubMessage, IndexedRecord> {

        private final String schemaStr;

        private transient Schema schema;

        private transient DatumReader<GenericRecord> datumReader;

        private transient BinaryDecoder decoder;

        ConvertToAvro(String schemaStr) {
            this.schemaStr = schemaStr;
        }

        @DoFn.ProcessElement
        public void processElement(ProcessContext c) throws IOException {
            if (schema == null) {
                schema = new Schema.Parser().parse(schemaStr);
                datumReader = new GenericDatumReader<GenericRecord>(schema);
            }
            decoder = DecoderFactory.get().binaryDecoder(c.element().getPayload(), decoder);
            GenericRecord record = datumReader.read(null, decoder);
            c.output(record);
        }
    }

    static class BoundedReaderFn extends DoFn<String, PubsubMessage> {

        private PubSubInputProperties spec;

        private PubSubClient client;

        private int maxNum = 100;

        private long maxTime = 1000l;// 1 second

        private boolean ack = true;

        private boolean runOnDataflow = false;

        private BoundedReaderFn(PubSubInputProperties spec, boolean runOnDataflow) {
            this.spec = spec;
            this.runOnDataflow = runOnDataflow;
        }

        @Setup
        public void setup() {
            client = PubSubConnection.createClient(spec.getDatasetProperties().getDatastoreProperties(), runOnDataflow);
            if (spec.useMaxNumRecords.getValue()) {
                maxNum = spec.maxNumRecords.getValue();
            }
            if (spec.useMaxReadTime.getValue()) {
                maxTime = spec.maxReadTime.getValue();
            }
            ack = !spec.noACK.getValue();
        }

        @ProcessElement
        public void processElement(ProcessContext context) {
            int num = 0;
            Instant endTime = Instant.now().plus(maxTime);
            while (num < maxNum && Instant.now().isBefore(endTime)) {
                try {
                    List<String> ackIds = new ArrayList<>();
                    List<ReceivedMessage> receivedMessages =
                            client.pull(spec.getDatasetProperties().subscription.getValue(), maxNum - num);
                    if (receivedMessages == null) {
                        continue;
                    }
                    for (ReceivedMessage receivedMessage : receivedMessages) {
                        context.output(new PubsubMessage(receivedMessage.getMessage().decodeData(),
                                ImmutableMap.<String, String> of()));
                        ackIds.add(receivedMessage.getAckId());
                        num++;
                    }
                    if (ack && !ackIds.isEmpty()) { // do not call this when getSample, else the message will be removed
                        client.ack(spec.getDatasetProperties().subscription.getValue(), ackIds);
                    }
                } catch (IOException e) {
                    throw TalendRuntimeException.createUnexpectedException(e);
                }
            }
        }
    }

}
