// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.components.marklogic.exceptions;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MarkLogicErrorCodeTest {

    private MarkLogicErrorCode errorCode;

    @Test
    public void testGetGroup() {
        errorCode = new MarkLogicErrorCode("some error code");

        assertEquals(MarkLogicErrorCode.GROUP_COMPONENT_MARKLOGIC, errorCode.getGroup());
    }

    @Test
    public void testGetProduct() {
        errorCode = new MarkLogicErrorCode("some error code", "some entry");

        assertEquals(MarkLogicErrorCode.PRODUCT_TALEND_COMPONENTS, errorCode.getProduct());
    }

    @Test
    public void testGetHttpStatus() {
        int someHttpStatus = 200;
        errorCode = new MarkLogicErrorCode("some code", someHttpStatus, "some entry");

        assertEquals(someHttpStatus, errorCode.getHttpStatus());
    }
}
