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
package org.talend.components.api.service;

import java.io.InputStream;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

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
import org.talend.components.api.service.testcomponent.*;
import org.talend.components.api.wizard.ComponentWizard;
import org.talend.components.api.wizard.ComponentWizardDefinition;
import org.talend.components.api.wizard.WizardImageType;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = SpringApp.class)
public class LocalComponentTest extends TestCase {

    @Autowired
    protected ComponentService componentService;

    public LocalComponentTest() {
    }

    public static void checkSerialize(ComponentProperties props) {
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
                if (formChild instanceof Form)
                    name = ((Form) formChild).getProperties().getName();
                System.out.println("  prop: " + formChild.getName() + " name to be used: " + name);
                NamedThing newChild = newForm.getChild(name);
                String newName = newChild.getName();
                if (newChild instanceof Form)
                    newName = ((Form) newChild).getProperties().getName();
                assertEquals(name, newName);
            }
        }
    }

    @Test
    public void testGetPropsList() {
        ComponentProperties props = componentService.getComponentProperties(TestComponentDefinition.COMPONENT_NAME);
        List<SchemaElement> pList = props.getProperties();
        assertTrue(pList.get(0) != null);
        assertEquals(3, pList.size());
    }

    @Test
    public void testGetProps() {
        ComponentProperties props = componentService.getComponentProperties(TestComponentDefinition.COMPONENT_NAME);
        Form f = props.getForm(TestComponentProperties.TESTCOMPONENT);
        assertTrue(f.getWidget("userId").isVisible());
    }

    @Test
    public void testSerialize() {
        ComponentProperties props = componentService.getComponentProperties(TestComponentDefinition.COMPONENT_NAME);
        checkSerialize(props);
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

    @Test()
    public void testGetWizard() {
        ComponentWizard wizard = componentService.getComponentWizard(TestComponentWizardDefinition.COMPONENT_WIZARD_NAME,
                "userdata");
        assertTrue(wizard instanceof TestComponentWizard);
        assertEquals("userdata", wizard.getRepositoryLocation());
    }

    @Test(expected = ComponentException.class)
    public void testGetWizardNotFound() {
        ComponentWizard wizard = componentService.getComponentWizard("not found", "userdata");
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
        Set<ComponentDefinition> allComponents = componentService.getAllComponents();
        assertEquals(1, allComponents.size());
        ComponentDefinition componentDefinition = allComponents.iterator().next();
        TestComponentDefinition tcd = (TestComponentDefinition) componentDefinition;
        TestComponentProperties componentProperties = (TestComponentProperties) tcd.createProperties();
        ComponentProperties nestedProp = (ComponentProperties) componentProperties.getProperty("thenestedproperty"); //$NON-NLS-1$
        assertNotNull(nestedProp);
        SchemaElement greatProperty = nestedProp.getProperty(NestedComponentProperties.A_GREAT_PROP_NAME);
        assertNotNull(greatProperty);
        assertEquals("A Fanstastic Property", greatProperty.getDisplayName()); //$NON-NLS-1$
    }

}
