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

package org.talend.components.service.rest.impl;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.IMAGE_PNG_VALUE;
import static org.talend.components.service.rest.PropertiesController.IMAGE_SVG_VALUE;

import java.io.IOException;
import java.util.Locale;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.talend.components.service.rest.AbstractSpringIntegrationTests;
import org.talend.daikon.definition.DefinitionImageType;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.restassured.response.Response;

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
    public void testGetIcon_imageTypeNotFound() throws Exception {
        given().expect() //
                .statusCode(404) //
                .log().ifValidationFails() //
                .get("/properties/{name}/icon/{type}", DATA_STORE_DEFINITION_NAME, DefinitionImageType.SVG_ICON);
    }

    @Test
    public void testGetIcon_pngImageTypeFound() throws Exception {
        given().expect() //
                .contentType(IMAGE_PNG_VALUE) //
                .body(Matchers.startsWith("\uFFFDPNG")) // Magic header for PNG files.
                .statusCode(200).log().ifError() //
                .get("/properties/{name}/icon/{type}", DATA_STORE_DEFINITION_NAME, DefinitionImageType.PALETTE_ICON_32X32);
    }

    @Test
    public void testGetIcon_svgImageTypeFound() throws Exception {
        given().expect() //
                .statusCode(200).log().ifError() //
                .body(Matchers.containsString("</svg>")) //
                .contentType(IMAGE_SVG_VALUE) //
                .get("/properties/{name}/icon/{type}", DATA_SET_DEFINITION_NAME, DefinitionImageType.SVG_ICON);
    }

    @Test
    public void testGetConnectors_badDefinition() throws Exception {
        given().expect() //
                .statusCode(400) //
                .log().ifValidationFails() //
                // TODO: check returned error
                .body(Matchers.containsString("definitionClass")) //
                .get("/properties/{name}/connectors", DATA_SET_DEFINITION_NAME);
    }

    @Test
    public void testValidateProperties() throws Exception {
        ObjectNode validationResult = given().accept(APPLICATION_JSON_UTF8_VALUE) //
                .expect() //
                .statusCode(200).log().ifError() //
                .with() //
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
                .with() //
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
                .log().ifValidationFails() //
                .with() //
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
                .with() //
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
                .log().ifValidationFails() //
                .with() //
                .content(buildTestDataStoreFormData()) //
                .contentType(APPLICATION_JSON_UTF8_VALUE) //
                .post("/properties/{name}/dataset", dataStoreName).as(ApiError.class);

        assertEquals("Talend_ALL_UNEXPECTED_EXCEPTION", errorContainer.getCode());
        assertNotNull(errorContainer.getMessageTitle());
        assertNotNull(errorContainer.getMessage());
    }

    @Test
    public void testGetProperties_internationalized() throws Exception {
        Locale.setDefault(Locale.US);
        assertEquals("Database dataset ENGLISH display name", getDataStoreDefinitionPropertiesTitle(Locale.ENGLISH));
        assertEquals("Database dataset ENGLISH display name", getDataStoreDefinitionPropertiesTitle(Locale.CHINA));
        assertEquals("Database dataset FRENCH display name", getDataStoreDefinitionPropertiesTitle(Locale.FRENCH));
        assertEquals("Database dataset FRENCH display name", getDataStoreDefinitionPropertiesTitle(Locale.FRANCE));
    }

    private String getDataStoreDefinitionPropertiesTitle(Locale locale) throws IOException {
        Response response = given().accept(APPLICATION_JSON_UTF8_VALUE) //
                .expect() //
                .statusCode(200).log().ifError() //
                .with().port(localServerPort) //
                .header(HttpHeaders.ACCEPT_LANGUAGE, locale.toLanguageTag())
                .get("/properties/{name}", DATA_STORE_DEFINITION_NAME);

        JsonNode jsonNode = mapper.readTree(response.asInputStream());
        return jsonNode.get("jsonSchema").get("title").asText();
    }
}
