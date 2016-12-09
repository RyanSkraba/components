//==============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.restassured.response.Response;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.talend.components.service.rest.AbstractSpringIntegrationTests;
import org.talend.components.service.rest.dto.DatasetConnectionInfo;
import org.talend.components.service.rest.mock.MockDatasetRuntime;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

public class RuntimeControllerImplTest extends AbstractSpringIntegrationTests {

    private static final String DATA_STORE_FORM_DATA =
            "{\"@definitionName\":\"" + DATA_STORE_DEFINITION_NAME + "\",\"tag\":\"tata\", \"tagId\":\"256\"}";

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void validateConnection() throws Exception {
        // when
        Response response = given().accept(APPLICATION_JSON_UTF8_VALUE) //
                .expect() //
                .statusCode(HttpStatus.OK.value()).log().ifError() //
                .with().content(DATA_STORE_FORM_DATA) //
                .contentType(APPLICATION_JSON_UTF8_VALUE) //
                .post("/runtimes/{definitionName}", DATA_STORE_DEFINITION_NAME);

        // then
        assertNotNull(response);
        String content = response.asString();
        assertNotNull(content);
    }

    @Test
    public void getDatasetSchema() throws Exception {
        // given
        DatasetConnectionInfo formDataContainer = new DatasetConnectionInfo();
        formDataContainer.setDataStoreFormData(mapper.readValue(DATA_STORE_FORM_DATA, ObjectNode.class));
        formDataContainer.setDataSetFormData(
                mapper.readValue("{\"@definitionName\":\"" + DATA_SET_DEFINITION_NAME + "\",\"tag\":\"tata\", \"tagId\":\"256\"}",
                        ObjectNode.class));

        // when
        Response response = given().accept(APPLICATION_JSON_UTF8_VALUE) //
                .expect() //
                .statusCode(200).log().ifError() //
                .with().content(formDataContainer) //
                .contentType(APPLICATION_JSON_UTF8_VALUE) //
                .post("/runtimes/{datasetDefinitionName}/schema", DATA_SET_DEFINITION_NAME);

        // then
        assertNotNull(response);
        String content = response.asString();
        assertNotNull(content);
        assertEquals(MockDatasetRuntime.getSchemaJsonRepresentation(), content);
    }

    @Test
    public void getDatasetData() throws Exception {
        // given
        DatasetConnectionInfo formDataContainer = new DatasetConnectionInfo();
        formDataContainer.setDataStoreFormData(mapper.readValue(
                "{\"@definitionName\":\"" + DATA_STORE_DEFINITION_NAME + "\",\"tag\":\"tata\", \"tagId\":\"256\"}",
                ObjectNode.class));
        formDataContainer.setDataSetFormData(
                mapper.readValue("{\"@definitionName\":\"" + DATA_SET_DEFINITION_NAME + "\",\"tag\":\"tata\", \"tagId\":\"256\"}",
                        ObjectNode.class));

        // when
        Response response = given().accept(APPLICATION_JSON_UTF8_VALUE) //
                .expect() //
                .statusCode(200).log().ifError() //
                .with().content(formDataContainer) //
                .contentType(APPLICATION_JSON_UTF8_VALUE) //
                .post("/runtimes/{datasetDefinitionName}/data", DATA_SET_DEFINITION_NAME);

        // then
        assertNotNull(response);
        String content = response.asString();
        assertNotNull(content);

        assertEquals(MockDatasetRuntime.getRecordJsonRepresentation(), content);
    }

}