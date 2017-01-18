//==============================================================================
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
//==============================================================================

package org.talend.components.service.rest.impl;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.restassured.response.Response;
import org.junit.Test;
import org.talend.components.service.rest.AbstractSpringIntegrationTests;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

public class PropertiesControllerImplTest extends AbstractSpringIntegrationTests {

    @Test
    public void testGetProperties() throws Exception {
        given().accept(APPLICATION_JSON_UTF8_VALUE) //
                .expect() //
                .statusCode(200).log().ifError() //
                .with().port(localServerPort) //
                .get("/properties/{name}", DATA_STORE_DEFINITION_NAME);
    }

    @Test
    public void testValidateProperties() throws Exception {
        ObjectNode validationResult = given().accept(APPLICATION_JSON_UTF8_VALUE) //
                .expect() //
                .statusCode(200).log().ifError() //
                .with().port(localServerPort) //
                .content(buildTestDataStoreFormData()) //
                .contentType(APPLICATION_JSON_UTF8_VALUE) //
                .post("/properties/{name}/validate", DATA_STORE_DEFINITION_NAME).as(ObjectNode.class);
        assertNotNull(validationResult);
        assertEquals("OK", validationResult.get("status").textValue());
    }

    @Test
    public void testTriggerOnProperty() throws Exception {
        String callback = "validate";
        String propName = "tagId";
        Response response = given().accept(APPLICATION_JSON_UTF8_VALUE) //
                .expect() //
                .statusCode(200).log().ifError() //
                .with().port(localServerPort) //
                .content(buildTestDataStoreFormData()) //
                .contentType(APPLICATION_JSON_UTF8_VALUE) //
                .post("/properties/{name}/{callback}/{propName}", DATA_STORE_DEFINITION_NAME, callback, propName);
        assertNotNull(response);
        String content = response.asString();
        assertNotNull(content);
    }

    @Test
    public void testTriggerOnProperty_nonExistentTrigger() throws Exception {
        String callback = "toto";
        String propName = "tagId";
        ApiError errorContainer = given().accept(APPLICATION_JSON_UTF8_VALUE) //
                .expect() //
                .statusCode(400)
                .with()
                .port(localServerPort) //
                .content(buildTestDataStoreFormData()) //
                .contentType(APPLICATION_JSON_UTF8_VALUE) //
                .post("/properties/{name}/{callback}/{propName}", DATA_STORE_DEFINITION_NAME, callback, propName)
                .as(ApiError.class);

        assertEquals("Talend_ALL_UNEXPECTED_ARGUMENT", errorContainer.getCode());
        assertNotNull(errorContainer.getMessageTitle());
        assertNotNull(errorContainer.getMessage());
    }

    @Test
    public void testGetDatasetProperties() throws Exception {
        given().accept(APPLICATION_JSON_UTF8_VALUE) //
                .expect() //
                .statusCode(200).log().ifError() //
                .with().port(localServerPort) //
                .content(buildTestDataStoreFormData()) //
                .contentType(APPLICATION_JSON_UTF8_VALUE) //
                .post("/properties/{name}/dataset", DATA_STORE_DEFINITION_NAME);
    }

    @Test
    public void testGetDatasetProperties_wrongDataStoreName() throws Exception {
        String dataStoreName = "toto";
        ApiError errorContainer = given().accept(APPLICATION_JSON_UTF8_VALUE) //
                .expect() //
                .statusCode(500) //
                .with()
                .port(localServerPort) //
                .content(buildTestDataStoreFormData()) //
                .contentType(APPLICATION_JSON_UTF8_VALUE) //
                .post("/properties/{name}/dataset", dataStoreName).as(ApiError.class);

        assertEquals("Talend_ALL_UNEXPECTED_EXCEPTION", errorContainer.getCode());
        assertNotNull(errorContainer.getMessageTitle());
        assertNotNull(errorContainer.getMessage());
    }
}
