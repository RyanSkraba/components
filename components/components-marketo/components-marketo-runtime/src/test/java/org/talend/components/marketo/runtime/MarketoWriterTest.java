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

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.marketo.tmarketooutput.TMarketoOutputProperties;

public class MarketoWriterTest extends MarketoRuntimeTestBase {

    MarketoWriteOperation wop;

    MarketoWriter writer;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        TMarketoOutputProperties pout = new TMarketoOutputProperties("test");
        pout.connection.setupProperties();
        pout.setupProperties();
        MarketoSink sink = new MarketoSink();
        sink.initialize(null, pout);
        wop = (MarketoWriteOperation) sink.createWriteOperation();
        writer = (MarketoWriter) wop.createWriter(null);
    }

    @Test
    public void testGetWriteOperation() throws Exception {
        assertEquals(wop, writer.getWriteOperation());
    }

    @Test
    public void testGetSuccessfulWrites() throws Exception {
        assertEquals(Collections.emptyList(), writer.getSuccessfulWrites());
    }

    @Test
    public void testGetRejectedWrites() throws Exception {
        assertEquals(Collections.emptyList(), writer.getRejectedWrites());
    }

}
