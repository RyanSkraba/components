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
package org.talend.components.kafka.runtime;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.IndexedRecord;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DecoderFactory;
import org.apache.beam.sdk.coders.ByteArrayCoder;
import org.apache.beam.sdk.coders.Coder;
import org.apache.beam.sdk.io.kafka.KafkaIO;
import org.apache.beam.sdk.io.kafka.KafkaRecord;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.Values;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PBegin;
import org.apache.beam.sdk.values.PCollection;
import org.joda.time.Duration;
import org.talend.components.adapter.beam.coders.LazyAvroCoder;
import org.talend.components.adapter.beam.transform.ConvertToIndexedRecord;
import org.talend.components.api.component.runtime.RuntimableRuntime;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.kafka.input.KafkaInputProperties;
import org.talend.daikon.properties.ValidationResult;

public class KafkaInputPTransformRuntime extends PTransform<PBegin, PCollection<IndexedRecord>>
        implements RuntimableRuntime<KafkaInputProperties> {

    private KafkaInputProperties properties;

    @Override
    public PCollection<IndexedRecord> expand(PBegin pBegin) {

        KafkaIO.Read<byte[], byte[]> kafkaRead = KafkaIO.readBytes()
                .withBootstrapServers(properties.getDatasetProperties().getDatastoreProperties().brokers.getValue())
                .withTopics(Arrays.asList(new String[] { properties.getDatasetProperties().topic.getValue() }))
                .updateConsumerProperties(KafkaConnection.createInputMaps(properties));

        if (properties.useMaxReadTime.getValue()) {
            kafkaRead = kafkaRead.withMaxReadTime(new Duration(properties.maxReadTime.getValue()));
        }
        if (properties.useMaxNumRecords.getValue()) {
            kafkaRead = kafkaRead.withMaxNumRecords(properties.maxNumRecords.getValue());
        }
        // only consider value of kafkaRecord no matter which format selected
        PCollection<byte[]> kafkaRecords = pBegin.apply(kafkaRead) //
                .apply(ParDo.of(new ExtractRecord())) //
                .apply(Values.<byte[]> create());
        switch (properties.getDatasetProperties().valueFormat.getValue()) {
        case AVRO: {
            Schema schema = null;
            if (properties.getDatasetProperties().isHierarchy.getValue()) {
                // use component's schema directly? should be done on design time, no?
                schema = new Schema.Parser().parse(properties.getDatasetProperties().avroSchema.getValue());
            } else {
                // use component's schema directly as we are avro natural
                schema = properties.getDatasetProperties().main.schema.getValue();
            }
            return kafkaRecords.apply(ParDo.of(new ConvertToAvro(schema.toString()))).setCoder(getDefaultOutputCoder());
        }
        case CSV: {
            // FIXME(bchen) KafkaAvroRegistry do not have way to record adaptation, it infer schema by the data rather
            // than use the defined schema
            return ((PCollection) kafkaRecords
                    .apply(ParDo.of(new ExtractCsvSplit(properties.getDatasetProperties().fieldDelimiter.getValue())))
                    .apply((PTransform) ConvertToIndexedRecord.of())).setCoder(getDefaultOutputCoder());
        }
        default:
            throw new RuntimeException("To be implemented: " + properties.getDatasetProperties().valueFormat.getValue());
        }

    }

    @Override
    public ValidationResult initialize(RuntimeContainer container, KafkaInputProperties properties) {
        this.properties = properties;
        return ValidationResult.OK;
    }

    @Override
    public Coder getDefaultOutputCoder() {
        return LazyAvroCoder.of();
    }

    public static class ExtractRecord extends DoFn<KafkaRecord<byte[], byte[]>, KV<byte[], byte[]>> {

        @DoFn.ProcessElement
        public void processElement(ProcessContext c) {
            c.output(c.element().getKV());
        }
    }

    public static class ConvertToAvro extends DoFn<byte[], IndexedRecord> {

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
            decoder = DecoderFactory.get().binaryDecoder(c.element(), decoder);
            GenericRecord record = datumReader.read(null, decoder);
            c.output(record);
        }
    }

}
