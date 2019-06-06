// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.marketo.helpers;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.talend.daikon.properties.presentation.Form;

import com.google.gson.JsonElement;

public class TokenTableTest {

    TokenTable table;

    @Before
    public void setUp() throws Exception {
        table = new TokenTable("test");
        table.setupProperties();
        table.setupLayout();
    }

    @Test
    public void testSetupLayout() throws Exception {
        Form f = table.getForm(Form.MAIN);
        assertTrue(f.getWidget("tokenName").isVisible());
        assertTrue(f.getWidget("tokenValue").isVisible());
    }

    @Test
    public void testGetTokens() throws Exception {
        table.tokenName.setValue(Collections.emptyList());
        assertTrue(table.getTokens().isEmpty());
        table.tokenName.setValue(null);
        assertTrue(table.getTokens().isEmpty());
        table.tokenName.setValue(Arrays.asList("t1", "t2"));
        table.tokenValue.setValue(Arrays.asList("", ""));
        Map<String, String> tokens = table.getTokens();
        assertNotNull(tokens);
        assertEquals(2, table.size());
        assertEquals(2, tokens.size());
        for (String k : tokens.keySet()) {
            assertEquals(k, tokens.get(k));
        }
        table.tokenValue.setValue(Arrays.asList(null, null));
        tokens = table.getTokens();
        assertNotNull(tokens);
        assertEquals(2, table.size());
        assertEquals(2, tokens.size());
        for (String k : tokens.keySet()) {
            assertEquals(k, tokens.get(k));
        }
        table.tokenValue.setValue(Arrays.asList("v1", "v2"));
        tokens = table.getTokens();
        assertNotNull(tokens);
        assertEquals(2, table.size());
        assertEquals(2, tokens.size());
    }

    @Test
    public void testGetTokensAsJson() throws Exception {
        table.tokenName.setValue(Collections.emptyList());
        assertEquals("[]", table.getTokensAsJson().toString());
        table.tokenName.setValue(null);
        assertEquals("[]", table.getTokensAsJson().toString());

        table.tokenName.setValue(Arrays.asList("t1", "t2"));
        table.tokenValue.setValue(Arrays.asList("v1", "v2"));
        JsonElement jTokens = table.getTokensAsJson();
        assertNotNull(jTokens);
        assertTrue(jTokens.isJsonArray());
    }

    @Test
    public void testSize() throws Exception {
        assertEquals(0, table.size());
        table.tokenName.setValue(null);
        assertEquals(0, table.size());
    }
}
