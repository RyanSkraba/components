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
package org.talend.components.api.exception.error;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * created by pbailly on 16 Dec 2015 Detailled comment
 *
 */
public class ComponentsErrorCodeTest {

    @Test
    public void test() {
        assertEquals(404, ComponentsApiErrorCode.COMPUTE_DEPENDENCIES_FAILED.getHttpStatus());
        assertEquals("TCOMP", ComponentsApiErrorCode.COMPUTE_DEPENDENCIES_FAILED.getProduct());
        assertEquals("ALL", ComponentsApiErrorCode.COMPUTE_DEPENDENCIES_FAILED.getGroup());
        assertEquals("COMPUTE_DEPENDENCIES_FAILED", ComponentsApiErrorCode.COMPUTE_DEPENDENCIES_FAILED.toString());
        assertEquals(1, ComponentsApiErrorCode.COMPUTE_DEPENDENCIES_FAILED.getExpectedContextEntries().size());

    }

}
