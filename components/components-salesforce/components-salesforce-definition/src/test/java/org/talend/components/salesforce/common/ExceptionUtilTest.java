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

import org.junit.Test;
import org.talend.daikon.properties.ValidationResult;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 *
 */
public class ExceptionUtilTest {

    @Test
    public void testExceptionToValidationResult() {
        ValidationResult vr = ExceptionUtil.exceptionToValidationResult(new RuntimeException("TEST"));
        assertNotNull(vr);
        assertEquals(ValidationResult.Result.ERROR, vr.getStatus());
        assertEquals("TEST", vr.getMessage());
    }

}
