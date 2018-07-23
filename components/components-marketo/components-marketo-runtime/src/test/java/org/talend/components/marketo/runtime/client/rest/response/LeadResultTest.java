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
package org.talend.components.marketo.runtime.client.rest.response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class LeadResultTest {

    LeadResult r;

    @Before
    public void setUp() throws Exception {
        r = new LeadResult();
        Map<String, String> m = new HashMap<>();
        r.setResult(Arrays.asList(m));
    }

    @Test
    public void testGetResult() throws Exception {
        assertNotNull(r.getResult());
    }

    @Test
    public void testToString() throws Exception {
        String s = "LeadResult{requestId='null', success=false, errors=null, result=[{}], moreResult=false, nextPageToken=null}";
        assertEquals(s, r.toString());
    }

}
