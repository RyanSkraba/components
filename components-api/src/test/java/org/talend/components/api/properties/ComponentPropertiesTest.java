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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

public class ComponentPropertiesTest {

    private final class NestedNestedProperties extends ComponentPropertiesImpl{

        public Property value = PropertyFactory.newString("value");

        /**
         * 
         * @param name
         */
        private NestedNestedProperties(String name) {
            super(name);
        }
    }

    private final class NestedProperty extends ComponentPropertiesImpl {

        public Property three = PropertyFactory.newString("three");

        public NestedNestedProperties four = new NestedNestedProperties("four");

        /**
         * 
         * @param name
         */
        private NestedProperty(String name) {
            super(name);
        }
    }

    private final class ComponentPropertiesTestClass extends ComponentPropertiesImpl {

        public Property one = PropertyFactory.newString("one");

        public NestedProperty two = new NestedProperty("two");

        /**
         * 
         * @param name
         */
        public ComponentPropertiesTestClass(String name) {
            super(name);
        }
    }
    
    ComponentPropertiesTestClass foo;

    @Before
    public void init() {
        foo = (ComponentPropertiesTestClass) new ComponentPropertiesTestClass("foo").init();
    }

    @Test
    public void testUpdateNestedProperties() throws IllegalAccessException {
        NestedNestedProperties nestedProperties = new NestedNestedProperties("bar");
        nestedProperties.value.setValue("XYZ");
        assertNull(foo.two.four.value.getValue());
        ComponentProperties oldProp = foo.two.four;
        foo.updateNestedProperties(nestedProperties);
        assertEquals("XYZ", foo.two.four.value.getValue());
        // check that instance have not changed, that only the values have been copied
        assertEquals(oldProp, foo.two.four);
    }
}
