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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

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
        assertNull(r.getAttributes());
    }

    @Test
    public void testToString() throws Exception {
        r.setActivityDate(null);
        String s = "LeadChangeRecord [id=1, leadId=2, activityDate=null, activityTypeId=3, activityTypeValue=value, "
                + "fields=null, attributes=null]";
        assertEquals(s, r.toString());
    }

}
