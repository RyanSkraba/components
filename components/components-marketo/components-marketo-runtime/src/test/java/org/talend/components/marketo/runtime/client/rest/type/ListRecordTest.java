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
package org.talend.components.marketo.runtime.client.rest.type;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

public class ListRecordTest {

    ListRecord r;

    @Before
    public void setUp() throws Exception {
        r = new ListRecord();
        r.setId(1);
        r.setCreatedAt(new Date());
        r.setDescription("desc");
        r.setName("name");
        r.setProgramName("prog");
        r.setUpdatedAt(new Date());
    }

    @Test
    public void testGetters() throws Exception {
        assertEquals("1", r.getId().toString());
        assertEquals("name", r.getName());
        assertEquals("desc", r.getDescription());
        assertNotNull(r.getCreatedAt());
        assertNotNull(r.getUpdatedAt());
        assertEquals("prog", r.getProgramName());
    }

    @Test
    public void testToString() throws Exception {
        String s = "ListRecord [id=1, name=name, description=desc, programName=prog, createdAt=null, updatedAt=null]";
        r.setCreatedAt(null);
        r.setUpdatedAt(null);
        assertEquals(s, r.toString());
    }

}
