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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;
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
import org.talend.components.service.rest.Application;
import org.talend.components.service.rest.dto.PropertiesDto;
import org.talend.daikon.properties.test.PropertiesTestUtils;

import static com.jayway.restassured.RestAssured.given;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
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
        return mapper.readValue(
                IOUtils.toString(getClass().getResourceAsStream("fullexample_data_store_properties.json")),
                ObjectNode.class);
    }

    @Test
    public void initializeFullExampleDatastoreProperties() throws java.io.IOException {
        // given
        PropertiesDto properties = new PropertiesDto();
        properties.setProperties(getFullExampleDataStoreProperties());

        // when
        Response response = given().content(properties).contentType(APPLICATION_JSON_UTF8_VALUE) //
                .accept(APPLICATION_JSON_UTF8_VALUE) //
                .expect().statusCode(200).log().ifError() //
                .post("properties/{definitionName}", DATA_STORE_DEFINITION_NAME);

        // then
        ObjectNode fullexampleProperties = mapper.readerFor(ObjectNode.class).readValue(response.asInputStream());
        // should resemble fullexample_data_store_form.json
        assertNotNull(fullexampleProperties.get("jsonSchema"));
        assertNotNull(fullexampleProperties.get("properties"));
        assertNotNull(fullexampleProperties.get("uiSchema"));
        assertEquals("FullExampleDatastore", fullexampleProperties.get("properties").get("@definitionName").textValue());
    }

    @Test
    public void initializeFullExampleDatasetProperties() throws java.io.IOException {
        // given
        PropertiesDto propertiesDto = new PropertiesDto();
        propertiesDto.setProperties(getFileAsObjectNode("fullexample_dataset_properties.json"));
        propertiesDto.setDependencies(singletonList(getFullExampleDataStoreProperties()));
        String dataSetDefinitionName = "FullExampleDataset";

        // when
        Response response = given().content(propertiesDto).contentType(APPLICATION_JSON_UTF8_VALUE) //
                .accept(APPLICATION_JSON_UTF8_VALUE) //
                .expect().statusCode(200).log().ifError() //
                .post("properties/{definitionName}", dataSetDefinitionName);

        // then
        ObjectNode fullexampleProperties = mapper.readerFor(ObjectNode.class).readValue(response.asInputStream());
        assertNotNull(fullexampleProperties.get("jsonSchema"));
        assertNotNull(fullexampleProperties.get("properties"));
        assertNotNull(fullexampleProperties.get("uiSchema"));
        assertEquals("FullExampleDataset", fullexampleProperties.get("properties").get("@definitionName").textValue());
        assertEquals("hidden", fullexampleProperties.get("uiSchema").get("moduleName").get("ui:widget").textValue());
        assertEquals("textarea", fullexampleProperties.get("uiSchema").get("query").get("ui:widget").textValue());
    }

    private ObjectNode getFileAsObjectNode(String file) throws java.io.IOException {
        return mapper.readerFor(ObjectNode.class).readValue(getClass().getResourceAsStream(file));
    }

}
