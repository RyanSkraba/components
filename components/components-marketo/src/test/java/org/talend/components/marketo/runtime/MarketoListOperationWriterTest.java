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

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.marketo.tmarketolistoperation.TMarketoListOperationProperties;

public class MarketoListOperationWriterTest {

    MarketoListOperationWriter w;

    @Before
    public void setUp() throws Exception {
        MarketoSink sink = new MarketoSink();
        TMarketoListOperationProperties p = new TMarketoListOperationProperties("test");
        p.connection.setupProperties();
        p.setupProperties();
        sink.initialize(null, p);
        w = new MarketoListOperationWriter(new MarketoWriteOperation(sink), null);
        assertNotNull(w);
    }

    @Test(expected = IOException.class)
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
