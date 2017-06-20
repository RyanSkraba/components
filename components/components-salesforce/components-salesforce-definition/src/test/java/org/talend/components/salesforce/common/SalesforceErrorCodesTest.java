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

package org.talend.components.salesforce.common;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 *
 */
public class SalesforceErrorCodesTest {

    @Test
    public void testErrorCodes() {
        for (SalesforceErrorCodes code : SalesforceErrorCodes.values()) {
            assertErrorCode(code);
        }
    }

    private void assertErrorCode(SalesforceErrorCodes code) {
        assertNotNull(code.getCode());
        assertNotNull(code.getGroup());
        assertTrue(code.getHttpStatus() != 0);
        assertNotNull(code.getProduct());
        assertNotNull(code.getExpectedContextEntries());
    }
}
