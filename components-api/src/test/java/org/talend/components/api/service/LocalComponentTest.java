package org.talend.components.api.service;
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

import static org.junit.Assert.*;

import static org.junit.Assert.*;

import java.io.InputStream;
import java.util.List;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.talend.components.api.NamedThing;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.api.internal.SpringApp;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.presentation.Form;
import org.talend.components.api.schema.SchemaElement;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.service.testcomponent.ComponentPropertiesWithDefinedI18N;
import org.talend.components.api.service.testcomponent.TestComponentDefinition;
import org.talend.components.api.service.testcomponent.TestComponentProperties;
import org.talend.components.api.service.testcomponent.TestComponentWizard;
import org.talend.components.api.service.testcomponent.TestComponentWizardDefinition;
import org.talend.components.api.service.testcomponent.nestedprop.NestedComponentProperties;
import org.talend.components.api.service.testcomponent.nestedprop.inherited.InheritedComponentProperties;
import org.talend.components.api.wizard.ComponentWizard;
import org.talend.components.api.wizard.ComponentWizardDefinition;
import org.talend.components.api.wizard.WizardImageType;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = SpringApp.class)
public class LocalComponentTest {

    @Autowired
    protected ComponentService componentService;

    public LocalComponentTest() {
    }

    public static ComponentProperties checkSerialize(ComponentProperties props) {
        String s = props.toSerialized();
        ComponentProperties.Deserialized d = ComponentProperties.fromSerialized(s);
        ComponentProperties deserProps = d.properties;
        assertFalse(d.migration.isMigrated());
        List<SchemaElement> newProps = deserProps.getProperties();
        List<Form> newForms = deserProps.getForms();
        int i = 0;
        for (NamedThing prop : props.getProperties()) {
            System.out.println(prop.getName());
            assertEquals(prop.getName(), newProps.get(i).getName());
            i++;
        }
        i = 0;
        for (Form form : props.getForms()) {
            System.out.println("Form: " + form.getName());
            Form newForm = newForms.get(i++);
            assertEquals(form.getName(), form.getName());
            for (NamedThing formChild : form.getChildren()) {
                String name = formChild.getName();
                if (formChild instanceof Form) {
                    name = ((Form) formChild).getProperties().getName();
                }
                System.out.println("  prop: " + formChild.getName() + " name to be used: " + name);
                NamedThing newChild = newForm.getChild(name);
                String newName = newChild.getName();
                if (newChild instanceof Form) {
                    newName = ((Form) newChild).getProperties().getName();
                }
                assertEquals(name, newName);
            }
        }
        return deserProps;
    }

    @Test
    public void testGetPropsList() {
        ComponentProperties props = componentService.getComponentProperties(TestComponentDefinition.COMPONENT_NAME);
        List<SchemaElement> pList = props.getProperties();
        assertTrue(pList.get(0) != null);
        assertEquals(4, pList.size());
    }

    @Test
    public void testGetProps() {
        ComponentProperties props = componentService.getComponentProperties(TestComponentDefinition.COMPONENT_NAME);
        Form f = props.getForm(TestComponentProperties.TESTCOMPONENT);
        assertTrue(f.getWidget("userId").isVisible());
    }

    @Test
    public void testGetPropFields() {
        ComponentProperties props = componentService.getComponentProperties(TestComponentDefinition.COMPONENT_NAME);
        TestComponentProperties tProps = (TestComponentProperties) props;
        List<String> fieldNames = props.getPropertyFieldNames();
        System.out.println(fieldNames);
        assertEquals(4, fieldNames.size());
        assertTrue(tProps.userId == props.getPropertyByFieldName("userId"));
        assertTrue(tProps.nestedProps == props.getPropertyByFieldName("nestedProps"));
    }

    @Test
    public void testSerialize() {
        ComponentProperties props = componentService.getComponentProperties(TestComponentDefinition.COMPONENT_NAME);
        checkSerialize(props);
    }

    @Test
    public void testSerializeValues() {
        TestComponentProperties props = (TestComponentProperties) componentService.getComponentProperties(TestComponentDefinition.COMPONENT_NAME);
        props.setValue(props.userId, "testUser");
        NestedComponentProperties nestedProp = (NestedComponentProperties) props.getProperty(NestedComponentProperties.class);
        nestedProp.setValue(nestedProp.aGreatProperty, "greatness");
        assertNotNull(nestedProp);
        props = (TestComponentProperties) checkSerialize(props);
        assertEquals("testUser", props.getValue(props.userId));
        assertEquals("greatness", props.nestedProps.getValue(props.nestedProps.aGreatProperty));
    }

    @Test
    public void testSupportsProps() throws Throwable {
        ComponentProperties props = componentService.getComponentProperties(TestComponentDefinition.COMPONENT_NAME);
        List<ComponentDefinition> comps = componentService.getPossibleComponents(props);
        assertEquals("TestComponent", comps.get(0).getName());
        props = new NestedComponentProperties();
        comps = componentService.getPossibleComponents(props);
        assertEquals(0, comps.size());
    }

    @Test
    public void testGetWizardIconOk() {
        InputStream iconStream = componentService.getWizardPngImage(TestComponentWizardDefinition.COMPONENT_WIZARD_NAME,
                WizardImageType.TREE_ICON_16X16);
        assertNotNull(iconStream);
    }

