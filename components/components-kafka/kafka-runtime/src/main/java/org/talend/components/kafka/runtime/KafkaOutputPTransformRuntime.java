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

import java.nio.charset.Charset;

import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.sdk.coders.ByteArrayCoder;
import org.apache.beam.sdk.io.kafka.KafkaIO;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.SerializableFunction;
import org.apache.beam.sdk.transforms.SimpleFunction;
import org.apache.beam.sdk.transforms.WithKeys;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PDone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.adapter.beam.coders.LazyAvroCoder;
import org.talend.components.api.component.runtime.RuntimableRuntime;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.kafka.dataset.KafkaDatasetProperties;
import org.talend.components.kafka.output.KafkaOutputProperties;
import org.talend.daikon.properties.ValidationResult;

public class KafkaOutputPTransformRuntime extends PTransform<PCollection<IndexedRecord>, PDone> implements
        RuntimableRuntime<KafkaOutputProperties> {

    private static Logger LOG = LoggerFactory.getLogger(KafkaOutputPTransformRuntime.class);

    private KafkaOutputProperties properties;

    @Override
    public PDone expand(PCollection<IndexedRecord> objectPCollection) {
        final boolean useAvro = properties.getDatasetProperties().valueFormat
                .getValue() == KafkaDatasetProperties.ValueFormat.AVRO;

        KafkaIO.Write<byte[], byte[]> kafkaWrite = KafkaIO.<byte[], byte[]> write()
                .withBootstrapServers(properties.getDatasetProperties().getDatastoreProperties().brokers.getValue())
                .withTopic(properties.getDatasetProperties().topic.getValue())
                .updateProducerProperties(KafkaConnection.createOutputMaps(properties));

        switch (properties.partitionType.getValue()) {
        case COLUMN: {
            PCollection pc1 = objectPCollection.apply(WithKeys.of(new ProduceKey(properties.keyColumn.getValue())));
            if (useAvro) {
                // TODO for now use incoming avro schema directly, do not check configured schema, improvement it.
                return (PDone) pc1.apply(kafkaWrite.withKeyCoder(ByteArrayCoder.of()).withValueCoder(LazyAvroCoder.of()));
            } else { // csv
                return ((PCollection<KV<byte[], byte[]>>) pc1.apply("formatCsvKV",
                        MapElements.via(new FormatCsvKV(properties.getDatasetProperties().fieldDelimiter.getValue()))))
                        .apply(kafkaWrite.withKeyCoder(ByteArrayCoder.of()).withValueCoder(ByteArrayCoder.of()));
            }
        }
        case ROUND_ROBIN: {
            if (useAvro) {
                // TODO for now use incoming avro schema directly, do not check configured schema, improvement it.
                return (PDone) objectPCollection.apply(kafkaWrite.withKeyCoder(ByteArrayCoder.of())
                        .withValueCoder(LazyAvroCoder.of()).values());
            } else { // csv
                return (PDone) objectPCollection.apply(
                        MapElements.via(new FormatCsv(properties.getDatasetProperties().fieldDelimiter.getValue()))).apply(
                        kafkaWrite.withKeyCoder(ByteArrayCoder.of()).withValueCoder(ByteArrayCoder.of()).values());
            }
        }
        default:
            throw new RuntimeException("To be implemented: " + properties.partitionType.getValue());
        }
    }

    @Override
    public ValidationResult initialize(RuntimeContainer container, KafkaOutputProperties properties) {
        this.properties = properties;
        return ValidationResult.OK;
    }

    public static class FormatCsvKV extends SimpleFunction<KV<byte[], IndexedRecord>, KV<byte[], byte[]>> {

        public final FormatCsvFunction function;

        public FormatCsvKV(String fieldDelimiter) {
            function = new FormatCsvFunction(fieldDelimiter);
        }

        @Override
        public KV<byte[], byte[]> apply(KV<byte[], IndexedRecord> input) {
            return KV.of(input.getKey(), function.apply(input.getValue()));
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

    public static class ProduceKey implements SerializableFunction<IndexedRecord, byte[]> {

        private final String keyName;

        public ProduceKey(String keyName) {
            this.keyName = keyName;
        }

        @Override
        public byte[] apply(IndexedRecord input) {
            Object k = input.get(input.getSchema().getField(keyName).pos());
            return String.valueOf(k).getBytes(Charset.forName("UTF-8"));
        }
    }
}
