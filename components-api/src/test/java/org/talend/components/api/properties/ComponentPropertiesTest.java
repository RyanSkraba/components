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
package org.talend.components.api.properties;

import static org.junit.Assert.*;

import org.junit.Test;
import org.talend.daikon.properties.Property;
import org.talend.daikon.schema.SchemaElement;
import org.talend.daikon.schema.SchemaElement.Type;

/**
 * created by sgandon on 1 févr. 2016
 */
public class ComponentPropertiesTest {

    @Test
    public void testSetReturnsProperty() {
        SchemaElement element = ComponentProperties.setReturnsProperty();
        assertEquals("returns", element.getName());
        assertEquals(Type.STRING, element.getType());
    }

    @Test
    public void testNewReturnProperty() throws IllegalAccessException {
        Property element = ComponentProperties.setReturnsProperty();
        Property returnProperty = ComponentPropertyFactory.newReturnProperty(element, Type.BOOLEAN, "childName");
        assertEquals("childName", returnProperty.getName());
        assertEquals(Type.BOOLEAN, returnProperty.getType());
        assertEquals(returnProperty, element.getChild("childName"));
    }

}