    @Test(expected = ComponentException.class)
    public void testGetWizardIconWrongName() {
        InputStream iconStream = componentService.getWizardPngImage("not an existing wizard name",
                WizardImageType.TREE_ICON_16X16);
        assertNull(iconStream);
    }

    @Test
    public void testGetWizard() {
        ComponentWizard wizard = componentService.getComponentWizard(TestComponentWizardDefinition.COMPONENT_WIZARD_NAME,
                "userdata");
        assertTrue(wizard instanceof TestComponentWizard);
        assertEquals("userdata", wizard.getRepositoryLocation());
    }

    @Test(expected = ComponentException.class)
    public void testGetWizardNotFound() {
        componentService.getComponentWizard("not found", "userdata");
    }

    @Test
    public void testGetWizardWithProps() {
        TestComponentWizard wizard = (TestComponentWizard) componentService
                .getComponentWizard(TestComponentWizardDefinition.COMPONENT_WIZARD_NAME, "userdata");
        wizard.props = new TestComponentProperties().init();
        ComponentProperties props = wizard.props;
        List<ComponentWizard> wizards = componentService.getComponentWizardsForProperties(props, "userdata");
        assertTrue(props == ((TestComponentWizard) wizards.get(0)).props);
    }

    @Test
    public void testi18NForComponentDefintion() {
        Set<ComponentDefinition> allComponents = componentService.getAllComponents();
        assertEquals(1, allComponents.size());
        ComponentDefinition componentDefinition = allComponents.iterator().next();
        TestComponentDefinition tcd = (TestComponentDefinition) componentDefinition;
        assertEquals("Test Component", tcd.getDisplayName());
        assertEquals("Ze Test Component Title", tcd.getTitle());
    }

    @Test
    public void testi18NForWizardDefintion() {
        Set<ComponentWizardDefinition> wizardDefs = componentService.getTopLevelComponentWizards();
        assertEquals(1, wizardDefs.size());
        ComponentWizardDefinition cwd = wizardDefs.iterator().next();
        assertNotNull(cwd);
        assertEquals("Test Wizard", cwd.getDisplayName());
        assertEquals("Ze Test Wizard Title", cwd.getTitle());
        assertEquals("Ze Test Wizard menu", cwd.getMenuItemName());
    }

    @Test
    public void testi18NForDirectProperty() {
        Set<ComponentDefinition> allComponents = componentService.getAllComponents();
        assertEquals(1, allComponents.size());
        ComponentDefinition componentDefinition = allComponents.iterator().next();
        TestComponentDefinition tcd = (TestComponentDefinition) componentDefinition;
        TestComponentProperties componentProperties = (TestComponentProperties) tcd.createProperties();
        SchemaElement userIdProp = componentProperties.getProperty("userId");
        assertNotNull(userIdProp);
        assertEquals("User Identifier", userIdProp.getDisplayName()); //$NON-NLS-1$
    }

    @Test
    public void testi18NForNestedProperty() {
        TestComponentProperties componentProperties = (TestComponentProperties) componentService
                .getComponentProperties(TestComponentDefinition.COMPONENT_NAME);
        ComponentProperties nestedProp = (ComponentProperties) componentProperties.getProperty(NestedComponentProperties.class);
        assertNotNull(nestedProp);
        SchemaElement greatProperty = nestedProp.getProperty(NestedComponentProperties.A_GREAT_PROP_NAME);
        assertNotNull(greatProperty);
        assertEquals("A Fanstastic Property", greatProperty.getDisplayName()); //$NON-NLS-1$
    }

    @Test
    public void testi18NForNestedPropertyWithDefinedI18N() {
        TestComponentProperties componentProperties = (TestComponentProperties) componentService
                .getComponentProperties(TestComponentDefinition.COMPONENT_NAME);
        ComponentProperties nestedProp = (ComponentProperties) componentProperties
                .getProperty(ComponentPropertiesWithDefinedI18N.class);
        assertNotNull(nestedProp);
        SchemaElement greatProperty = nestedProp.getProperty(ComponentPropertiesWithDefinedI18N.A_GREAT_PROP_NAME2);
        assertNotNull(greatProperty);
        assertEquals("A second Fanstastic Property", greatProperty.getDisplayName()); //$NON-NLS-1$
    }

    @Test
    @Ignore("this does not work yet")
    public void testi18NForInheritedProperty() {
        TestComponentProperties componentProperties = (TestComponentProperties) componentService
                .getComponentProperties(TestComponentDefinition.COMPONENT_NAME);
        ComponentProperties nestedProp = (ComponentProperties) componentProperties
                .getProperty(InheritedComponentProperties.class);
        assertNotNull(nestedProp);
        SchemaElement greatProperty = nestedProp.getProperty(NestedComponentProperties.A_GREAT_PROP_NAME);
        assertNotNull(greatProperty);
        assertEquals("A Fanstastic Property", greatProperty.getDisplayName()); //$NON-NLS-1$
    }

    @Test
    public void testGetDepenencies() {
        // check the comp def return the proper stream for the pom
        TestComponentDefinition testComponentDefinition = new TestComponentDefinition();
        assertNotNull(testComponentDefinition.getMavenPom());
        Set<String> mavenUriDependencies = componentService.getMavenUriDependencies(TestComponentDefinition.COMPONENT_NAME);
        System.out.println("deps:" + mavenUriDependencies);
        assertEquals(5, mavenUriDependencies.size());
    }

}
