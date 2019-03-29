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

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import io.restassured.RestAssured;

import java.util.Collections;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.talend.components.service.rest.ServiceConstants;
import org.talend.components.service.rest.dto.SerPropertiesDto;
import org.talend.components.service.rest.fullexample.dataset.FullExampleDatasetProperties;
import org.talend.components.service.rest.fullexample.dataset.FullExampleDatasetProperties.SourceType;
import org.talend.components.service.rest.fullexample.datastore.FullExampleDatastoreProperties;
import org.talend.components.service.spring.SpringTestApp;
import org.talend.daikon.properties.test.PropertiesTestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

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

    protected String getVersionPrefix() {
        return ServiceConstants.V0;
    }

    @Test
    public void initializeFullExampleDatastoreProperties() throws java.io.IOException {
        SerPropertiesDto serProperties = new SerPropertiesDto();
        serProperties.setProperties(new FullExampleDatastoreProperties("").init().toSerialized());

        given().body(serProperties).contentType(ServiceConstants.JSONIO_CONTENT_TYPE) //
                .accept(ServiceConstants.UI_SPEC_CONTENT_TYPE) //
                .expect().statusCode(200).log().ifError() //
                .when()//
                .post(getVersionPrefix() + "/properties/uispec")//
                .then()//
                .body("jsonSchema", notNullValue())//
                .body("properties", notNullValue())//
                .body("uiSchema", notNullValue())//
                .body("properties.@definitionName", equalTo("FullExampleDatastore"))//
        ;
    }

    @Test
    public void initializeFullExampleDatasetProperties() throws java.io.IOException {
        SerPropertiesDto serPropertiesDto = createDatasetDatastoreSerPropertiesDto();

        given().body(serPropertiesDto).contentType(ServiceConstants.JSONIO_CONTENT_TYPE) //
                .accept(ServiceConstants.UI_SPEC_CONTENT_TYPE) //
                .expect().statusCode(200).log().ifError() //
                .when()//
                .post(getVersionPrefix() + "/properties/uispec")//
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
        SerPropertiesDto serPropertiesDto = createDatasetDatastoreSerPropertiesDto();
        given().body(serPropertiesDto).contentType(ServiceConstants.JSONIO_CONTENT_TYPE) //
                .accept(ServiceConstants.UI_SPEC_CONTENT_TYPE) //
                .expect().statusCode(200).log().ifError() //
                .when()//
                .post(getVersionPrefix() + "/properties/uispec")//
                .then()//
                .body("jsonSchema", notNullValue())//
                .body("properties", notNullValue())//
                .body("uiSchema", notNullValue())//
                .body("properties.testAfterDatastoreTrigger", equalTo("bar"))//
        ;
    }

    private SerPropertiesDto createDatasetDatastoreSerPropertiesDto() {
        FullExampleDatastoreProperties datastoreProperties = new FullExampleDatastoreProperties("foo");
        datastoreProperties.init();
        datastoreProperties.tag.setValue("DERBY");
        FullExampleDatasetProperties datasetProperties = new FullExampleDatasetProperties("bar");
        datasetProperties.init();
        datasetProperties.sourceType.setValue(SourceType.SOQL_QUERY);
        datasetProperties.moduleName.setValue("Account");
        datasetProperties.query.setValue("SELECT * FROM users");
        SerPropertiesDto serPropertiesDto = new SerPropertiesDto();
        serPropertiesDto.setProperties(datasetProperties.toSerialized());
        serPropertiesDto.setDependencies(Collections.singletonList(datastoreProperties.toSerialized()));
        return serPropertiesDto;
    }

}
