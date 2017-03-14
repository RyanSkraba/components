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
package org.talend.components.marketo.runtime.client.rest.response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

public class ActivityTypesResultTest {

    ActivityTypesResult r;
    @Before
    public void setUp() throws Exception {
        r = new ActivityTypesResult();
        r.setResult(null);
    }
    @Test
    public void testGetResult() throws Exception {
        assertNull(r.getResult());
    }
    @Test
    public void testToString() throws Exception {
        String s = "ActivityTypesResult{requestId='null', success=false, errors=null, result=null, moreResult=false}";
        assertEquals(s, r.toString());
    }

}