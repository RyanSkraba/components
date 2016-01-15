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

import java.util.List;

import org.junit.Test;
import org.talend.components.api.NamedThing;
import org.talend.components.api.properties.presentation.Form;
import org.talend.components.api.service.testcomponent.ComponentPropertiesWithDefinedI18N;
import org.talend.components.api.service.testcomponent.TestComponentDefinition;
import org.talend.components.api.service.testcomponent.TestComponentProperties;
import org.talend.components.api.service.testcomponent.nestedprop.NestedComponentProperties;
import org.talend.components.api.service.testcomponent.nestedprop.inherited.InheritedComponentProperties;
import org.talend.components.test.ComponentTestUtils;

public class PropertiesTest {

    @Test
    public void testSerializeProp() {
        ComponentProperties props = new TestComponentProperties("test").init();
        ComponentTestUtils.checkSerialize(props);
    }

    @Test
    public void testSerializeValues() {
        TestComponentProperties props = (TestComponentProperties) new TestComponentProperties("test").init();
        props.userId.setValue("testUser");
        props.password.setValue("testPassword");
        assertTrue(props.password.getFlags().contains(Property.Flags.ENCRYPT));
        assertTrue(props.password.getFlags().contains(Property.Flags.SUPPRESS_LOGGING));
        assertTrue(props.password.getFlags().contains(Property.Flags.UI_PASSWORD));
        NestedComponentProperties nestedProp = (NestedComponentProperties) props.getProperty("nestedProps");
        nestedProp.aGreatProperty.setValue("greatness");
        assertNotNull(nestedProp);
        props = (TestComponentProperties) ComponentTestUtils.checkSerialize(props);

        // Should be encrypted
        assertFalse(props.toSerialized().contains("testPassword"));

        assertEquals("testUser", props.userId.getStringValue());
        assertEquals("testPassword", props.password.getValue());
        assertEquals("greatness", props.nestedProps.aGreatProperty.getValue());

    }

    @Test
    public void testGetProperty() {
        TestComponentProperties props = (TestComponentProperties) new TestComponentProperties("test").init();
        assertEquals("userId", props.getProperty("userId").getName());
        assertEquals("integer", props.getProperty("integer").getName());
        assertEquals("aGreatProperty", props.getProperty("nestedProps.aGreatProperty").getName());
    }

    @Test
    public void testFindForm() {
        TestComponentProperties props = (TestComponentProperties) new TestComponentProperties("test").init();
        Form main = props.getForm(Form.MAIN);
        assertTrue(main == props.mainForm);
        assertEquals(Form.MAIN, main.getName());
        Form restoreTest = props.getForm("restoreTest");
        assertTrue(restoreTest == props.restoreForm);
        assertEquals("restoreTest", restoreTest.getName());
    }

    @Test
    public void testCopyValues() {
        TestComponentProperties props = (TestComponentProperties) new TestComponentProperties("test1").init();
        props.integer.setValue(1);
        props.userId.setValue("User1");
        ((Property) props.getProperty("nestedProps.aGreatProperty")).setValue("great1");

        TestComponentProperties props2 = (TestComponentProperties) new TestComponentProperties("test2").init();
        props2.copyValuesFrom(props);
        assertEquals(1, ((Property) props2.getProperty("integer")).getIntValue());
        assertEquals("User1", ((Property) props2.getProperty("userId")).getStringValue());
        assertEquals("great1", ((Property) props2.getProperty("nestedProps.aGreatProperty")).getStringValue());
    }

    @Test
    public void testWrongFieldAndPropertyName() {
        TestComponentProperties props = (TestComponentProperties) new TestComponentProperties("test1").init();
        props.setValue("nestedProps.aGreatProperty", "great1");
        assertEquals("great1", ((Property) props.getProperty("nestedProps.aGreatProperty")).getStringValue());
        try {
            props.setValue("nestedProps", "bad");
            fail("did not get expected exception");
        } catch (IllegalArgumentException ex) {
            // expected
        }
    }

    @Test
    public void testSetValueQualified() {
        TestComponentProperties props = (TestComponentProperties) new TestComponentProperties("test1").init();
        props.setValue("nestedProps.aGreatProperty", "great1");
        assertEquals("great1", ((Property) props.getProperty("nestedProps.aGreatProperty")).getStringValue());
        try {
            props.setValue("nestedProps", "bad");
            fail("did not get expected exception");
        } catch (IllegalArgumentException ex) {
            // expected
        }
    }

    @Test
    public void testi18NForComponentDefintion() {
        TestComponentDefinition tcd = new TestComponentDefinition();
        assertEquals("Test Component", tcd.getDisplayName());
        assertEquals("Ze Test Component Title", tcd.getTitle());
    }

