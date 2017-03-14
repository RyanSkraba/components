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
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.marketo.tmarketoinput.TMarketoInputProperties;
import org.talend.daikon.properties.ValidationResult;

public class MarketoSourceTest {

    MarketoSource source;

    @Before
    public void setUp() throws Exception {
        source = new MarketoSource();
    }

    @Test
    public void splitIntoBundles() throws Exception {
        assertTrue(source.splitIntoBundles(1000, null).size() > 0);
    }

    @Test
    public void getEstimatedSizeBytes() throws Exception {
        assertEquals(0, source.getEstimatedSizeBytes(null));
    }

    @Test
    public void producesSortedKeys() throws Exception {
        assertFalse(source.producesSortedKeys(null));
    }

    @Test
    public void testValidate() throws Exception {
        TMarketoInputProperties props = new TMarketoInputProperties("test");
        source.initialize(null, props);
        assertEquals(ValidationResult.Result.ERROR, source.validate(null).getStatus());
    }

}
