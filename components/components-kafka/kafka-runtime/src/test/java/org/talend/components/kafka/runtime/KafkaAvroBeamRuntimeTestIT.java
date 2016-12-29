package org.talend.components.kafka.runtime;

import static org.junit.Assert.assertEquals;
import static org.talend.components.kafka.runtime.KafkaTestConstants.BOOTSTRAP_HOST;
import static org.talend.components.kafka.runtime.KafkaTestConstants.TOPIC_AVRO_IN;
import static org.talend.components.kafka.runtime.KafkaTestConstants.TOPIC_AVRO_OUT;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.IndexedRecord;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.EncoderFactory;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.PipelineResult;
import org.apache.beam.sdk.testing.TestPipeline;
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

public class KafkaAvroBeamRuntimeTestIT {

    public static final String USER_SCHEMA = "{" + "\"type\":\"record\"," + "\"name\":\"abc\"," + "\"fields\":["
            + "  { \"name\":\"str1\", \"type\":\"string\" }," + "  { \"name\":\"str2\", \"type\":\"string\" },"
            + "  { \"name\":\"int1\", \"type\":\"int\" }" + "]}";

    KafkaDatastoreProperties datastoreProperties;

    KafkaDatasetProperties inputDatasetProperties;

    KafkaDatasetProperties outputDatasetProperties;

    Integer maxRecords = 10;

    List<Map<String, String>> assertMessages = new ArrayList<>();

    @Before
    public void init() throws IOException {

        Properties props = new Properties();
        props.put("bootstrap.servers", BOOTSTRAP_HOST);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");

        Schema.Parser parser = new Schema.Parser();
        Schema schema = parser.parse(USER_SCHEMA);

        Producer<String, byte[]> producer = new KafkaProducer<>(props);
        for (int i = 0; i < maxRecords; i++) {
            GenericData.Record avroRecord = new GenericData.Record(schema);
            avroRecord.put("str1", "v1-" + i);
            avroRecord.put("str2", "v2-" + i);
            avroRecord.put("int1", i);
            DatumWriter<GenericRecord> datumWriter = new GenericDatumWriter<GenericRecord>(schema);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(out, null);
            datumWriter.write(avroRecord, encoder);
            encoder.flush();
            out.close();
            ProducerRecord<String, byte[]> message = new ProducerRecord<>(TOPIC_AVRO_IN, Integer.toString(i), out.toByteArray());
            producer.send(message);
            HashMap<String, String> assertMessage = new HashMap<>();
            assertMessage.put(message.key(), avroRecord.toString());
            assertMessages.add(assertMessage);
        }

        producer.close();

        datastoreProperties = new KafkaDatastoreProperties("datastore");
        datastoreProperties.init();
        datastoreProperties.brokers.setValue(BOOTSTRAP_HOST);

        inputDatasetProperties = new KafkaDatasetProperties("inputDataset");
        inputDatasetProperties.init();
        inputDatasetProperties.setDatastoreProperties(datastoreProperties);
        inputDatasetProperties.topic.setValue(TOPIC_AVRO_IN);
        inputDatasetProperties.isHierarchy.setValue(false);
        inputDatasetProperties.valueFormat.setValue(KafkaDatasetProperties.ValueFormat.AVRO);

        SchemaBuilder.FieldAssembler<Schema> fields = SchemaBuilder.record("row").namespace("kafka").fields();
        fields = fields.name("key").type(Schema.create(Schema.Type.BYTES)).noDefault();
        fields = fields.name("str1").type(Schema.create(Schema.Type.STRING)).noDefault();
        fields = fields.name("str2").type(Schema.create(Schema.Type.STRING)).noDefault();
        fields = fields.name("int1").type(Schema.create(Schema.Type.INT)).noDefault();
        Schema customSchema = fields.endRecord();

        inputDatasetProperties.main.schema.setValue(customSchema);

        outputDatasetProperties = new KafkaDatasetProperties("outputDataset");
        outputDatasetProperties.init();
        outputDatasetProperties.setDatastoreProperties(datastoreProperties);
        outputDatasetProperties.topic.setValue(TOPIC_AVRO_OUT);
        outputDatasetProperties.isHierarchy.setValue(false);
        outputDatasetProperties.valueFormat.setValue(KafkaDatasetProperties.ValueFormat.AVRO);

        fields = SchemaBuilder.record("row").namespace("kafka").fields();
        fields = fields.name("key").type(Schema.create(Schema.Type.BYTES)).noDefault();
        fields = fields.name("str1").type(Schema.create(Schema.Type.STRING)).noDefault();
        fields = fields.name("str2").type(Schema.create(Schema.Type.STRING)).noDefault();
        fields = fields.name("int1").type(Schema.create(Schema.Type.INT)).noDefault();
        customSchema = fields.endRecord();

        outputDatasetProperties.main.schema.setValue(customSchema);
    }

    @Test
    public void pipelineTest() throws IOException {
        Pipeline pipeline = TestPipeline.create();

        KafkaInputProperties inputProperties = new KafkaInputProperties("input");
        inputProperties.init();
        inputProperties.setDatasetProperties(inputDatasetProperties);
        // inputProperties.groupID.setValue("test");
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

        PCollection<IndexedRecord> indexRecords = pipeline.apply(inputRuntime);
        indexRecords.apply(outputRuntime);
        // IndexedRecordToKV indexedRecordToKV = new IndexedRecordToKV();
        // PCollection kv = indexedRecordToKV.apply(indexRecords);

        // PAssert.that(kv).satisfies(new StartWith("k", "v"));

        PipelineResult result = pipeline.run();

        Properties props = new Properties();
        props.put("bootstrap.servers", BOOTSTRAP_HOST);
        props.put("group.id", "getResult");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");
        props.put("auto.offset.reset", "earliest");
        KafkaConsumer<String, byte[]> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Arrays.asList(TOPIC_AVRO_OUT));
        List<Map<String, String>> results = new ArrayList<>();
        while (true) {
            ConsumerRecords<String, byte[]> records = consumer.poll(100);
            for (ConsumerRecord<String, byte[]> record : records) {
                Map<String, String> resultMessage = new HashMap<>();
                Schema.Parser parser = new Schema.Parser();
                Schema schema = parser.parse(USER_SCHEMA);
                DatumReader<GenericRecord> datumReader = new GenericDatumReader<GenericRecord>(schema);
                BinaryDecoder decoder = null;
                decoder = DecoderFactory.get().binaryDecoder(record.value(), decoder);
                GenericRecord avroValue = datumReader.read(null, decoder);
                resultMessage.put(record.key(), avroValue.toString());
                results.add(resultMessage);
            }
            if (results.size() >= maxRecords) {
                break;
            }
        }
        assertEquals(assertMessages, results);
    }
}
