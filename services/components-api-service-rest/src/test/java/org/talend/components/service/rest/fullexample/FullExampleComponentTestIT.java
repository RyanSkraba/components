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
package org.talend.components.service.rest.fullexample;

import static com.jayway.restassured.RestAssured.given;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.talend.components.service.rest.dto.PropertiesDto;
import org.talend.components.service.spring.SpringTestApp;
import org.talend.daikon.properties.test.PropertiesTestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.restassured.RestAssured;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringTestApp.class, webEnvironment = RANDOM_PORT)
@TestPropertySource(properties = { "server.contextPath=" })
public class FullExampleComponentTestIT {

    @LocalServerPort
    private int localServerPort;

    public static final String DATA_STORE_DEFINITION_NAME = "FullExampleDatastore";

    private ObjectMapper mapper = new ObjectMapper();

    @BeforeClass
    public static void registerPaxUrlMavenHandler() {
        PropertiesTestUtils.setupPaxUrlFromMavenLaunch();
    }

    @Before
    public void setUp() throws Exception {
        RestAssured.port = localServerPort;
    }

    @After
    public void tearDown() {

    }

    private ObjectNode getFullExampleDataStoreProperties() throws java.io.IOException {
        return mapper.readValue(IOUtils.toString(getClass().getResourceAsStream("fullexample_data_store_properties.json")),
                ObjectNode.class);
    }

    @Test
    public void initializeFullExampleDatastoreProperties() throws java.io.IOException {
        PropertiesDto properties = new PropertiesDto();
        properties.setProperties(getFullExampleDataStoreProperties());

        given().content(properties).contentType(APPLICATION_JSON_UTF8_VALUE) //
                .accept(APPLICATION_JSON_UTF8_VALUE) //
                .expect().statusCode(200).log().ifError() //
                .when()//
                .post("properties/{definitionName}", DATA_STORE_DEFINITION_NAME)//
                .then()//
                .body("jsonSchema", notNullValue())//
                .body("properties", notNullValue())//
                .body("uiSchema", notNullValue())//
                .body("properties.@definitionName", equalTo("FullExampleDatastore"))//
        ;
    }

    @Test
    public void initializeFullExampleDatasetProperties() throws java.io.IOException {
        PropertiesDto propertiesDto = new PropertiesDto();
        propertiesDto.setProperties(getFileAsObjectNode("fullexample_dataset_properties.json"));
        propertiesDto.setDependencies(singletonList(getFullExampleDataStoreProperties()));
        String dataSetDefinitionName = "FullExampleDataset";

        given().content(propertiesDto).contentType(APPLICATION_JSON_UTF8_VALUE) //
                .accept(APPLICATION_JSON_UTF8_VALUE) //
                .expect().statusCode(200).log().ifError() //
                .when()//
                .post("properties/{definitionName}", dataSetDefinitionName)//
                .then()//
                .body("jsonSchema", notNullValue())//
                .body("properties", notNullValue())//
                .body("uiSchema", notNullValue())//
                .body("properties.@definitionName", equalTo("FullExampleDataset"))//
                .body("uiSchema.moduleName.'ui:widget'", equalTo("hidden"))//
                .body("uiSchema.query.'ui:widget'", equalTo("textarea"))//
        ;
    }

    private ObjectNode getFileAsObjectNode(String file) throws java.io.IOException {
        return mapper.readerFor(ObjectNode.class).readValue(getClass().getResourceAsStream(file));
    }

    @Test
    public void testAfterDatastoreCalled() throws java.io.IOException {
        PropertiesDto propertiesDto = new PropertiesDto();
        propertiesDto.setProperties(getFileAsObjectNode("fullexample_dataset_properties.json"));
        propertiesDto.setDependencies(singletonList(getFullExampleDataStoreProperties()));
        String dataSetDefinitionName = "FullExampleDataset";

        given().content(propertiesDto).contentType(APPLICATION_JSON_UTF8_VALUE) //
                .accept(APPLICATION_JSON_UTF8_VALUE) //
                .expect().statusCode(200).log().ifError() //
                .when()//
                .post("properties/{definitionName}", dataSetDefinitionName)//
                .then()//
                .body("jsonSchema", notNullValue())//
                .body("properties", notNullValue())//
                .body("uiSchema", notNullValue())//
                .body("properties.testAfterDatastoreTrigger", equalTo("bar"))//
        ;
    }

}
