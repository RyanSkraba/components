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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.*;

import java.io.InputStream;
import java.util.Set;

import org.junit.rules.ErrorCollector;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.ComponentImageType;
import org.talend.components.api.component.InputComponentDefinition;
import org.talend.components.api.component.OutputComponentDefinition;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.service.testcomponent.TestComponentDefinition;
import org.talend.components.api.wizard.ComponentWizardDefinition;
import org.talend.components.api.wizard.WizardImageType;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.test.PropertiesTestUtils;

// import static org.hamcrest.Matchers.*;

public class ComponentTestUtils {

    public static Properties checkSerialize(Properties props, ErrorCollector errorCollector) {
        return PropertiesTestUtils.checkSerialize(props, errorCollector);
    }

    /**
     * check all properties of a component for i18n, check form i18n, check ComponentProperties title is i18n
     * 
     * @param componentService where to get all the components
     * @param errorCollector used to collect all errors at once. @see
     *            <a href="http://junit.org/apidocs/org/junit/rules/ErrorCollector.html">ErrorCollector</a>
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
            // check component definition title
            errorCollector.checkThat("missing I18n property :" + cd.getTitle(), cd.getTitle().contains("component."), is(false));
            // check return properties i18n
            checkAllPropertyI18n(cd.getReturnProperties(), cd, errorCollector);
        }
    }

    public static void checkAllPropertyI18n(Property<?>[] propertyArray, Object parent, ErrorCollector errorCollector) {
        if (propertyArray != null) {
            for (Property<?> prop : propertyArray) {
                PropertiesTestUtils.chekProperty(errorCollector, prop, parent);
            }
        } // else no property to check so ignore.
    }

    static public void checkAllI18N(Properties checkedProps, ErrorCollector errorCollector) {
        PropertiesTestUtils.checkAllI18N(checkedProps, errorCollector);
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
            if (cd instanceof TestComponentDefinition) {
                continue;
            }
            Object runtime = null;
            if (cd instanceof InputComponentDefinition) {
                runtime = ((InputComponentDefinition) cd).getRuntime();
            } else if (cd instanceof OutputComponentDefinition) {
                runtime = ((OutputComponentDefinition) cd).getRuntime();
            } else {
                continue;
                // FIXME - need to add support for transformation runtime
            }
            assertNotNull("the Runtime associated with component [" + cd.getName() + "] should never be null.", runtime);
        }
    }

    /**
     * check that the depenencies file is present during integration test.
     * 
     * @param componentService service to get the components to be checked.
     */
    public static void testAllDesignDependenciesPresent(ComponentService componentService, ErrorCollector errorCollector) {
        Set<ComponentDefinition> allComponents = componentService.getAllComponents();
        for (ComponentDefinition compDef : allComponents) {
            errorCollector.checkThat(compDef.getMavenGroupId(), is(not(nullValue())));
            errorCollector.checkThat(compDef.getMavenArtifactId(), is(not(nullValue())));
            Set<String> mavenUriDependencies = componentService.getMavenUriDependencies(compDef.getName());
            errorCollector.checkThat(mavenUriDependencies, is(not(nullValue())));
            errorCollector.checkThat(mavenUriDependencies.isEmpty(), is(false));
        }
    }

    // public static void checkFixedSchemaSetDefinitionSchemaPathAreOk(ComponentService componentService,
    // ErrorCollector errorCollector) {
    // Set<ComponentDefinition> allComponents = componentService.getAllComponents();
    // for (ComponentDefinition cd : allComponents) {
    // if (cd instanceof AbstractFixedSchemaSetComponentDefinition) {
    // AbstractFixedSchemaSetComponentDefinition afsd = (AbstractFixedSchemaSetComponentDefinition) cd;
    // ComponentProperties properties = afsd.createProperties();
    // try {
    // Set<String> outputConnections = afsd.getOutputConnections(properties);
    // String[] outputSchemasPaths = afsd.getOutputSchemasPaths();
    // if (outputSchemasPaths != null && outputConnections != null) {
    // errorCollector.checkThat(
    // cd.getName() + "should not have an SchemaProperties instance related to each schema path :"
    // + ArrayUtils.toString(outputSchemasPaths),
    // outputConnections.size(), equalTo(outputSchemasPaths.length));
    // } else {
    // errorCollector.checkThat(cd.getName() + "should not have an connector if not path is specified."
    // + ArrayUtils.toString(outputSchemasPaths), outputConnections.size(), equalTo(0));
    // }
    // } catch (Exception e) {
    // errorCollector.addError(new Exception(
    // "Failed to compute output connections for class " + cd.getClass().getCanonicalName(), e));
    // }
    // }
    // }
    // }

}
