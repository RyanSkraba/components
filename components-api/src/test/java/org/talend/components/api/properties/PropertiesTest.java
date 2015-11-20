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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.talend.components.api.ComponentTestUtils;
import org.talend.components.api.properties.presentation.Form;
import org.talend.components.api.schema.SchemaElement;
import org.talend.components.api.service.testcomponent.ComponentPropertiesWithDefinedI18N;
import org.talend.components.api.service.testcomponent.TestComponentDefinition;
import org.talend.components.api.service.testcomponent.TestComponentProperties;
import org.talend.components.api.service.testcomponent.nestedprop.NestedComponentProperties;
import org.talend.components.api.service.testcomponent.nestedprop.inherited.InheritedComponentProperties;

public class PropertiesTest {

    @BeforeClass
    public static void init() {
        ComponentTestUtils.setupGlobalContext();
    }

    @AfterClass
    public static void unset() {
        ComponentTestUtils.unsetGlobalContext();
    }

    @Test
    public void testSerializeProp() {
        ComponentProperties props = new TestComponentProperties("test");
        ComponentTestUtils.checkSerialize(props);
    }

    @Test
    public void testSerializeValues() {
        TestComponentProperties props = new TestComponentProperties("test");
        props.setValue(props.userId, "testUser");
        NestedComponentProperties nestedProp = (NestedComponentProperties) props.getProperty("nestedProps");
        nestedProp.setValue(nestedProp.aGreatProperty, "greatness");
        assertNotNull(nestedProp);
        props = (TestComponentProperties) ComponentTestUtils.checkSerialize(props);
        assertEquals("testUser", props.getValue(props.userId));
        assertEquals("greatness", props.nestedProps.getValue(props.nestedProps.aGreatProperty));
    }

    @Test
    public void testi18NForComponentDefintion() {
        TestComponentDefinition tcd = new TestComponentDefinition();
        assertEquals("Test Component", tcd.getDisplayName());
        assertEquals("Ze Test Component Title", tcd.getTitle());
    }

    @Test
    public void testi18NForDirectProperty() {
        TestComponentProperties componentProperties = new TestComponentProperties("test");
        SchemaElement userIdProp = componentProperties.getProperty("userId");
        assertNotNull(userIdProp);
        assertEquals("User Identifier", userIdProp.getDisplayName()); //$NON-NLS-1$
    }

    @Test
    public void testi18NForNestedProperty() {
        TestComponentProperties componentProperties = new TestComponentProperties("test");
        ComponentProperties nestedProp = (ComponentProperties) componentProperties.getProperty("nestedProps");
        assertNotNull(nestedProp);
        SchemaElement greatProperty = nestedProp.getProperty(NestedComponentProperties.A_GREAT_PROP_NAME);
        assertNotNull(greatProperty);
        assertEquals("A Fanstastic Property", greatProperty.getDisplayName()); //$NON-NLS-1$
    }

    @Test
    public void testi18NForNestedPropertyWithDefinedI18N() {
        TestComponentProperties componentProperties = new TestComponentProperties("test");
        ComponentProperties nestedProp = (ComponentProperties) componentProperties
                .getProperty("nestedProp2");
        assertNotNull(nestedProp);
        SchemaElement greatProperty = nestedProp.getProperty(ComponentPropertiesWithDefinedI18N.A_GREAT_PROP_NAME2);
        assertNotNull(greatProperty);
        assertEquals("A second Fanstastic Property", greatProperty.getDisplayName()); //$NON-NLS-1$
    }

    @Test
    public void testi18NForInheritedProperty() {
        TestComponentProperties componentProperties = new TestComponentProperties("test");
        ComponentProperties nestedProp = (ComponentProperties) componentProperties
                .getProperty("nestedProp3");
        assertNotNull(nestedProp);
        SchemaElement greatProperty = nestedProp.getProperty(NestedComponentProperties.A_GREAT_PROP_NAME);
        assertNotNull(greatProperty);
        assertEquals("A Fanstastic Property", greatProperty.getDisplayName()); //$NON-NLS-1$
    }

    @Test
    public void testGetPropsList() {
        TestComponentProperties componentProperties = new TestComponentProperties("test");
        List<SchemaElement> pList = componentProperties.getProperties();
        assertTrue(pList.get(0) != null);
        assertEquals(5, pList.size());
    }

    @Test
    public void testGetPropsListInherited() {
        ComponentProperties componentProperties = new InheritedComponentProperties("test");
        List<SchemaElement> pList = componentProperties.getProperties();
        System.out.println(pList);
        assertTrue(pList.get(0) != null);
        assertEquals(2, pList.size());
    }

    @Test
    public void testGetProps() {
        TestComponentProperties componentProperties = new TestComponentProperties("test");
        Form f = componentProperties.getForm(TestComponentProperties.TESTCOMPONENT);
        assertTrue(f.getWidget("userId").isVisible());
    }

    @Test
    public void testGetPropFields() {
        TestComponentProperties tProps = new TestComponentProperties("test");
        List<String> fieldNames = tProps.getPropertyFieldNames();
        System.out.println(fieldNames);
        assertEquals(5, fieldNames.size());
        assertTrue(tProps.userId == tProps.getPropertyByFieldName("userId"));
        assertTrue(tProps.nestedProps == tProps.getPropertyByFieldName("nestedProps"));
    }

    @Test
    public void testSerialize() {
        TestComponentProperties props = new TestComponentProperties("test");
        ComponentTestUtils.checkSerialize(props);
    }

}
