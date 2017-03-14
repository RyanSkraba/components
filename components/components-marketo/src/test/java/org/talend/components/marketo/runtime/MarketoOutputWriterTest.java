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
package org.talend.components.marketo.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

public class MarketoOutputWriterTest {

    MarketoOutputWriter w;

    @Before
    public void setUp() throws Exception {
        w = new MarketoOutputWriter(new MarketoWriteOperation(new MarketoSink()), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOpen() throws Exception {
        w.open("test");
        fail("Should be here");
    }

    @Test
    public void testWrite() throws Exception {
        w.write(null);
        assertEquals(0, w.result.totalCount);
    }

    @Test
    public void testGetWriteOperation() throws Exception {
        assertTrue(w.getWriteOperation() instanceof MarketoWriteOperation);
    }

    @Test
    public void testClose() throws Exception {
        w.close();
        assertEquals(0, w.result.totalCount);
    }

    @Test
    public void testGetSuccessfulWrites() throws Exception {
        assertEquals(Collections.emptyList(), w.getSuccessfulWrites());
    }

    @Test
    public void testGetRejectedWrites() throws Exception {
        assertEquals(Collections.emptyList(), w.getRejectedWrites());
    }

}
