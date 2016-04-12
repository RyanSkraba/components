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
package org.talend.components.salesforce;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Ignore;
import org.junit.Test;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.container.DefaultComponentRuntimeContainerImpl;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.ComponentReferenceProperties;
import org.talend.components.api.test.ComponentTestUtils;
import org.talend.components.api.wizard.ComponentWizard;
import org.talend.components.api.wizard.ComponentWizardDefinition;
import org.talend.components.api.wizard.WizardImageType;
import org.talend.components.common.oauth.OauthProperties;
import org.talend.components.salesforce.runtime.SalesforceSourceOrSink;
import org.talend.components.salesforce.tsalesforceconnection.TSalesforceConnectionDefinition;
import org.talend.components.salesforce.tsalesforceinput.TSalesforceInputDefinition;
import org.talend.components.salesforce.tsalesforceinput.TSalesforceInputProperties;
import org.talend.components.salesforce.tsalesforceoutput.TSalesforceOutputDefinition;
import org.talend.components.salesforce.tsalesforceoutput.TSalesforceOutputProperties;
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.PresentationItem;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.Property;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.service.PropertiesServiceTest;
import org.talend.daikon.properties.service.Repository;
import org.talend.daikon.properties.test.PropertiesTestUtils;

public class SalesforceComponentTestIT extends SalesforceTestBase {

    public SalesforceComponentTestIT() {
        super();
    }

    @Override
    protected ComponentProperties checkAndAfter(Form form, String propName, ComponentProperties props) throws Throwable {
        assertTrue(form.getWidget(propName).isCallAfter());
        return getComponentService().afterProperty(propName, props);
    }

    @Test
    public void testGetProps() {
        ComponentProperties props = new TSalesforceConnectionDefinition().createProperties();
        Form f = props.getForm(Form.MAIN);
        ComponentTestUtils.checkSerialize(props, errorCollector);
        System.out.println(f);
        System.out.println(props);
        assertEquals(Form.MAIN, f.getName());
    }

    @Test
    public void testAfterLoginType() throws Throwable {
        ComponentProperties props;

        props = new TSalesforceConnectionDefinition().createProperties();
        ComponentTestUtils.checkSerialize(props, errorCollector);
        Property loginType = (Property) props.getProperty("loginType");
        System.out.println(loginType.getPossibleValues());
        assertEquals("Basic", loginType.getPossibleValues().get(0).toString());
        assertEquals("OAuth", loginType.getPossibleValues().get(1).toString());
        assertEquals(SalesforceConnectionProperties.LOGIN_BASIC, loginType.getValue());
        Form mainForm = props.getForm(Form.MAIN);
        assertEquals("Salesforce Connection Settings", mainForm.getTitle());
        assertTrue(mainForm.getWidget(SalesforceUserPasswordProperties.class).isVisible());
        assertFalse(mainForm.getWidget(OauthProperties.class).isVisible());

        loginType.setValue(SalesforceConnectionProperties.LOGIN_OAUTH);
        props = checkAndAfter(mainForm, "loginType", props);
        mainForm = props.getForm(Form.MAIN);
        assertTrue(mainForm.isRefreshUI());

        assertFalse(mainForm.getWidget(SalesforceUserPasswordProperties.class).isVisible());
        assertTrue(mainForm.getWidget(OauthProperties.class).isVisible());
    }

    @Test
    public void testInputProps() throws Throwable {
        TSalesforceInputProperties props = (TSalesforceInputProperties) new TSalesforceInputDefinition().createProperties();
        assertEquals(2, props.queryMode.getPossibleValues().size());
        Property returns = (Property) props.getProperty(ComponentProperties.RETURNS);
        assertEquals("NB_LINE", returns.getChildren().get(0).getName());
    }

    static class RepoProps {

        Properties props;

        String name;

        String repoLocation;

        Schema schema;

        String schemaPropertyName;

        RepoProps(Properties props, String name, String repoLocation, String schemaPropertyName) {
            this.props = props;
            this.name = name;
            this.repoLocation = repoLocation;
            this.schemaPropertyName = schemaPropertyName;
            if (schemaPropertyName != null) {
                this.schema = new Schema.Parser().parse(props.getValuedProperty(schemaPropertyName).getStringValue());
            }
        }

