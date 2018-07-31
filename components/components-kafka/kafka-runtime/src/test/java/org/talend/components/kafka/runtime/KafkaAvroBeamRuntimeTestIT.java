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
import static org.talend.components.kafka.runtime.KafkaTestConstants.TOPIC_AVRO_IN;
import static org.talend.components.kafka.runtime.KafkaTestConstants.TOPIC_AVRO_OUT;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import org.apache.beam.sdk.PipelineResult;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.transforms.Filter;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.talend.components.kafka.dataset.KafkaDatasetProperties;
import org.talend.components.kafka.datastore.KafkaDatastoreProperties;
import org.talend.components.kafka.input.KafkaInputProperties;
import org.talend.components.kafka.output.KafkaOutputProperties;

public class KafkaAvroBeamRuntimeTestIT {

    KafkaDatastoreProperties datastoreProperties;

    KafkaDatasetProperties inputDatasetProperties;

    KafkaDatasetProperties outputDatasetProperties;

    Integer maxRecords = 10;

    List<Person> expectedPersons = new ArrayList<>();

    @Rule
    public final TestPipeline pipeline = TestPipeline.create();

    @Before
    public void init() throws IOException {

        datastoreProperties = new KafkaDatastoreProperties("datastore");
        datastoreProperties.init();
        datastoreProperties.brokers.setValue(BOOTSTRAP_HOST);

        inputDatasetProperties = new KafkaDatasetProperties("inputDataset");
        inputDatasetProperties.init();
        inputDatasetProperties.setDatastoreProperties(datastoreProperties);
        inputDatasetProperties.topic.setValue(TOPIC_AVRO_IN);
        inputDatasetProperties.valueFormat.setValue(KafkaDatasetProperties.ValueFormat.AVRO);
        inputDatasetProperties.isHierarchy.setValue(false);
        inputDatasetProperties.main.schema.setValue(Person.schema);

        outputDatasetProperties = new KafkaDatasetProperties("outputDataset");
        outputDatasetProperties.init();
        outputDatasetProperties.setDatastoreProperties(datastoreProperties);
        outputDatasetProperties.topic.setValue(TOPIC_AVRO_OUT);
        outputDatasetProperties.valueFormat.setValue(KafkaDatasetProperties.ValueFormat.AVRO);
        outputDatasetProperties.isHierarchy.setValue(false);
        outputDatasetProperties.main.schema.setValue(Person.schema);
    }

    /**
     * Read avro(Person) format and write avro(Person) format with schema.
     */
    @Test
    public void avroBasicTest() throws IOException {
        String testID = "avroBasicTest" + new Random().nextInt();

        expectedPersons = Person.genRandomList(testID, maxRecords);

        // ----------------- Send data to TOPIC_AVRO_IN start --------------------
        Properties props = new Properties();
        props.put("bootstrap.servers", BOOTSTRAP_HOST);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");

        Producer<Void, byte[]> producer = new KafkaProducer<>(props);
        for (Person person : expectedPersons) {
            ProducerRecord<Void, byte[]> message = new ProducerRecord<>(TOPIC_AVRO_IN, person.serToAvroBytes());
            producer.send(message);
        }
        producer.close();
        // ----------------- Send data to TOPIC_AVRO_IN done --------------------

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
        pipeline.apply(inputRuntime).apply(Filter.by(new KafkaCsvBeamRuntimeTestIT.FilterByGroup(testID))).apply(outputRuntime);

        PipelineResult result = pipeline.run();
        // ----------------- pipeline done --------------------

        // ----------------- Read data from TOPIC_AVRO_OUT start --------------------
        props = new Properties();
        props.put("bootstrap.servers", BOOTSTRAP_HOST);
        props.put("group.id", testID);
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");
        props.put("auto.offset.reset", "earliest");
        KafkaConsumer<String, byte[]> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Arrays.asList(TOPIC_AVRO_OUT));
        List<Person> results = new ArrayList<>();
        while (true) {
            ConsumerRecords<String, byte[]> records = consumer.poll(100);
            for (ConsumerRecord<String, byte[]> record : records) {
                Person person = Person.desFromAvroBytes(record.value());
                if (testID.equals(person.group)) {
                    results.add(person);
                }
            }
            if (results.size() >= maxRecords) {
                break;
            }
        }
        // ----------------- Read data from TOPIC_AVRO_OUT done --------------------
        assertEquals(expectedPersons, results);
    }

    /**
     * Read avro(Person) format and write avro(Person) format with schema.
     */
    @Test
    public void avroBasicTest2() throws IOException {
        String testID = "avroBasicTest2" + new Random().nextInt();

        expectedPersons = Person.genRandomList(testID, maxRecords);

        // ----------------- Send data to TOPIC_AVRO_IN start --------------------
        Properties props = new Properties();
        props.put("bootstrap.servers", BOOTSTRAP_HOST);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");

        Producer<Void, byte[]> producer = new KafkaProducer<>(props);
        for (Person person : expectedPersons) {
            ProducerRecord<Void, byte[]> message = new ProducerRecord<>(TOPIC_AVRO_IN, person.serToAvroBytes());
            producer.send(message);
        }
        producer.close();
        // ----------------- Send data to TOPIC_AVRO_IN done --------------------

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
        outputProperties.keyColumn.setValue("name");
        outputProperties.useCompress.setValue(false);


        KafkaInputPTransformRuntime inputRuntime = new KafkaInputPTransformRuntime();
        inputRuntime.initialize(null, inputProperties);

        KafkaOutputPTransformRuntime outputRuntime = new KafkaOutputPTransformRuntime();
        outputRuntime.initialize(null, outputProperties);

        // ----------------- pipeline start --------------------
        pipeline.apply(inputRuntime).apply(Filter.by(new KafkaCsvBeamRuntimeTestIT.FilterByGroup(testID))).apply(outputRuntime);

        PipelineResult result = pipeline.run();
        // ----------------- pipeline done --------------------

        // ----------------- Read data from TOPIC_AVRO_OUT start --------------------
        props = new Properties();
        props.put("bootstrap.servers", BOOTSTRAP_HOST);
        props.put("group.id", testID);
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");
        props.put("auto.offset.reset", "earliest");
        KafkaConsumer<String, byte[]> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Arrays.asList(TOPIC_AVRO_OUT));
        List<Person> results = new ArrayList<>();
        List<String> keys = new ArrayList<>();
        while (true) {
            ConsumerRecords<String, byte[]> records = consumer.poll(100);
            for (ConsumerRecord<String, byte[]> record : records) {
                Person person = Person.desFromAvroBytes(record.value());
                if (testID.equals(person.group)) {
                    keys.add(record.key());
                    results.add(person);
                }
            }
            if (results.size() >= maxRecords) {
                break;
            }
        }
        // ----------------- Read data from TOPIC_AVRO_OUT done --------------------
        assertEquals(expectedPersons, results);
        List<String> expectedKeys = new ArrayList<>();
        for (Person person : results) {
            expectedKeys.add(person.name);
        }
        assertEquals(expectedKeys, keys);

    }
}
