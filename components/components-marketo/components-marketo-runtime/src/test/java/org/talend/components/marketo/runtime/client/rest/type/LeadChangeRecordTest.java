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
package org.talend.components.marketo.runtime.client.rest.type;

import static org.junit.Assert.*;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

public class LeadChangeRecordTest {

    LeadChangeRecord r;

    @Before
    public void setUp() throws Exception {
        r = new LeadChangeRecord();
        r.setId(1);
        r.setLeadId(2);
        r.setActivityDate(new Date());
        r.setActivityTypeId(3);
        r.setActivityTypeValue("value");
        r.setFields(null);
        r.setAttributes(null);
    }

    @Test
    public void testGetters() throws Exception {
        assertEquals((Integer) 1, r.getId());
        assertEquals((Integer) 2, r.getLeadId());
        assertEquals((Integer) 3, r.getActivityTypeId());
        assertNotNull(r.getActivityDate());
        assertEquals("value", r.getActivityTypeValue());
        assertNull(r.getFields());
        assertNotNull(r.getAttributes());
    }

    @Test
    public void testToString() throws Exception {
        String t = "LeadChangeRecord [id=null, leadId=null, activityDate=null, activityTypeId=null, activityTypeValue=null, fields=null, attributes=[]]";
        assertEquals(t, new LeadChangeRecord().toString());
    }

}
