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
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.talend.components.service.rest.AbstractSpringIntegrationTests;
import org.talend.components.service.rest.ServiceConstants;
import org.talend.components.service.rest.dto.VersionDto;

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

/** Unit tests for {@link VersionControllerImpl}. */
public class VersionControllerImplTest extends AbstractSpringIntegrationTests {

    protected String getVersionPrefix() {
        return ServiceConstants.V0;
    }

    @Test
    public void testGetVersion() throws Exception {
        Response r = given().accept(APPLICATION_JSON_UTF8_VALUE) //
                .expect() //
                .statusCode(200).log().ifError() //
                .with().port(localServerPort) //
                .get(getVersionPrefix() + "/version");

        r.then() //
                .statusCode(HttpStatus.OK.value()).log().ifError() //
                .contentType(APPLICATION_JSON_UTF8_VALUE);

        // We don't care what values are returned as long as they are filled.
        JsonPath jsonPathEvaluator = r.jsonPath();
        assertThat(jsonPathEvaluator.get("version"), allOf(notNullValue(), not(equalTo(VersionDto.N_A))));
        assertThat(jsonPathEvaluator.get("commit"), allOf(notNullValue(), not(equalTo(VersionDto.N_A))));
        assertThat(jsonPathEvaluator.get("time"), allOf(notNullValue(), not(equalTo(VersionDto.N_A))));
    }
}
