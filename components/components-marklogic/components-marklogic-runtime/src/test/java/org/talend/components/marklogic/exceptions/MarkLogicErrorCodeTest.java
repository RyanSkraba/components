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