        @Override
        public String toString() {
            return "RepoProps: " + repoLocation + "/" + name + " props: " + props;
        }
    }

    class TestRepository implements Repository {

        private int locationNum;

        public String componentIdToCheck;

        public ComponentProperties properties;

        public List<RepoProps> repoProps;

        TestRepository(List<RepoProps> repoProps) {
            this.repoProps = repoProps;
        }

        @Override
        public String storeProperties(Properties properties, String name, String repositoryLocation, String schemaPropertyName) {
            RepoProps rp = new RepoProps(properties, name, repositoryLocation, schemaPropertyName);
            repoProps.add(rp);
            System.out.println(rp);
            return repositoryLocation + ++locationNum;
        }
    }

    class TestRuntimeContainer extends DefaultComponentRuntimeContainerImpl {
    }

    @Test
    public void testFamily() {
        ComponentDefinition cd = getComponentService().getComponentDefinition("tSalesforceConnectionNew");
        assertEquals(2, cd.getFamilies().length);
        assertEquals("Business/Salesforce", cd.getFamilies()[0]);
        assertEquals("Cloud/Salesforce", cd.getFamilies()[1]);
    }

    @Test
    public void testWizard() throws Throwable {
        final List<RepoProps> repoProps = new ArrayList<>();

        Repository repo = new TestRepository(repoProps);
        getComponentService().setRepository(repo);

        Set<ComponentWizardDefinition> wizards = getComponentService().getTopLevelComponentWizards();
        int count = 0;
        ComponentWizardDefinition wizardDef = null;
        for (ComponentWizardDefinition wizardDefinition : wizards) {
            if (wizardDefinition instanceof SalesforceConnectionWizardDefinition) {
                wizardDef = wizardDefinition;
                count++;
            }
        }
        assertEquals(1, count);
        assertEquals("Create SalesforceNew Connection", wizardDef.getMenuItemName());
        ComponentWizard wiz = getComponentService().getComponentWizard(SalesforceConnectionWizardDefinition.COMPONENT_WIZARD_NAME,
                "nodeSalesforce");
        assertNotNull(wiz);
        assertEquals("nodeSalesforce", wiz.getRepositoryLocation());
        SalesforceConnectionWizard swiz = (SalesforceConnectionWizard) wiz;
        List<Form> forms = wiz.getForms();
        Form connFormWizard = forms.get(0);
        assertEquals("Wizard", connFormWizard.getName());
        assertFalse(connFormWizard.isAllowBack());
        assertFalse(connFormWizard.isAllowForward());
        assertFalse(connFormWizard.isAllowFinish());
        // Main from SalesforceModuleListProperties
        assertEquals("Main", forms.get(1).getName());
        assertEquals("Salesforce Connection Settings", connFormWizard.getTitle());
        assertEquals("Complete these fields in order to connect to your Salesforce account.", connFormWizard.getSubtitle());

        SalesforceConnectionProperties connProps = (SalesforceConnectionProperties) connFormWizard.getProperties();

        Form af = connProps.getForm(Form.ADVANCED);
        assertTrue(
                ((PresentationItem) connFormWizard.getWidget("advanced").getContent()).getFormtoShow() + " should be == to " + af,
                ((PresentationItem) connFormWizard.getWidget("advanced").getContent()).getFormtoShow() == af);

        Object image = getComponentService().getWizardPngImage(SalesforceConnectionWizardDefinition.COMPONENT_WIZARD_NAME,
                WizardImageType.TREE_ICON_16X16);
        assertNotNull(image);
        image = getComponentService().getWizardPngImage(SalesforceConnectionWizardDefinition.COMPONENT_WIZARD_NAME,
                WizardImageType.WIZARD_BANNER_75X66);
        assertNotNull(image);

        // Check the non-top-level wizard

        // check password i18n
        assertEquals("Name", connProps.getProperty("name").getDisplayName());
        connProps.name.setValue("connName");
        setupProps(connProps, !ADD_QUOTES);
        Form userPassword = (Form) connFormWizard.getWidget("userPassword").getContent();
        Property passwordSe = (Property) userPassword.getWidget("password").getContent();
        assertEquals("Password", passwordSe.getDisplayName());
        // check name i18n
        NamedThing nameProp = connFormWizard.getWidget("name").getContent(); //$NON-NLS-1$
        assertEquals("Name", nameProp.getDisplayName());
        connProps = (SalesforceConnectionProperties) PropertiesServiceTest.checkAndValidate(getComponentService(), connFormWizard,
                "testConnection", connProps);
        assertTrue(connFormWizard.isAllowForward());

        Form modForm = forms.get(1);
        SalesforceModuleListProperties mlProps = (SalesforceModuleListProperties) modForm.getProperties();
        assertFalse(modForm.isCallAfterFormBack());
        assertFalse(modForm.isCallAfterFormNext());
        assertTrue(modForm.isCallAfterFormFinish());
        assertTrue(modForm.isCallBeforeFormPresent());
        assertFalse(modForm.isAllowBack());
        assertFalse(modForm.isAllowForward());
        assertFalse(modForm.isAllowFinish());
        mlProps = (SalesforceModuleListProperties) getComponentService().beforeFormPresent(modForm.getName(), mlProps);
        assertTrue(modForm.isAllowBack());
        assertFalse(modForm.isAllowForward());
        assertTrue(modForm.isAllowFinish());
        System.out.println(mlProps.moduleName.getValue());
        @SuppressWarnings("unchecked")
        List<NamedThing> all = (List<NamedThing>) mlProps.moduleName.getValue();
        assertNull(all);
        // TCOMP-9 Change the module list to use getPossibleValues() for SalesforceModuleListProperties
        List<NamedThing> possibleValues = (List<NamedThing>) mlProps.moduleName.getPossibleValues();
        assertTrue(possibleValues.size() > 50);
        List<NamedThing> selected = new ArrayList<>();
        selected.add(possibleValues.get(0));
        selected.add(possibleValues.get(2));
        selected.add(possibleValues.get(3));

        mlProps.moduleName.setValue(selected);
        getComponentService().afterFormFinish(modForm.getName(), mlProps);
        System.out.println(repoProps);
        assertEquals(4, repoProps.size());
        int i = 0;
        for (RepoProps rp : repoProps) {
            if (i == 0) {
                assertEquals("connName", rp.name);
                SalesforceConnectionProperties storedConnProps = (SalesforceConnectionProperties) rp.props;
                assertEquals(userId, storedConnProps.userPassword.userId.getValue());
                assertEquals(password, storedConnProps.userPassword.password.getValue());
            } else {
                SalesforceModuleProperties storedModule = (SalesforceModuleProperties) rp.props;
                assertEquals(selected.get(i - 1).getName(), storedModule.moduleName.getValue());
                assertTrue(rp.schema.getFields().size() > 10);
                assertThat(storedModule.schema.schema.getStringValue(), is(rp.schema.toString()));
            }
            i++;
        }
    }

