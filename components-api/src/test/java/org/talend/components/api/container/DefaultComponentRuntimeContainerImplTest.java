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
package org.talend.components.api.container;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.junit.Test;
import org.talend.daikon.properties.Property;

public class DefaultComponentRuntimeContainerImplTest {

    @Test
    public void testFormatDate() throws ParseException {
        DefaultComponentRuntimeContainerImpl runtimeContainer = new DefaultComponentRuntimeContainerImpl();
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
        Date fixedDate = format.parse("02-01-1970 04:46:40");
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

        // FIXME - convert to Avro
//        List<Schema.Field> list = new ArrayList<Schema.Field>();
//        list.add(SchemaBuilder.FieldBuilder<>.new Property("testProperty"));
//        assertNull(schema.getSchemaElements());
//        schema.setSchemaElements(list);
//        assertNotNull(schema.getSchemaElements());
//        assertEquals("testProperty", schema.getSchemaElements().get(0).getName());
    }
}
