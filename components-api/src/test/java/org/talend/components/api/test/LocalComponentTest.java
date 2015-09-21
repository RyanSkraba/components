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
package org.talend.components.api.test;

import java.io.InputStream;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.talend.components.api.NamedThing;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.api.internal.SpringApp;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.presentation.Form;
import org.talend.components.api.schema.SchemaElement;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.test.testcomponent.TestComponentDefinition;
import org.talend.components.api.test.testcomponent.TestComponentProperties;
import org.talend.components.api.test.testcomponent.TestComponentWizard;
import org.talend.components.api.test.testcomponent.TestComponentWizardDefinition;
import org.talend.components.api.wizard.ComponentWizard;

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
            assertEquals(prop.getName(), newProps.get(i++).getName());
        }
        i = 0;
        for (Form form : props.getForms()) {
            System.out.println("Form: " + form.getName());
            Form newForm = newForms.get(i++);
            assertEquals(form.getName(), form.getName());
            for (NamedThing formChild : form.getChildren()) {
                System.out.println("  prop: " + formChild.getName());
                assertEquals(formChild.getName(), newForm.getChild(formChild.getName()).getName());
            }
        }
    }



    @Test
    public void testGetPropsList() {
        ComponentProperties props = componentService.getComponentProperties(TestComponentDefinition.COMPONENT_NAME);
        List<SchemaElement> pList = props.getProperties();
        assertTrue(pList.get(0) != null);
        assertEquals(2, pList.size());
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
        InputStream iconStream = componentService.getWizardPngImage(TestComponentWizardDefinition.COMPONENT_WIZARD_NAME);
        assertNotNull(iconStream);
    }

    @Test(expected = ComponentException.class)
    public void testGetWizardIconWrongName() {
        InputStream iconStream = componentService.getWizardPngImage("not an existing wizard name");
        assertNull(iconStream);
    }

    @Test()
    public void testGetWizard() {
        ComponentWizard wizard = componentService.getComponentWizard(TestComponentWizardDefinition.COMPONENT_WIZARD_NAME,
                "userdata");
        assertTrue(wizard instanceof TestComponentWizard);
        assertEquals("userdata", wizard.getUserData());
    }

    @Test(expected = ComponentException.class)
    public void testGetWizardNotFound() {
        ComponentWizard wizard = componentService.getComponentWizard("not found", "userdata");
    }
}