    @Test
    public void testModuleWizard() throws Throwable {
        ComponentWizard wiz = getComponentService().getComponentWizard(SalesforceConnectionWizardDefinition.COMPONENT_WIZARD_NAME,
                "nodeSalesforce");
        List<Form> forms = wiz.getForms();
        Form connFormWizard = forms.get(0);
        SalesforceConnectionProperties connProps = (SalesforceConnectionProperties) connFormWizard.getProperties();

        ComponentWizard[] subWizards = getComponentService().getComponentWizardsForProperties(connProps, "location")
                .toArray(new ComponentWizard[3]);
        Arrays.sort(subWizards, new Comparator<ComponentWizard>() {

            @Override
            public int compare(ComponentWizard o1, ComponentWizard o2) {
                return o1.getDefinition().getName().compareTo(o2.getDefinition().getName());
            }
        });
        assertEquals(3, subWizards.length);
        // Edit connection wizard - we copy the connection properties, as we present the UI, so we use the
        // connection properties object created by the new wizard
        assertFalse(connProps == subWizards[1].getForms().get(0).getProperties());
        // Add module wizard - we refer to the existing connection properties as we don't present the UI
        // for them.
        assertTrue(connProps == ((SalesforceModuleListProperties) subWizards[2].getForms().get(0).getProperties())
                .getConnectionProps());
        assertFalse(subWizards[1].getDefinition().isTopLevel());
        assertEquals("Edit SalesforceNew Connection", subWizards[1].getDefinition().getMenuItemName());
        assertTrue(subWizards[0].getDefinition().isTopLevel());
        assertEquals("Create SalesforceNew Connection", subWizards[0].getDefinition().getMenuItemName());
        assertFalse(subWizards[2].getDefinition().isTopLevel());
        assertEquals("Add SalesforceNew Modules", subWizards[2].getDefinition().getMenuItemName());
    }

