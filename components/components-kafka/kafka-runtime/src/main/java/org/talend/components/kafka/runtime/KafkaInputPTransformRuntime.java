package org.talend.components.kafka.runtime;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.apache.avro.reflect.ReflectData;
import org.apache.beam.sdk.coders.AvroCoder;
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
import org.talend.components.kafka.input.KafkaInputProperties;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.ValidationResult;

public class KafkaInputPTransformRuntime extends PTransform<PBegin, PCollection<IndexedRecord>>
        implements RuntimableRuntime<KafkaInputProperties> {

    private transient KafkaInputProperties properties;

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
        // return kafkaRecords.apply("ConvertToObject", ParDo.of(new DoFn<KafkaRecord<byte[], byte[]>, Object>() {
        // @DoFn.ProcessElement
        // public void processElement(ProcessContext c) throws Exception {
        // c.output(new KafkaIndexedRecord(c.element()));
        // }
        // }));
        return kafkaRecords.apply("ProduceIndexedRecord", ParDo.of(new DoFn<KafkaRecord<byte[], byte[]>, IndexedRecord>() {

            @DoFn.ProcessElement
            public void processElement(ProcessContext c) throws Exception {
                c.output(new KafkaIndexedRecord(c.element()));
            }
        }));
    }

    @Override
    public Coder getDefaultOutputCoder() {
        return AvroCoder.of(KafkaIndexedRecord.class, properties.getDatasetProperties().main.schema.getValue());
    }

    @Override
    public ValidationResult initialize(RuntimeContainer container, KafkaInputProperties properties) {
        this.properties = properties;
        return ValidationResult.OK;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeUTF((properties).toSerialized());
    }

    private void readObject(ObjectInputStream in) throws IOException {
        properties = Properties.Helper.fromSerializedPersistent(in.readUTF(), KafkaInputProperties.class).object;
    }

    class KafkaIndexedRecord implements IndexedRecord, Comparable<IndexedRecord> {

        private Schema schema = properties.getDatasetProperties().main.schema.getValue();

        private KafkaRecord<byte[], byte[]> kafkaRecord;

        public KafkaIndexedRecord(KafkaRecord<byte[], byte[]> kafkaRecord) {
            this.kafkaRecord = kafkaRecord;
        }

        @Override
        public int compareTo(IndexedRecord that) {
            return ReflectData.get().compare(this, that, getSchema());
        }

        @Override
        public void put(int i, Object o) {
            throw new UnsupportedOperationException("Should not write to a read-only item.");
        }

        @Override
        public Object get(int i) {
            if (i == 0) {
                return kafkaRecord.getKV().getKey();
            } else if (i == 1) {
                return kafkaRecord.getKV().getValue();
            } else {
                return null;
            }
        }

        @Override
        public Schema getSchema() {
            return schema;
        }
    }
}
