// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.salesforce.integration;

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
import java.util.List;
import java.util.Set;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.Connector;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.components.api.container.DefaultComponentRuntimeContainerImpl;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.ComponentReferenceProperties;
import org.talend.components.api.test.ComponentTestUtils;
import org.talend.components.api.wizard.ComponentWizard;
import org.talend.components.api.wizard.ComponentWizardDefinition;
import org.talend.components.api.wizard.WizardImageType;
import org.talend.components.api.wizard.WizardNameComparator;
import org.talend.components.common.CommonTestUtils;
import org.talend.components.salesforce.SalesforceConnectionProperties;
import org.talend.components.salesforce.SalesforceConnectionProperties.LoginType;
import org.talend.components.salesforce.SalesforceConnectionWizardDefinition;
import org.talend.components.salesforce.SalesforceModuleListProperties;
import org.talend.components.salesforce.SalesforceModuleProperties;
import org.talend.components.salesforce.SalesforceOutputProperties.OutputAction;
import org.talend.components.salesforce.SalesforceUserPasswordProperties;
import org.talend.components.salesforce.runtime.SalesforceSourceOrSink;
import org.talend.components.salesforce.tsalesforcebulkexec.TSalesforceBulkExecDefinition;
import org.talend.components.salesforce.tsalesforceconnection.TSalesforceConnectionDefinition;
import org.talend.components.salesforce.tsalesforceinput.TSalesforceInputDefinition;
import org.talend.components.salesforce.tsalesforceinput.TSalesforceInputProperties;
import org.talend.components.salesforce.tsalesforceoutput.TSalesforceOutputDefinition;
import org.talend.components.salesforce.tsalesforceoutput.TSalesforceOutputProperties;
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.PresentationItem;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.service.Repository;
import org.talend.daikon.properties.test.PropertiesTestUtils;

