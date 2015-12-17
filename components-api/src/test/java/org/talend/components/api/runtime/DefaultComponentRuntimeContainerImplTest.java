// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.api.runtime;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.talend.components.api.properties.Property;
import org.talend.components.api.schema.SchemaElement;

/**
 * created by pbailly on 16 Dec 2015 Detailled comment
 *
 */
public class DefaultComponentRuntimeContainerImplTest {

    @Test
    public void testGlobalMap() {
        DefaultComponentRuntimeContainerImpl runtimeContainer = new DefaultComponentRuntimeContainerImpl();
        Map<String, Object> globalMap = runtimeContainer.getGlobalMap();
        assertEquals(0, globalMap.size());
        globalMap.put("key", "value");
        assertEquals(1, runtimeContainer.getGlobalMap().size());
    }

    @Test
    public void testFormatDate() {
        DefaultComponentRuntimeContainerImpl runtimeContainer = new DefaultComponentRuntimeContainerImpl();
        Date fixedDate = new Date(100000000l);
        assertEquals("02-01-1970", runtimeContainer.formatDate(fixedDate, "dd-MM-yyyy"));
        assertEquals("02-01-1970 04:46:40", runtimeContainer.formatDate(fixedDate, "dd-MM-yyyy hh:mm:ss"));
    }

    @Test
    public void testCreateDynamicHolder() {
        DefaultComponentRuntimeContainerImpl runtimeContainer = new DefaultComponentRuntimeContainerImpl();
        DefaultComponentRuntimeContainerImpl.Dynamic schema = runtimeContainer.createDynamicHolder();
        assertNull(schema.getFieldValue("key"));
        schema.addFieldValue("key", "value");
        assertEquals("value", schema.getFieldValue("key"));
        schema.resetValues();
        assertNull(schema.getFieldValue("key"));

        List<SchemaElement> list = new ArrayList<SchemaElement>();
        list.add(new Property("testProperty"));
        assertNull(schema.getSchemaElements());
        schema.setSchemaElements(list);
        assertNotNull(schema.getSchemaElements());
        assertEquals("testProperty", schema.getSchemaElements().get(0).getName());
    }
}
