/*
 * Copyright (C) 2006-2015 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package org.talend.components.service.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.wizard.WizardImageType;
import org.talend.components.fullexample.FullExampleInputDefinition;
import org.talend.components.fullexample.FullExampleProperties;
import org.talend.components.service.rest.serialization.JsonSerializationHelper;
import org.talend.daikon.properties.Properties;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.IMAGE_PNG_VALUE;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
public class DataStoreControllerIT {

    private static final Logger log = LoggerFactory.getLogger(DataStoreControllerIT.class);

    private JsonSerializationHelper jsonSerializationHelper = new JsonSerializationHelper();

    @LocalServerPort
    private int localServerPort;

    @Autowired
    private ComponentService componentService;

    @Test
    public void listDataStoreDefinitions() throws Exception {
        Response fullExampleDatastore = given().accept(ContentType.JSON) //
                .expect().statusCode(200).log().ifError() //
                .with().port(localServerPort).get("/datastoresDefinitions");
        log.error("datastore list response:" + fullExampleDatastore.asString());
    }

    @Test
    public void getDatastoreDefinition() throws Exception {
        String dataStoreName = "FullExampleDatastore";
        Response response = given().accept(ContentType.JSON) //
                .expect() //
                .statusCode(200).log().ifError() //
                .with().port(localServerPort) //
                .get("/datastoresDefinitions/{dataStoreName}/properties", dataStoreName);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(response.asString());
        log.error("datastore properties:" + jsonNode);
        JsonNode jsonSchemaNode = jsonNode.get("jsonSchema");
        assertNotNull(jsonSchemaNode);
        assertEquals("object", jsonSchemaNode.get("type").textValue());
        // {"jsonSchema":"{\"jsonSchema\":{\"type\":\"object\",\"properties\":{\"tag\":{\"type\":\"string\"},\"tagId\":{\"type\":\"integer\"}}},\"properties\":{},\"uiSchema\":{\"ui:order\":[\"tag\",\"tagId\"]}}","uiSchema":null,"formData":null}
    }

    @Test
    public void getWizardImageRest() throws Exception {
        String dataStoreName = "FullExampleDatastore";
        Response response = given().accept(IMAGE_PNG_VALUE) //
                .expect() //
                .statusCode(200).log().ifError() //
                .with().port(localServerPort) //
                .get("/components/wizards/{name}/icon/{type}", dataStoreName, WizardImageType.TREE_ICON_16X16);

        log.error("datastore properties:" + response.asString());
    }

    @Test
    public void checkDatastoreConnection() throws Exception {
        String dataStoreName = "FullExampleDatastore";

        FullExampleProperties cp = (FullExampleProperties) componentService.getComponentProperties(
                FullExampleInputDefinition.COMPONENT_NAME);

        Properties properties = cp.getProperties("stringProp");

        given().accept(ContentType.JSON) //
                .expect() //
                .statusCode(200)
                .log()
                .ifError() //
                .with().port(localServerPort) //
                .body(jsonSerializationHelper.toJson(properties)) //
                .contentType(ContentType.JSON) //
                .post("/runtimes/datastores/{dataStoreName}", dataStoreName);
    }

}
