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
package org.talend.components.marketo.runtime.client.rest.type;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class SyncStatusTest {

    SyncStatus s;

    @Before
    public void setUp() throws Exception {
        s = new SyncStatus();
        s.setId(1);
        s.setStatus("status");
        List<Map<String, String>> reasons = new ArrayList<>();
        s.setReasons(reasons);
    }

    @Test
    public void testConstruct() throws Exception {
        s = new SyncStatus(10, "good");
        assertEquals(new Integer(10), s.getId());
        assertEquals("good", s.getStatus());
    }

    @Test
    public void testGetters() throws Exception {
        assertEquals("1", s.getId().toString());
        assertEquals("status", s.getStatus());
        assertNotNull(s.getReasons());
        assertNull(s.getErrorMessage());
        s.setErrorMessage("err");
        assertEquals("err", s.getErrorMessage());
    }

    @Test
    public void testToString() throws Exception {
        String st = "SyncStatus{id=1, marketoGUID='null', seq=null, status='status', reasons=[], errorMessage='null'}";
        assertEquals(st, s.toString());
    }
}
