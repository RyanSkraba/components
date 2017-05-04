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
import static org.junit.Assert.assertNotNull;

import java.util.Collections;

import org.junit.Test;

public class MarketoSyncResultTest {

    @Test
    public void testConstruct() throws Exception {
        MarketoSyncResult r = new MarketoSyncResult("STREAM", 1, 2, null);
        assertEquals("STREAM", r.getStreamPosition());
        assertEquals(1, r.getRecordCount());
        assertEquals(2, r.getRemainCount());
        assertNotNull(r.getRecords());
        r = new MarketoSyncResult("STREAM", 10, 20, null);
        assertEquals("STREAM", r.getStreamPosition());
        assertEquals(10, r.getRecordCount());
        assertEquals(20, r.getRemainCount());
        assertNotNull(r.getRecords());
        r = new MarketoSyncResult();
        assertEquals("", r.getStreamPosition());
        assertEquals(0, r.getRecordCount());
        assertEquals(0, r.getRemainCount());
        assertEquals(Collections.emptyList(), r.getRecords());
    }
}