    @Test
    public void testi18NForDirectProperty() {
        TestComponentProperties componentProperties = (TestComponentProperties) new TestComponentProperties("test").init();
        NamedThing userIdProp = componentProperties.getProperty("userId");
        assertNotNull(userIdProp);
        assertEquals("User Identifier", userIdProp.getDisplayName()); //$NON-NLS-1$
    }

    @Test
    public void testi18NForNestedProperty() {
        TestComponentProperties componentProperties = (TestComponentProperties) new TestComponentProperties("test").init();
        ComponentProperties nestedProp = (ComponentProperties) componentProperties.getProperty("nestedProps");
        assertNotNull(nestedProp);
        NamedThing greatProperty = nestedProp.getProperty(NestedComponentProperties.A_GREAT_PROP_NAME);
        assertNotNull(greatProperty);
        assertEquals("A Fanstastic Property", greatProperty.getDisplayName()); //$NON-NLS-1$
    }

    @Test
    public void testi18NForNestedPropertyWithDefinedI18N() {
        TestComponentProperties componentProperties = (TestComponentProperties) new TestComponentProperties("test").init();
        ComponentProperties nestedProp = (ComponentProperties) componentProperties.getProperty("nestedProp2");
        assertNotNull(nestedProp);
        NamedThing greatProperty = nestedProp.getProperty(ComponentPropertiesWithDefinedI18N.A_GREAT_PROP_NAME2);
        assertNotNull(greatProperty);
        assertEquals("A second Fanstastic Property", greatProperty.getDisplayName()); //$NON-NLS-1$
    }

    @Test
    public void testi18NForInheritedProperty() {
        TestComponentProperties componentProperties = (TestComponentProperties) new TestComponentProperties("test").init();
        ComponentProperties nestedProp = (ComponentProperties) componentProperties.getProperty("nestedProp3");
        assertNotNull(nestedProp);
        NamedThing greatProperty = nestedProp.getProperty(NestedComponentProperties.A_GREAT_PROP_NAME);
        assertNotNull(greatProperty);
        assertEquals("A Fanstastic Property", greatProperty.getDisplayName()); //$NON-NLS-1$
    }

    @Test
    public void testGetPropsList() {
        TestComponentProperties componentProperties = (TestComponentProperties) new TestComponentProperties("test").init();
        List<NamedThing> pList = componentProperties.getProperties();
        assertTrue(pList.get(0) != null);
        assertEquals(13, pList.size());
    }

    @Test
    public void testGetPropsListInherited() {
        ComponentProperties componentProperties = new InheritedComponentProperties("test");
        List<NamedThing> pList = componentProperties.getProperties();
        System.out.println(pList);
        assertTrue(pList.get(0) != null);
        assertEquals(3, pList.size());
    }

    @Test
    public void testGetProps() {
        TestComponentProperties componentProperties = (TestComponentProperties) new TestComponentProperties("test").init();
        Form f = componentProperties.getForm(Form.MAIN);
        assertTrue(f.getWidget("userId").isVisible());
    }

    // @Test
    // public void testGetPropFields() {
    // TestComponentProperties tProps = (TestComponentProperties) new TestComponentProperties("test").init();
    // List<String> fieldNames = tProps.getPropertyFieldNames();
    // System.out.println(fieldNames);
    // assertEquals(11, fieldNames.size());
    // assertTrue(tProps.userId == tProps.getPropertyByFieldName("userId"));
    // assertTrue(tProps.nestedProps == tProps.getPropertyByFieldName("nestedProps"));
    // }

    @Test(expected = IllegalArgumentException.class)
    public void testGetValuedProperties() {
        TestComponentProperties tProps = (TestComponentProperties) new TestComponentProperties("test").init();
        assertNotNull(tProps.getValuedProperty("date"));
        assertNull(tProps.getValuedProperty("nestedProps"));
        // expected to throw exception
        tProps.getValuedProperty("foo.nestedProps");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetComponentProperties() {
        TestComponentProperties tProps = (TestComponentProperties) new TestComponentProperties("test").init();
        assertNotNull(tProps.getComponentProperties("nestedProps"));
        assertNull(tProps.getComponentProperties("date"));
        // expected to throw exception
        tProps.getComponentProperties("foo.nestedProps");
    }

    @Test
    public void testSerialize() {
        TestComponentProperties props = (TestComponentProperties) new TestComponentProperties("test").init();
        ComponentTestUtils.checkSerialize(props);
    }

    @Test
    public void testPropertyInitializedDuringSetup() {
        TestComponentProperties props = (TestComponentProperties) new TestComponentProperties("test").init();
        // check that getValue returns null, cause is not initialized it will throw an NPE.
        assertNull(props.initLater.getValue());
        assertNull(props.nestedInitLater.anotherProp.getValue());
    }

    @Test
    public void testCreatePropertiesForRuntime() {
        TestComponentProperties props = (TestComponentProperties) new TestComponentProperties("test").initForRuntime();
        assertNull(props.initLater.getValue());
        assertNull(props.mainForm);
    }

}
