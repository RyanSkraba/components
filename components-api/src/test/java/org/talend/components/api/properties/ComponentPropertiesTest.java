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
import org.talend.daikon.properties.PropertyFactory;

public class ComponentPropertiesTest {

    @Test
    public void testSetReturnsProperty() {
        Property<String> element = ComponentPropertyFactory.newReturnsProperty();
        assertEquals("returns", element.getName());
        assertEquals(String.class, element.getTypeLiteral().getType());
    }

    @Test
    public void testNewReturnProperty() throws IllegalAccessException {
        Property<String> element = ComponentPropertyFactory.newReturnsProperty();
        Property<Boolean> returnProperty = ComponentPropertyFactory.newReturnProperty(element,
                PropertyFactory.newBoolean("childName"));
        assertEquals("childName", returnProperty.getName());
        assertEquals(Boolean.class, returnProperty.getTypeLiteral().getType());
        assertEquals(returnProperty, element.getChild("childName"));
    }

}
