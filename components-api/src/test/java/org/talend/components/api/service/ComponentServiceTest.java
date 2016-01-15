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

import static org.junit.Assert.*;

import java.io.InputStream;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.ValidationResult;
import org.talend.components.api.properties.ValidationResult.Result;
import org.talend.components.api.properties.presentation.Form;
import org.talend.components.api.schema.AbstractSchemaElement;
import org.talend.components.api.service.internal.ComponentServiceImpl;
import org.talend.components.api.service.testcomponent.TestComponentDefinition;
import org.talend.components.api.service.testcomponent.TestComponentProperties;
import org.talend.components.api.service.testcomponent.TestComponentWizard;
import org.talend.components.api.service.testcomponent.TestComponentWizardDefinition;
import org.talend.components.api.service.testcomponent.nestedprop.NestedComponentProperties;
import org.talend.components.api.wizard.ComponentWizard;
import org.talend.components.api.wizard.WizardImageType;
import org.talend.components.test.ComponentTestUtils;
import org.talend.components.test.SimpleComponentRegistry;

public class ComponentServiceTest extends AbstractComponentTest {

    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();

    private ComponentServiceImpl componentService;

    @Before
    public void initializeComponentRegistryAnsService() {
        // reset the component service
        componentService = null;
    }

    // default implementation for pure java test. Shall be overriden of Spring or OSGI tests
    @Override
    public ComponentService getComponentService() {
        if (componentService == null) {
            SimpleComponentRegistry testComponentRegistry = new SimpleComponentRegistry();
            testComponentRegistry.addComponent(TestComponentDefinition.COMPONENT_NAME, new TestComponentDefinition());
            testComponentRegistry.addWizard(TestComponentWizardDefinition.COMPONENT_WIZARD_NAME,
                    new TestComponentWizardDefinition());
            componentService = new ComponentServiceImpl(testComponentRegistry);
        }
        return componentService;
    }

    @Test
    public void testSupportsProps() throws Throwable {
        ComponentProperties props = getComponentService().getComponentProperties(TestComponentDefinition.COMPONENT_NAME);
        List<ComponentDefinition> comps = getComponentService().getPossibleComponents(props);
        assertEquals("TestComponent", comps.get(0).getName());
        props = new NestedComponentProperties("props");
        comps = getComponentService().getPossibleComponents(props);
        assertEquals(0, comps.size());
    }

    @Test
    public void testBefore() throws Throwable {
        ComponentProperties props = getComponentService().getComponentProperties(TestComponentDefinition.COMPONENT_NAME);

        checkAndBeforePresent(props.getForm(Form.MAIN), "nameList", props);
        assertEquals(3, ((AbstractSchemaElement) props.getProperty("nameList")).getPossibleValues().size());
        assertEquals("name1", ((AbstractSchemaElement) props.getProperty("nameList")).getPossibleValues().get(0));

        checkAndBeforeActivate(props.getForm(Form.MAIN), "nameListRef", props);
        assertEquals(3, ((AbstractSchemaElement) props.getProperty("nameListRef")).getPossibleValues().size());
        assertEquals("namer1", ((AbstractSchemaElement) props.getProperty("nameListRef")).getPossibleValues().get(0));

        assertFalse(props.getForm(Form.MAIN).getWidget("nameList").isCallBeforeActivate());
        assertFalse(props.getForm(Form.MAIN).getWidget("nameListRef").isCallBeforePresent());
    }

    @Test
    public void testBeforePresentWithValidationResults() throws Throwable {
        ComponentProperties props = getComponentService().getComponentProperties(TestComponentDefinition.COMPONENT_NAME);
        assertNull(props.getValidationResult());
        checkAndBeforePresent(props.getForm(Form.MAIN), "nameList", props);
        assertNotNull(props.getValidationResult());
    }

    @Test
    public void testAfterPresentWithValidationResultsWarning() throws Throwable {
        ComponentProperties props = getComponentService().getComponentProperties(TestComponentDefinition.COMPONENT_NAME);
        assertNull(props.getValidationResult());
        checkAndAfter(props.getForm("restoreTest"), "integer", props);
        assertEquals(ValidationResult.Result.WARNING, props.getValidationResult().getStatus());
    }

    @Test
    public void testBeforeActivateWithDefaultValidationResults() throws Throwable {
        ComponentProperties props = getComponentService().getComponentProperties(TestComponentDefinition.COMPONENT_NAME);
        assertNull(props.getValidationResult());
        checkAndBeforeActivate(props.getForm(Form.MAIN), "nameListRef", props);
        assertNotNull(props.getValidationResult());
    }

