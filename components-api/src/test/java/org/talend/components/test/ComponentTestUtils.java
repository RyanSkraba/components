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

import static org.hamcrest.CoreMatchers.*;
// import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.InputStream;
import java.util.List;
import java.util.Set;

import org.junit.rules.ErrorCollector;
import org.talend.components.api.NamedThing;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.ComponentImageType;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.Properties;
import org.talend.components.api.properties.presentation.Form;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.wizard.ComponentWizardDefinition;
import org.talend.components.api.wizard.WizardImageType;

public class ComponentTestUtils {

    public static Properties checkSerialize(Properties props, ErrorCollector errorCollector) {
        String s = props.toSerialized();
        Properties.Deserialized d = Properties.fromSerialized(s);
        Properties deserProps = d.properties;
        checkAllI18N(deserProps, errorCollector);
        assertFalse(d.migration.isMigrated());
        List<NamedThing> newProps = deserProps.getProperties();
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

    /**
     * check all properties of a component for i18n, check form i18n, check ComponentProperties title is i18n
     * 
     * @param componentService where to get all the components
     * @param errorCollector used to collect all errors at once. @see
     * <a href="http://junit.org/apidocs/org/junit/rules/ErrorCollector.html">ErrorCollector</a>
     */
    static public void testAlli18n(ComponentService componentService, ErrorCollector errorCollector) {
        Set<ComponentDefinition> allComponents = componentService.getAllComponents();
        for (ComponentDefinition cd : allComponents) {
            ComponentProperties props = cd.createProperties();
            // check all properties
            if (props != null) {
                checkAllI18N(props, errorCollector);
            } else {
                System.out.println("No properties to check fo I18n for :" + cd.getName());
            }
            // check component properties title
            errorCollector.checkThat("missing I18n property :" + cd.getTitle(), cd.getTitle().contains("component."), is(false));
        }
    }

    /**
     * check that all Components have theirs internationnalisation properties setup correctly.
     * 
     * @param errorCollector
     * 
     * @param componentService service to get the components to be checked.
     */
    static public void checkAllI18N(Properties checkedProps, ErrorCollector errorCollector) {
        if (checkedProps == null) {
            System.out.println("No properties to be checked.");
        } else {
            // checking properties
            System.out.println("Checking: " + checkedProps);
            List<NamedThing> properties = checkedProps.getProperties();
            for (NamedThing prop : properties) {
                if (!(prop instanceof ComponentProperties)) {
                    errorCollector.checkThat(
                            "property [" + checkedProps.getClass().getCanonicalName() + "/" + prop.getName()
                                    + "] should have a translated message key [property." + prop.getName()
                                    + ".displayName] in [the proper messages.properties]",
                            prop.getDisplayName().endsWith(".displayName"), is(false));
                } else {
                    checkAllI18N((ComponentProperties) prop, errorCollector);
                }
            }
            // check forms
            List<Form> forms = checkedProps.getForms();
            for (Form form : forms) {
                errorCollector.checkThat(
                        "Form [" + form.getComponentProperties().getClass().getCanonicalName() + "/" + form.getName()
                                + "] should have a translated message key [form." + form.getName()
                                + ".displayName] in [the proper messages.properties]",
                        form.getDisplayName().endsWith(".displayName"), is(false));
                errorCollector.checkThat("Form [" + form.getComponentProperties().getClass().getCanonicalName() + "/"
                        + form.getName() + "] should have a translated message key [form." + form.getName()
                        + ".title] in [the proper messages.properties]", form.getTitle().endsWith(".title"), is(false));

            }

        }
    }

    /**
     * check that all Components and Wizards have theirs images properly set.
     * 
     * @param componentService service to get the components to be checked.
     */
    public static void testAllImages(ComponentService componentService) {
        // check components
        Set<ComponentDefinition> allComponents = componentService.getAllComponents();
        for (ComponentDefinition compDef : allComponents) {
            for (ComponentImageType compIT : ComponentImageType.values()) {
                String pngImagePath = compDef.getPngImagePath(compIT);
                assertNotNull("the component [" + compDef.getName() + "] must return an image path for type [" + compIT + "]",
                        pngImagePath);
                InputStream resourceAsStream = compDef.getClass().getResourceAsStream(pngImagePath);
                assertNotNull(
                        "Failed to find the image for path [" + pngImagePath + "] for the component:type [" + compDef.getName()
                                + ":" + compIT + "].\nIt should be located at ["
                                + compDef.getClass().getPackage().getName().replace('.', '/') + "/" + pngImagePath + "]",
                        resourceAsStream);
            }
        }
        // check wizards
        Set<ComponentWizardDefinition> allWizards = componentService.getTopLevelComponentWizards();
        for (ComponentWizardDefinition wizDef : allWizards) {
            for (WizardImageType wizIT : WizardImageType.values()) {
                String pngImagePath = wizDef.getPngImagePath(wizIT);
                assertNotNull("the wizard [" + wizDef.getName() + "] must return an image path for type [" + wizIT + "]",
                        pngImagePath);
                InputStream resourceAsStream = wizDef.getClass().getResourceAsStream(pngImagePath);
                assertNotNull(
                        "Failed to find the image for path [" + pngImagePath + "] for the component:type [" + wizDef.getName()
                                + ":" + wizIT + "].\nIt should be located at ["
                                + wizDef.getClass().getPackage().getName().replace('.', '/') + "/" + pngImagePath + "]",
                        resourceAsStream);
            }
        }
    }

    /**
     * check that all Components have a runtime not null.
     * 
     * @param componentService service to get the components to be checked.
     */
    public static void testAllRuntimeAvaialble(ComponentService componentService) {
        Set<ComponentDefinition> allComponents = componentService.getAllComponents();
        for (ComponentDefinition cd : allComponents) {
            assertNotNull("the Runtime associated with component [" + cd.getName() + "] should never be null.",
                    cd.createRuntime());
        }
    }

}
