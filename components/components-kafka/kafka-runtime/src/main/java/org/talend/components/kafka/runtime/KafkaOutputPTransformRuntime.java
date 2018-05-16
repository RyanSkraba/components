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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.IndexedRecord;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.beam.sdk.io.kafka.KafkaIO;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.SerializableFunction;
import org.apache.beam.sdk.transforms.SimpleFunction;
import org.apache.beam.sdk.transforms.WithKeys;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PDone;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.RuntimableRuntime;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.kafka.dataset.KafkaDatasetProperties;
import org.talend.components.kafka.output.KafkaOutputProperties;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.properties.ValidationResult;

public class KafkaOutputPTransformRuntime extends PTransform<PCollection<IndexedRecord>, PDone>
        implements RuntimableRuntime<KafkaOutputProperties> {

    private static Logger LOG = LoggerFactory.getLogger(KafkaOutputPTransformRuntime.class);

    private KafkaOutputProperties properties;

    @Override
    public PDone expand(PCollection<IndexedRecord> objectPCollection) {
        final boolean useAvro =
                properties.getDatasetProperties().valueFormat.getValue() == KafkaDatasetProperties.ValueFormat.AVRO;
        final String kafkaDatasetStringSchema = properties.getDatasetProperties().avroSchema.getValue();
        final boolean useCustomAvroSchema = properties.getDatasetProperties().isHierarchy.getValue();
        final IndexedRecordHelper indexedRecordHelper =
                new IndexedRecordHelper(kafkaDatasetStringSchema, useCustomAvroSchema);

        KafkaIO.Write<byte[], byte[]> kafkaWrite = KafkaIO
                .<byte[], byte[]> write()
                .withBootstrapServers(properties.getDatasetProperties().getDatastoreProperties().brokers.getValue())
                .withTopic(properties.getDatasetProperties().topic.getValue())
                .withKeySerializer(ByteArraySerializer.class)
                .withValueSerializer(ByteArraySerializer.class)
                .updateProducerProperties(KafkaConnection.createOutputMaps(properties));

        switch (properties.partitionType.getValue()) {
        case COLUMN: {
            PCollection pc1 = objectPCollection.apply(WithKeys.of(new ProduceKey(properties.keyColumn.getValue())));
            if (useAvro) {
                // TODO for now use incoming avro schema directly, do not check configured schema, improvement it.
                return ((PCollection<KV<byte[], byte[]>>) pc1
                        .apply(ParDo.of(new AvroKVToByteArrayDoFn(indexedRecordHelper)))).apply(kafkaWrite);
            } else { // csv
                return ((PCollection<KV<byte[], byte[]>>) pc1
                        .apply(MapElements.via(new FormatCsvKV(properties.getDatasetProperties().getFieldDelimiter()))))
                                .apply(kafkaWrite);
            }
        }
        case ROUND_ROBIN: {
            if (useAvro) {
                // TODO for now use incoming avro schema directly, do not check configured schema, improvement it.
                return (PDone) objectPCollection.apply(ParDo.of(new AvroToByteArrayDoFn(indexedRecordHelper))).apply(
                        kafkaWrite.values());
            } else { // csv
                return (PDone) objectPCollection
                        .apply(MapElements.via(new FormatCsv(properties.getDatasetProperties().getFieldDelimiter())))
                        .apply(kafkaWrite.values());
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

    /**
     * An {@link IndexedRecord} wrapper to wrap {@link IndexedRecord} with a custom
     * Kafka Avro schema. Calls are delegated to the wrapped {@link IndexedRecord},
     * with the exception of @getSchema that returns a fixed {@link Schema}
     */
    public static class KafkaIndexedRecordWrapper implements IndexedRecord {

        IndexedRecord incomingIndexedRecord;

        Schema datasetSchema;

        public void setIndexedRecord(IndexedRecord incomingIndexedRecord) {
            this.incomingIndexedRecord = incomingIndexedRecord;
        }

        public void setDatasetSchema(Schema datasetSchema) {
            this.datasetSchema = datasetSchema;
        }

        @Override
        public void put(int i, Object v) {
            incomingIndexedRecord.put(i, v);
        }

        @Override
        public Object get(int i) {
            return incomingIndexedRecord.get(i);
        }

        @Override
        public Schema getSchema() {
            return datasetSchema;
        }
    }

    /**
     * Transform Avro key value {@link IndexedRecord} into a byte array.
     *
     * In case of a dataset that uses a custom Avro schema, the @processElement method
     * uses the {@link IndexedRecordHelper} to wrap incoming records in
     * {@link KafkaIndexedRecordWrapper} and then writes them using the custom Avro schema.
     * Otherwise, the incoming records are written with their original schema.
     */
    public static class AvroKVToByteArrayDoFn extends DoFn<KV<byte[], IndexedRecord>, KV<byte[], byte[]>> {

        IndexedRecordHelper helper;

        AvroKVToByteArrayDoFn(IndexedRecordHelper helper) {
            this.helper = helper;
        }

        @Setup
        public void setup() {
            helper.setup();
        }

        @ProcessElement
        public void processElement(ProcessContext c) {
            try {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(out, null);
                if (helper.isUseCustomAvroSchema()) {
                    helper.getKafkaIndexedRecordWrapper().setIndexedRecord(c.element().getValue());
                    helper.getDatumWriter().write(helper.getKafkaIndexedRecordWrapper(), encoder);
                } else {
                    if (helper.getDatumWriter() == null) {
                        // set the datumWriter for the first time with the incoming record schema
                        helper.setDatumWriter(new GenericDatumWriter(c.element().getValue().getSchema()));
                    }
                    helper.getDatumWriter().write(c.element().getValue(), encoder);
                }
                encoder.flush();
                byte[] result = out.toByteArray();
                out.close();
                c.output(KV.of(c.element().getKey(), result));
            } catch (IOException e) {
                throw TalendRuntimeException.createUnexpectedException(e);
            }
        }
    }

    /**
     * Transform Avro {@link IndexedRecord} into a byte array.
     *
     * In case of a dataset that uses a custom Avro schema, the @processElement method
     * uses the {@link IndexedRecordHelper} to wrap incoming records in
     * {@link KafkaIndexedRecordWrapper} and then writes them using the custom Avro schema.
     * Otherwise, the incoming records are written with their original schema.
     */
    public static class AvroToByteArrayDoFn extends DoFn<IndexedRecord, byte[]> {

        IndexedRecordHelper helper;

        AvroToByteArrayDoFn(IndexedRecordHelper helper) {
            this.helper = helper;
        }

        @Setup
        public void setup() {
            helper.setup();
        }

        @ProcessElement
        public void processElement(ProcessContext c) {
            try {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(out, null);
                if (helper.isUseCustomAvroSchema()) {
                    helper.getKafkaIndexedRecordWrapper().setIndexedRecord(c.element());
                    helper.getDatumWriter().write(helper.getKafkaIndexedRecordWrapper(), encoder);
                } else {
                    if (helper.getDatumWriter() == null) {
                        // set the datumWriter for the first time with the incoming record schema
                        helper.setDatumWriter(new GenericDatumWriter(c.element().getSchema()));
                    }
                    helper.getDatumWriter().write(c.element(), encoder);
                }
                encoder.flush();
                byte[] result = out.toByteArray();
                out.close();
                c.output(result);
            } catch (IOException e) {
                throw TalendRuntimeException.createUnexpectedException(e);
            }
        }
    }

    /**
     * {@link IndexedRecord} helper to setup {@link DoFn} classes for working with incoming records
     * {@link IndexedRecord} to Byte arrays and avoid object instantiation
     * inside @{@link org.apache.beam.sdk.transforms.DoFn.ProcessElement}
     *
     * If @useCustomAvroSchema is set to true, we initialize some helper objects in the @setup().
     */
    public static class IndexedRecordHelper implements Serializable {

        private String kafkaDatasetStringSchema;

        private boolean useCustomAvroSchema;

        private KafkaIndexedRecordWrapper kafkaIndexedRecordWrapper;

        private DatumWriter<IndexedRecord> datumWriter;

        IndexedRecordHelper(String kafkaDatasetStringSchema, boolean useCustomAvroSchema) {
            this.useCustomAvroSchema = useCustomAvroSchema;
            this.kafkaDatasetStringSchema = kafkaDatasetStringSchema;
        }

        public void setup() {
            if (useCustomAvroSchema) {
                kafkaIndexedRecordWrapper = new KafkaIndexedRecordWrapper();
                org.apache.avro.Schema.Parser parser = new org.apache.avro.Schema.Parser();
                Schema kafkaDatasetSchema = parser.parse(kafkaDatasetStringSchema);
                kafkaIndexedRecordWrapper.setDatasetSchema(kafkaDatasetSchema);
                datumWriter = new GenericDatumWriter(kafkaDatasetSchema);
            }
        }

        public KafkaIndexedRecordWrapper getKafkaIndexedRecordWrapper() {
            return kafkaIndexedRecordWrapper;
        }

        public DatumWriter<IndexedRecord> getDatumWriter() {
            return datumWriter;
        }

        public boolean isUseCustomAvroSchema() {
            return useCustomAvroSchema;
        }

        public void setDatumWriter(DatumWriter<IndexedRecord> datumWriter) {
            this.datumWriter = datumWriter;
        }

        @Override
        public String toString() {
            return "IndexedRecordHelper{" + "kafkaDatasetStringSchema='" + kafkaDatasetStringSchema + '\''
                    + ", useCustomAvroSchema=" + useCustomAvroSchema + ", kafkaIndexedRecordWrapper="
                    + kafkaIndexedRecordWrapper + ", datumWriter=" + datumWriter + '}';
        }
    }
}
