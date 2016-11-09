package org.talend.components.kafka.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.talend.components.kafka.runtime.KafkaTestConstants.*;

import java.io.IOException;
import java.util.*;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.PipelineResult;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.SerializableFunction;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PBegin;
import org.apache.beam.sdk.values.PCollection;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.kafka.dataset.KafkaDatasetProperties;
import org.talend.components.kafka.datastore.KafkaDatastoreProperties;
import org.talend.components.kafka.input.KafkaInputProperties;
import org.talend.components.kafka.output.KafkaOutputProperties;

public class KafkaBeamRuntimeTestIT {

    KafkaDatastoreProperties datastoreProperties;

    KafkaDatasetProperties inputDatasetProperties;

    KafkaDatasetProperties outputDatasetProperties;

    Integer maxRecords = 10;

    List<Map<String, String>> assertMessages = new ArrayList<>();

    @Before
    public void init() {

        Properties props = new Properties();
        props.put("bootstrap.servers", BOOTSTRAP_HOST);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        Producer<String, String> producer = new KafkaProducer<>(props);
        for (int i = 0; i < maxRecords; i++) {
            ProducerRecord<String, String> message = new ProducerRecord<>(TOPIC_IN, Integer.toString(i), Integer.toString(i));
            producer.send(message);
            HashMap<String, String> assertMessage = new HashMap<>();
            assertMessage.put(message.key(), message.value());
            assertMessages.add(assertMessage);
        }

        producer.close();

        datastoreProperties = new KafkaDatastoreProperties("datastore");
        datastoreProperties.init();
        datastoreProperties.brokers.setValue(BOOTSTRAP_HOST);
        inputDatasetProperties = new KafkaDatasetProperties("inputDataset");
        inputDatasetProperties.init();
        inputDatasetProperties.setDatastoreProperties(datastoreProperties);
        inputDatasetProperties.topic.setValue(TOPIC_IN);
        outputDatasetProperties = new KafkaDatasetProperties("outputDataset");
        outputDatasetProperties.init();
        outputDatasetProperties.setDatastoreProperties(datastoreProperties);
        outputDatasetProperties.topic.setValue(TOPIC_OUT);
    }

    @Test
    public void pipelineTest() {
        Pipeline pipeline = TestPipeline.create();

        KafkaInputProperties inputProperties = new KafkaInputProperties("input");
        inputProperties.init();
        inputProperties.setDatasetProperties(inputDatasetProperties);
        inputProperties.useMaxNumRecords.setValue(true);
        inputProperties.maxNumRecords.setValue(maxRecords.longValue());
        inputProperties.autoOffsetReset.setValue(KafkaInputProperties.OffsetType.EARLIEST);
        KafkaOutputProperties outputProperties = new KafkaOutputProperties("output");
        outputProperties.init();
        outputProperties.setDatasetProperties(outputDatasetProperties);

        KafkaInputPTransformRuntime inputRuntime = new KafkaInputPTransformRuntime();
        inputRuntime.initialize(null, inputProperties);

        KafkaOutputPTransformRuntime outputRuntime = new KafkaOutputPTransformRuntime();
        outputRuntime.initialize(null, outputProperties);

        PCollection indexRecords = inputRuntime.apply(PBegin.in(pipeline)).setCoder(inputRuntime.getDefaultOutputCoder());
        outputRuntime.apply(indexRecords);
        // IndexedRecordToKV indexedRecordToKV = new IndexedRecordToKV();
        // PCollection kv = indexedRecordToKV.apply(indexRecords);

        // PAssert.that(kv).satisfies(new StartWith("k", "v"));

        PipelineResult result = pipeline.run();

        try {
            result.waitUntilFinish();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Properties props = new Properties();
        props.put("bootstrap.servers", BOOTSTRAP_HOST);
        props.put("group.id", "getResult");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("auto.offset.reset", "earliest");
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Arrays.asList(TOPIC_OUT));
        List<Map<String, String>> results = new ArrayList<>();
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(100);
            for (ConsumerRecord<String, String> record : records) {
                Map<String, String> resultMessage = new HashMap<>();
                resultMessage.put(record.key(), record.value());
                results.add(resultMessage);
            }
            if (results.size() >= maxRecords) {
                break;
            }
        }
        assertEquals(assertMessages, results);
    }

    private static class StartWith implements SerializableFunction<Iterable<KV<String, String>>, Void> {

        String key;

        String value;

        public StartWith(String key, String value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public Void apply(Iterable<KV<String, String>> kvs) {
            for (KV<String, String> kv : kvs) {
                assertTrue(kv.getKey().startsWith(key));
                assertTrue(kv.getValue().startsWith(value));
            }

            return null;
        }
    }

    private static class IndexedRecordToKV extends PTransform<PCollection<IndexedRecord>, PCollection<KV<String, String>>> {

        @Override
        public PCollection<KV<String, String>> apply(PCollection<IndexedRecord> indexedRecordPCollection) {
            PCollection<KV<byte[], byte[]>> kafkaCollection = indexedRecordPCollection.apply("ExtractIndexedRecord",
                    ParDo.of(new DoFn<IndexedRecord, KV<byte[], byte[]>>() {

                        @DoFn.ProcessElement
                        public void processElement(ProcessContext c) throws Exception {
                            // FIXME auto convert type before here, or use converter with some built-in auto convert
                            // function for basic type
                            Schema schema = c.element().getSchema();
                            c.output(KV.of((byte[]) c.element().get(schema.getField("key").pos()),
                                    (byte[]) c.element().get(schema.getField("value").pos())));
                        }
                    }));

            return kafkaCollection.apply("ConvertToString", ParDo.of(new DoFn<KV<byte[], byte[]>, KV<String, String>>() {

                @DoFn.ProcessElement
                public void processElement(ProcessContext c) throws Exception {
                    c.output(KV.of(new String(c.element().getKey()), new String(c.element().getValue())));
                }
            }));
        }
    }

}