    @Test
    // TCOMP-15 Handle OK/Cancel button on advanced properties dialog from Wizard
    public void testFormOkCancel() throws Throwable {
        TestComponentProperties props = (TestComponentProperties) getComponentService()
                .getComponentProperties(TestComponentDefinition.COMPONENT_NAME);

        ComponentProperties savedProps = props;
        ComponentProperties savedNested = props.nestedProps;

        Date dateNow = new Date();
        dateNow.setTime(System.currentTimeMillis());
        Date dateLater = new Date();
        dateLater.setTime(dateLater.getTime() + 10000);

        props.userId.setValue("userId");
        props.integer.setValue(1);
        props.decimal.setValue(2);
        props.date.setValue(dateNow);
        props.dateTime.setValue(dateNow);
        props.nestedProps.aGreatProperty.setValue("propPrevious1");
        props.nestedProps.anotherProp.setValue("propPrevious2");

        props = (TestComponentProperties) getComponentService().makeFormCancelable(props, "restoreTest");

        Form form = props.getForm("restoreTest");

        form.setValue("userId", "userIdnew");
        form.setValue("nestedProps.aGreatProperty", "propPrevious1new");

        Date dateTimeLater = new Date();

        form.setValue("integer", 10);
        form.setValue("decimal", 20);
        form.setValue("date", dateLater);
        form.setValue("dateTime", dateLater);

        assertEquals("userId", props.userId.getValue());
        assertEquals("propPrevious1", props.nestedProps.aGreatProperty.getValue());
        assertEquals(1, props.integer.getIntValue());
        // FIXME - finish this
        // assertEquals(2, props.getDecimalValue(props.decimal));
        // assertEquals(dateNow, props.getCalendarValue(props.date));
        assertTrue(props == savedProps);
        assertTrue(props.nestedProps == savedNested);

        props = (TestComponentProperties) getComponentService().commitFormValues(props, "restoreTest");
        assertEquals("userIdnew", props.userId.getValue());
        assertEquals("propPrevious1new", props.nestedProps.aGreatProperty.getValue());
    }

    @Test
    public void testAfterFormFinish() throws Throwable {
        ComponentService componentService = getComponentService();
        ComponentProperties props = componentService.getComponentProperties(TestComponentDefinition.COMPONENT_NAME);
        assertNull(props.getValidationResult());
        componentService.afterFormFinish(Form.MAIN, props);
        assertNotNull(props.getValidationResult());
        assertEquals(Result.ERROR, props.getValidationResult().getStatus());
    }

    @Test
    public void testGetWizardIconOk() {
        InputStream iconStream = getComponentService().getWizardPngImage(TestComponentWizardDefinition.COMPONENT_WIZARD_NAME,
                WizardImageType.TREE_ICON_16X16);
        assertNotNull(iconStream);
    }

    @Test(expected = ComponentException.class)
    public void testGetWizardIconWrongName() {
        InputStream iconStream = getComponentService().getWizardPngImage("not an existing wizard name",
                WizardImageType.TREE_ICON_16X16);
        assertNull(iconStream);
    }

    @Test
    public void testGetWizard() {
        ComponentWizard wizard = getComponentService().getComponentWizard(TestComponentWizardDefinition.COMPONENT_WIZARD_NAME,
                "userdata");
        assertTrue(wizard instanceof TestComponentWizard);
        assertEquals("userdata", wizard.getRepositoryLocation());
    }

    @Test(expected = ComponentException.class)
    public void testGetWizardNotFound() {
        getComponentService().getComponentWizard("not found", "userdata");
    }

    @Test
    public void testGetWizardWithProps() {
        TestComponentWizard wizard = (TestComponentWizard) getComponentService()
                .getComponentWizard(TestComponentWizardDefinition.COMPONENT_WIZARD_NAME, "userdata");
        wizard.props = new TestComponentProperties("props").init();
        ComponentProperties props = wizard.props;
        List<ComponentWizard> wizards = getComponentService().getComponentWizardsForProperties(props, "userdata");
        assertTrue(props == ((TestComponentWizard) wizards.get(0)).props);
    }

    @Test
    public void testFamilies() {
        TestComponentDefinition testComponentDefinition = new TestComponentDefinition();
        assertEquals(2, testComponentDefinition.getFamilies().length);
    }

    @Test
    public void testAlli18n() {
        ComponentTestUtils.testAlli18n(getComponentService(), errorCollector);
    }

    @Test
    public void testAllImages() {
        ComponentTestUtils.testAllImages(getComponentService());
    }

    @Test
    public void testAllRuntime() {
        ComponentTestUtils.testAllRuntimeAvaialble(getComponentService());
    }

}
