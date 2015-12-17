// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.api.exception;

import static org.junit.Assert.*;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.exception.error.ComponentsErrorCode;
import org.talend.daikon.exception.ExceptionContext;

/**
 * created by pbailly on 16 Dec 2015 Detailled comment
 *
 */
public class ComponentExceptionTest {

    @Test
    public void test() {
        Logger LOG = LoggerFactory.getLogger(ComponentExceptionTest.class);

        LOG.warn("____________________");
        LOG.warn("This unit test is testing errors, so not mind the display if the test is OK.");
        ComponentException exception = new ComponentException(ComponentsErrorCode.WRONG_COMPONENT_NAME);
        assertEquals(ComponentsErrorCode.WRONG_COMPONENT_NAME, exception.getCode());

        exception = new ComponentException(ComponentsErrorCode.MISSING_I18N_TRANSLATOR, ExceptionContext.build());
        assertEquals(ComponentsErrorCode.MISSING_I18N_TRANSLATOR, exception.getCode());

        exception = new ComponentException(ComponentsErrorCode.COMPUTE_DEPENDENCIES_FAILED, new Throwable("message"),
                ExceptionContext.build());
        assertEquals(ComponentsErrorCode.COMPUTE_DEPENDENCIES_FAILED, exception.getCode());
        assertEquals("message", exception.getCause().getMessage());

        exception = new ComponentException(ComponentsErrorCode.WRONG_WIZARD_NAME, new Throwable("message v2"));
        assertEquals(ComponentsErrorCode.WRONG_WIZARD_NAME, exception.getCode());
        assertEquals("message v2", exception.getCause().getMessage());

        LOG.warn("End of test.");
        LOG.warn("____________________");
    }

}
