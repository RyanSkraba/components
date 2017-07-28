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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class MarketoRuntimeExceptionTest {

    MarketoRuntimeException mrte;

    @Test
    public void testMarketoRuntimeException() throws Exception {
        mrte = new MarketoRuntimeException("test");
        assertNotNull(mrte);
        assertTrue(mrte.getMessage().contains("test"));
    }

    @Test
    public void testName() throws Exception {
        System.out.println("mrte = " + mrte);
    }
}
