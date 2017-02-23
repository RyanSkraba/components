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

import static org.junit.Assert.assertEquals;
import static org.talend.components.kafka.runtime.KafkaTestConstants.BOOTSTRAP_HOST;
import static org.talend.components.kafka.runtime.KafkaTestConstants.TOPIC_IN;
import static org.talend.components.kafka.runtime.KafkaTestConstants.TOPIC_OUT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.sdk.PipelineResult;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.transforms.Filter;
import org.apache.beam.sdk.transforms.SerializableFunction;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.talend.components.kafka.dataset.KafkaDatasetProperties;
import org.talend.components.kafka.datastore.KafkaDatastoreProperties;
import org.talend.components.kafka.input.KafkaInputProperties;
import org.talend.components.kafka.output.KafkaOutputProperties;

public class KafkaCsvBeamRuntimeTestIT {

    KafkaDatastoreProperties datastoreProperties;

    KafkaDatasetProperties inputDatasetProperties;

    KafkaDatasetProperties outputDatasetProperties;

    Integer maxRecords = 10;

    String fieldDelimiter = ";";

    List<Person> expectedPersons = new ArrayList<>();

    @Rule
    public final TestPipeline pipeline = TestPipeline.create();

    @Before
    public void init() {
        datastoreProperties = new KafkaDatastoreProperties("datastore");
        datastoreProperties.init();
        datastoreProperties.brokers.setValue(BOOTSTRAP_HOST);

        inputDatasetProperties = new KafkaDatasetProperties("inputDataset");
        inputDatasetProperties.init();
        inputDatasetProperties.setDatastoreProperties(datastoreProperties);
        inputDatasetProperties.topic.setValue(TOPIC_IN);
        inputDatasetProperties.valueFormat.setValue(KafkaDatasetProperties.ValueFormat.CSV);
        inputDatasetProperties.fieldDelimiter.setValue(fieldDelimiter);
        // no schema defined

        outputDatasetProperties = new KafkaDatasetProperties("outputDataset");
        outputDatasetProperties.init();
        outputDatasetProperties.setDatastoreProperties(datastoreProperties);
        outputDatasetProperties.topic.setValue(TOPIC_OUT);
        outputDatasetProperties.valueFormat.setValue(KafkaDatasetProperties.ValueFormat.CSV);
        outputDatasetProperties.fieldDelimiter.setValue(fieldDelimiter);
        // no schema defined
    }

    /**
     * Read csv format value and write csv format value without schema and do not write key
     */
    @Test
    @Ignore("Temp fix to unlock others")
    public void csvBasicTest() {
        String testID = "csvBasicTest" + new Random().nextInt();

        expectedPersons = Person.genRandomList(testID, maxRecords);

        // ----------------- Send data to TOPIC_IN start --------------------
        Properties props = new Properties();
        props.put("bootstrap.servers", BOOTSTRAP_HOST);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        Producer<Void, String> producer = new KafkaProducer<>(props);
        for (Person person : expectedPersons) {
            ProducerRecord<Void, String> message = new ProducerRecord<>(TOPIC_IN, person.toCSV(fieldDelimiter));
            producer.send(message);
        }
        producer.close();
        // ----------------- Send data to TOPIC_IN done --------------------

        KafkaInputProperties inputProperties = new KafkaInputProperties("input");
        inputProperties.init();
        inputProperties.setDatasetProperties(inputDatasetProperties);
        inputProperties.autoOffsetReset.setValue(KafkaInputProperties.OffsetType.EARLIEST);
        inputProperties.useMaxNumRecords.setValue(false);
        // inputProperties.maxNumRecords.setValue(maxRecords.longValue());
        inputProperties.useMaxReadTime.setValue(true);
        inputProperties.maxReadTime.setValue(5000l);

        KafkaOutputProperties outputProperties = new KafkaOutputProperties("output");
        outputProperties.init();
        outputProperties.setDatasetProperties(outputDatasetProperties);
        outputProperties.partitionType.setValue(KafkaOutputProperties.PartitionType.ROUND_ROBIN);
        outputProperties.useCompress.setValue(false);

        KafkaInputPTransformRuntime inputRuntime = new KafkaInputPTransformRuntime();
        inputRuntime.initialize(null, inputProperties);

        KafkaOutputPTransformRuntime outputRuntime = new KafkaOutputPTransformRuntime();
        outputRuntime.initialize(null, outputProperties);

        // ----------------- pipeline start --------------------
        pipeline.apply(inputRuntime).apply(Filter.by(new FilterByGroup(testID))).apply(outputRuntime);

        PipelineResult result = pipeline.run();
        // ----------------- pipeline done --------------------

        // ----------------- Read data from TOPIC_OUT start --------------------
        props = new Properties();
        props.put("bootstrap.servers", BOOTSTRAP_HOST);
        props.put("group.id", "getResult");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("auto.offset.reset", "earliest");
        KafkaConsumer<Void, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Arrays.asList(TOPIC_OUT));
        List<Person> results = new ArrayList<>();
        while (true) {
            ConsumerRecords<Void, String> records = consumer.poll(100);
            for (ConsumerRecord<Void, String> record : records) {
                Person person = Person.fromCSV(record.value(), fieldDelimiter);
                if (testID.equals(person.group)) {
                    results.add(person);
                }
            }
            if (results.size() >= maxRecords) {
                break;
            }
        }
        // ----------------- Read data from TOPIC_OUT end --------------------

