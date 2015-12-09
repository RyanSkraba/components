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
package org.talend.components.test;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Set;

import org.talend.components.api.NamedThing;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.presentation.Form;
import org.talend.components.api.schema.SchemaElement;
import org.talend.components.api.service.ComponentService;

public class ComponentTestUtils {

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
                    name = ((Form) formChild).getComponentProperties().getName();
                }
                System.out.println("  prop: " + formChild.getName() + " name to be used: " + name);
                NamedThing newChild = newForm.getChild(name);
                String newName = newChild.getName();
                if (newChild instanceof Form) {
                    newName = ((Form) newChild).getComponentProperties().getName();
                }
                assertEquals(name, newName);
            }
        }
        return deserProps;

    }

    static public void testAlli18n(ComponentService componentService) {
        Set<ComponentDefinition> allComponents = componentService.getAllComponents();
        for (ComponentDefinition cd : allComponents) {
            ComponentProperties props = cd.createProperties();
            checkAllI18NProperties(props);
            // Make sure this translates
            System.out.println(cd.getTitle());
            assertFalse(cd.getTitle().contains("component."));
        }
    }

    static private void checkAllI18NProperties(ComponentProperties checkProps) {
        System.out.println("Checking: " + checkProps);
        List<SchemaElement> properties = checkProps.getProperties();
        for (SchemaElement prop : properties) {
            if (!(prop instanceof ComponentProperties)) {
                assertFalse(
                        "property [" + checkProps.getClass().getCanonicalName() + "/" + prop.getName()
                                + "] should have a translated message key [property." + prop.getName()
                                + ".displayName] in [the proper messages.properties]",
                        prop.getDisplayName().endsWith(".displayName"));
            } else {
                checkAllI18NProperties((ComponentProperties) prop);
            }
        }
    }

}
