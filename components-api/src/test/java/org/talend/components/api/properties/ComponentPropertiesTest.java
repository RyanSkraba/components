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

import org.apache.commons.lang3.reflect.TypeUtils;
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
    
    /**
     * Test class for {@link ComponentPropertiesTest#testReturnPropertiesDisplayName()},
     * which contains common returns properties
     */
    private final class WithCommonReturnsProperties extends ComponentPropertiesImpl {

        public Property<Integer> NB_LINE = ComponentPropertyFactory.newReturnProperty(getReturns(), PropertyFactory.newInteger(NB_LINE_NAME));
        
        public Property<Integer> NB_SUCCESS = ComponentPropertyFactory.newReturnProperty(getReturns(), PropertyFactory.newInteger(NB_SUCCESS_NAME));
        
        public Property<Integer> NB_REJECT = ComponentPropertyFactory.newReturnProperty(getReturns(), PropertyFactory.newInteger(NB_REJECT_NAME));
        
        public Property<Integer> ERROR_MESSAGE = ComponentPropertyFactory.newReturnProperty(getReturns(), PropertyFactory.newInteger(ERROR_MESSAGE_NAME));
        
        public WithCommonReturnsProperties(String name) {
            super(name);
        }        
    }

    ComponentPropertiesTestClass foo;

    @Before
    public void init() {
        foo = (ComponentPropertiesTestClass) new ComponentPropertiesTestClass("foo").init();

    }

    @Test
    public void testSetReturnsProperty() {
        Property<String> element = ComponentPropertyFactory.newReturnsProperty();
        assertEquals("returns", element.getName());
        assertEquals(TypeUtils.toString(String.class), element.getType());
    }

    @Test
    public void testNewReturnProperty() throws IllegalAccessException {
        Property<String> element = ComponentPropertyFactory.newReturnsProperty();
        Property<Boolean> returnProperty = ComponentPropertyFactory.newReturnProperty(element,
                PropertyFactory.newBoolean("childName"));
        assertEquals("childName", returnProperty.getName());
        assertEquals(TypeUtils.toString(Boolean.class), returnProperty.getType());
        assertEquals(returnProperty, element.getChild("childName"));
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
    
    /**
     * Checks display names for common returns properties are correctly set in ComponentPropertiesImpl.properties
     */
    @Test
    public void testReturnPropertiesDisplayName() {
        WithCommonReturnsProperties properties = new WithCommonReturnsProperties("foo");
        properties.init();

        assertEquals("Number of line", properties.NB_LINE.getDisplayName());
        assertEquals("Number of success", properties.NB_SUCCESS.getDisplayName());
        assertEquals("Number of reject", properties.NB_REJECT.getDisplayName());
        assertEquals("Error Message", properties.ERROR_MESSAGE.getDisplayName());
    }
}
