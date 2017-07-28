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
import static org.talend.components.marketo.MarketoComponentDefinition.RETURN_NB_CALL;

import org.junit.Before;
import org.junit.Test;

public class MarketoResultTest {

    MarketoResult result;

    @Before
    public void setUp() throws Exception {
        result = new MarketoResult();
    }

    @Test
    public void testConstruct() throws Exception {
        result = new MarketoResult("test", 1, 2, 3, 4);
        assertEquals(1, result.totalCount);
        assertEquals(2, result.successCount);
        assertEquals(3, result.rejectCount);
        assertEquals(4, result.apiCalls);
        assertEquals(4, result.getApiCalls());
        result.setApiCalls(10);
        assertEquals(10, result.getApiCalls());
    }

    @Test
    public void testToMap() throws Exception {
        result.setApiCalls(10);
        assertEquals(10, result.toMap().get(RETURN_NB_CALL));
    }

    @Test
    public void testToString() throws Exception {
        String s1 = "total: 0 success: 0 reject: 0 API calls: 0";
        String s2 = "total: 0 success: 0 reject: 0 API calls: 10";
        assertEquals(s1, result.toString());
        result.setApiCalls(10);
        assertEquals(s2, result.toString());
    }

    @Test
    public void testAdd() throws Exception {
        MarketoResult result1 = new MarketoResult("test", 1, 2, 3, 4);
        MarketoResult result2 = new MarketoResult("test", 1, 2, 3, 4);
        result1.add(result2);
        assertEquals(2, result1.totalCount);
        assertEquals(4, result1.successCount);
        assertEquals(6, result1.rejectCount);
        assertEquals(8, result1.apiCalls);
        assertEquals(8, result1.getApiCalls());
    }

}