    @Test
    public void testLogin() throws Throwable {
        SalesforceConnectionProperties props = setupProps(null, !ADD_QUOTES);
        Form f = props.getForm(SalesforceConnectionProperties.FORM_WIZARD);
        props = (SalesforceConnectionProperties) PropertiesServiceTest.checkAndValidate(getComponentService(), f,
                "testConnection", props);
        System.out.println(props.getValidationResult());
        assertEquals(ValidationResult.Result.OK, props.getValidationResult().getStatus());
    }

    @Test
    public void testLoginWithQuotes() throws Throwable {
        SalesforceConnectionProperties props = setupProps(null, ADD_QUOTES);
        Form f = props.getForm(SalesforceConnectionProperties.FORM_WIZARD);
        props = (SalesforceConnectionProperties) PropertiesServiceTest.checkAndValidate(getComponentService(), f,
                "testConnection", props);
        System.out.println(props.getValidationResult());
        assertEquals(ValidationResult.Result.OK, props.getValidationResult().getStatus());
    }

    @Test
    public void testLoginFail() throws Throwable {
        SalesforceConnectionProperties props = setupProps(null, !ADD_QUOTES);
        props.userPassword.userId.setValue("blah");
        Form f = props.getForm(SalesforceConnectionProperties.FORM_WIZARD);
        props = (SalesforceConnectionProperties) PropertiesServiceTest.checkAndValidate(getComponentService(), f,
                "testConnection", props);
        System.out.println(props.getValidationResult());
        assertEquals(ValidationResult.Result.ERROR, props.getValidationResult().getStatus());
    }

    @Test
    public void testBulkLogin() throws Throwable {
        SalesforceConnectionProperties props = setupProps(null, !ADD_QUOTES);
        props.bulkConnection.setValue(true);
        Form f = props.getForm(SalesforceConnectionProperties.FORM_WIZARD);
        props = (SalesforceConnectionProperties) PropertiesServiceTest.checkAndValidate(getComponentService(), f,
                "testConnection", props);
        assertEquals(ValidationResult.Result.OK, props.getValidationResult().getStatus());
        System.out.println(props.getValidationResult());
    }

    @Test
    public void testBulkLoginWithQuotes() throws Throwable {
        SalesforceConnectionProperties props = setupProps(null, ADD_QUOTES);
        props.bulkConnection.setValue(true);
        Form f = props.getForm(SalesforceConnectionProperties.FORM_WIZARD);
        props = (SalesforceConnectionProperties) PropertiesServiceTest.checkAndValidate(getComponentService(), f,
                "testConnection", props);
        assertEquals(ValidationResult.Result.OK, props.getValidationResult().getStatus());
        System.out.println(props.getValidationResult());
    }

    private SalesforceConnectionProperties setupOAuthProps(SalesforceConnectionProperties props) throws Throwable {
        if (props == null) {
            props = (SalesforceConnectionProperties) getComponentService()
                    .getComponentProperties(TSalesforceConnectionDefinition.COMPONENT_NAME);
        }
        props.loginType.setValue(SalesforceConnectionProperties.LOGIN_OAUTH);
        Form mainForm = props.getForm(Form.MAIN);
        props = (SalesforceConnectionProperties) checkAndAfter(mainForm, "loginType", props);
        props.oauth.clientId.setValue("3MVG9Y6d_Btp4xp6ParHznfCCUh0d9fU3LYcvd_hCXz3G3Owp4KvaDhNuEOrXJTBd09JMoPdZeDtNYxXZM4X2");
        props.oauth.clientSecret.setValue("3545101463828280342");
        props.oauth.callbackHost.setValue("localhost");
        props.oauth.callbackPort.setValue(8115);
        // props.oauth.tokenFile.setValue();
        return props;
    }

    @Ignore("oauth need manual operation")
    @Test
    public void testOAuthLogin() throws Throwable {
        SalesforceConnectionProperties props = setupOAuthProps(null);
        Form f = props.getForm(Form.MAIN);
        props = (SalesforceConnectionProperties) PropertiesServiceTest.checkAndValidate(getComponentService(), f,
                "testConnection", props);
        assertEquals(ValidationResult.Result.OK, props.getValidationResult().getStatus());
        System.out.println(props.getValidationResult());
    }

