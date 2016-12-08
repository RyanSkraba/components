// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.kafka.runtime;

import java.util.Arrays;

import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.sdk.coders.ByteArrayCoder;
import org.apache.beam.sdk.coders.Coder;
import org.apache.beam.sdk.io.kafka.KafkaIO;
import org.apache.beam.sdk.io.kafka.KafkaRecord;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PBegin;
import org.apache.beam.sdk.values.PCollection;
import org.joda.time.Duration;
import org.talend.components.api.component.runtime.RuntimableRuntime;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.kafka.dataset.KafkaDatasetProperties;
import org.talend.components.kafka.input.KafkaInputProperties;
import org.talend.daikon.properties.ValidationResult;

public class KafkaInputPTransformRuntime extends PTransform<PBegin, PCollection<IndexedRecord>>
        implements RuntimableRuntime<KafkaInputProperties> {

    private KafkaInputProperties properties;

    @Override
    public PCollection<IndexedRecord> apply(PBegin pBegin) {

        KafkaIO.Read<byte[], byte[]> kafkaRead = KafkaIO.read()
                .withBootstrapServers(properties.getDatasetProperties().getDatastoreProperties().brokers.getValue())
                .withTopics(Arrays.asList(new String[] { properties.getDatasetProperties().topic.getValue() }))
                .withKeyCoder(ByteArrayCoder.of()).withValueCoder(ByteArrayCoder.of())
                .updateConsumerProperties(KafkaConnection.createInputMaps(properties));
        if (properties.useMaxReadTime.getValue()) {
            kafkaRead = kafkaRead.withMaxReadTime(new Duration(properties.maxReadTime.getValue()));
        }
        if (properties.useMaxNumRecords.getValue()) {
            kafkaRead = kafkaRead.withMaxNumRecords(properties.maxNumRecords.getValue());
        }
        PCollection<KafkaRecord<byte[], byte[]>> kafkaRecords = pBegin.apply(kafkaRead);
        return kafkaRecords.apply("ProduceIndexedRecord", ParDo.of(new DoFn<KafkaRecord<byte[], byte[]>, IndexedRecord>() {

            @DoFn.ProcessElement
            public void processElement(ProcessContext c) throws Exception {
                c.output(new KafkaIndexedRecord(properties.getDatasetProperties().main.schema.getValue(),
                        c.element().getKV().getKey(), c.element().getKV().getValue(),
                        properties.getDatasetProperties().valueFormat.getValue() == KafkaDatasetProperties.ValueFormat.AVRO,
                        !properties.getDatasetProperties().isHierarchy.getValue(),
                        properties.getDatasetProperties().getValueAvroSchema()));
            }
        })).setCoder(getDefaultOutputCoder());
    }

    @Override
    public ValidationResult initialize(RuntimeContainer container, KafkaInputProperties properties) {
        this.properties = properties;
        return ValidationResult.OK;
    }

    @Override
    public Coder getDefaultOutputCoder() {
        return KafkaIndexedRecordCoder.of();
    }

}
