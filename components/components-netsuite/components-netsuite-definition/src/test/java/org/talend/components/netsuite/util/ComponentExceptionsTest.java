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

package org.talend.components.netsuite.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.xml.ws.WebServiceException;

import org.junit.Test;
import org.talend.components.api.exception.ComponentException;
import org.talend.daikon.exception.error.CommonErrorCodes;
import org.talend.daikon.properties.ValidationResult;

/**
 *
 */
public class ComponentExceptionsTest {

    @Test
    public void testExceptionToValidationResult() {
        ValidationResult result1 = ComponentExceptions.exceptionToValidationResult(new WebServiceException("TEST"));
        assertNotNull(result1);
        assertEquals(ValidationResult.Result.ERROR, result1.getStatus());
        assertEquals("TEST", result1.getMessage());

        ValidationResult result2 = ComponentExceptions.exceptionToValidationResult(
                new ComponentException(CommonErrorCodes.UNEXPECTED_EXCEPTION, new WebServiceException("TEST")));
        assertNotNull(result2);
        assertEquals(ValidationResult.Result.ERROR, result2.getStatus());
        assertEquals(CommonErrorCodes.UNEXPECTED_EXCEPTION.name(), result2.getMessage());

        ValidationResult controlResult = new ValidationResult(ValidationResult.Result.ERROR, "TEST");
        ValidationResult result3 = ComponentExceptions.exceptionToValidationResult(new ComponentException(controlResult));
        assertEquals(controlResult, result3);
    }
}
