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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.marketo.tmarketoinput.TMarketoInputProperties;
import org.talend.components.marketo.tmarketolistoperation.TMarketoListOperationProperties;
import org.talend.components.marketo.tmarketooutput.TMarketoOutputProperties;

public class MarketoWriteOperationTest {

    MarketoWriteOperation wop;

    @Before
    public void setUp() throws Exception {
        wop = new MarketoWriteOperation(new MarketoSink());
        wop.initialize(null);
    }

    @Test
    public void testFinalize() throws Exception {
        List<Result> wr = new ArrayList<Result>();
        wr.add(new Result());
        assertEquals("{successRecordCount=0, rejectRecordCount=0, totalRecordCount=0}", wop.finalize(wr, null).toString());
    }

    @Test
    public void testCreateWriter() throws Exception {
        MarketoSink sink = new MarketoSink();
        TMarketoListOperationProperties plist = new TMarketoListOperationProperties("test");
        plist.init();
        TMarketoOutputProperties pout = new TMarketoOutputProperties("test");
        pout.init();
        TMarketoInputProperties pin = new TMarketoInputProperties("test");
        pin.init();
        sink.initialize(null, null);
        wop = new MarketoWriteOperation(sink);
        assertNull(wop.createWriter(null));
        sink.initialize(null, plist);
        wop = new MarketoWriteOperation(sink);
        assertEquals(MarketoListOperationWriter.class, wop.createWriter(null).getClass());
        sink.initialize(null, pout);
        wop = new MarketoWriteOperation(sink);
        assertEquals(MarketoOutputWriter.class, wop.createWriter(null).getClass());
        sink.initialize(null, pin);
        wop = new MarketoWriteOperation(sink);
        assertEquals(MarketoInputWriter.class, wop.createWriter(null).getClass());
    }

    @Test
    public void testGetSink() throws Exception {
        assertTrue(wop.getSink() instanceof MarketoSink);
    }

}
