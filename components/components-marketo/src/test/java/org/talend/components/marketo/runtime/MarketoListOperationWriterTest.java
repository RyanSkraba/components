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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

public class MarketoListOperationWriterTest {

    MarketoListOperationWriter w;

    @Before
    public void setUp() throws Exception {
        w = new MarketoListOperationWriter(new MarketoWriteOperation(new MarketoSink()), null);
        assertNotNull(w);
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

}
