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

public class MarketoExceptionTest {

    MarketoException e;
    @Before
    public void setUp() throws Exception {
        e = new MarketoException("REST", "erreur");
    }

    @Test
    public void testConstruct() throws Exception {
        e = new MarketoException("REST", "898", "898T");
        assertEquals("MarketoException{api='REST', code='898', message='898T', cause='null'}", e.toString());
        e = new MarketoException("REST", 898, "898T");
        assertEquals("MarketoException{api='REST', code='898', message='898T', cause='null'}", e.toString());
        e = new MarketoException("REST", "898", "898T", new Throwable());
        assertEquals("MarketoException{api='REST', code='898', message='898T', cause='java.lang.Throwable'}", e.toString());
        e = new MarketoException("REST", 898, "898T", new Throwable());
        assertEquals("MarketoException{api='REST', code='898', message='898T', cause='java.lang.Throwable'}", e.toString());
        e = new MarketoException("REST", "898T", new Throwable());
        assertEquals("MarketoException{api='REST', code='', message='898T', cause='java.lang.Throwable'}", e.toString());
        e = new MarketoException("REST", new Throwable());
        assertEquals("MarketoException{api='REST', code='', message='java.lang.Throwable', cause='java.lang.Throwable'}",
                e.toString());
    }

    @Test
    public void testSetCode() throws Exception {
        e.setCode("666");
        assertEquals("666", e.getCode());
    }

    @Test
    public void testGetApiMode() throws Exception {
        assertEquals("REST", e.getApiMode());
    }

    @Test
    public void testSetApiMode() throws Exception {
        e.setApiMode("SOAP");
        assertEquals("SOAP", e.getApiMode());
    }

    @Test
    public void testGetCode() throws Exception {
        assertEquals("", e.getCode());
    }

    @Test
    public void testToMarketoError() throws Exception {
        MarketoError me = new MarketoError("REST", "", "erreur");
        assertEquals(me.toString(), e.toMarketoError().toString());
    }

    @Test
    public void testToString() throws Exception {
        String s = "MarketoException{api='REST', code='', message='erreur', cause='null'}";
        assertEquals(s, e.toString());
    }

}