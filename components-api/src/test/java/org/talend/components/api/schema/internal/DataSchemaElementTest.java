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
package org.talend.components.api.schema.internal;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.talend.components.api.properties.Property;
import org.talend.components.api.schema.AbstractSchemaElement;
import org.talend.components.api.schema.SchemaElement;
import org.talend.components.api.schema.SchemaElement.Type;

/**
 * created by pbailly on 16 Dec 2015 Detailled comment
 *
 */
public class DataSchemaElementTest {

    @Test
    public void test() {
        DataSchemaElement element = new DataSchemaElement();
        assertNull(element.getDisplayName());
        assertEquals("testName", element.setDisplayName("testName").getDisplayName());

    }

    @Test
    public void testAbstractSchemaElement() {
        AbstractSchemaElement element = new DataSchemaElement();
        assertNull(element.getName());
        assertEquals(element, element.setName("testName"));
        assertEquals("testName", element.getName());

        // displayName use the name
        assertEquals("testName", element.getDisplayName());
        assertEquals(element, element.setDisplayName("testDisplayName"));
        assertEquals("testDisplayName", element.getDisplayName());

        assertNull(element.getTitle());
        assertEquals(element, element.setTitle("testTitle"));
        assertEquals("testTitle", element.getTitle());

        assertNull(element.getType());
        assertEquals(element, element.setType(Type.BYTE_ARRAY));
        assertEquals(Type.BYTE_ARRAY, element.getType());

        assertEquals(0, element.getSize());
        assertFalse(element.isSizeUnbounded());
        assertEquals(element, element.setSize(28));
        assertEquals(28, element.getSize());
        assertFalse(element.isSizeUnbounded());
        assertEquals(element, element.setSize(-1));
        assertTrue(element.isSizeUnbounded());

        assertEquals(0, element.getOccurMinTimes());
        assertFalse(element.isRequired());
        assertEquals(element, element.setOccurMinTimes(33));
        assertEquals(33, element.getOccurMinTimes());
        assertTrue(element.isRequired());

        assertEquals(0, element.getOccurMaxTimes());
        assertEquals(element, element.setOccurMaxTimes(42));
        assertEquals(42, element.getOccurMaxTimes());

        assertEquals(element, element.setOccurMinTimes(0));
        element.setRequired();
        assertTrue(element.isRequired());
        assertEquals(1, element.getOccurMinTimes());
        assertEquals(1, element.getOccurMaxTimes());

        assertEquals(0, element.getPrecision());
        assertEquals(element, element.setPrecision(222));
        assertEquals(222, element.getPrecision());

        assertNull(element.getPattern());
        assertEquals(element, element.setPattern("mypattern"));
        assertEquals("mypattern", element.getPattern());

        assertNull(element.getDefaultValue());
        assertEquals(element, element.setDefaultValue("mypattern"));
        assertEquals("mypattern", element.getDefaultValue());

        assertFalse(element.isNullable());
        assertEquals(element, element.setNullable(true));
        assertTrue(element.isNullable());
        assertEquals(element, element.setNullable(false));
        assertFalse(element.isNullable());

        assertEquals("testDisplayName", element.toStringIndent(0));
        assertEquals(" testDisplayName", element.toStringIndent(1));
        assertEquals("    testDisplayName", element.toStringIndent(4));
    }

    @Test
    public void testChildren() {
        AbstractSchemaElement element = new DataSchemaElement();
        SchemaElement child = new Property("myElement");
        assertNotNull(element.addChild(child).getChild("myElement"));
        assertEquals("myElement", element.getChild("myElement").getName());

        List<SchemaElement> children = element.getChildren();
        assertEquals(1, children.size());
        assertEquals("myElement", children.get(0).getName());

        children.add(new Property("myElement2"));
        element.setChildren(children);
        assertEquals("myElement", element.getChild("myElement").getName());
        assertEquals("myElement2", element.getChild("myElement2").getName());

        Map<String, SchemaElement> childrenMap = element.getChildMap();
        assertEquals(2, childrenMap.size());
        assertEquals("myElement", childrenMap.get("myElement").getName());
        assertEquals("myElement2", childrenMap.get("myElement2").getName());
        childrenMap.put("myElement3", new Property("myElement3"));
    }

}
