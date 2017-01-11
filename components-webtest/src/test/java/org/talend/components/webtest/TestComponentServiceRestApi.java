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
package org.talend.components.webtest;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.EmbeddedWebApplicationContext;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;
import org.talend.components.common.datastore.DatastoreDefinition;

/**
 * Test the REST API for {@link org.talend.components.api.service.ComponentService}.
 *
 * TODO: This is in a poor state -- currently JSON IO serialization is not correctly supported.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = TestApplication.class)
// @SpringApplicationConfiguration(classes = Application.class)
@WebIntegrationTest("server.port:0")
public class TestComponentServiceRestApi {

    @Autowired
    private EmbeddedWebApplicationContext server;

    @Value("${local.server.port}")
    private int port;

    private RestTemplate restTemplate = new TestRestTemplate();

    private URI baseApiUri = null;

    @Before
    public void setup() throws URISyntaxException {
        // This must occur once before any test case, but after the port has been assigned.
        if (baseApiUri == null)
            baseApiUri = new URI("http", null, "localhost", port, "/components/", null, null);
    }

    @Test
    public void testGetComponentNames() {
        URI restUriToTest = baseApiUri.resolve("names");

        // An example test based on String matching.
        String jsonString = restTemplate.getForObject(restUriToTest, String.class);
        assertThat(jsonString, is("[\"FullExampleInput\"]"));

        // An example test based on an object.
        List<String> jsonObject = restTemplate.getForObject(restUriToTest, List.class);
        assertThat(jsonObject, hasSize(1));
        assertThat(jsonObject, containsInAnyOrder("FullExampleInput"));
    }

    @Test
    public void testGetComponent() {
        URI restUriToTest = baseApiUri.resolve("definitions/");

        // An example test based on String matching.
        String jsonString = restTemplate.getForObject(restUriToTest, String.class);
        assertThat(jsonString, not(containsString("\"status\":500")));
        assertThat(jsonString, not(nullValue()));

        Object jsonObject = restTemplate.getForObject(restUriToTest, Object.class);
        assertThat(jsonObject, not(nullValue()));

        // TODO: How do we interpret the result as a ComponentDefinition?
        // This is certainly wrong, but it checks a result...
        List<Map<String, Object>> wrong = (List<Map<String, Object>>) jsonObject;
        assertThat(wrong, hasSize(1));
        assertThat(wrong.get(0).get("componentName"), is((Object) "FullExampleInput"));
    }

    @Test
    public void testGetDefinitionByType() {
        URI restUriToTest = baseApiUri.resolve("definitions/" + DatastoreDefinition.class.getName());

        // An example test based on String matching.
        String jsonString = restTemplate.getForObject(restUriToTest, String.class);
        assertThat(jsonString, not(containsString("\"status\":500")));
        assertThat(jsonString, not(nullValue()));

        Object jsonObject = restTemplate.getForObject(restUriToTest, Object.class);
        assertThat(jsonObject, not(nullValue()));

        // TODO: How do we interpret the result as a DatastoreDefinition?
        // This is certainly wrong, but it checks a result...
        List<Map<String, Object>> wrong = (List<Map<String, Object>>) jsonObject;
        assertThat(wrong, hasSize(1));
        assertThat(wrong.get(0).get("name"), is((Object) "FullExampleDatastore"));
    }

    @Test
    public void testGetDefinitionByType_Exception() {
        URI restUriToTest = baseApiUri.resolve("definitions/i.do.not.exist");

        // An example test based on String matching.
        String jsonString = restTemplate.getForObject(restUriToTest, String.class);
        System.out.println(jsonString);
        assertThat(jsonString, not(nullValue()));
        assertThat(jsonString, containsString("\"status\":500"));

        // TODO: How do we interpret this as a TalendRuntimeException?
    }

}
