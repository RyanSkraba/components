package org.talend.components.service.rest.impl;

import static com.fasterxml.jackson.core.JsonToken.*;
import static org.apache.commons.lang3.Validate.isTrue;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Objects;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.IndexedRecord;
import org.apache.avro.io.DecoderFactory;
import org.talend.components.service.rest.dto.UiSpecsPropertiesDto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Allow parsing as much object-mapping oriented as possible the streamed payload to write a dataset.
 * The payload must have to parts: one is the {@code configuration} structured as {@link UiSpecsPropertiesDto}, and the {@code data} itself.
 * It can then be read as an iteration of values.
 *
 */
public class DatasetWritePayload implements AutoCloseable {

    private static final String CONFIGURATION_FIELD_NAME = "configuration";

    private static final String AVRO_SCHEMA_FIELD_NAME = "avroSchema";

    private static final String DATA_FIELD_NAME = "data";

    private final UiSpecsPropertiesDto configuration;
    
    private final Iterator<IndexedRecord> data;

    private final Closeable resource;

    public DatasetWritePayload(UiSpecsPropertiesDto configuration, Iterator<IndexedRecord> data, Closeable resource) {
        this.configuration = configuration;
        this.data = data;
        this.resource = resource;
    }

    public static DatasetWritePayload readData(InputStream input, ObjectMapper mapper) throws IOException {
        JsonParser parser = mapper.getFactory().createParser(input);

        JsonToken objectStartToken = parser.nextToken();
        isTrue(START_OBJECT == objectStartToken, invalidInputMessage(START_OBJECT, objectStartToken));

        UiSpecsPropertiesDto configuration = readConfiguration(parser);
        Schema schema = readAvroSchema(parser);
        Iterator<IndexedRecord> streamToReadData = createStreamToReadData(parser, mapper, schema, input);

        return new DatasetWritePayload(configuration, streamToReadData, parser);
    }

    private static Iterator<IndexedRecord> createStreamToReadData(JsonParser parser, ObjectMapper mapper, Schema schema, InputStream input) throws IOException {
        JsonToken dataFieldToken = parser.nextToken();
        isTrue(FIELD_NAME == dataFieldToken, invalidInputMessage(FIELD_NAME,dataFieldToken));
        isTrue(Objects.equals(DATA_FIELD_NAME, parser.getText()), invalidInputMessage(DATA_FIELD_NAME, parser.getText()));

        JsonToken dataArrayStartToken = parser.nextToken();
        isTrue(START_ARRAY == dataArrayStartToken, invalidInputMessage(START_ARRAY,dataArrayStartToken));
        JsonToken firstDataObjectStartToken = parser.nextToken();
        isTrue(START_OBJECT == firstDataObjectStartToken, invalidInputMessage(START_OBJECT,firstDataObjectStartToken));

        Iterator<ObjectNode> objectNodeIterator = parser.readValuesAs(ObjectNode.class);
        GenericDatumReader<GenericRecord> reader = new GenericDatumReader<>(schema);

        return new Iterator<IndexedRecord>() {

            @Override
            public boolean hasNext() {
                return objectNodeIterator.hasNext();
            }

            @Override
            public IndexedRecord next() {
                try {
                    ObjectNode next = objectNodeIterator.next();
                    return reader.read(null, DecoderFactory.get().jsonDecoder(schema, mapper.writeValueAsString(next)));
                } catch (IOException e) {
                    throw new RuntimeException("Error while reading avro data.", e);
                }
            }
        };
    }

    private static Schema readAvroSchema(JsonParser parser) throws IOException {
        JsonToken avroSchemaFieldToken = parser.nextToken();
        isTrue(FIELD_NAME == avroSchemaFieldToken, invalidInputMessage(FIELD_NAME,avroSchemaFieldToken));
        isTrue(Objects.equals(AVRO_SCHEMA_FIELD_NAME, parser.getText()), invalidInputMessage(AVRO_SCHEMA_FIELD_NAME,parser.getText()));

        JsonToken configFieldObjectStartToken = parser.nextToken();
        isTrue(START_OBJECT == configFieldObjectStartToken, invalidInputMessage(START_OBJECT,configFieldObjectStartToken));

        // This code is so awful I will certainly have cancer
        ObjectNode schemaAsJson = parser.readValueAsTree();
        Schema.Parser avroSchemaParser = new Schema.Parser();
        return avroSchemaParser.parse(new ObjectMapper().writeValueAsString(schemaAsJson));
    }

    private static UiSpecsPropertiesDto readConfiguration(JsonParser parser) throws IOException {
        JsonToken configFieldToken = parser.nextToken();
        isTrue(FIELD_NAME == configFieldToken, invalidInputMessage(FIELD_NAME,configFieldToken));
        isTrue(Objects.equals(CONFIGURATION_FIELD_NAME, parser.getText()), invalidInputMessage(CONFIGURATION_FIELD_NAME,parser.getText()));

        JsonToken avroSchemaFieldObjectStartToken = parser.nextToken();
        isTrue(START_OBJECT == avroSchemaFieldObjectStartToken, invalidInputMessage(START_OBJECT,avroSchemaFieldObjectStartToken));

        return parser.readValueAs(UiSpecsPropertiesDto.class);
    }
    
    private static String invalidInputMessage(JsonToken expected, JsonToken actual) {
        return "Invalid input, expected " + expected + " but got " + actual;
    }

    
    private static String invalidInputMessage(String expected, String actual) {
        return "Invalid input, expected " + expected + " but got " + actual;
    }

    public UiSpecsPropertiesDto getConfiguration() {
        return configuration;
    }

    public Iterator<IndexedRecord> getData() {
        return data;
    }

    @Override
    public void close() throws IOException {
        if (resource != null) {
            resource.close();
        }
    }

}