        assertEquals(expectedPersons, results);
    }

    /**
     * Read csv format value and write csv format value without schema and do not write key
     */
    @Test
    @Ignore("Temp fix to unlock others")
    public void csvBasicTest2() {
        String testID = "csvBasicTest2" + new Random().nextInt();

        expectedPersons = Person.genRandomList(testID, maxRecords);

        // ----------------- Send data to TOPIC_IN start --------------------
        Properties props = new Properties();
        props.put("bootstrap.servers", BOOTSTRAP_HOST);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        Producer<Void, String> producer = new KafkaProducer<>(props);
        for (Person person : expectedPersons) {
            ProducerRecord<Void, String> message = new ProducerRecord<>(TOPIC_IN, person.toCSV(fieldDelimiter));
            producer.send(message);
        }
        producer.close();
        // ----------------- Send data to TOPIC_IN done --------------------

        KafkaInputProperties inputProperties = new KafkaInputProperties("input");
        inputProperties.init();
        inputProperties.setDatasetProperties(inputDatasetProperties);
        inputProperties.autoOffsetReset.setValue(KafkaInputProperties.OffsetType.EARLIEST);
        inputProperties.useMaxNumRecords.setValue(false);
        // inputProperties.maxNumRecords.setValue(maxRecords.longValue());
        inputProperties.useMaxReadTime.setValue(true);
        inputProperties.maxReadTime.setValue(5000l);

        KafkaOutputProperties outputProperties = new KafkaOutputProperties("output");
        outputProperties.init();
        outputProperties.setDatasetProperties(outputDatasetProperties);
        outputProperties.partitionType.setValue(KafkaOutputProperties.PartitionType.COLUMN);
        outputProperties.keyColumn.setValue("field1"); // name generated by KafkaAvroRegistry
        outputProperties.useCompress.setValue(false);

        KafkaInputPTransformRuntime inputRuntime = new KafkaInputPTransformRuntime();
        inputRuntime.initialize(null, inputProperties);

        KafkaOutputPTransformRuntime outputRuntime = new KafkaOutputPTransformRuntime();
        outputRuntime.initialize(null, outputProperties);

        // ----------------- pipeline start --------------------
        pipeline.apply(inputRuntime).apply(Filter.by(new FilterByGroup(testID))).apply(outputRuntime);

        PipelineResult result = pipeline.run();
        // ----------------- pipeline done --------------------

        // ----------------- Read data from TOPIC_OUT start --------------------
        props = new Properties();
        props.put("bootstrap.servers", BOOTSTRAP_HOST);
        props.put("group.id", "getResult");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("auto.offset.reset", "earliest");
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Arrays.asList(TOPIC_OUT));
        List<Person> results = new ArrayList<>();
        List<String> keys = new ArrayList<>();
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(100);
            for (ConsumerRecord<String, String> record : records) {
                Person person = Person.fromCSV(record.value(), fieldDelimiter);
                if (testID.equals(person.group)) {
                    keys.add(record.key());
                    results.add(person);
                }
            }
            if (results.size() >= maxRecords) {
                break;
            }
        }
        // ----------------- Read data from TOPIC_OUT end --------------------

        assertEquals(expectedPersons, results);
        List<String> expectedKeys = new ArrayList<>();
        for (Person person : results) {
            expectedKeys.add(person.name);
        }
        assertEquals(expectedKeys, keys);
    }

    public static class FilterByGroup implements SerializableFunction<IndexedRecord, Boolean> {

        private final String groupID;

        public FilterByGroup(String groupID) {
            this.groupID = groupID;
        }

        @Override
        public Boolean apply(IndexedRecord input) {
            // schema of input is not same as Person.schema
            return groupID.equals(input.get(0).toString());
        }
    }
}
