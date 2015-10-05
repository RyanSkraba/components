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
package org.talend.components.api;

import static org.junit.Assert.*;
import static org.ops4j.pax.exam.CoreOptions.*;

import java.io.InputStream;
import java.util.Set;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.ComponentImageType;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.wizard.ComponentWizardDefinition;
import org.talend.components.api.wizard.WizardImageType;
import org.talend.components.salesforce.SalesforceConnectionProperties;
import org.talend.components.salesforce.SalesforceConnectionWizardDefinition;
import org.talend.components.salesforce.tsalesforceconnection.TSalesforceConnectionDefinition;
import org.talend.components.salesforce.tsalesforceinput.TSalesforceInputDefinition;
import org.talend.components.salesforce.tsalesforceinput.TSalesforceInputProperties;

/**
 * created by sgandon on 7 sept. 2015 Detailled comment
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class TestComponentServiceWithComponents {

    @Inject
    private ComponentService componentService;

    @Configuration
    public Option[] config() {

        return options(composite(PaxExamOptions.getOptions()),
                provision(mavenBundle().groupId("org.talend.components").artifactId("components-common"),
                        mavenBundle().groupId("org.talend.components").artifactId("components-common-oauth"),
                        mavenBundle().groupId("org.talend.components").artifactId("components-salesforce")),
                junitBundles()
        // these debug option do not work, I still don't know how to debug this :, cleanCaches(),
        // vmOption("-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"),systemTimeout(0)
        );
    }

    @Test
    public void testTSalesforceConnectExists() {
        System.out.println("JUNIT test start.... *********************");
        assertNotNull(componentService);
        assertNotNull(componentService.getComponentProperties("tSalesforceConnection")); //$NON-NLS-1$
    }

    @Test
    public void testComponentsNameNotEmpty() {
        assertNotNull(componentService);
        Set<String> allComponentsName = componentService.getAllComponentNames();
        assertFalse(allComponentsName.isEmpty());
    }

    @Test
    public void testComponentWizards() {
        assertNotNull(componentService);
        Set<ComponentWizardDefinition> allWizards = componentService.getTopLevelComponentWizards();
        assertFalse(allWizards.isEmpty());
    }

    @Test
    public void testAllComponents() {
        assertNotNull(componentService);
        Set<ComponentDefinition> allComponents = componentService.getAllComponents();
        assertFalse(allComponents.isEmpty());
    }

    @Test
    public void testWizardIconForSalesForce() {
        assertNotNull(componentService);
        InputStream wizardPngIconStream = componentService
                .getWizardPngImage(SalesforceConnectionWizardDefinition.COMPONENT_WIZARD_NAME, WizardImageType.TREE_ICON_16X16);
        assertNotNull(wizardPngIconStream);
    }

    @Test
    public void testComponentImageForSalesforce() {
        assertNotNull(componentService);
        InputStream componentPngIconStream = componentService.getComponentPngImage(TSalesforceInputDefinition.COMPONENT_NAME,
                ComponentImageType.PALLETE_ICON_32X32);
        assertNotNull(componentPngIconStream);
    }

    @Test
    public void testI18nDirectPropertyForSalesforce() {
        assertNotNull(componentService);
        TSalesforceInputProperties siProps = (TSalesforceInputProperties) componentService
                .getComponentProperties(TSalesforceInputDefinition.COMPONENT_NAME);
        assertNotNull(siProps);
        assertEquals("Column Name Delimiter", siProps.getProperty("ColumnNameDelimiter").getDisplayName()); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Test
    public void testI18nNestedPropertyForSalesforce() {
        assertNotNull(componentService);
        SalesforceConnectionProperties scProps = (SalesforceConnectionProperties) componentService
                .getComponentProperties(TSalesforceConnectionDefinition.COMPONENT_NAME);
        assertNotNull(scProps);
        assertEquals("Client Id", ((ComponentProperties) scProps.getProperty("oauth")).getProperty("clientId").getDisplayName()); //$NON-NLS-1$
    }

    @Test
    public void testI18nWizardMenuForSalesforce() {
        assertNotNull(componentService);
        Set<ComponentWizardDefinition> wizardDefs = componentService.getTopLevelComponentWizards();
        ComponentWizardDefinition salesforceWizDef = findWizardDefinition(wizardDefs, "salesforce");
        assertNotNull(salesforceWizDef);
        assertEquals("Salesforce Connection", salesforceWizDef.getMenuItemName()); //$NON-NLS-1$
    }

    /**
     * DOC sgandon Comment method "findWizardDefinition".
     * 
     * @param wizardDefs
     * @param wizardName
     * @return
     */
    private ComponentWizardDefinition findWizardDefinition(Set<ComponentWizardDefinition> wizardDefs, String wizardName) {
        for (ComponentWizardDefinition cwd : wizardDefs) {
            if (wizardName.equals(cwd.getName())) {
                return cwd;
            } // else keep looking
        }
        return null;
    }

}