public abstract class SalesforceComponentTestIT extends SalesforceTestBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(SalesforceComponentTestIT.class);

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    public SalesforceComponentTestIT() {
        super();
    }

    @Test
    public void testGetProps() {
        ComponentProperties props = new TSalesforceConnectionDefinition().createProperties();
        Form f = props.getForm(Form.MAIN);
        ComponentTestUtils.checkSerialize(props, errorCollector);
        LOGGER.debug(f.toString());
        LOGGER.debug(props.toString());
        assertEquals(Form.MAIN, f.getName());
    }

    @Test
    public void testAfterLoginType() throws Throwable {
        ComponentProperties props;

        props = new TSalesforceConnectionDefinition().createProperties();
        ComponentTestUtils.checkSerialize(props, errorCollector);
        Property<LoginType> loginType = (Property<LoginType>) props.getProperty("loginType");
        LOGGER.debug(loginType.getPossibleValues().toString());
        assertEquals(SalesforceConnectionProperties.LoginType.Basic, loginType.getPossibleValues().get(0));
        assertEquals(SalesforceConnectionProperties.LoginType.OAuth, loginType.getPossibleValues().get(1));
        assertEquals(SalesforceConnectionProperties.LoginType.Basic, loginType.getValue());
        Form mainForm = props.getForm(Form.MAIN);
        assertEquals("Salesforce Connection Settings", mainForm.getTitle());
        assertFalse(mainForm.getWidget(SalesforceUserPasswordProperties.class).isHidden());

        loginType.setValue(SalesforceConnectionProperties.LoginType.OAuth);
        props = checkAndAfter(mainForm, "loginType", props);
        mainForm = props.getForm(Form.MAIN);
        assertTrue(mainForm.isRefreshUI());

        assertTrue(mainForm.getWidget(SalesforceUserPasswordProperties.class).isHidden());
    }

    @Test
    public void testConnectionProps() throws Throwable {
        SalesforceConnectionProperties props = (SalesforceConnectionProperties) new TSalesforceConnectionDefinition()
                .createProperties();
        assertTrue(props.userPassword.userId.isRequired());
        assertTrue(props.userPassword.password.isRequired());
        assertFalse(props.userPassword.securityKey.isRequired());

        assertFalse(props.proxy.userPassword.userId.isRequired());
        assertFalse(props.proxy.userPassword.password.isRequired());

        Form mainForm = props.getForm(Form.MAIN);

        Form advancedForm = props.getForm(Form.ADVANCED);
        assertTrue(advancedForm.getWidget(props.reuseSession.getName()).isVisible());
        assertFalse(advancedForm.getWidget(props.sessionDirectory).isVisible());

        // Check "loginType" value changes influence "reuseSession"/"sessionDirectory" visible
        props.loginType.setValue(LoginType.OAuth);
        checkAndAfter(mainForm, "loginType", props);
        assertFalse(advancedForm.getWidget(props.reuseSession.getName()).isVisible());
        assertFalse(advancedForm.getWidget(props.sessionDirectory).isVisible());

        props.loginType.setValue(LoginType.Basic);
        checkAndAfter(mainForm, "loginType", props);
        assertTrue(advancedForm.getWidget(props.reuseSession.getName()).isVisible());
        assertFalse(advancedForm.getWidget(props.sessionDirectory).isVisible());

        // Check "bulkConnection" value changes influence "reuseSession"/"sessionDirectory" visible
        props.bulkConnection.setValue(true);
        checkAndAfter(advancedForm, "bulkConnection", props);
        assertFalse(advancedForm.getWidget(props.reuseSession.getName()).isVisible());
        assertFalse(advancedForm.getWidget(props.sessionDirectory).isVisible());

        props.bulkConnection.setValue(false);
        checkAndAfter(advancedForm, "bulkConnection", props);
        assertTrue(advancedForm.getWidget(props.reuseSession.getName()).isVisible());
        assertFalse(advancedForm.getWidget(props.sessionDirectory).isVisible());

        // Check "reuseSession" value changes influence "sessionDirectory" visible
        props.reuseSession.setValue(true);
        checkAndAfter(advancedForm, "reuseSession", props);
        assertTrue(advancedForm.getWidget(props.sessionDirectory).isVisible());
        props.reuseSession.setValue(false);
        checkAndAfter(advancedForm, "reuseSession", props);
        assertFalse(advancedForm.getWidget(props.sessionDirectory).isVisible());

    }

    @Test
    public void testInputProps() throws Throwable {
        TSalesforceInputProperties props = (TSalesforceInputProperties) new TSalesforceInputDefinition().createProperties();
        assertEquals(2, props.queryMode.getPossibleValues().size());
        Property[] returns = new TSalesforceInputDefinition().getReturnProperties();
        assertEquals(ComponentDefinition.RETURN_TOTAL_RECORD_COUNT, returns[1].getName());

        // Default query mode
        Form queryAdvancedForm = props.getForm(Form.ADVANCED);
        assertTrue(queryAdvancedForm.getChildForm(props.connection.getName()).getWidget(props.connection.bulkConnection.getName())
                .isHidden());
        assertFalse(queryAdvancedForm.getWidget(props.normalizeDelimiter.getName()).isHidden());
        assertFalse(queryAdvancedForm.getWidget(props.columnNameDelimiter.getName()).isHidden());
        assertFalse(queryAdvancedForm.getChildForm(props.connection.getName()).getWidget(props.connection.httpChunked.getName())
                .isHidden());
        assertTrue(queryAdvancedForm.getChildForm(props.connection.getName())
                .getWidget(props.connection.httpTraceMessage.getName()).isHidden());
        // Change to bulk query mode
        props.queryMode.setValue(TSalesforceInputProperties.QueryMode.Bulk);
        props.afterQueryMode();

        Form bulkQueryAdvancedForm = props.getForm(Form.ADVANCED);
        assertTrue(bulkQueryAdvancedForm.getWidget(props.normalizeDelimiter.getName()).isHidden());
        assertTrue(bulkQueryAdvancedForm.getWidget(props.columnNameDelimiter.getName()).isHidden());
        assertTrue(bulkQueryAdvancedForm.getChildForm(props.connection.getName())
                .getWidget(props.connection.httpChunked.getName()).isHidden());
        assertFalse(bulkQueryAdvancedForm.getChildForm(props.connection.getName())
                .getWidget(props.connection.httpTraceMessage.getName()).isHidden());
        assertTrue(bulkQueryAdvancedForm.getChildForm(props.connection.getName())
                .getWidget(props.connection.bulkConnection.getName()).isHidden());
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
                this.schema = (Schema) props.getValuedProperty(schemaPropertyName).getValue();
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
            LOGGER.debug(rp.toString());
            return repositoryLocation + ++locationNum;
        }
    }

    class TestRuntimeContainer extends DefaultComponentRuntimeContainerImpl {
    }

    @Test
    public void testFamily() {
        ComponentDefinition cd = getComponentService().getComponentDefinition("tSalesforceConnection");
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
        int connectionWizardDefinitionNumber = 0;
        ComponentWizardDefinition wizardDef = null;
        for (ComponentWizardDefinition wizardDefinition : wizards) {
            if (wizardDefinition instanceof SalesforceConnectionWizardDefinition) {
                wizardDef = wizardDefinition;
                connectionWizardDefinitionNumber++;
            }
        }
        assertEquals(1, connectionWizardDefinitionNumber);
        assertEquals("Salesforce Connection", wizardDef.getMenuItemName());
        ComponentWizard connectionWizard = getComponentService()
                .getComponentWizard(SalesforceConnectionWizardDefinition.COMPONENT_WIZARD_NAME, "nodeSalesforce");
        assertNotNull(connectionWizard);
        assertEquals("nodeSalesforce", connectionWizard.getRepositoryLocation());
        List<Form> forms = connectionWizard.getForms();
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

        Form advancedForm = connProps.getForm(Form.ADVANCED);
        assertTrue(
                ((PresentationItem) connFormWizard.getWidget("advanced").getContent()).getFormtoShow() + " should be == to "
                        + advancedForm,
                ((PresentationItem) connFormWizard.getWidget("advanced").getContent()).getFormtoShow() == advancedForm);

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
        connProps = (SalesforceConnectionProperties) PropertiesTestUtils.checkAndValidate(getComponentService(), connFormWizard,
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
        @SuppressWarnings("unchecked")
        List<NamedThing> all = mlProps.selectedModuleNames.getValue();
        assertNull(all);
        // TCOMP-9 Change the module list to use getPossibleValues() for SalesforceModuleListProperties
        List<NamedThing> possibleValues = (List<NamedThing>) mlProps.selectedModuleNames.getPossibleValues();
        assertTrue(possibleValues.size() > 50);
        List<NamedThing> selected = new ArrayList<>();
        selected.add(possibleValues.get(0));
        selected.add(possibleValues.get(2));
        selected.add(possibleValues.get(3));

        mlProps.selectedModuleNames.setValue(selected);
        getComponentService().afterFormFinish(modForm.getName(), mlProps);
        LOGGER.debug(repoProps.toString());
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
                assertThat(storedModule.main.schema.getStringValue(), is(rp.schema.toString()));
            }
            i++;
        }
    }

    @Test
    public void testModuleWizard() throws Throwable {
        ComponentWizard connectionWizard = getComponentService()
                .getComponentWizard(SalesforceConnectionWizardDefinition.COMPONENT_WIZARD_NAME, "nodeSalesforce");
        List<Form> forms = connectionWizard.getForms();
        Form connectionWizardForm = forms.get(0);
        SalesforceConnectionProperties connProps = (SalesforceConnectionProperties) connectionWizardForm.getProperties();

        ComponentWizard[] subWizards = getComponentService().getComponentWizardsForProperties(connProps, "location")
                .toArray(new ComponentWizard[3]);
        Arrays.sort(subWizards, new WizardNameComparator());
        assertEquals(3, subWizards.length);

        assertTrue(subWizards[0].getDefinition().isTopLevel());
        assertEquals("Salesforce Connection", subWizards[0].getDefinition().getMenuItemName());
        
        assertFalse(subWizards[1].getDefinition().isTopLevel());
        assertEquals("Edit Salesforce", subWizards[1].getDefinition().getMenuItemName());
        
        assertFalse(subWizards[2].getDefinition().isTopLevel());
        assertEquals("Salesforce Modules", subWizards[2].getDefinition().getMenuItemName());
    }

    @Test
    public void testLogin() throws Throwable {
        SalesforceConnectionProperties props = setupProps(null, !ADD_QUOTES);
        Form f = props.getForm(SalesforceConnectionProperties.FORM_WIZARD);
        props = (SalesforceConnectionProperties) PropertiesTestUtils.checkAndValidate(getComponentService(), f, "testConnection",
                props);
        LOGGER.debug(props.getValidationResult().toString());
        assertEquals(ValidationResult.Result.OK, props.getValidationResult().getStatus());
    }

    @Test
    public void testLoginWithQuotes() throws Throwable {
        SalesforceConnectionProperties props = setupProps(null, ADD_QUOTES);
        Form f = props.getForm(SalesforceConnectionProperties.FORM_WIZARD);
        props = (SalesforceConnectionProperties) PropertiesTestUtils.checkAndValidate(getComponentService(), f, "testConnection",
                props);
        LOGGER.debug(props.getValidationResult().toString());
        assertEquals(ValidationResult.Result.OK, props.getValidationResult().getStatus());
    }

    /*
     * If the logic changes for this test please specify appropriate timeout.
     * The average execution time for this test 1.7-1.8 sec.
     */
    @Test(timeout = 30_000)
    public void testLoginFail() throws Throwable {
        SalesforceConnectionProperties props = setupProps(null, !ADD_QUOTES);
        props.userPassword.userId.setValue("blah");
        Form f = props.getForm(SalesforceConnectionProperties.FORM_WIZARD);
        props = (SalesforceConnectionProperties) PropertiesTestUtils.checkAndValidate(getComponentService(), f, "testConnection",
                props);
        LOGGER.debug(props.getValidationResult().toString());
        assertEquals(ValidationResult.Result.ERROR, props.getValidationResult().getStatus());
    }

    @Test
    public void testBulkLogin() throws Throwable {
        SalesforceConnectionProperties props = setupProps(null, !ADD_QUOTES);
        props.bulkConnection.setValue(true);
        Form f = props.getForm(SalesforceConnectionProperties.FORM_WIZARD);
        props = (SalesforceConnectionProperties) PropertiesTestUtils.checkAndValidate(getComponentService(), f, "testConnection",
                props);
        assertEquals(ValidationResult.Result.OK, props.getValidationResult().getStatus());
        LOGGER.debug(props.getValidationResult().toString());
    }

    @Test
    public void testBulkLoginWithQuotes() throws Throwable {
        SalesforceConnectionProperties props = setupProps(null, ADD_QUOTES);
        props.bulkConnection.setValue(true);
        Form f = props.getForm(SalesforceConnectionProperties.FORM_WIZARD);
        props = (SalesforceConnectionProperties) PropertiesTestUtils.checkAndValidate(getComponentService(), f, "testConnection",
                props);
        assertEquals(ValidationResult.Result.OK, props.getValidationResult().getStatus());
        LOGGER.debug(props.getValidationResult().toString());
    }

    private SalesforceConnectionProperties setupOAuthProps(SalesforceConnectionProperties props) throws Throwable {
        if (props == null) {
            props = (SalesforceConnectionProperties) getComponentService()
                    .getComponentProperties(TSalesforceConnectionDefinition.COMPONENT_NAME);
        }
        props.loginType.setValue(LoginType.OAuth);
        Form mainForm = props.getForm(Form.MAIN);
        props = (SalesforceConnectionProperties) checkAndAfter(mainForm, "loginType", props);
        props.oauth.clientId.setValue("3MVG9Y6d_Btp4xp6ParHznfCCUh0d9fU3LYcvd_hCXz3G3Owp4KvaDhNuEOrXJTBd09JMoPdZeDtNYxXZM4X2");
        props.oauth.clientSecret.setValue("3545101463828280342");
        props.oauth.callbackHost.setValue("localhost");
        props.oauth.callbackPort.setValue(8115);
        String tokenFile = tempFolder.newFile("token" + createNewRandom()).getPath();
        props.oauth.tokenFile.setValue(tokenFile);
        return props;
    }

    @Ignore("oauth need manual operation")
    @Test
    public void testOAuthLogin() throws Throwable {
        SalesforceConnectionProperties props = setupOAuthProps(null);
        Form f = props.getForm(SalesforceConnectionProperties.FORM_WIZARD);
        props = (SalesforceConnectionProperties) PropertiesTestUtils.checkAndValidate(getComponentService(), f, "testConnection",
                props);
        assertEquals(ValidationResult.Result.OK, props.getValidationResult().getStatus());
        LOGGER.debug(props.getValidationResult().toString());
    }

    @Ignore("oauth need manual operation")
    @Test
    public void testOAuthBulkLogin() throws Throwable {
        SalesforceConnectionProperties props = setupOAuthProps(null);
        props.bulkConnection.setValue(true);
        Form f = props.getForm(SalesforceConnectionProperties.FORM_WIZARD);
        props = (SalesforceConnectionProperties) PropertiesTestUtils.checkAndValidate(getComponentService(), f, "testConnection",
                props);
        assertEquals(ValidationResult.Result.OK, props.getValidationResult().getStatus());
        LOGGER.debug(props.getValidationResult().toString());
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
        moduleProps = (ComponentProperties) PropertiesTestUtils.checkAndBeforeActivate(getComponentService(), f, "moduleName",
                moduleProps);
        Property prop = (Property) f.getWidget("moduleName").getContent();
        assertTrue(prop.getPossibleValues().size() > 100);
        LOGGER.debug(prop.getPossibleValues().toString());
        LOGGER.debug(moduleProps.getValidationResult().toString());
    }

    @Test
    public void testSchema() throws Throwable {
        testSchemaWithAPIVersion("42.0");
        testSchemaWithAPIVersion("41.0");
        testSchemaWithAPIVersion("40.0");
        testSchemaWithAPIVersion("39.0");
        testSchemaWithAPIVersion("38.0");
        testSchemaWithAPIVersion("37.0");
        testSchemaWithAPIVersion("36.0");
        testSchemaWithAPIVersion("35.0");
        testSchemaWithAPIVersion("34.0");
        testSchemaWithAPIVersion("29.0");
        testSchemaWithAPIVersion("25.0");
        testSchemaWithAPIVersion("24.0");
        testSchemaWithAPIVersion("21.0");
        testSchemaWithAPIVersion("19.0");
        testSchemaWithAPIVersion("18.0");
        // ignore it now as it fail and it seems the reason is the jar classpath issue, need to research on it, why before it
        // success? i think
        // the refactor make it up, but not from the code
        // testSchemaWithAPIVersion("15.0");
        // testSchemaWithAPIVersion("10.0");
    }

    protected void testSchemaWithAPIVersion(String version) throws Throwable {
        TSalesforceInputProperties props = (TSalesforceInputProperties) getComponentService()
                .getComponentProperties(TSalesforceInputDefinition.COMPONENT_NAME);
        props.connection.endpoint.setValue("https://login.salesforce.com/services/Soap/u/" + version);
        setupProps(props.connection, !ADD_QUOTES);

        Form f = props.module.getForm(Form.REFERENCE);
        SalesforceModuleProperties moduleProps = (SalesforceModuleProperties) f.getProperties();
        moduleProps = (SalesforceModuleProperties) PropertiesTestUtils.checkAndBeforeActivate(getComponentService(), f,
                "moduleName", moduleProps);
        moduleProps.moduleName.setValue("Account");
        moduleProps = (SalesforceModuleProperties) checkAndAfter(f, "moduleName", moduleProps);
        Schema schema = moduleProps.main.schema.getValue();
        LOGGER.debug(schema.toString());
        for (Schema.Field child : schema.getFields()) {
            LOGGER.debug(child.name());
        }
        assertEquals("Id", schema.getFields().get(0).name());
        LOGGER.debug("Endpoint:" + props.connection.endpoint.getValue());
        LOGGER.debug("Module \"Account\" column size:" + schema.getFields().size());
        assertTrue(schema.getFields().size() > 40);
    }

    @Test
    public void testOutputActionType() throws Throwable {
        ComponentDefinition definition = getComponentService().getComponentDefinition(TSalesforceOutputDefinition.COMPONENT_NAME);
        TSalesforceOutputProperties outputProps = (TSalesforceOutputProperties) getComponentService()
                .getComponentProperties(TSalesforceOutputDefinition.COMPONENT_NAME);
        setupProps(outputProps.connection, !ADD_QUOTES);

        outputProps.outputAction.setValue(OutputAction.DELETE);
        setupModule(outputProps.module, "Account");

        ComponentTestUtils.checkSerialize(outputProps, errorCollector);
        List<IndexedRecord> rows = new ArrayList<>();
        try {
            writeRows(null, outputProps, rows);
        } catch (Exception ex) {
            if (ex instanceof ClassCastException) {
                LOGGER.debug("Exception: " + ex.getMessage());
                fail("Get error before delete!");
            }
        }
    }

    /*
     * If the logic changes for this test please specify appropriate timeout.
     * The average execution time for this test 1.4-1.9 sec.
     */
    @Test(timeout = 30_000)
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
        props.connection.referencedComponent.setReference(cProps);
        checkAndAfter(props.connection.getForm(Form.REFERENCE), "referencedComponent", props.connection);

        salesforceSourceOrSink = new SalesforceSourceOrSink();
        salesforceSourceOrSink.initialize(null, props);
        salesforceSourceOrSink.validate(null);
        assertEquals(ValidationResult.Result.ERROR, salesforceSourceOrSink.validate(null).getStatus());

        // Back to using the connection props of the salesforce input component
        props.connection.referencedComponent.referenceType.setValue(ComponentReferenceProperties.ReferenceType.THIS_COMPONENT);
        props.connection.referencedComponent.componentInstanceId.setValue(null);
        props.connection.referencedComponent.setReference(null);
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
            LOGGER.debug("Nested Props for (" + cd.getClass().getSimpleName() + ".java:1)" + javaCode);
        }
    }

    @Override
    @Test
    public void testAlli18n() {
        ComponentTestUtils.testAlli18n(getComponentService(), errorCollector);
    }

    @Override
    @Test
    public void testAllImages() {
        ComponentTestUtils.testAllImages(getComponentService());
    }

    /**
     * ignore it now as no schema path necessary for datastore/dataset
     */
    @Ignore
    @Test
    public void checkConnectorsSchema() {
        CommonTestUtils.checkAllSchemaPathAreSchemaTypes(getComponentService(), errorCollector);
    }

    @Test
    public void testSchemaSerialized() throws Throwable {
        // ComponentDefinition definition =
        // getComponentService().getComponentDefinition(TSalesforceOutputDefinition.COMPONENT_NAME);
        TSalesforceOutputProperties outputProps = (TSalesforceOutputProperties) getComponentService()
                .getComponentProperties(TSalesforceOutputDefinition.COMPONENT_NAME);

        Schema reject = SchemaBuilder.record("Reject").fields().name("A").type().stringType().noDefault().name("B").type()
                .stringType().noDefault().endRecord();

        Schema main = SchemaBuilder.record("Main").fields().name("C").type().stringType().noDefault().name("D").type()
                .stringType().noDefault().endRecord();

        assertEquals(2, outputProps.getAvailableConnectors(null, true).size());
        for (Connector connector : outputProps.getAvailableConnectors(null, true)) {
            if (connector.getName().equals(Connector.MAIN_NAME)) {
                outputProps.setConnectedSchema(connector, main, true);
            } else {
                outputProps.setConnectedSchema(connector, reject, true);
            }
        }

        String serialized = outputProps.toSerialized();

        TSalesforceOutputProperties afterSerialized = Properties.Helper.fromSerializedPersistent(serialized,
                TSalesforceOutputProperties.class).object;
        assertEquals(2, afterSerialized.getAvailableConnectors(null, true).size());
        for (Connector connector : afterSerialized.getAvailableConnectors(null, true)) {
            if (connector.getName().equals(Connector.MAIN_NAME)) {
                Schema main2 = afterSerialized.getSchema(connector, true);
                assertEquals(main.toString(), main2.toString());
            } else {
                Schema reject2 = afterSerialized.getSchema(connector, true);
                assertEquals(reject.toString(), reject2.toString());
            }
        }
    }

    @Test
    public void testSchemaSerialized2() throws Throwable {
        ComponentDefinition definition = getComponentService().getComponentDefinition(TSalesforceOutputDefinition.COMPONENT_NAME);
        TSalesforceOutputProperties outputProps = (TSalesforceOutputProperties) getComponentService()
                .getComponentProperties(TSalesforceOutputDefinition.COMPONENT_NAME);

        Schema reject = SchemaBuilder.record("Reject").fields().name("A").type().stringType().noDefault().name("B").type()
                .stringType().noDefault().endRecord();

        Schema main = SchemaBuilder.record("Main").fields().name("C").type().stringType().noDefault().name("D").type()
                .stringType().noDefault().endRecord();

        outputProps.setValue("module.main.schema", main);
        outputProps.setValue("schemaReject.schema", reject);

        Schema main2 = (Schema) outputProps.getValuedProperty("module.main.schema").getValue();
        Schema reject2 = (Schema) outputProps.getValuedProperty("schemaReject.schema").getValue();
        assertEquals(main.toString(), main2.toString());
        assertEquals(reject.toString(), reject2.toString());

        String serialized = outputProps.toSerialized();

        TSalesforceOutputProperties afterSerialized = Properties.Helper.fromSerializedPersistent(serialized,
                TSalesforceOutputProperties.class).object;

        main2 = (Schema) afterSerialized.getValuedProperty("module.main.schema").getValue();
        reject2 = (Schema) afterSerialized.getValuedProperty("schemaReject.schema").getValue();
        assertEquals(main.toString(), main2.toString());
        assertEquals(reject.toString(), reject2.toString());
    }

    @Test
    public void testSerializationIssue_TUP_4851() {
        String serialized = "{\"@id\":37,\"@type\":\"org.talend.components.salesforce.tsalesforceinput.TSalesforceInputProperties\",\"queryMode\":{\"@id\":52,\"@type\":\"org.talend.daikon.properties.property.EnumProperty\",\"flags\":null,\"taggedValues\":{\"@type\":\"java.util.HashMap\",\"SUPPORT_CONTEXT\":true,\"IS_DYNAMIC\":false},\"storedValue\":{\"@type\":\"org.talend.components.salesforce.tsalesforceinput.TSalesforceInputProperties$QueryMode\",\"name\":\"Query\",\"ordinal\":0},\"size\":-1,\"occurMinTimes\":0,\"occurMaxTimes\":0,\"precision\":0,\"pattern\":null,\"nullable\":false,\"possibleValues\":{\"@type\":\"java.util.Arrays$ArrayList\",\"@items\":[{\"@type\":\"org.talend.components.salesforce.tsalesforceinput.TSalesforceInputProperties$QueryMode\",\"name\":\"Query\",\"ordinal\":0},{\"@type\":\"org.talend.components.salesforce.tsalesforceinput.TSalesforceInputProperties$QueryMode\",\"name\":\"Bulk\",\"ordinal\":1}]},\"children\":{\"@type\":\"java.util.ArrayList\"},\"currentType\":\"org.talend.components.salesforce.tsalesforceinput.TSalesforceInputProperties.QueryMode\",\"name\":\"queryMode\",\"displayName\":null,\"title\":null},\"condition\":{\"@id\":51,\"@type\":\"org.talend.daikon.properties.property.StringProperty\",\"possibleValues2\":null,\"flags\":null,\"taggedValues\":{\"@type\":\"java.util.HashMap\",\"SUPPORT_CONTEXT\":true,\"IS_DYNAMIC\":false},\"storedValue\":\"\\\"\\\"\",\"size\":-1,\"occurMinTimes\":0,\"occurMaxTimes\":0,\"precision\":0,\"pattern\":null,\"nullable\":false,\"possibleValues\":null,\"children\":{\"@type\":\"java.util.ArrayList\"},\"currentType\":\"java.lang.String\",\"name\":\"condition\",\"displayName\":null,\"title\":null},\"manualQuery\":{\"@id\":50,\"flags\":null,\"taggedValues\":{\"@type\":\"java.util.HashMap\",\"SUPPORT_CONTEXT\":true,\"IS_DYNAMIC\":false},\"storedValue\":{\"@type\":\"boolean\",\"value\":false},\"size\":-1,\"occurMinTimes\":0,\"occurMaxTimes\":0,\"precision\":0,\"pattern\":null,\"nullable\":false,\"possibleValues\":null,\"children\":{\"@type\":\"java.util.ArrayList\"},\"currentType\":\"java.lang.Boolean\",\"name\":\"manualQuery\",\"displayName\":null,\"title\":null},\"query\":{\"@id\":49,\"@type\":\"org.talend.daikon.properties.property.StringProperty\",\"possibleValues2\":null,\"flags\":{\"@type\":\"java.util.RegularEnumSet\",\"@items\":[{\"@type\":\"org.talend.daikon.properties.property.Property$Flags\",\"name\":\"HIDDEN\",\"ordinal\":3}]},\"taggedValues\":{\"@type\":\"java.util.HashMap\",\"SUPPORT_CONTEXT\":true,\"IS_DYNAMIC\":false},\"storedValue\":\"\\\"\\\"\",\"size\":-1,\"occurMinTimes\":0,\"occurMaxTimes\":0,\"precision\":0,\"pattern\":null,\"nullable\":false,\"possibleValues\":null,\"children\":{\"@type\":\"java.util.ArrayList\"},\"currentType\":\"java.lang.String\",\"name\":\"query\",\"displayName\":null,\"title\":null},\"includeDeleted\":{\"@id\":48,\"flags\":null,\"taggedValues\":{\"@type\":\"java.util.HashMap\",\"SUPPORT_CONTEXT\":true,\"IS_DYNAMIC\":false},\"storedValue\":{\"@type\":\"boolean\",\"value\":false},\"size\":-1,\"occurMinTimes\":0,\"occurMaxTimes\":0,\"precision\":0,\"pattern\":null,\"nullable\":false,\"possibleValues\":null,\"children\":{\"@type\":\"java.util.ArrayList\"},\"currentType\":\"java.lang.Boolean\",\"name\":\"includeDeleted\",\"displayName\":null,\"title\":null},\"batchSize\":{\"@id\":47,\"flags\":null,\"taggedValues\":{\"@type\":\"java.util.HashMap\",\"SUPPORT_CONTEXT\":true,\"IS_DYNAMIC\":false},\"storedValue\":{\"@type\":\"int\",\"value\":250},\"size\":-1,\"occurMinTimes\":0,\"occurMaxTimes\":0,\"precision\":0,\"pattern\":null,\"nullable\":false,\"possibleValues\":null,\"children\":{\"@type\":\"java.util.ArrayList\"},\"currentType\":\"java.lang.Integer\",\"name\":\"batchSize\",\"displayName\":null,\"title\":null},\"normalizeDelimiter\":{\"@id\":46,\"@type\":\"org.talend.daikon.properties.property.StringProperty\",\"possibleValues2\":null,\"flags\":null,\"taggedValues\":{\"@type\":\"java.util.HashMap\",\"SUPPORT_CONTEXT\":true,\"IS_DYNAMIC\":false},\"storedValue\":\"\\\";\\\"\",\"size\":-1,\"occurMinTimes\":0,\"occurMaxTimes\":0,\"precision\":0,\"pattern\":null,\"nullable\":false,\"possibleValues\":null,\"children\":{\"@type\":\"java.util.ArrayList\"},\"currentType\":\"java.lang.String\",\"name\":\"normalizeDelimiter\",\"displayName\":null,\"title\":null},\"columnNameDelimiter\":{\"@id\":45,\"@type\":\"org.talend.daikon.properties.property.StringProperty\",\"possibleValues2\":null,\"flags\":null,\"taggedValues\":{\"@type\":\"java.util.HashMap\",\"SUPPORT_CONTEXT\":true,\"IS_DYNAMIC\":false},\"storedValue\":\"\\\"_\\\"\",\"size\":-1,\"occurMinTimes\":0,\"occurMaxTimes\":0,\"precision\":0,\"pattern\":null,\"nullable\":false,\"possibleValues\":null,\"children\":{\"@type\":\"java.util.ArrayList\"},\"currentType\":\"java.lang.String\",\"name\":\"columnNameDelimiter\",\"displayName\":null,\"title\":null},\"connection\":{\"@id\":19,\"endpoint\":{\"@id\":36,\"@type\":\"org.talend.daikon.properties.property.StringProperty\",\"possibleValues2\":null,\"flags\":null,\"taggedValues\":{\"@type\":\"java.util.HashMap\",\"SUPPORT_CONTEXT\":true,\"IS_DYNAMIC\":false},\"storedValue\":\"\\\"https://www.salesforce.com/services/Soap/u/34.0\\\"\",\"size\":-1,\"occurMinTimes\":1,\"occurMaxTimes\":1,\"precision\":0,\"pattern\":null,\"nullable\":false,\"possibleValues\":null,\"children\":{\"@type\":\"java.util.ArrayList\"},\"currentType\":\"java.lang.String\",\"name\":\"endpoint\",\"displayName\":null,\"title\":null},\"name\":{\"@id\":35,\"@type\":\"org.talend.daikon.properties.property.StringProperty\",\"possibleValues2\":null,\"flags\":null,\"taggedValues\":{\"@type\":\"java.util.HashMap\"},\"storedValue\":null,\"size\":-1,\"occurMinTimes\":1,\"occurMaxTimes\":1,\"precision\":0,\"pattern\":null,\"nullable\":false,\"possibleValues\":null,\"children\":{\"@type\":\"java.util.ArrayList\"},\"currentType\":\"java.lang.String\",\"name\":\"name\",\"displayName\":null,\"title\":null},\"loginType\":{\"@id\":22,\"@type\":\"org.talend.daikon.properties.property.EnumProperty\",\"flags\":null,\"taggedValues\":{\"@type\":\"java.util.HashMap\",\"SUPPORT_CONTEXT\":true,\"IS_DYNAMIC\":false},\"storedValue\":{\"@type\":\"org.talend.components.salesforce.SalesforceConnectionProperties$LoginType\",\"name\":\"Basic\",\"ordinal\":0},\"size\":-1,\"occurMinTimes\":1,\"occurMaxTimes\":1,\"precision\":0,\"pattern\":null,\"nullable\":false,\"possibleValues\":{\"@type\":\"java.util.Arrays$ArrayList\",\"@items\":[{\"@type\":\"org.talend.components.salesforce.SalesforceConnectionProperties$LoginType\",\"name\":\"Basic\",\"ordinal\":0},{\"@type\":\"org.talend.components.salesforce.SalesforceConnectionProperties$LoginType\",\"name\":\"OAuth\",\"ordinal\":1}]},\"children\":{\"@type\":\"java.util.ArrayList\"},\"currentType\":\"org.talend.components.salesforce.SalesforceConnectionProperties.LoginType\",\"name\":\"loginType\",\"displayName\":null,\"title\":null},\"bulkConnection\":{\"@id\":34,\"flags\":{\"@type\":\"java.util.RegularEnumSet\"},\"taggedValues\":{\"@type\":\"java.util.HashMap\",\"SUPPORT_CONTEXT\":true,\"IS_DYNAMIC\":false},\"storedValue\":{\"@type\":\"boolean\",\"value\":false},\"size\":-1,\"occurMinTimes\":0,\"occurMaxTimes\":0,\"precision\":0,\"pattern\":null,\"nullable\":false,\"possibleValues\":null,\"children\":{\"@type\":\"java.util.ArrayList\"},\"currentType\":\"java.lang.Boolean\",\"name\":\"bulkConnection\",\"displayName\":null,\"title\":null},\"needCompression\":{\"@id\":33,\"flags\":null,\"taggedValues\":{\"@type\":\"java.util.HashMap\",\"SUPPORT_CONTEXT\":true,\"IS_DYNAMIC\":false},\"storedValue\":{\"@type\":\"boolean\",\"value\":false},\"size\":-1,\"occurMinTimes\":0,\"occurMaxTimes\":0,\"precision\":0,\"pattern\":null,\"nullable\":false,\"possibleValues\":null,\"children\":{\"@type\":\"java.util.ArrayList\"},\"currentType\":\"java.lang.Boolean\",\"name\":\"needCompression\",\"displayName\":null,\"title\":null},\"timeout\":{\"@id\":32,\"flags\":null,\"taggedValues\":{\"@type\":\"java.util.HashMap\",\"SUPPORT_CONTEXT\":true,\"IS_DYNAMIC\":false},\"storedValue\":{\"@type\":\"int\",\"value\":60000},\"size\":-1,\"occurMinTimes\":0,\"occurMaxTimes\":0,\"precision\":0,\"pattern\":null,\"nullable\":false,\"possibleValues\":null,\"children\":{\"@type\":\"java.util.ArrayList\"},\"currentType\":\"java.lang.Integer\",\"name\":\"timeout\",\"displayName\":null,\"title\":null},\"httpTraceMessage\":{\"@id\":31,\"flags\":{\"@type\":\"java.util.RegularEnumSet\",\"@items\":[{\"@type\":\"org.talend.daikon.properties.property.Property$Flags\",\"name\":\"HIDDEN\",\"ordinal\":3}]},\"taggedValues\":{\"@type\":\"java.util.HashMap\",\"SUPPORT_CONTEXT\":true,\"IS_DYNAMIC\":false},\"storedValue\":{\"@type\":\"boolean\",\"value\":false},\"size\":-1,\"occurMinTimes\":0,\"occurMaxTimes\":0,\"precision\":0,\"pattern\":null,\"nullable\":false,\"possibleValues\":null,\"children\":{\"@type\":\"java.util.ArrayList\"},\"currentType\":\"java.lang.Boolean\",\"name\":\"httpTraceMessage\",\"displayName\":null,\"title\":null},\"httpChunked\":{\"@id\":30,\"flags\":null,\"taggedValues\":{\"@type\":\"java.util.HashMap\",\"SUPPORT_CONTEXT\":true,\"IS_DYNAMIC\":false},\"storedValue\":{\"@type\":\"boolean\",\"value\":true},\"size\":-1,\"occurMinTimes\":0,\"occurMaxTimes\":0,\"precision\":0,\"pattern\":null,\"nullable\":false,\"possibleValues\":null,\"children\":{\"@type\":\"java.util.ArrayList\"},\"currentType\":\"java.lang.Boolean\",\"name\":\"httpChunked\",\"displayName\":null,\"title\":null},\"clientId\":{\"@id\":29,\"@type\":\"org.talend.daikon.properties.property.StringProperty\",\"possibleValues2\":null,\"flags\":null,\"taggedValues\":{\"@type\":\"java.util.HashMap\",\"SUPPORT_CONTEXT\":true,\"IS_DYNAMIC\":false},\"storedValue\":\"\\\"\\\"\",\"size\":-1,\"occurMinTimes\":0,\"occurMaxTimes\":0,\"precision\":0,\"pattern\":null,\"nullable\":false,\"possibleValues\":null,\"children\":{\"@type\":\"java.util.ArrayList\"},\"currentType\":\"java.lang.String\",\"name\":\"clientId\",\"displayName\":null,\"title\":null},\"testConnection\":{\"@id\":28,\"formtoShow\":null,\"name\":\"testConnection\",\"displayName\":\"Test connection\",\"title\":null},\"advanced\":{\"@id\":27,\"formtoShow\":{\"@id\":20,\"subtitle\":null,\"properties\":{\"@ref\":19},\"widgetMap\":{\"endpoint\":{\"@type\":\"org.talend.daikon.properties.presentation.Widget\",\"row\":1,\"order\":1,\"hidden\":false,\"widgetType\":\"widget.type.default\",\"longRunning\":false,\"deemphasize\":false,\"callBeforeActivate\":false,\"callBeforePresent\":false,\"callValidate\":false,\"callAfter\":false,\"content\":{\"@ref\":36}},\"bulkConnection\":{\"@type\":\"org.talend.daikon.properties.presentation.Widget\",\"row\":2,\"order\":1,\"hidden\":false,\"widgetType\":\"widget.type.default\",\"longRunning\":false,\"deemphasize\":false,\"callBeforeActivate\":false,\"callBeforePresent\":false,\"callValidate\":false,\"callAfter\":true,\"content\":{\"@ref\":34}},\"needCompression\":{\"@type\":\"org.talend.daikon.properties.presentation.Widget\",\"row\":3,\"order\":1,\"hidden\":false,\"widgetType\":\"widget.type.default\",\"longRunning\":false,\"deemphasize\":false,\"callBeforeActivate\":false,\"callBeforePresent\":false,\"callValidate\":false,\"callAfter\":false,\"content\":{\"@ref\":33}},\"httpTraceMessage\":{\"@type\":\"org.talend.daikon.properties.presentation.Widget\",\"row\":4,\"order\":1,\"hidden\":true,\"widgetType\":\"widget.type.default\",\"longRunning\":false,\"deemphasize\":false,\"callBeforeActivate\":false,\"callBeforePresent\":false,\"callValidate\":false,\"callAfter\":false,\"content\":{\"@ref\":31}},\"httpChunked\":{\"@type\":\"org.talend.daikon.properties.presentation.Widget\",\"row\":5,\"order\":1,\"hidden\":false,\"widgetType\":\"widget.type.default\",\"longRunning\":false,\"deemphasize\":false,\"callBeforeActivate\":false,\"callBeforePresent\":false,\"callValidate\":false,\"callAfter\":false,\"content\":{\"@ref\":30}},\"clientId\":{\"@type\":\"org.talend.daikon.properties.presentation.Widget\",\"row\":6,\"order\":1,\"hidden\":false,\"widgetType\":\"widget.type.default\",\"longRunning\":false,\"deemphasize\":false,\"callBeforeActivate\":false,\"callBeforePresent\":false,\"callValidate\":false,\"callAfter\":false,\"content\":{\"@ref\":29}},\"timeout\":{\"@type\":\"org.talend.daikon.properties.presentation.Widget\",\"row\":7,\"order\":1,\"hidden\":false,\"widgetType\":\"widget.type.default\",\"longRunning\":false,\"deemphasize\":false,\"callBeforeActivate\":false,\"callBeforePresent\":false,\"callValidate\":false,\"callAfter\":false,\"content\":{\"@ref\":32}},\"proxy\":{\"@type\":\"org.talend.daikon.properties.presentation.Widget\",\"row\":8,\"order\":1,\"hidden\":false,\"widgetType\":\"widget.type.default\",\"longRunning\":false,\"deemphasize\":false,\"callBeforeActivate\":false,\"callBeforePresent\":false,\"callValidate\":false,\"callAfter\":false,\"content\":{\"@id\":4,\"@type\":\"org.talend.daikon.properties.presentation.Form\",\"subtitle\":null,\"properties\":{\"@id\":24,\"@type\":\"org.talend.components.common.ProxyProperties\",\"useProxy\":{\"@id\":8,\"flags\":null,\"taggedValues\":{\"@type\":\"java.util.HashMap\",\"SUPPORT_CONTEXT\":true,\"IS_DYNAMIC\":false},\"storedValue\":{\"@type\":\"boolean\",\"value\":false},\"size\":-1,\"occurMinTimes\":1,\"occurMaxTimes\":1,\"precision\":0,\"pattern\":null,\"nullable\":false,\"possibleValues\":null,\"children\":{\"@type\":\"java.util.ArrayList\"},\"currentType\":\"java.lang.Boolean\",\"name\":\"useProxy\",\"displayName\":null,\"title\":null},\"host\":{\"@id\":7,\"@type\":\"org.talend.daikon.properties.property.StringProperty\",\"possibleValues2\":null,\"flags\":{\"@type\":\"java.util.RegularEnumSet\",\"@items\":[{\"@type\":\"org.talend.daikon.properties.property.Property$Flags\",\"name\":\"HIDDEN\",\"ordinal\":3}]},\"taggedValues\":{\"@type\":\"java.util.HashMap\",\"SUPPORT_CONTEXT\":true,\"IS_DYNAMIC\":false},\"storedValue\":\"\\\"\\\"\",\"size\":-1,\"occurMinTimes\":1,\"occurMaxTimes\":1,\"precision\":0,\"pattern\":null,\"nullable\":false,\"possibleValues\":null,\"children\":{\"@type\":\"java.util.ArrayList\"},\"currentType\":\"java.lang.String\",\"name\":\"host\",\"displayName\":null,\"title\":null},\"port\":{\"@id\":6,\"flags\":{\"@type\":\"java.util.RegularEnumSet\",\"@items\":[{\"@type\":\"org.talend.daikon.properties.property.Property$Flags\",\"name\":\"HIDDEN\",\"ordinal\":3}]},\"taggedValues\":{\"@type\":\"java.util.HashMap\",\"SUPPORT_CONTEXT\":true,\"IS_DYNAMIC\":false},\"storedValue\":null,\"size\":-1,\"occurMinTimes\":1,\"occurMaxTimes\":1,\"precision\":0,\"pattern\":null,\"nullable\":false,\"possibleValues\":null,\"children\":{\"@type\":\"java.util.ArrayList\"},\"currentType\":\"java.lang.Integer\",\"name\":\"port\",\"displayName\":null,\"title\":null},\"userPassword\":{\"@id\":5,\"userId\":{\"@id\":3,\"@type\":\"org.talend.daikon.properties.property.StringProperty\",\"possibleValues2\":null,\"flags\":{\"@type\":\"java.util.RegularEnumSet\",\"@items\":[{\"@type\":\"org.talend.daikon.properties.property.Property$Flags\",\"name\":\"HIDDEN\",\"ordinal\":3}]},\"taggedValues\":{\"@type\":\"java.util.HashMap\",\"SUPPORT_CONTEXT\":true,\"IS_DYNAMIC\":false},\"storedValue\":\"\\\"\\\"\",\"size\":-1,\"occurMinTimes\":1,\"occurMaxTimes\":1,\"precision\":0,\"pattern\":null,\"nullable\":false,\"possibleValues\":null,\"children\":{\"@type\":\"java.util.ArrayList\"},\"currentType\":\"java.lang.String\",\"name\":\"userId\",\"displayName\":null,\"title\":null},\"password\":{\"@id\":2,\"@type\":\"org.talend.daikon.properties.property.StringProperty\",\"possibleValues2\":null,\"flags\":{\"@type\":\"java.util.RegularEnumSet\",\"@items\":[{\"@type\":\"org.talend.daikon.properties.property.Property$Flags\",\"name\":\"ENCRYPT\",\"ordinal\":0},{\"@type\":\"org.talend.daikon.properties.property.Property$Flags\",\"name\":\"SUPPRESS_LOGGING\",\"ordinal\":1},{\"@type\":\"org.talend.daikon.properties.property.Property$Flags\",\"name\":\"HIDDEN\",\"ordinal\":3}]},\"taggedValues\":{\"@type\":\"java.util.HashMap\",\"SUPPORT_CONTEXT\":true,\"IS_DYNAMIC\":false},\"storedValue\":\"k3WNzwEJ9MTsQqCStv4d5g==\",\"size\":-1,\"occurMinTimes\":1,\"occurMaxTimes\":1,\"precision\":0,\"pattern\":null,\"nullable\":false,\"possibleValues\":null,\"children\":{\"@type\":\"java.util.ArrayList\"},\"currentType\":\"java.lang.String\",\"name\":\"password\",\"displayName\":null,\"title\":null},\"name\":\"userPassword\",\"validationResult\":null},\"name\":\"proxy\",\"validationResult\":null},\"widgetMap\":{\"useProxy\":{\"@type\":\"org.talend.daikon.properties.presentation.Widget\",\"row\":1,\"order\":1,\"hidden\":false,\"widgetType\":\"widget.type.default\",\"longRunning\":false,\"deemphasize\":false,\"callBeforeActivate\":false,\"callBeforePresent\":false,\"callValidate\":false,\"callAfter\":true,\"content\":{\"@ref\":8}},\"host\":{\"@type\":\"org.talend.daikon.properties.presentation.Widget\",\"row\":2,\"order\":1,\"hidden\":true,\"widgetType\":\"widget.type.default\",\"longRunning\":false,\"deemphasize\":false,\"callBeforeActivate\":false,\"callBeforePresent\":false,\"callValidate\":false,\"callAfter\":false,\"content\":{\"@ref\":7}},\"port\":{\"@type\":\"org.talend.daikon.properties.presentation.Widget\",\"row\":3,\"order\":1,\"hidden\":true,\"widgetType\":\"widget.type.default\",\"longRunning\":false,\"deemphasize\":false,\"callBeforeActivate\":false,\"callBeforePresent\":false,\"callValidate\":false,\"callAfter\":false,\"content\":{\"@ref\":6}},\"userPassword\":{\"@type\":\"org.talend.daikon.properties.presentation.Widget\",\"row\":4,\"order\":1,\"hidden\":true,\"widgetType\":\"widget.type.default\",\"longRunning\":false,\"deemphasize\":false,\"callBeforeActivate\":false,\"callBeforePresent\":false,\"callValidate\":false,\"callAfter\":false,\"content\":{\"@id\":1,\"@type\":\"org.talend.daikon.properties.presentation.Form\",\"subtitle\":null,\"properties\":{\"@ref\":5},\"widgetMap\":{\"userId\":{\"@type\":\"org.talend.daikon.properties.presentation.Widget\",\"row\":1,\"order\":1,\"hidden\":true,\"widgetType\":\"widget.type.default\",\"longRunning\":false,\"deemphasize\":false,\"callBeforeActivate\":false,\"callBeforePresent\":false,\"callValidate\":false,\"callAfter\":false,\"content\":{\"@ref\":3}},\"password\":{\"@type\":\"org.talend.daikon.properties.presentation.Widget\",\"row\":2,\"order\":1,\"hidden\":true,\"widgetType\":\"widget.type.hidden.text\",\"longRunning\":false,\"deemphasize\":false,\"callBeforeActivate\":false,\"callBeforePresent\":false,\"callValidate\":false,\"callAfter\":false,\"content\":{\"@ref\":2}}},\"cancelable\":false,\"originalValues\":null,\"callBeforeFormPresent\":false,\"callAfterFormBack\":false,\"callAfterFormNext\":false,\"callAfterFormFinish\":false,\"allowBack\":false,\"allowForward\":false,\"allowFinish\":false,\"refreshUI\":true,\"name\":\"Main\",\"displayName\":\"Main\",\"title\":null}}},\"cancelable\":false,\"originalValues\":null,\"callBeforeFormPresent\":false,\"callAfterFormBack\":false,\"callAfterFormNext\":false,\"callAfterFormFinish\":false,\"allowBack\":false,\"allowForward\":false,\"allowFinish\":false,\"refreshUI\":true,\"name\":\"Main\",\"displayName\":\"Main\",\"title\":null}}},\"cancelable\":false,\"originalValues\":null,\"callBeforeFormPresent\":false,\"callAfterFormBack\":false,\"callAfterFormNext\":false,\"callAfterFormFinish\":false,\"allowBack\":false,\"allowForward\":false,\"allowFinish\":false,\"refreshUI\":true,\"name\":\"Advanced\",\"displayName\":\"Advanced\",\"title\":null},\"name\":\"advanced\",\"displayName\":\"Advanced...\",\"title\":null},\"oauth\":{\"@id\":26,\"clientId\":{\"@id\":18,\"@type\":\"org.talend.daikon.properties.property.StringProperty\",\"possibleValues2\":null,\"flags\":{\"@type\":\"java.util.RegularEnumSet\",\"@items\":[{\"@type\":\"org.talend.daikon.properties.property.Property$Flags\",\"name\":\"HIDDEN\",\"ordinal\":3}]},\"taggedValues\":{\"@type\":\"java.util.HashMap\",\"SUPPORT_CONTEXT\":true,\"IS_DYNAMIC\":false},\"storedValue\":\"\\\"\\\"\",\"size\":-1,\"occurMinTimes\":1,\"occurMaxTimes\":1,\"precision\":0,\"pattern\":null,\"nullable\":false,\"possibleValues\":null,\"children\":{\"@type\":\"java.util.ArrayList\"},\"currentType\":\"java.lang.String\",\"name\":\"clientId\",\"displayName\":null,\"title\":null},\"clientSecret\":{\"@id\":17,\"@type\":\"org.talend.daikon.properties.property.StringProperty\",\"possibleValues2\":null,\"flags\":{\"@type\":\"java.util.RegularEnumSet\",\"@items\":[{\"@type\":\"org.talend.daikon.properties.property.Property$Flags\",\"name\":\"ENCRYPT\",\"ordinal\":0},{\"@type\":\"org.talend.daikon.properties.property.Property$Flags\",\"name\":\"SUPPRESS_LOGGING\",\"ordinal\":1},{\"@type\":\"org.talend.daikon.properties.property.Property$Flags\",\"name\":\"HIDDEN\",\"ordinal\":3}]},\"taggedValues\":{\"@type\":\"java.util.HashMap\",\"SUPPORT_CONTEXT\":true,\"IS_DYNAMIC\":false},\"storedValue\":\"k3WNzwEJ9MTsQqCStv4d5g==\",\"size\":-1,\"occurMinTimes\":1,\"occurMaxTimes\":1,\"precision\":0,\"pattern\":null,\"nullable\":false,\"possibleValues\":null,\"children\":{\"@type\":\"java.util.ArrayList\"},\"currentType\":\"java.lang.String\",\"name\":\"clientSecret\",\"displayName\":null,\"title\":null},\"callbackHost\":{\"@id\":16,\"@type\":\"org.talend.daikon.properties.property.StringProperty\",\"possibleValues2\":null,\"flags\":{\"@type\":\"java.util.RegularEnumSet\",\"@items\":[{\"@type\":\"org.talend.daikon.properties.property.Property$Flags\",\"name\":\"HIDDEN\",\"ordinal\":3}]},\"taggedValues\":{\"@type\":\"java.util.HashMap\",\"SUPPORT_CONTEXT\":true,\"IS_DYNAMIC\":false},\"storedValue\":\"\\\"\\\"\",\"size\":-1,\"occurMinTimes\":1,\"occurMaxTimes\":1,\"precision\":0,\"pattern\":null,\"nullable\":false,\"possibleValues\":null,\"children\":{\"@type\":\"java.util.ArrayList\"},\"currentType\":\"java.lang.String\",\"name\":\"callbackHost\",\"displayName\":null,\"title\":null},\"callbackPort\":{\"@id\":15,\"flags\":{\"@type\":\"java.util.RegularEnumSet\",\"@items\":[{\"@type\":\"org.talend.daikon.properties.property.Property$Flags\",\"name\":\"HIDDEN\",\"ordinal\":3}]},\"taggedValues\":{\"@type\":\"java.util.HashMap\",\"SUPPORT_CONTEXT\":true,\"IS_DYNAMIC\":false},\"storedValue\":null,\"size\":-1,\"occurMinTimes\":1,\"occurMaxTimes\":1,\"precision\":0,\"pattern\":null,\"nullable\":false,\"possibleValues\":null,\"children\":{\"@type\":\"java.util.ArrayList\"},\"currentType\":\"java.lang.Integer\",\"name\":\"callbackPort\",\"displayName\":null,\"title\":null},\"tokenFile\":{\"@id\":14,\"@type\":\"org.talend.daikon.properties.property.StringProperty\",\"possibleValues2\":null,\"flags\":{\"@type\":\"java.util.RegularEnumSet\",\"@items\":[{\"@type\":\"org.talend.daikon.properties.property.Property$Flags\",\"name\":\"HIDDEN\",\"ordinal\":3}]},\"taggedValues\":{\"@type\":\"java.util.HashMap\",\"SUPPORT_CONTEXT\":true,\"IS_DYNAMIC\":false},\"storedValue\":\"\\\"\\\"\",\"size\":-1,\"occurMinTimes\":1,\"occurMaxTimes\":1,\"precision\":0,\"pattern\":null,\"nullable\":false,\"possibleValues\":null,\"children\":{\"@type\":\"java.util.ArrayList\"},\"currentType\":\"java.lang.String\",\"name\":\"tokenFile\",\"displayName\":null,\"title\":null},\"name\":\"oauth\",\"validationResult\":null},\"userPassword\":{\"@id\":25,\"securityKey\":{\"@id\":12,\"@type\":\"org.talend.daikon.properties.property.StringProperty\",\"possibleValues2\":null,\"flags\":{\"@type\":\"java.util.RegularEnumSet\",\"@items\":[{\"@type\":\"org.talend.daikon.properties.property.Property$Flags\",\"name\":\"ENCRYPT\",\"ordinal\":0},{\"@type\":\"org.talend.daikon.properties.property.Property$Flags\",\"name\":\"SUPPRESS_LOGGING\",\"ordinal\":1}]},\"taggedValues\":{\"@type\":\"java.util.HashMap\",\"SUPPORT_CONTEXT\":true,\"IS_DYNAMIC\":false},\"storedValue\":\"k3WNzwEJ9MTsQqCStv4d5g==\",\"size\":-1,\"occurMinTimes\":1,\"occurMaxTimes\":1,\"precision\":0,\"pattern\":null,\"nullable\":false,\"possibleValues\":null,\"children\":{\"@type\":\"java.util.ArrayList\"},\"currentType\":\"java.lang.String\",\"name\":\"securityKey\",\"displayName\":null,\"title\":null},\"userId\":{\"@id\":11,\"@type\":\"org.talend.daikon.properties.property.StringProperty\",\"possibleValues2\":null,\"flags\":null,\"taggedValues\":{\"@type\":\"java.util.HashMap\",\"SUPPORT_CONTEXT\":true,\"IS_DYNAMIC\":false},\"storedValue\":\"\\\"\\\"\",\"size\":-1,\"occurMinTimes\":1,\"occurMaxTimes\":1,\"precision\":0,\"pattern\":null,\"nullable\":false,\"possibleValues\":null,\"children\":{\"@type\":\"java.util.ArrayList\"},\"currentType\":\"java.lang.String\",\"name\":\"userId\",\"displayName\":null,\"title\":null},\"password\":{\"@id\":10,\"@type\":\"org.talend.daikon.properties.property.StringProperty\",\"possibleValues2\":null,\"flags\":{\"@type\":\"java.util.RegularEnumSet\",\"@items\":[{\"@type\":\"org.talend.daikon.properties.property.Property$Flags\",\"name\":\"ENCRYPT\",\"ordinal\":0},{\"@type\":\"org.talend.daikon.properties.property.Property$Flags\",\"name\":\"SUPPRESS_LOGGING\",\"ordinal\":1}]},\"taggedValues\":{\"@type\":\"java.util.HashMap\",\"SUPPORT_CONTEXT\":true,\"IS_DYNAMIC\":false},\"storedValue\":\"k3WNzwEJ9MTsQqCStv4d5g==\",\"size\":-1,\"occurMinTimes\":1,\"occurMaxTimes\":1,\"precision\":0,\"pattern\":null,\"nullable\":false,\"possibleValues\":null,\"children\":{\"@type\":\"java.util.ArrayList\"},\"currentType\":\"java.lang.String\",\"name\":\"password\",\"displayName\":null,\"title\":null},\"name\":\"userPassword\",\"validationResult\":null},\"proxy\":{\"@ref\":24},\"referencedComponent\":{\"@id\":23,\"referenceType\":{\"@type\":\"org.talend.daikon.properties.property.EnumProperty\",\"flags\":null,\"taggedValues\":{\"@type\":\"java.util.HashMap\"},\"storedValue\":null,\"size\":-1,\"occurMinTimes\":0,\"occurMaxTimes\":0,\"precision\":0,\"pattern\":null,\"nullable\":false,\"possibleValues\":{\"@type\":\"java.util.Arrays$ArrayList\",\"@items\":[{\"@type\":\"org.talend.components.api.properties.ComponentReferenceProperties$ReferenceType\",\"name\":\"THIS_COMPONENT\",\"ordinal\":0},{\"@type\":\"org.talend.components.api.properties.ComponentReferenceProperties$ReferenceType\",\"name\":\"COMPONENT_TYPE\",\"ordinal\":1},{\"@type\":\"org.talend.components.api.properties.ComponentReferenceProperties$ReferenceType\",\"name\":\"COMPONENT_INSTANCE\",\"ordinal\":2}]},\"children\":{\"@type\":\"java.util.ArrayList\"},\"currentType\":\"org.talend.components.api.properties.ComponentReferenceProperties.ReferenceType\",\"name\":\"referenceType\",\"displayName\":null,\"title\":null},\"componentType\":{\"@type\":\"org.talend.daikon.properties.property.StringProperty\",\"possibleValues2\":null,\"flags\":{\"@type\":\"java.util.RegularEnumSet\",\"@items\":[{\"@type\":\"org.talend.daikon.properties.property.Property$Flags\",\"name\":\"DESIGN_TIME_ONLY\",\"ordinal\":2}]},\"taggedValues\":{\"@type\":\"java.util.HashMap\"},\"storedValue\":\"tSalesforceConnection\",\"size\":-1,\"occurMinTimes\":0,\"occurMaxTimes\":0,\"precision\":0,\"pattern\":null,\"nullable\":false,\"possibleValues\":null,\"children\":{\"@type\":\"java.util.ArrayList\"},\"currentType\":\"java.lang.String\",\"name\":\"componentType\",\"displayName\":null,\"title\":null},\"componentInstanceId\":{\"@type\":\"org.talend.daikon.properties.property.StringProperty\",\"possibleValues2\":null,\"flags\":null,\"taggedValues\":{\"@type\":\"java.util.HashMap\",\"ADD_QUOTES\":true},\"storedValue\":null,\"size\":-1,\"occurMinTimes\":0,\"occurMaxTimes\":0,\"precision\":0,\"pattern\":null,\"nullable\":false,\"possibleValues\":null,\"children\":{\"@type\":\"java.util.ArrayList\"},\"currentType\":\"java.lang.String\",\"name\":\"componentInstanceId\",\"displayName\":null,\"title\":null},\"componentProperties\":null,\"enclosingProperties\":{\"@ref\":19},\"name\":\"referencedComponent\",\"validationResult\":null},\"org.talend.daikon.properties.PropertiesImpl.name\":\"connection\",\"validationResult\":null},\"module\":{\"@id\":42,\"connection\":{\"@ref\":19},\"moduleName\":{\"@id\":43,\"possibleValues2\":null,\"flags\":null,\"taggedValues\":{\"@type\":\"java.util.HashMap\",\"SUPPORT_CONTEXT\":true,\"IS_DYNAMIC\":false},\"storedValue\":\"\\\"\\\"\",\"size\":-1,\"occurMinTimes\":0,\"occurMaxTimes\":0,\"precision\":0,\"pattern\":null,\"nullable\":false,\"possibleValues\":null,\"children\":{\"@type\":\"java.util.ArrayList\"},\"currentType\":\"java.lang.String\",\"name\":\"moduleName\",\"displayName\":null,\"title\":null},\"schemaListener\":null,\"main\":{\"@id\":40,\"@type\":\"org.talend.components.salesforce.SalesforceModuleProperties$1\",\"this$0\":{\"@ref\":42},\"schema\":{\"@id\":39,\"@type\":\"org.talend.daikon.properties.property.SchemaProperty\",\"flags\":null,\"taggedValues\":{\"@type\":\"java.util.HashMap\",\"SUPPORT_CONTEXT\":false,\"IS_DYNAMIC\":false},\"storedValue\":\"{\\\"type\\\":\\\"record\\\",\\\"name\\\":\\\"EmptyRecord\\\",\\\"fields\\\":[]}\",\"size\":-1,\"occurMinTimes\":0,\"occurMaxTimes\":0,\"precision\":0,\"pattern\":null,\"nullable\":false,\"possibleValues\":null,\"children\":{\"@type\":\"java.util.ArrayList\"},\"currentType\":\"org.apache.avro.Schema\",\"name\":\"schema\",\"displayName\":null,\"title\":null},\"name\":\"main\",\"validationResult\":{\"status\":{\"name\":\"OK\",\"ordinal\":0},\"number\":0,\"message\":null}},\"name\":\"module\",\"validationResult\":null},\"name\":\"root\",\"validationResult\":null}";

        // Should not get exception
        try {
            Properties.Helper.fromSerializedPersistent(serialized, ComponentProperties.class);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Unexpected exception: " + e);
        }

    }

    @Test
    public void testRuntimeInfo() {
        ComponentDefinition definition = getComponentService()
                .getComponentDefinition(TSalesforceBulkExecDefinition.COMPONENT_NAME);
        ComponentProperties properties = this.getComponentService()
                .getComponentProperties(TSalesforceBulkExecDefinition.COMPONENT_NAME);

        assertNotNull("should not null", definition.getRuntimeInfo(ExecutionEngine.DI, properties, ConnectorTopology.NONE));
        assertNotNull("should not null", definition.getRuntimeInfo(ExecutionEngine.DI, properties, ConnectorTopology.OUTGOING));
    }

}
