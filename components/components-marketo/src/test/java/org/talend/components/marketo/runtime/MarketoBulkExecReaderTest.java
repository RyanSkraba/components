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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.talend.components.api.component.ComponentDefinition.RETURN_ERROR_MESSAGE;
import static org.talend.components.marketo.MarketoComponentDefinition.RETURN_NB_CALL;

import java.io.IOException;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.marketo.tmarketobulkexec.TMarketoBulkExecProperties;

public class MarketoBulkExecReaderTest {

    MarketoBulkExecReader reader;

    @Before
    public void setUp() throws Exception {
        MarketoSource source = new MarketoSource();
        TMarketoBulkExecProperties props = new TMarketoBulkExecProperties("test");
        props.connection.setupProperties();
        props.setupProperties();
        source.initialize(null, props);
        reader = (MarketoBulkExecReader) source.createReader(null);
        assertTrue(reader instanceof MarketoBulkExecReader);
    }

    @Test(expected = IOException.class)
    public void testStart() throws Exception {
        assertFalse(reader.start());
        fail("Shouldn't be here");
    }

    @Test
    public void testAdvance() throws Exception {
        assertFalse(reader.advance());
    }

    @Test
    public void testGetCurrent() throws Exception {
        assertNull(reader.getCurrent());
    }

    @Test
    public void testGetReturnValues() throws Exception {
        Map<String, Object> r = reader.getReturnValues();
        assertNotNull(r);
        assertEquals(0, r.get(RETURN_NB_CALL));
        assertNull(r.get(RETURN_ERROR_MESSAGE));
    }

}
