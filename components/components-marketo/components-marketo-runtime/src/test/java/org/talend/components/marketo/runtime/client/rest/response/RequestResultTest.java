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
package org.talend.components.marketo.runtime.client.rest.response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.marketo.runtime.client.type.MarketoError;

public class RequestResultTest {

    RequestResult r;

    @Before
    public void setUp() throws Exception {
        r = new RequestResult() {

            @Override
            public List<?> getResult() {
                return null;
            }
        };
        r.setSuccess(true);
        r.setMoreResult(true);
        r.setErrors(Arrays.asList(new MarketoError("REST")));
        r.setRequestId("REST::666");
    }

    @Test
    public void testGetRequestId() throws Exception {
        assertEquals("REST::666", r.getRequestId());
    }

    @Test
    public void testIsSuccess() throws Exception {
        assertTrue(r.isSuccess());
    }

    @Test
    public void testGetErrors() throws Exception {
        assertNotNull(r.getErrors());
    }

    @Test
    public void testGetResult() throws Exception {
        assertNull(r.getResult());
    }

    @Test
    public void testIsMoreResult() throws Exception {
        assertTrue(r.isMoreResult());
    }

    @Test
    public void testSetMoreResult() throws Exception {
        r.setMoreResult(false);
        assertFalse(r.isMoreResult());
    }

    @Test
    public void testSetRequestId() throws Exception {
        r.setRequestId("SOAP");
        assertEquals("SOAP", r.getRequestId());
    }

    @Test
    public void testSetSuccess() throws Exception {
        r.setSuccess(false);
        assertFalse(r.isSuccess());
    }

    @Test
    public void testSetErrors() throws Exception {
        r.setErrors(Arrays.asList(null, new MarketoError("SOAP")));
        assertEquals(2, r.getErrors().size());
    }

    @Test
    public void testToString() throws Exception {
        String s = "{requestId='REST::666', success=true, errors=[MarketoError{api='REST', code='', message=''}], result=null, moreResult=true}";
        assertEquals(s, r.toString());
    }

    @Test
    public void testGetErrorsString() throws Exception {
        assertNotNull(r.getErrorsString());
        r.setErrors(Arrays.asList(new MarketoError("REST", "404", "not found¿")));
        assertNotNull(r.getErrorsString());
        r.setErrors(null);
        assertNotNull(r.getErrorsString());
    }

}
