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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class ActivityTypeTest {

    ActivityType a;

    @Before
    public void setUp() throws Exception {
        a = new ActivityType();
        a.setId(1);
        a.setDescription("desc");
        a.setName("name");
        List<Map<String, Object>> attrs = new ArrayList<>();
        a.setAttributes(attrs);
        Map<String, String> pattrs = new HashMap<>();
        a.setPrimaryAttribute(pattrs);
    }

    @Test
    public void testGetters() throws Exception {
        assertEquals((Integer) 1, a.getId());
        assertEquals("desc", a.getDescription());
        assertEquals("name", a.getName());
        assertEquals(0, a.getAttributes().size());
        assertEquals(0, a.getPrimaryAttribute().size());
    }

}
