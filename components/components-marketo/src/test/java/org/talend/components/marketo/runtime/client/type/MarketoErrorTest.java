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
package org.talend.components.marketo.runtime.client.type;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class MarketoErrorTest {

    MarketoError error;
    @Before
    public void setUp() throws Exception {
        error = new MarketoError("REST", "666", "erreur");
    }

    @Test
    public void testConstruct() throws Exception {
        error = new MarketoError("REST2");
        assertEquals("REST2", error.getApiMode());
        error = new MarketoError("REST1", "erreur2");
        assertEquals("REST1", error.getApiMode());
        assertEquals("erreur2", error.getMessage());
    }

    @Test
    public void testGetApiMode() throws Exception {
        assertEquals("REST", error.getApiMode());
    }

    @Test
    public void testSetApiMode() throws Exception {
        error.setApiMode("SOAP");
        assertEquals("SOAP", error.getApiMode());
    }

    @Test
    public void testSetCode() throws Exception {
        error.setCode("777");
        assertEquals("777", error.getCode());
    }

    @Test
    public void testGetCode() throws Exception {
        assertEquals("666", error.getCode());
    }

    @Test
    public void testSetMessage() throws Exception {
        error.setMessage("to you rudy...");
        assertEquals("to you rudy...", error.getMessage());
    }

    @Test
    public void testGetMessage() throws Exception {
        assertEquals("erreur", error.getMessage());
    }

    @Test
    public void testToString() throws Exception {
        String s = "MarketoError{api='REST', code='666', message='erreur'}";
        assertEquals(s, error.toString());
    }

}