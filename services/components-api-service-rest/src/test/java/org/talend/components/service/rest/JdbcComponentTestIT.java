// ==============================================================================
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
// ==============================================================================

package org.talend.components.service.rest;

import static com.jayway.restassured.RestAssured.given;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.talend.components.service.rest.dto.DefinitionDTO;
import org.talend.components.service.rest.dto.PropertiesDto;
import org.talend.components.service.rest.impl.ApiError;
import org.talend.daikon.properties.test.PropertiesTestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@TestPropertySource(properties = { "server.contextPath=" })
public class JdbcComponentTestIT {

    private static final Logger log = LoggerFactory.getLogger(JdbcComponentTestIT.class);

    public static final String DATA_STORE_DEFINITION_NAME = "JDBCDatastore";

    @LocalServerPort
    private int localServerPort;

    protected EmbeddedDatabase db;

    protected String dbUrl;

    private ObjectMapper mapper = new ObjectMapper();

    @BeforeClass
    public static void registerPaxUrlMavenHandler() {
        PropertiesTestUtils.setupPaxUrlFromMavenLaunch();
    }

    @Before
    public void setUp() throws Exception {
        db = new EmbeddedDatabaseBuilder().generateUniqueName(true).setType(EmbeddedDatabaseType.DERBY).setName("testdb")
                .setScriptEncoding("UTF-8").addScript("/org/talend/components/service/rest/schema.sql")
                .addScript("/org/talend/components/service/rest/data_users.sql").build();
        // addresss: Starting embedded database:
        // url='jdbc:derby:memory:2dc86c66-5d3a-48fd-b903-56aa27d20e3b;create=true',
        // username='sa'
        try (Connection connection = db.getConnection()) {
            dbUrl = connection.getMetaData().getURL();
        }

        RestAssured.port = localServerPort;
    }

    @After
    public void tearDown() {
        db.shutdown();
    }

    @Test
    public void testGetDataBinary() throws java.io.IOException {
        // given
        PropertiesDto propertiesDto = new PropertiesDto();
        propertiesDto.setProperties(getFileAsObjectNode("jdbc_data_set_properties_with_schema.json"));
        propertiesDto.setDependencies(singletonList(getJdbcDataStoreProperties()));

        String dataSetDefinitionName = "JDBCDataset";

        // when
        Response schemaResponse = given().content(propertiesDto).contentType(APPLICATION_JSON_UTF8_VALUE) //
                .accept(APPLICATION_JSON_UTF8_VALUE) //
                .expect().statusCode(200).log().ifError() //
                .post("runtimes/{definitionName}/schema", dataSetDefinitionName);

        Schema schema = new Schema.Parser().parse(schemaResponse.asInputStream());

        Response response = given().content(propertiesDto).contentType(APPLICATION_JSON_UTF8_VALUE) //
                .accept(RuntimesController.AVRO_BINARY_MIME_TYPE_OFFICIAL_INVALID) //
                .expect().statusCode(200).log().ifError() //
                .post("runtimes/{definitionName}/data", dataSetDefinitionName);

        // then
        GenericDatumReader<GenericRecord> reader = new GenericDatumReader<>(schema);
        DecoderFactory decoderFactory = DecoderFactory.get();
        Decoder decoder = decoderFactory.binaryDecoder(response.asInputStream(), null);
        assertRecordsEqualsToTestValues(reader, decoder);
    }

    @Test
    public void testGetData() throws java.io.IOException {
        // given
        PropertiesDto propertiesDto = new PropertiesDto();
        propertiesDto.setProperties(getFileAsObjectNode("jdbc_data_set_properties_with_schema.json"));
        propertiesDto.setDependencies(singletonList(getJdbcDataStoreProperties()));

        String dataSetDefinitionName = "JDBCDataset";

        Response schemaResponse = given().content(propertiesDto).contentType(APPLICATION_JSON_UTF8_VALUE) //
                .accept(APPLICATION_JSON_UTF8_VALUE) //
                .expect().statusCode(200).log().ifError() //
                .post("runtimes/{definitionName}/schema", dataSetDefinitionName);

        Schema schema = new Schema.Parser().parse(schemaResponse.asInputStream());

        // when
        Response response = given().content(propertiesDto).contentType(APPLICATION_JSON_UTF8_VALUE) //
                .accept(RuntimesController.AVRO_JSON_MIME_TYPE_OFFICIAL_INVALID) //
                .expect().statusCode(200).log().ifError() //
                .post("runtimes/{definitionName}/data", dataSetDefinitionName);

        // then
        GenericDatumReader<GenericRecord> reader = new GenericDatumReader<>(schema);
        DecoderFactory decoderFactory = DecoderFactory.get();
        Decoder decoder = decoderFactory.jsonDecoder(schema, response.asInputStream());

        assertRecordsEqualsToTestValues(reader, decoder);
    }

