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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.talend.components.service.rest.AbstractSpringIntegrationTests;
import org.talend.components.service.rest.ServiceConstants;
import org.talend.components.service.rest.dto.SerPropertiesDto;
import org.talend.components.service.rest.dto.UiSpecsPropertiesDto;
import org.talend.components.service.rest.mock.MockDatasetRuntime;

public class RuntimeControllerImplTest extends AbstractSpringIntegrationTests {

    protected String getVersionPrefix() {
        return ServiceConstants.V0;
    }

    @Test
    public void validateConnectionUiSpecs() throws Exception {
        UiSpecsPropertiesDto propertiesDto = buildTestDataStoreFormData();
        given().accept(APPLICATION_JSON_UTF8_VALUE) //
                .with().content(propertiesDto) //
                .contentType(ServiceConstants.UI_SPEC_CONTENT_TYPE) //
                .when()//
                .post(getVersionPrefix() + "/runtimes/check")//
                .then()//
                .statusCode(HttpStatus.OK.value()).log().ifError()//
                .assertThat().body(notNullValue())//
                .assertThat().body(instanceOf(String.class));
    }

    @Test
    public void validateConnectionJsonio() throws Exception {
        SerPropertiesDto propertiesDto = buildTestDataStoreSerProps();

        given().accept(APPLICATION_JSON_UTF8_VALUE) //
                .with().content(propertiesDto) //
                .contentType(ServiceConstants.JSONIO_CONTENT_TYPE) //
                .when()//
                .post(getVersionPrefix() + "/runtimes/check")//
                .then()//
                .statusCode(HttpStatus.OK.value()).log().ifError() //
                .assertThat().body(notNullValue())//
                .assertThat().body(instanceOf(String.class));
    }

    @Test
    public void getDatasetSchemaUiSpec() throws Exception {
        // given
        UiSpecsPropertiesDto formDataContainer = buildTestDataSetFormData();

        given().accept(APPLICATION_JSON_UTF8_VALUE) //
                .with().content(formDataContainer) //
                .contentType(ServiceConstants.UI_SPEC_CONTENT_TYPE) //
                .when()//
                .post(getVersionPrefix() + "/runtimes/schema")//
                .then()//
                .statusCode(200).log().ifError() //
                .assertThat().body(equalTo(MockDatasetRuntime.getSchemaJsonRepresentation()));

    }

    @Test
    public void getDatasetSchemaJsonIo() throws Exception {
        // given
        SerPropertiesDto formDataContainer = buildTestDataSetSerProps();

        given().accept(APPLICATION_JSON_UTF8_VALUE) //
                .with().content(formDataContainer) //
                .contentType(ServiceConstants.JSONIO_CONTENT_TYPE) //
                .when()//
                .post(getVersionPrefix() + "/runtimes/schema")//
                .then()//
                .statusCode(200).log().ifError() //
                .assertThat().body(equalTo(MockDatasetRuntime.getSchemaJsonRepresentation()));
    }

    @Test
    public void getDatasetDataUisSpecs() throws Exception {
        // given
        UiSpecsPropertiesDto formDataContainer = buildTestDataSetFormData();

        given().accept(APPLICATION_JSON_UTF8_VALUE) //
                .with().content(formDataContainer) //
                .contentType(ServiceConstants.UI_SPEC_CONTENT_TYPE) //
                .when()//
                .post(getVersionPrefix() + "/runtimes/data")//
                .then()//
                .statusCode(200).log().ifError() //
                .assertThat().body(equalTo(MockDatasetRuntime.getRecordJsonRepresentation()));
    }

    @Test
    public void getDatasetDataJsonio() throws Exception {
        // given
        SerPropertiesDto formDataContainer = buildTestDataSetSerProps();

        given().accept(APPLICATION_JSON_UTF8_VALUE) //
                .with().content(formDataContainer) //
                .contentType(ServiceConstants.JSONIO_CONTENT_TYPE) //
                .when()//
                .post(getVersionPrefix() + "/runtimes/data")//
                .then()//
                .statusCode(200).log().ifError() //
                .assertThat().body(equalTo(MockDatasetRuntime.getRecordJsonRepresentation()));
    }
}
