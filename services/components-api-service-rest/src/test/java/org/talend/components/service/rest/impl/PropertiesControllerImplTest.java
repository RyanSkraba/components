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

import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.http.MediaType.IMAGE_PNG_VALUE;
import static org.talend.components.service.rest.PropertiesController.IMAGE_SVG_VALUE;
import static org.talend.components.service.rest.configuration.RequestParameterLocaleResolver.LANGUAGE_QUERY_PARAMETER_NAME;

import io.restassured.response.Response;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.Locale;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.talend.components.service.rest.AbstractSpringIntegrationTests;
import org.talend.components.service.rest.ServiceConstants;
import org.talend.components.service.rest.mock.MockDatasetProperties;
import org.talend.daikon.definition.DefinitionImageType;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.serialize.SerializerDeserializer.Deserialized;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class PropertiesControllerImplTest extends AbstractSpringIntegrationTests {

    protected String getVersionPrefix() {
        return ServiceConstants.V0;
    }

    @Test
    public void testGetProperties() throws Exception {
        given().accept(ServiceConstants.UI_SPEC_CONTENT_TYPE) //
                .expect() //
                .statusCode(200).log().ifError() //
                .with().port(localServerPort) //
                .get(getVersionPrefix() + "/properties/{name}", DATA_STORE_DEFINITION_NAME);
    }

    @Test
    public void testGetIcon_imageTypeNotFound() throws Exception {
        given().get(getVersionPrefix() + "/properties/{name}/icon/{type}", DATA_STORE_DEFINITION_NAME,
                        DefinitionImageType.SVG_ICON)
        .then().statusCode(404).log().ifValidationFails();
    }

    @Test
    public void testGetIcon_pngImageTypeFound() throws Exception {
        given().get(getVersionPrefix() + "/properties/{name}/icon/{type}", DATA_STORE_DEFINITION_NAME, DefinitionImageType.PALETTE_ICON_32X32)
               .then().statusCode(200).log().ifError()
               .contentType(IMAGE_PNG_VALUE) //
               .body(Matchers.startsWith("\uFFFDPNG")); // Magic header for PNG files.
    }

    @Test
    public void testGetIcon_svgImageTypeFound() throws Exception {
        given()
                .get(getVersionPrefix() + "/properties/{name}/icon/{type}", DATA_SET_DEFINITION_NAME,
                        DefinitionImageType.SVG_ICON).then() //
                .statusCode(200).log().ifError() //
                .body(Matchers.containsString("</svg>")) //
                .contentType(IMAGE_SVG_VALUE);
    }

    @Test
    public void testGetConnectors_badDefinition() throws Exception {
        given().get(getVersionPrefix() + "/properties/{name}/connectors", DATA_SET_DEFINITION_NAME).then() //
                .statusCode(400) //
                .log().ifValidationFails() //
                // TODO: check returned error
                .body(Matchers.containsString("definitionClass"));
    }

    @Test
    public void testValidateProperties() throws Exception {
        ObjectNode validationResult = given().accept(ServiceConstants.UI_SPEC_CONTENT_TYPE) //
                .expect() //
                .statusCode(200).log().ifError() //
                .with() //
                .content(buildTestDataStoreFormData()) //
                .contentType(ServiceConstants.UI_SPEC_CONTENT_TYPE) //
                .post(getVersionPrefix() + "/properties/validate").as(ObjectNode.class);
        assertNotNull(validationResult);
        assertEquals("OK", validationResult.get("status").textValue());
    }

    @Test
    public void testValidatePropertiesJsonio() throws Exception {
        ObjectNode validationResult = given().accept(ServiceConstants.UI_SPEC_CONTENT_TYPE) //
                .expect() //
                .statusCode(200).log().ifError() //
                .with() //
                .content(buildTestDataStoreSerProps()) //
                .contentType(ServiceConstants.JSONIO_CONTENT_TYPE) //
                .post(getVersionPrefix() + "/properties/validate").as(ObjectNode.class);
        assertNotNull(validationResult);
        assertEquals("OK", validationResult.get("status").textValue());
    }

    @Test
    public void testTriggerOnProperty() throws Exception {
        String callback = "validate";
        String propName = "tagId";
        Response response = given().accept(ServiceConstants.UI_SPEC_CONTENT_TYPE) //
                                   .expect() //
                                   .statusCode(200).log().ifError() //
                                   .with() //
                                   .body(buildTestDataStoreFormData()) //
                                   .contentType(ServiceConstants.UI_SPEC_CONTENT_TYPE) //
                                   .post(getVersionPrefix() + "/properties/trigger/{callback}/{propName}", callback, propName);
        assertNotNull(response);
        String content = response.asString();
        assertNotNull(content);
    }

    @Test
    public void testTriggerOnProperty_nonExistentTrigger() throws Exception {
        String callback = "toto";
        String propName = "tagId";
        ApiError errorContainer = given().accept(ServiceConstants.UI_SPEC_CONTENT_TYPE) //
                .expect() //
                .statusCode(400).log().ifValidationFails() //
                .with() //
                .content(buildTestDataStoreFormData()) //
                .contentType(ServiceConstants.UI_SPEC_CONTENT_TYPE) //
                .post(getVersionPrefix() + "/properties/trigger/{callback}/{propName}", callback, propName).as(ApiError.class);

        assertEquals("Talend_ALL_UNEXPECTED_ARGUMENT", errorContainer.getCode());
        assertNotNull(errorContainer.getMessageTitle());
        assertNotNull(errorContainer.getMessage());
    }

    @Test
    public void testGetDatasetPropertiesFromUiSpecs() throws Exception {
        given().accept(ServiceConstants.UI_SPEC_CONTENT_TYPE) //
                .expect() //
                .statusCode(200).log().ifError() //
                .with() //
                .content(buildTestDataStoreFormData()) //
                .contentType(ServiceConstants.UI_SPEC_CONTENT_TYPE) //
                .post(getVersionPrefix() + "/properties/dataset");
    }

    @Test
    public void testGetDatasetPropertiesFromSer() throws Exception {
        given().accept(ServiceConstants.UI_SPEC_CONTENT_TYPE) //
                .expect() //
                .statusCode(200).log().ifError() //
                .with() //
                .content(buildTestDataStoreSerProps()) //
                .contentType(ServiceConstants.JSONIO_CONTENT_TYPE) //
                .post(getVersionPrefix() + "/properties/dataset");
    }

    
    @Test
    public void testGetProperties_internationalized() throws Exception {
        Locale.setDefault(Locale.US);
        assertEquals("Database dataset ENGLISH display name", getDataStoreDefinitionPropertiesTitle(Locale.ENGLISH));
        assertEquals("Database dataset ENGLISH display name", getDataStoreDefinitionPropertiesTitle(Locale.ITALY));
        assertEquals("Database dataset FRENCH display name", getDataStoreDefinitionPropertiesTitle(Locale.FRENCH));
        assertEquals("Database dataset FRENCH display name", getDataStoreDefinitionPropertiesTitle(Locale.FRANCE));
        assertEquals("ñóǹ äŝçíì 汉语/漢語  华语/華語 Huáyǔ; 中文 Zhōngwén 漢字仮名交じり文 Lech Wałęsa æøå", getDataStoreDefinitionPropertiesTitle(Locale.CHINA));
    }

    private String getDataStoreDefinitionPropertiesTitle(Locale locale) throws IOException {
        Response response = given().accept(ServiceConstants.UI_SPEC_CONTENT_TYPE) //
                .expect() //
                .statusCode(200).log().ifError() //
                .with().port(localServerPort) //
                .param(LANGUAGE_QUERY_PARAMETER_NAME, locale.toLanguageTag())
                .get(getVersionPrefix() + "/properties/{name}", DATA_STORE_DEFINITION_NAME);

        JsonNode jsonNode = mapper.readTree(response.asInputStream());
        return jsonNode.get("jsonSchema").get("title").asText();
    }
    
    @Test
    public void testInitializePropertiesJsonio() throws Exception {
        Response response = given().accept(ServiceConstants.UI_SPEC_CONTENT_TYPE) //
                .expect() //
                .statusCode(200).log().ifError() //
                .with() //
                .content(buildTestDataSetSerProps()) //
                .contentType(ServiceConstants.JSONIO_CONTENT_TYPE) //
                .post(getVersionPrefix() + "/properties/uispec");
        assertNotNull(response);
        String content = response.asString();
        assertEquals(getMockDatasetMainFormUISpecs(), content);
    }

    @Test
    public void testInitializePropertiesUiSpecs() throws Exception {
        Response response = given().accept(ServiceConstants.UI_SPEC_CONTENT_TYPE) //
                .expect() //
                .statusCode(200).log().ifError() //
                .with() //
                .content(buildTestDataSetFormData()) //
                .contentType(ServiceConstants.UI_SPEC_CONTENT_TYPE) //
                .post(getVersionPrefix() + "/properties/uispec");
        assertNotNull(response);
        String content = response.asString();
        assertEquals(getMockDatasetMainFormUISpecs(), content);
    }

    @Test
    public void testSerializeProperties() throws Exception {
        Response response = given().accept(ServiceConstants.JSONIO_CONTENT_TYPE) //
                .expect() //
                .statusCode(200).log().ifError() //
                .with().port(localServerPort) //
                .content(buildTestDataSetFormData()) //
                .contentType(ServiceConstants.UI_SPEC_CONTENT_TYPE) //
                .post(getVersionPrefix() + "/properties/serialize");
        assertNotNull(response);
        String content = response.asString();
        assertNotNull(content);
        //take the jsonIO
        JsonNode jsonNode = mapper.readTree(content);
        String jsonioProperties = jsonNode.get("properties").asText();

        Deserialized<MockDatasetProperties> fromSerializedPersistent = Properties.Helper.fromSerializedPersistent(
                jsonioProperties, MockDatasetProperties.class);
        assertNotNull(fromSerializedPersistent);
        assertNotNull(fromSerializedPersistent.object);
        MockDatasetProperties deserializedProps = fromSerializedPersistent.object;
        assertEquals("tata", deserializedProps.tag.getValue());
    }


    @Test
    public void testSerializeDeserializeProperties() throws Exception {
        Response response = given().accept(ServiceConstants.JSONIO_CONTENT_TYPE) //
                .expect() //
                .statusCode(200).log().ifError() //
                .with().port(localServerPort) //
                .content(buildTestDataSetFormData()) //
                .contentType(ServiceConstants.UI_SPEC_CONTENT_TYPE) //
                .post(getVersionPrefix() + "/properties/serialize");
        assertNotNull(response);
        String content = response.asString();
        assertNotNull(content);
        response = given().accept(ServiceConstants.UI_SPEC_CONTENT_TYPE) //
                .expect() //
                .statusCode(200).log().ifError() //
                .with() //
                .content(content) //
                .contentType(ServiceConstants.JSONIO_CONTENT_TYPE) //
                .post(getVersionPrefix() + "/properties/uispec");
        assertNotNull(response);
        assertEquals(getMockDatasetMainFormUISpecs(), response.asString());
    }

}