    private void assertRecordsEqualsToTestValues(GenericDatumReader<GenericRecord> reader, Decoder decoder) throws IOException {
        try (Stream<String[]> insertedValues = getInsertedValues()) {
            insertedValues.forEach((value) -> {
                try {
                    GenericRecord record = reader.read(null, decoder);
                    assertRecordEquals(value, record);
                } catch (IOException e) {
                    // When reading is done...
                }
            });
        }
    }

    private void assertRecordEquals(String[] expected, GenericRecord record) {
        String errorMessage = "Record " + record + " is incorrect. Expected " + Arrays.toString(expected);
        assertEquals(errorMessage, expected[0], record.get(0).toString());
        assertEquals(errorMessage, expected[1], record.get(1).toString());
        assertEquals(errorMessage, expected[2], record.get(2).toString());
        assertEquals(errorMessage, expected[3], record.get(3).toString());
        assertEquals(errorMessage, expected[4], record.get(4).toString());
        assertEquals(errorMessage, expected[5], record.get(5).toString());
    }

    /**
     * Quick and dirty to parse the test SQL for the inserted records. The stream must be closed to close the source.
     **/
    private Stream<String[]> getInsertedValues() throws IOException {
        BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(getClass().getResourceAsStream("data_users.sql")));
        return bufferedReader.lines().map(in -> in.substring(in.indexOf("VALUES (") + "VALUES (".length(), in.lastIndexOf("');")))
                .map(in -> in.split("'?\\s*,\\s*'?"));
    }

    @Test
    public void testGetSchema() throws java.io.IOException {
        // given
        PropertiesDto propertiesDto = new PropertiesDto();
        propertiesDto.setProperties(getFileAsObjectNode("jdbc_data_set_properties_no_schema.json"));
        propertiesDto.setDependencies(singletonList(getJdbcDataStoreProperties()));
        String dataSetDefinitionName = "JDBCDataset";

        // when
        Response response = given().content(propertiesDto).contentType(APPLICATION_JSON_UTF8_VALUE) //
                .accept(APPLICATION_JSON_UTF8_VALUE) //
                .expect().statusCode(200).log().ifError() //
                .post("runtimes/{definitionName}/schema", dataSetDefinitionName);

        // then
        ObjectNode result = getResponseAsObjectNode(response);
        ObjectNode expected = getFileAsObjectNode("jdbc_data_set_schema.json");
        assertEquals(expected, result);
    }

    @Test
    public void testGetSchema_wrongSql() throws java.io.IOException {
        // given

        PropertiesDto datasetConnectionInfo = new PropertiesDto();
        datasetConnectionInfo.setProperties(mapper.readValue(
                getClass().getResourceAsStream("jdbc_data_set_properties_no_schema_wrong_table_name.json"), ObjectNode.class));
        datasetConnectionInfo.setDependencies(singletonList(getJdbcDataStoreProperties()));
        String dataSetDefinitionName = "JDBCDataset";

        // when
        ApiError response = given().content(datasetConnectionInfo).contentType(APPLICATION_JSON_UTF8_VALUE) //
                .accept(APPLICATION_JSON_UTF8_VALUE) //
                .expect().statusCode(400).log().ifValidationFails() //
                .post("runtimes/{definitionName}/schema", dataSetDefinitionName).as(ApiError.class);

        // then
        assertEquals("TCOMP_JDBC_SQL_SYNTAX_ERROR", response.getCode());
        assertEquals("Table/View 'TOTO' does not exist.", response.getMessage());
    }

    @Test
    public void getJdbcDataSetProperties() throws java.io.IOException {
        // given
        PropertiesDto properties = new PropertiesDto();
        properties.setProperties(getJdbcDataStoreProperties());

        // when
        Response response = given().content(properties).contentType(APPLICATION_JSON_UTF8_VALUE) //
                .accept(APPLICATION_JSON_UTF8_VALUE) //
                .expect().statusCode(200).log().ifError() //
                .post("properties/{definitionName}/dataset", DATA_STORE_DEFINITION_NAME);

        // then
        ObjectNode dataSetProperties = mapper.readerFor(ObjectNode.class).readValue(response.asInputStream());
        assertNotNull(dataSetProperties);
    }

    @Test
    public void validateDataStoreConnection() throws java.io.IOException {
        // given
        PropertiesDto properties = new PropertiesDto();
        properties.setProperties(getJdbcDataStoreProperties());

        // when
        given().content(properties).contentType(APPLICATION_JSON_UTF8_VALUE) //
                .accept(APPLICATION_JSON_UTF8_VALUE) //
                .expect().statusCode(200).log().ifError() //
                .post("runtimes/{definitionName}", DATA_STORE_DEFINITION_NAME);
    }

    private ObjectNode getJdbcDataStoreProperties() throws java.io.IOException {
        return mapper.readValue(
                IOUtils.toString(getClass().getResourceAsStream("jdbc_data_store_properties.json")).replace("JDBC_URL", dbUrl),
                ObjectNode.class);
    }

    @Test
    public void testTrigger() throws java.io.IOException {
        // given
        String triggerName = "after";
        String triggerProperty = "dbTypes";

        PropertiesDto properties = new PropertiesDto();
        properties.setProperties(getFileAsObjectNode("jdbc_data_store_properties.json"));

        // when
        Response response = given().content(properties).contentType(APPLICATION_JSON_UTF8_VALUE) //
                .accept(APPLICATION_JSON_UTF8_VALUE) //
                .expect().statusCode(200).log().ifError() //
                .post("properties/{definition}/{trigger}/{property}", DATA_STORE_DEFINITION_NAME, triggerName, triggerProperty);

        ObjectNode jdbcPropertiesAfterTrigger = getResponseAsObjectNode(response);

        // then
        // should resemble jdbc_data_store_form_after_trigger.json
        assertNotNull(jdbcPropertiesAfterTrigger.get("jsonSchema"));
        assertNotNull(jdbcPropertiesAfterTrigger.get("properties"));
        assertNotNull(jdbcPropertiesAfterTrigger.get("uiSchema"));
        assertEquals("JDBCDatastore", jdbcPropertiesAfterTrigger.get("properties").get("@definitionName").textValue());
    }

    @Test
    public void getJdbcProperties() throws java.io.IOException {
        // when
        Response response = given().accept(APPLICATION_JSON_UTF8_VALUE) //
                .expect().statusCode(200).log().ifError() //
                .get("properties/{definitionName}", DATA_STORE_DEFINITION_NAME);

        // then
        ObjectNode jdbcProperties = mapper.readerFor(ObjectNode.class).readValue(response.asInputStream());
        // should resemble jdbc_data_store_form.json
        assertNotNull(jdbcProperties.get("jsonSchema"));
        assertNotNull(jdbcProperties.get("properties"));
        assertNotNull(jdbcProperties.get("uiSchema"));
        assertEquals("JDBCDatastore", jdbcProperties.get("properties").get("@definitionName").textValue());
    }

    @Test
    public void initializeJDBCDatastoreProperties() throws java.io.IOException {
        // given
        PropertiesDto properties = new PropertiesDto();
        properties.setProperties(getJdbcDataStoreProperties());

        // when
        Response response = given().content(properties).contentType(APPLICATION_JSON_UTF8_VALUE) //
                .accept(APPLICATION_JSON_UTF8_VALUE) //
                .expect().statusCode(200).log().ifError() //
                .post("properties/{definitionName}", DATA_STORE_DEFINITION_NAME);

        // then
        ObjectNode jdbcProperties = mapper.readerFor(ObjectNode.class).readValue(response.asInputStream());
        // should resemble jdbc_data_store_form.json
        assertNotNull(jdbcProperties.get("jsonSchema"));
        assertNotNull(jdbcProperties.get("properties"));
        assertNotNull(jdbcProperties.get("uiSchema"));
        assertEquals("JDBCDatastore", jdbcProperties.get("properties").get("@definitionName").textValue());
    }

    @Test
    public void initializeJDBCDatasetProperties() throws java.io.IOException {
        // given
        PropertiesDto propertiesDto = new PropertiesDto();
        propertiesDto.setProperties(getFileAsObjectNode("jdbc_data_set_properties_no_schema.json"));
        propertiesDto.setDependencies(singletonList(getJdbcDataStoreProperties()));
        String dataSetDefinitionName = "JDBCDataset";

        // when
        Response response = given().content(propertiesDto).contentType(APPLICATION_JSON_UTF8_VALUE) //
                .accept(APPLICATION_JSON_UTF8_VALUE) //
                .expect().statusCode(200).log().ifError() //
                .post("properties/{definitionName}", dataSetDefinitionName);

        // then
        ObjectNode jdbcProperties = mapper.readerFor(ObjectNode.class).readValue(response.asInputStream());
        assertNotNull(jdbcProperties.get("jsonSchema"));
        assertNotNull(jdbcProperties.get("properties"));
        assertNotNull(jdbcProperties.get("uiSchema"));
        assertEquals("JDBCDataset", jdbcProperties.get("properties").get("@definitionName").textValue());
    }

    @Test
    public void getJdbcDefinition() throws java.io.IOException {
        // when
        Response response = given().accept(APPLICATION_JSON_UTF8_VALUE) //
                .expect() //
                .statusCode(200).log().ifError() //
                .get("/definitions/DATA_STORE");

        // then
        List<DefinitionDTO> definitions = mapper
                .readerFor(TypeFactory.defaultInstance().constructCollectionType(List.class, DefinitionDTO.class))
                .readValue(response.asInputStream());

        DefinitionDTO jdbcDef = null;

        for (DefinitionDTO definition : definitions) {
            if (DATA_STORE_DEFINITION_NAME.equals(definition.getName())) {
                jdbcDef = definition;
                break;
            }
        }
        assertNotNull(jdbcDef);
    }

    private ObjectNode getResponseAsObjectNode(Response response) throws java.io.IOException {
        return mapper.readerFor(ObjectNode.class).readValue(response.asInputStream());
    }

    private ObjectNode getFileAsObjectNode(String file) throws java.io.IOException {
        return mapper.readerFor(ObjectNode.class).readValue(getClass().getResourceAsStream(file));
    }

}
