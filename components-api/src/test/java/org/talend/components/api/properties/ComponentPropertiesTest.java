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

public class ComponentPropertiesTest {

    @Test
    public void testSetReturnsProperty() {
        Property element = ComponentPropertyFactory.newReturnsProperty();
        assertEquals("returns", element.getName());
        assertEquals(Property.Type.STRING, element.getType());
    }

    @Test
    public void testNewReturnProperty() throws IllegalAccessException {
        Property element = ComponentPropertyFactory.newReturnsProperty();
        Property returnProperty = ComponentPropertyFactory.newReturnProperty(element, Property.Type.BOOLEAN, "childName");
        assertEquals("childName", returnProperty.getName());
        assertEquals(Property.Type.BOOLEAN, returnProperty.getType());
        assertEquals(returnProperty, element.getChild("childName"));
    }

}
