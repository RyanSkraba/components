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
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.marketo.runtime.client.rest.type.LeadActivityRecord;

public class LeadActivitiesResultTest {

    LeadActivitiesResult r;
    @Before
    public void setUp() throws Exception {
        r = new LeadActivitiesResult();
        r.setResult(Arrays.asList(new LeadActivityRecord()));
    }
    @Test
    public void testGetResult() throws Exception {
        assertNotNull(r.getResult());
    }
    @Test
    public void testToString() throws Exception {
        String s = "LeadActivitiesResult{requestId='null', success=false, errors=null, result=[LeadActivityRecord [id=null, leadId=null, activityDate=null, activityTypeId=null, activityTypeValue=null, primaryAttributeValueId=null, primaryAttributeValue=null, attributes=null]], moreResult=false, nextPageToken=null}";
        assertEquals(s, r.toString());
    }

}