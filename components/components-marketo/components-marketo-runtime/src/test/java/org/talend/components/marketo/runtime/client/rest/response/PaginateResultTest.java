// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
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

import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class PaginateResultTest {

    PaginateResult r;

    @Before
    public void setUp() throws Exception {
        r = new PaginateResult() {

            @Override
            public List<?> getResult() {
                return null;
            }
        };
        r.setNextPageToken("popaul");
    }

    @Test
    public void testGetNextPageToken() throws Exception {
        assertEquals("popaul", r.getNextPageToken());
    }

    @Test
    public void testSetNextPageToken() throws Exception {
        r.setNextPageToken("home");
        assertEquals("home", r.getNextPageToken());
    }

    @Test
    public void testToString() throws Exception {
        String s = "{requestId='null', success=false, errors=null, result=null, moreResult=false, nextPageToken=popaul}";
        assertEquals(s, r.toString());
    }

}
