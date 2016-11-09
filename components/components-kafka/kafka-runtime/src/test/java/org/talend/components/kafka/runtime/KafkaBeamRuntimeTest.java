package org.talend.components.kafka.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

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
import org.junit.Before;
import org.junit.Test;
import org.talend.components.kafka.dataset.KafkaDatasetProperties;
import org.talend.components.kafka.datastore.KafkaDatastoreProperties;
import org.talend.components.kafka.input.KafkaInputProperties;
import org.talend.components.kafka.output.KafkaOutputProperties;

import kafka.producer.KeyedMessage;

public class KafkaBeamRuntimeTest extends KafkaTestBase {

    KafkaDatastoreProperties datastoreProperties;

    KafkaDatasetProperties inputDatasetProperties;

    KafkaDatasetProperties outputDatasetProperties;

    String topic_in = "test_in";

    String topic_out = "test_out";

    Integer maxRecords = 100;

    List<KeyedMessage<String, String>> assertMessages = new ArrayList<>();

    @Before
    public void init() {

        kafkaUnitRule.getKafkaUnit().createTopic(topic_in);
        kafkaUnitRule.getKafkaUnit().createTopic(topic_out);

        for (int i = 1; i <= maxRecords; i++) {
            KeyedMessage<String, String> message = new KeyedMessage<>(topic_in, "k" + i, "v" + i);
            kafkaUnitRule.getKafkaUnit().sendMessages(message);
            assertMessages.add(new KeyedMessage<String, String>(topic_out, message.key(), message.message()));
        }

        datastoreProperties = new KafkaDatastoreProperties("datastore");
        datastoreProperties.init();
        datastoreProperties.brokers.setValue(BROKER_URL);
        inputDatasetProperties = new KafkaDatasetProperties("inputDataset");
        inputDatasetProperties.init();
        inputDatasetProperties.setDatastoreProperties(datastoreProperties);
        inputDatasetProperties.topic.setValue(topic_in);
        outputDatasetProperties = new KafkaDatasetProperties("outputDataset");
        outputDatasetProperties.init();
        outputDatasetProperties.setDatastoreProperties(datastoreProperties);
        outputDatasetProperties.topic.setValue(topic_out);
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

        try {
            List<KeyedMessage<String, String>> keyedMessages = kafkaUnitRule.getKafkaUnit().readKeyedMessages(topic_out,
                    maxRecords);
            assertEquals(assertMessages, keyedMessages);
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
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