    @Ignore("oauth need manual operation")
    @Test
    public void testOAuthBulkLogin() throws Throwable {
        SalesforceConnectionProperties props = setupOAuthProps(null);
        props.bulkConnection.setValue(true);
        Form f = props.getForm(Form.MAIN);
        props = (SalesforceConnectionProperties) PropertiesServiceTest.checkAndValidate(getComponentService(), f,
                "testConnection", props);
        assertEquals(ValidationResult.Result.OK, props.getValidationResult().getStatus());
        System.out.println(props.getValidationResult());
    }

    @Test
    public void testModuleNames() throws Throwable {
        TSalesforceInputProperties props = (TSalesforceInputProperties) getComponentService()
                .getComponentProperties(TSalesforceInputDefinition.COMPONENT_NAME);
        setupProps(props.connection, !ADD_QUOTES);
        ComponentTestUtils.checkSerialize(props, errorCollector);

        assertEquals(2, props.getForms().size());
        Form f = props.module.getForm(Form.REFERENCE);
        assertTrue(f.getWidget("moduleName").isCallBeforeActivate());
        // The Form is bound to a Properties object that created it. The Forms might not always be associated with the
        // properties object
        // they came from.
        ComponentProperties moduleProps = (ComponentProperties) f.getProperties();
        moduleProps = (ComponentProperties) PropertiesServiceTest.checkAndBeforeActivate(getComponentService(), f, "moduleName",
                moduleProps);
        Property prop = (Property) f.getWidget("moduleName").getContent();
        assertTrue(prop.getPossibleValues().size() > 100);
        System.out.println(prop.getPossibleValues());
        System.out.println(moduleProps.getValidationResult());
    }

    @Test
    public void testSchema() throws Throwable {
        TSalesforceInputProperties props = (TSalesforceInputProperties) getComponentService()
                .getComponentProperties(TSalesforceInputDefinition.COMPONENT_NAME);
        setupProps(props.connection, !ADD_QUOTES);

        Form f = props.module.getForm(Form.REFERENCE);
        SalesforceModuleProperties moduleProps = (SalesforceModuleProperties) f.getProperties();
        moduleProps = (SalesforceModuleProperties) PropertiesServiceTest.checkAndBeforeActivate(getComponentService(), f,
                "moduleName", moduleProps);
        moduleProps.moduleName.setValue("Account");
        moduleProps = (SalesforceModuleProperties) checkAndAfter(f, "moduleName", moduleProps);
        Schema schema = new Schema.Parser().parse(moduleProps.schema.schema.getStringValue());
        System.out.println(schema);
        for (Schema.Field child : schema.getFields()) {
            System.out.println(child.name());
        }
        assertEquals("Id", schema.getFields().get(0).name());
        assertTrue(schema.getFields().size() > 50);
    }

    @Test
    public void testOutputActionType() throws Throwable {
        ComponentDefinition definition = getComponentService().getComponentDefinition(TSalesforceOutputDefinition.COMPONENT_NAME);
        TSalesforceOutputProperties outputProps = (TSalesforceOutputProperties) getComponentService()
                .getComponentProperties(TSalesforceOutputDefinition.COMPONENT_NAME);
        setupProps(outputProps.connection, !ADD_QUOTES);

        outputProps.outputAction.setValue(TSalesforceOutputProperties.ACTION_DELETE);
        setupModule(outputProps.module, "Account");

        ComponentTestUtils.checkSerialize(outputProps, errorCollector);
        List<IndexedRecord> rows = new ArrayList<>();
        try {
            writeRows(null, outputProps, rows);
        } catch (Exception ex) {
            if (ex instanceof ClassCastException) {
                System.out.println("Exception: " + ex.getMessage());
                fail("Get error before delete!");
            }
        }
    }

