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

package org.talend.components.simplefileio.s3.input;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.talend.daikon.properties.presentation.Form.MAIN;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.talend.components.simplefileio.input.SimpleFileIOInputDefinition;
import org.talend.daikon.serialize.jsonschema.JsonSchemaUtil;

/**
 * Unit tests for {@link S3InputProperties}.
 */
public class S3InputPropertiesTest {

    /**
     * Instance to test. A new instance is created for each test.
     */
    private S3InputProperties properties = null;

    @Before
    public void setup() {
        properties = new S3InputProperties("test");
        properties.init();
    }

    @Test
    public void testJsonSchemaSerialization() throws JSONException {
        String jsonString = JsonSchemaUtil.toJson(properties, MAIN, SimpleFileIOInputDefinition.NAME);
        assertThat(jsonString, notNullValue());

        JSONObject node = new JSONObject(jsonString);
        // TODO: look at the contents of the JSON node.
        assertThat(node, notNullValue());
        JSONAssert.assertEquals("{\"jsonSchema\": {},\"uiSchema\": {},\"properties\": {}} ", node, false);
    }

}