    @Test
    public void testInputConnectionRef() throws Throwable {
        ComponentDefinition definition = getComponentService().getComponentDefinition(TSalesforceInputDefinition.COMPONENT_NAME);
        TSalesforceInputProperties props = (TSalesforceInputProperties) getComponentService()
                .getComponentProperties(TSalesforceInputDefinition.COMPONENT_NAME);
        setupProps(props.connection, !ADD_QUOTES);

        SalesforceSourceOrSink salesforceSourceOrSink = new SalesforceSourceOrSink();
        salesforceSourceOrSink.initialize(null, props);
        assertEquals(ValidationResult.Result.OK, salesforceSourceOrSink.validate(null).getStatus());

        // Referenced properties simulating salesforce connect component
        SalesforceConnectionProperties cProps = (SalesforceConnectionProperties) getComponentService()
                .getComponentProperties(TSalesforceConnectionDefinition.COMPONENT_NAME);
        setupProps(cProps, !ADD_QUOTES);
        cProps.userPassword.password.setValue("xxx");

        String compId = "comp1";
        // Use the connection props of the salesforce connect component
        props.connection.referencedComponent.referenceType
                .setValue(ComponentReferenceProperties.ReferenceType.COMPONENT_INSTANCE);
        props.connection.referencedComponent.componentInstanceId.setValue(compId);
        props.connection.referencedComponent.componentProperties = cProps;
        checkAndAfter(props.connection.getForm(Form.REFERENCE), "referencedComponent", props.connection);

        salesforceSourceOrSink = new SalesforceSourceOrSink();
        salesforceSourceOrSink.initialize(null, props);
        salesforceSourceOrSink.validate(null);
        assertEquals(ValidationResult.Result.ERROR, salesforceSourceOrSink.validate(null).getStatus());

        // Back to using the connection props of the salesforce input component
        props.connection.referencedComponent.referenceType.setValue(ComponentReferenceProperties.ReferenceType.THIS_COMPONENT);
        props.connection.referencedComponent.componentInstanceId.setValue(null);
        props.connection.referencedComponent.componentProperties = null;
        // Check that the null referenced component works.
        checkAndAfter(props.connection.getForm(Form.REFERENCE), "referencedComponent", props.connection);

        salesforceSourceOrSink = new SalesforceSourceOrSink();
        salesforceSourceOrSink.initialize(null, props);
        salesforceSourceOrSink.validate(null);
        assertEquals(ValidationResult.Result.OK, salesforceSourceOrSink.validate(null).getStatus());
    }

    @Test
    public void testUseExistingConnection() throws Throwable {
        SalesforceConnectionProperties connProps = (SalesforceConnectionProperties) getComponentService()
                .getComponentProperties(TSalesforceConnectionDefinition.COMPONENT_NAME);
        setupProps(connProps, !ADD_QUOTES);

        final String currentComponentName = TSalesforceConnectionDefinition.COMPONENT_NAME + "_1";
        RuntimeContainer connContainer = new DefaultComponentRuntimeContainerImpl() {

            @Override
            public String getCurrentComponentId() {
                return currentComponentName;
            }
        };

        SalesforceSourceOrSink salesforceSourceOrSink = new SalesforceSourceOrSink();
        salesforceSourceOrSink.initialize(connContainer, connProps);
        assertEquals(ValidationResult.Result.OK, salesforceSourceOrSink.validate(connContainer).getStatus());

        // Input component get connection from the tSalesforceConnection
        ComponentDefinition inputDefinition = getComponentService()
                .getComponentDefinition(TSalesforceInputDefinition.COMPONENT_NAME);
        TSalesforceInputProperties inProps = (TSalesforceInputProperties) getComponentService()
                .getComponentProperties(TSalesforceInputDefinition.COMPONENT_NAME);
        inProps.connection.referencedComponent.componentInstanceId.setValue(currentComponentName);

        SalesforceSourceOrSink salesforceInputSourceOrSink = new SalesforceSourceOrSink();
        salesforceInputSourceOrSink.initialize(connContainer, inProps);
        assertEquals(ValidationResult.Result.OK, salesforceInputSourceOrSink.validate(connContainer).getStatus());
    }

    @Test
    public void generateJavaNestedCompPropClassNames() {
        Set<ComponentDefinition> allComponents = getComponentService().getAllComponents();
        for (ComponentDefinition cd : allComponents) {
            ComponentProperties props = cd.createProperties();
            String javaCode = PropertiesTestUtils.generatedNestedComponentCompatibilitiesJavaCode(props);
            System.out.println("Nested Props for (" + cd.getClass().getSimpleName() + ".java:1)" + javaCode);
        }
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