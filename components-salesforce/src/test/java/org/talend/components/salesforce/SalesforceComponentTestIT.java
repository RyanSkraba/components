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

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.talend.components.api.NamedThing;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.NameAndLabel;
import org.talend.components.api.properties.PresentationItem;
import org.talend.components.api.properties.Property;
import org.talend.components.api.properties.Repository;
import org.talend.components.api.properties.ValidationResult;
import org.talend.components.api.properties.presentation.Form;
import org.talend.components.api.runtime.ComponentDynamicHolder;
import org.talend.components.api.runtime.DefaultComponentRuntimeContainerImpl;
import org.talend.components.api.schema.Schema;
import org.talend.components.api.schema.SchemaElement;
import org.talend.components.api.schema.SchemaFactory;
import org.talend.components.api.service.AbstractComponentTest;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.wizard.ComponentWizard;
import org.talend.components.api.wizard.ComponentWizardDefinition;
import org.talend.components.api.wizard.WizardImageType;
import org.talend.components.common.oauth.OauthProperties;
import org.talend.components.salesforce.tsalesforcebulkexec.TSalesforceBulkExecDefinition;
import org.talend.components.salesforce.tsalesforcebulkexec.TSalesforceBulkExecProperties;
import org.talend.components.salesforce.tsalesforceconnection.TSalesforceConnectionDefinition;
import org.talend.components.salesforce.tsalesforcegetdeleted.TSalesforceGetDeletedDefinition;
import org.talend.components.salesforce.tsalesforcegetservertimestamp.TSalesforceGetServerTimestampDefinition;
import org.talend.components.salesforce.tsalesforcegetservertimestamp.TSalesforceGetServerTimestampProperties;
import org.talend.components.salesforce.tsalesforcegetupdated.TSalesforceGetUpdatedDefinition;
import org.talend.components.salesforce.tsalesforceinput.TSalesforceInputDefinition;
import org.talend.components.salesforce.tsalesforceinput.TSalesforceInputProperties;
import org.talend.components.salesforce.tsalesforceoutput.TSalesforceOutputDefinition;
import org.talend.components.salesforce.tsalesforceoutput.TSalesforceOutputProperties;
import org.talend.components.salesforce.tsalesforceoutputbulk.TSalesforceOutputBulkDefinition;
import org.talend.components.salesforce.tsalesforcewavebulkexec.TSalesforceWaveBulkExecDefinition;
import org.talend.components.salesforce.tsalesforcewaveoutputbulkexec.TSalesforceWaveOutputBulkExecDefinition;
import org.talend.components.test.ComponentTestUtils;
import org.talend.components.test.SimpleComponentRegistry;
import org.talend.components.test.SimpleComponentService;

public class SalesforceComponentTestIT extends AbstractComponentTest {

    private ComponentService componentService;

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
            testComponentRegistry.addComponent(TSalesforceConnectionDefinition.COMPONENT_NAME,
                    new TSalesforceConnectionDefinition());
            testComponentRegistry.addComponent(TSalesforceBulkExecDefinition.COMPONENT_NAME, new TSalesforceBulkExecDefinition());
            testComponentRegistry.addComponent(TSalesforceGetServerTimestampDefinition.COMPONENT_NAME,
                    new TSalesforceGetServerTimestampDefinition());
            testComponentRegistry.addComponent(TSalesforceInputDefinition.COMPONENT_NAME, new TSalesforceInputDefinition());
            testComponentRegistry.addComponent(TSalesforceOutputDefinition.COMPONENT_NAME, new TSalesforceOutputDefinition());
            testComponentRegistry.addComponent(TSalesforceGetDeletedDefinition.COMPONENT_NAME,
                    new TSalesforceGetDeletedDefinition());
            testComponentRegistry.addComponent(TSalesforceGetUpdatedDefinition.COMPONENT_NAME,
                    new TSalesforceGetUpdatedDefinition());
            testComponentRegistry.addComponent(TSalesforceOutputBulkDefinition.COMPONENT_NAME,
                    new TSalesforceOutputBulkDefinition());
            testComponentRegistry.addComponent(TSalesforceWaveBulkExecDefinition.COMPONENT_NAME,
                    new TSalesforceWaveBulkExecDefinition());
            testComponentRegistry.addComponent(TSalesforceWaveOutputBulkExecDefinition.COMPONENT_NAME,
                    new TSalesforceWaveOutputBulkExecDefinition());
            SalesforceConnectionWizardDefinition scwd = new SalesforceConnectionWizardDefinition();
            testComponentRegistry.addWizard(SalesforceConnectionWizardDefinition.COMPONENT_WIZARD_NAME, scwd);
            testComponentRegistry.addWizard(SalesforceModuleWizardDefinition.COMPONENT_WIZARD_NAME,
                    new SalesforceModuleWizardDefinition());
            testComponentRegistry.addWizard(SalesforceConnectionEditWizardDefinition.COMPONENT_WIZARD_NAME,
                    new SalesforceConnectionEditWizardDefinition());
            componentService = new SimpleComponentService(testComponentRegistry);
            scwd.setupComponentService(componentService);
        }
        return componentService;
    }

    static final boolean ADD_QUOTES = true;

    static final boolean DO_NOT_ADD_QUOTES = false;

    String userId;

    String password;

    String securityKey;

    // Test schema
    Schema schema;

    ComponentDynamicHolder dynamic;

    // SalesforceRuntime runtime;

    // Used to make sure we have our own data
    String random;

    public static final String TEST_KEY = "Address2 456";

    public SalesforceComponentTestIT() {
        random = Integer.toString(ThreadLocalRandom.current().nextInt(1, 100000));
        userId = System.getProperty("salesforce.user");
        password = System.getProperty("salesforce.password");
        securityKey = System.getProperty("salesforce.key");
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
        ComponentTestUtils.checkSerialize(props);
        System.out.println(f);
        System.out.println(props);
        assertEquals(Form.MAIN, f.getName());
    }

    @Test
    public void testAfterLoginType() throws Throwable {
        ComponentProperties props;

        props = new TSalesforceConnectionDefinition().createProperties();
        ComponentTestUtils.checkSerialize(props);
        Property loginType = (Property) props.getProperty("loginType");
        System.out.println(loginType.getPossibleValues());
        assertEquals("Basic", loginType.getPossibleValues().get(0).toString());
        assertEquals("OAuth", loginType.getPossibleValues().get(1).toString());
        assertEquals(SalesforceConnectionProperties.LOGIN_BASIC, props.getValue(loginType));
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
        SchemaElement returns = props.getProperty(ComponentProperties.RETURNS);
        assertEquals("NB_LINE", returns.getChildren().get(0).getName());
    }

    protected SalesforceRuntime createRuntime(ComponentDefinition definition) {
        SalesforceRuntime runtime = (SalesforceRuntime) definition.createRuntime();
        runtime.setContainer(new TestRuntimeContainer());
        return runtime;
    }

    static class RepoProps {

        ComponentProperties props;

        String name;

        String repoLocation;

        Schema schema;

        RepoProps(ComponentProperties props, String name, String repoLocation, Schema schema) {
            this.props = props;
            this.name = name;
            this.repoLocation = repoLocation;
            this.schema = schema;
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
        public String storeComponentProperties(ComponentProperties properties, String name, String repositoryLocation,
                Schema schema) {
            RepoProps rp = new RepoProps(properties, name, repositoryLocation, schema);
            repoProps.add(rp);
            System.out.println(rp);
            return repositoryLocation + ++locationNum;
        }

        @Override
        public ComponentProperties getPropertiesForComponent(String componentId) {
            if (componentId.equals(componentIdToCheck)) {
                System.out.println("getProps: " + componentId + " found: " + properties);
                return properties;
            }
            return null;
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

        SalesforceConnectionProperties connProps = (SalesforceConnectionProperties) connFormWizard.getComponentProperties();

        Form af = connProps.getForm(Form.ADVANCED);
        assertTrue(((PresentationItem) connFormWizard.getChild("advanced")).getFormtoShow() + " should be == to " + af,
                ((PresentationItem) connFormWizard.getChild("advanced")).getFormtoShow() == af);

        Object image = getComponentService().getWizardPngImage(SalesforceConnectionWizardDefinition.COMPONENT_WIZARD_NAME,
                WizardImageType.TREE_ICON_16X16);
        assertNotNull(image);
        image = getComponentService().getWizardPngImage(SalesforceConnectionWizardDefinition.COMPONENT_WIZARD_NAME,
                WizardImageType.WIZARD_BANNER_75X66);
        assertNotNull(image);

        // Check the non-top-level wizard

        // check password i18n
        assertEquals("Name", connProps.getProperty("name").getDisplayName());
        connProps.setValue(connProps.name, "connName");
        setupProps(connProps, DO_NOT_ADD_QUOTES);
        Form userPassword = (Form) connFormWizard.getChild("userPassword");
        SchemaElement passwordSe = (SchemaElement) userPassword.getChild("password");
        assertEquals("Password", passwordSe.getDisplayName());
        // check name i18n
        NamedThing nameProp = connFormWizard.getChild("name"); //$NON-NLS-1$
        assertEquals("Name", nameProp.getDisplayName());
        connProps = (SalesforceConnectionProperties) checkAndValidate(connFormWizard, "testConnection", connProps);
        assertTrue(connFormWizard.isAllowForward());

        Form modForm = forms.get(1);
        SalesforceModuleListProperties mlProps = (SalesforceModuleListProperties) modForm.getComponentProperties();
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
        System.out.println(mlProps.getValue(mlProps.moduleName));
        @SuppressWarnings("unchecked")
        List<NameAndLabel> all = (List<NameAndLabel>) mlProps.getValue(mlProps.moduleName);
        assertNull(all);
        // TCOMP-9 Change the module list to use getPossibleValues() for SalesforceModuleListProperties
        List<NameAndLabel> possibleValues = (List<NameAndLabel>) mlProps.moduleName.getPossibleValues();
        assertTrue(possibleValues.size() > 50);
        List<NameAndLabel> selected = new ArrayList<>();
        selected.add(possibleValues.get(0));
        selected.add(possibleValues.get(2));
        selected.add(possibleValues.get(3));

        mlProps.setValue(mlProps.moduleName, selected);
        getComponentService().afterFormFinish(modForm.getName(), mlProps);
        System.out.println(repoProps);
        assertEquals(4, repoProps.size());
        int i = 0;
        for (RepoProps rp : repoProps) {
            if (i == 0) {
                assertEquals("connName", rp.name);
                SalesforceConnectionProperties storedConnProps = (SalesforceConnectionProperties) rp.props;
                assertEquals(userId, storedConnProps.userPassword.getValue(storedConnProps.userPassword.userId));
                assertEquals(password, storedConnProps.userPassword.getValue(storedConnProps.userPassword.password));
            } else {
                SalesforceModuleProperties storedModule = (SalesforceModuleProperties) rp.props;
                assertEquals(selected.get(i - 1).name, storedModule.getValue(storedModule.moduleName));
                assertTrue(rp.schema.getRoot().getChildren().size() > 10);
                assertTrue(storedModule.schema.getValue(storedModule.schema.schema) == rp.schema);
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
        SalesforceConnectionProperties connProps = (SalesforceConnectionProperties) connFormWizard.getComponentProperties();

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
        assertFalse(connProps == subWizards[1].getForms().get(0).getComponentProperties());
        // Add module wizard - we refer to the existing connection properties as we don't present the UI
        // for them.
        assertTrue(connProps == ((SalesforceModuleListProperties) subWizards[2].getForms().get(0).getComponentProperties())
                .getConnectionProps());
        assertFalse(subWizards[1].getDefinition().isTopLevel());
        assertEquals("Edit SalesforceNew Connection", subWizards[1].getDefinition().getMenuItemName());
        assertTrue(subWizards[0].getDefinition().isTopLevel());
        assertEquals("Create SalesforceNew Connection", subWizards[0].getDefinition().getMenuItemName());
        assertFalse(subWizards[2].getDefinition().isTopLevel());
        assertEquals("Add SalesforceNew Modules", subWizards[2].getDefinition().getMenuItemName());
    }

    private SalesforceConnectionProperties setupProps(SalesforceConnectionProperties props, boolean addQuotes) {
        if (props == null) {
            props = (SalesforceConnectionProperties) getComponentService()
                    .getComponentProperties(TSalesforceConnectionDefinition.COMPONENT_NAME);
        }
        ComponentProperties userPassword = (ComponentProperties) props.getProperty("userPassword");
        userPassword.setValue(userPassword.getProperty("userId"), addQuotes ? "\"" + userId + "\"" : userId);
        userPassword.setValue(userPassword.getProperty("password"), addQuotes ? "\"" + password + "\"" : password);
        userPassword.setValue(userPassword.getProperty("securityKey"), addQuotes ? "\"" + securityKey + "\"" : securityKey);
        return props;
    }

    @Test
    public void testLogin() throws Throwable {
        SalesforceConnectionProperties props = setupProps(null, DO_NOT_ADD_QUOTES);
        Form f = props.getForm(SalesforceConnectionProperties.FORM_WIZARD);
        props = (SalesforceConnectionProperties) checkAndValidate(f, "testConnection", props);
        System.out.println(props.getValidationResult());
        assertEquals(ValidationResult.Result.OK, props.getValidationResult().getStatus());
    }

    @Test
    public void testLoginWithQuotes() throws Throwable {
        SalesforceConnectionProperties props = setupProps(null, ADD_QUOTES);
        Form f = props.getForm(SalesforceConnectionProperties.FORM_WIZARD);
        props = (SalesforceConnectionProperties) checkAndValidate(f, "testConnection", props);
        System.out.println(props.getValidationResult());
        assertEquals(ValidationResult.Result.OK, props.getValidationResult().getStatus());
    }

    @Test
    public void testLoginFail() throws Throwable {
        SalesforceConnectionProperties props = setupProps(null, DO_NOT_ADD_QUOTES);
        props.userPassword.setValue(props.userPassword.userId, "blah");
        Form f = props.getForm(SalesforceConnectionProperties.FORM_WIZARD);
        props = (SalesforceConnectionProperties) checkAndValidate(f, "testConnection", props);
        System.out.println(props.getValidationResult());
        assertEquals(ValidationResult.Result.ERROR, props.getValidationResult().getStatus());
    }

    @Test
    public void testBulkLogin() throws Throwable {
        SalesforceConnectionProperties props = setupProps(null, DO_NOT_ADD_QUOTES);
        props.setValue(props.bulkConnection, true);
        Form f = props.getForm(SalesforceConnectionProperties.FORM_WIZARD);
        props = (SalesforceConnectionProperties) checkAndValidate(f, "testConnection", props);
        assertEquals(ValidationResult.Result.OK, props.getValidationResult().getStatus());
        System.out.println(props.getValidationResult());
    }

    @Test
    public void testBulkLoginWithQuotes() throws Throwable {
        SalesforceConnectionProperties props = setupProps(null, ADD_QUOTES);
        props.setValue(props.bulkConnection, true);
        Form f = props.getForm(SalesforceConnectionProperties.FORM_WIZARD);
        props = (SalesforceConnectionProperties) checkAndValidate(f, "testConnection", props);
        assertEquals(ValidationResult.Result.OK, props.getValidationResult().getStatus());
        System.out.println(props.getValidationResult());
    }

    private SalesforceConnectionProperties setupOAuthProps(SalesforceConnectionProperties props) throws Throwable {
        if (props == null) {
            props = (SalesforceConnectionProperties) getComponentService()
                    .getComponentProperties(TSalesforceConnectionDefinition.COMPONENT_NAME);
        }
        props.setValue(props.loginType, SalesforceConnectionProperties.LOGIN_OAUTH);
        Form mainForm = props.getForm(Form.MAIN);
        props = (SalesforceConnectionProperties) checkAndAfter(mainForm, "loginType", props);
        props.oauth.setValue(props.oauth.clientId,
                "3MVG9Y6d_Btp4xp6ParHznfCCUh0d9fU3LYcvd_hCXz3G3Owp4KvaDhNuEOrXJTBd09JMoPdZeDtNYxXZM4X2");
        props.oauth.setValue(props.oauth.clientSecret, "3545101463828280342");
        props.oauth.setValue(props.oauth.callbackHost, "localhost");
        props.oauth.setValue(props.oauth.callbackPort, 8115);
        // props.oauth.tokenFile.setValue();
        return props;
    }

    @Ignore("oauth need manual operation")
    @Test
    public void testOAuthLogin() throws Throwable {
        SalesforceConnectionProperties props = setupOAuthProps(null);
        Form f = props.getForm(Form.MAIN);
        props = (SalesforceConnectionProperties) checkAndValidate(f, "testConnection", props);
        assertEquals(ValidationResult.Result.OK, props.getValidationResult().getStatus());
        System.out.println(props.getValidationResult());
    }

    @Ignore("oauth need manual operation")
    @Test
    public void testOAuthBulkLogin() throws Throwable {
        SalesforceConnectionProperties props = setupOAuthProps(null);
        props.setValue(props.bulkConnection, true);
        Form f = props.getForm(Form.MAIN);
        props = (SalesforceConnectionProperties) checkAndValidate(f, "testConnection", props);
        assertEquals(ValidationResult.Result.OK, props.getValidationResult().getStatus());
        System.out.println(props.getValidationResult());
    }

    @Test
    public void testModuleNames() throws Throwable {
        TSalesforceInputProperties props = (TSalesforceInputProperties) getComponentService()
                .getComponentProperties(TSalesforceInputDefinition.COMPONENT_NAME);
        setupProps(props.connection, DO_NOT_ADD_QUOTES);
        ComponentTestUtils.checkSerialize(props);

        assertEquals(2, props.getForms().size());
        Form f = props.module.getForm(Form.REFERENCE);
        assertTrue(f.getWidget("moduleName").isCallBeforeActivate());
        // The Form is bound to a Properties object that created it. The Forms might not always be associated with the
        // properties object
        // they came from.
        ComponentProperties moduleProps = f.getComponentProperties();
        moduleProps = checkAndBeforeActivate(f, "moduleName", moduleProps);
        SchemaElement prop = (SchemaElement) f.getChild("moduleName");
        assertTrue(prop.getPossibleValues().size() > 100);
        System.out.println(prop.getPossibleValues());
        System.out.println(moduleProps.getValidationResult());
    }

    @Test
    public void testSchema() throws Throwable {
        TSalesforceInputProperties props = (TSalesforceInputProperties) getComponentService()
                .getComponentProperties(TSalesforceInputDefinition.COMPONENT_NAME);
        setupProps(props.connection, DO_NOT_ADD_QUOTES);

        Form f = props.module.getForm(Form.REFERENCE);
        SalesforceModuleProperties moduleProps = (SalesforceModuleProperties) f.getComponentProperties();
        moduleProps = (SalesforceModuleProperties) checkAndBeforeActivate(f, "moduleName", moduleProps);
        moduleProps.setValue(moduleProps.moduleName, "Account");
        moduleProps = (SalesforceModuleProperties) checkAndAfter(f, "moduleName", moduleProps);
        Schema schema = (Schema) moduleProps.schema.getValue(moduleProps.schema.schema);
        System.out.println(schema);
        for (SchemaElement child : schema.getRoot().getChildren()) {
            System.out.println(child.getName());
        }
        assertEquals("Id", schema.getRoot().getChildren().get(0).getName());
        assertTrue(schema.getRoot().getChildren().size() > 50);
    }

    @Test
    public void testInputConnectionRef() throws Throwable {
        ComponentDefinition definition = getComponentService().getComponentDefinition(TSalesforceInputDefinition.COMPONENT_NAME);
        TSalesforceInputProperties props = (TSalesforceInputProperties) getComponentService()
                .getComponentProperties(TSalesforceInputDefinition.COMPONENT_NAME);
        setupProps(props.connection, DO_NOT_ADD_QUOTES);
        SalesforceRuntime runtime = createRuntime(definition);
        runtime.setComponentService(getComponentService());
        runtime.connect(props.connection);

        // Referenced properties simulating salesforce connect component
        SalesforceConnectionProperties cProps = (SalesforceConnectionProperties) getComponentService()
                .getComponentProperties(TSalesforceConnectionDefinition.COMPONENT_NAME);
        setupProps(cProps, DO_NOT_ADD_QUOTES);
        cProps.userPassword.setValue(cProps.userPassword.password, "xxx");

        String compId = "comp1";

        TestRepository repo = new TestRepository(null);
        repo.properties = cProps;
        repo.componentIdToCheck = compId;
        getComponentService().setRepository(repo);

        // Use the connection props of the salesforce connect component
        props.connection.setValue(props.connection.referencedComponentId, compId);
        checkAndAfter(props.connection.getForm(Form.REFERENCE), "referencedComponentId", props.connection);
        try {
            runtime.connect(props.connection);
            fail("Expected exception");
        } catch (Exception ex) {
            System.out.println("Got expected: " + ex.getMessage());
        }

        // Back to using the connection props of the salesforce input component
        props.connection.setValue(props.connection.referencedComponentId, null);
        runtime.connect(props.connection);
    }

    protected void setupModule(SalesforceModuleProperties moduleProps, String module) throws Throwable {
        Form f = moduleProps.getForm(Form.REFERENCE);
        moduleProps = (SalesforceModuleProperties) checkAndBeforeActivate(f, "moduleName", moduleProps);
        moduleProps.setValue(moduleProps.moduleName, module);
        moduleProps = (SalesforceModuleProperties) checkAndAfter(f, "moduleName", moduleProps);
        schema = (Schema) moduleProps.schema.getValue(moduleProps.schema.schema);
    }

    @Test
    public void testInput() throws Throwable {
        runInputTest(!DYNAMIC);
    }

    @Test
    public void testInputDynamic() throws Throwable {
        runInputTest(DYNAMIC);
    }

    protected static final boolean DYNAMIC = true;

    protected void runInputTest(boolean isDynamic) throws Throwable {
        ComponentDefinition definition = getComponentService().getComponentDefinition(TSalesforceInputDefinition.COMPONENT_NAME);
        TSalesforceInputProperties props = (TSalesforceInputProperties) getComponentService()
                .getComponentProperties(TSalesforceInputDefinition.COMPONENT_NAME);
        setupProps(props.connection, DO_NOT_ADD_QUOTES);

        setupModule(props.module, "Account");
        if (isDynamic) {
            fixSchemaForDynamic();
        }

        ComponentTestUtils.checkSerialize(props);
        SalesforceRuntime runtime = createRuntime(definition);

        Map<String, Object> row = new HashMap<>();

        int count = 10;
        List<Map<String, Object>> outputRows = makeRows(count);
        outputRows = writeRows(runtime, props, outputRows);

        List<Map<String, Object>> rows = new ArrayList<>();
        runtime.input(props, rows);
        checkRows(rows, count);
        deleteRows(runtime, outputRows);
    }

    protected boolean setupDynamic() {
        if (dynamic != null) {
            return true;
        }
        if (schema == null) {
            return false;
        }
        for (SchemaElement se : schema.getRoot().getChildren()) {
            if (se.getType() == SchemaElement.Type.DYNAMIC) {
                if (dynamic == null) {
                    TestRuntimeContainer container = new TestRuntimeContainer();
                    dynamic = container.createDynamicHolder();
                    Schema dynSchema = SchemaFactory.newSchema();
                    dynSchema.setRoot(SchemaFactory.newSchemaElement(SchemaElement.Type.STRING, "Root"));
                    dynSchema.getRoot().addChild(SchemaFactory.newSchemaElement(SchemaElement.Type.STRING, "ShippingState"));
                    dynamic.setSchemaElements(dynSchema.getRoot().getChildren());
                }
                return true;
            }
        }
        return false;
    }

    protected void addDynamicColumn(Map<String, Object> row) {
        if (setupDynamic()) {
            dynamic.addFieldValue("ShippingState", "CA");
            row.put("dynamic", dynamic);
        }
    }

    protected void fixSchemaForDynamic() {
        SchemaElement dynElement = SchemaFactory.newSchemaElement(SchemaElement.Type.DYNAMIC, "dynamic");
        schema.getRoot().addChild(dynElement);
        Iterator<SchemaElement> it = schema.getRoot().getChildren().iterator();
        while (it.hasNext()) {
            SchemaElement se = it.next();
            if (se.getName().equals("ShippingState")) {
                it.remove();
                break;
            }
        }
    }

    protected List<Map<String, Object>> makeRows(int count) {
        List<Map<String, Object>> outputRows = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Map<String, Object> row = new HashMap<>();
            row.put("Name", "TestName");
            row.put("ShippingStreet", TEST_KEY);
            row.put("ShippingPostalCode", Integer.toString(i));
            row.put("BillingStreet", "123 Main Street");
            row.put("BillingState", "CA");
            row.put("BillingPostalCode", random);
            addDynamicColumn(row);
            System.out.println("out: " + row.get("Name") + " id: " + row.get("Id") + " post: " + row.get("BillingPostalCode")
                    + " st: " + " street: " + row.get("BillingStreet"));
            outputRows.add(row);
        }
        return outputRows;
    }

    protected List<Map<String, Object>> checkRows(List<Map<String, Object>> rows, int count) {
        List<Map<String, Object>> checkedRows = new ArrayList<>();

        int checkCount = 0;
        int checkDynamicCount = 0;
        for (Map<String, Object> row : rows) {
            System.out.println("check: " + row.get("Name") + " id: " + row.get("Id") + " post: " + row.get("BillingPostalCode")
                    + " st: " + " post: " + row.get("BillingStreet"));
            String check = (String) row.get("ShippingStreet");
            if (check == null || !check.equals(TEST_KEY)) {
                continue;
            }
            check = (String) row.get("BillingPostalCode");
            if (check == null || !check.equals(random)) {
                continue;
            }
            checkCount++;
            if (dynamic != null) {
                ComponentDynamicHolder d = (ComponentDynamicHolder) row.get("dynamic");
                assertEquals("CA", d.getFieldValue("ShippingState"));
                checkDynamicCount++;
            }
            assertEquals("TestName", row.get("Name"));
            assertEquals("123 Main Street", row.get("BillingStreet"));
            assertEquals("CA", row.get("BillingState"));
            checkedRows.add(row);
        }
        assertEquals(count, checkCount);
        if (dynamic != null) {
            assertEquals(count, checkDynamicCount);
            System.out.println("Check dynamic rows: " + checkDynamicCount);
        }
        return checkedRows;
    }

    protected List<String> getDeleteIds(List<Map<String, Object>> rows) {
        List<String> ids = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            System.out.println("del: " + row.get("Name") + " id: " + row.get("Id") + " post: " + row.get("BillingPostalCode")
                    + " st: " + " post: " + row.get("BillingStreet"));
            String check = (String) row.get("ShippingStreet");
            if (check == null || !check.equals(TEST_KEY)) {
                continue;
            }
            ids.add((String) row.get("Id"));
        }
        return ids;
    }

    protected List<Map<String, Object>> readAndCheckRows(SalesforceRuntime runtime, SalesforceConnectionModuleProperties props,
            int count) throws Exception {
        List<Map<String, Object>> inputRows = new ArrayList<>();
        TSalesforceInputProperties inputProps = (TSalesforceInputProperties) getComponentService()
                .getComponentProperties(TSalesforceInputDefinition.COMPONENT_NAME);
        inputProps.connection = props.connection;
        inputProps.module = props.module;
        inputProps.setValue(inputProps.batchSize, 200);
        runtime.input(inputProps, inputRows);
        return checkRows(inputRows, count);
    }

    // Returns the rows written (having been re-read so they have their Ids)
    protected List<Map<String, Object>> writeRows(SalesforceRuntime runtime, SalesforceConnectionModuleProperties props,
            List<Map<String, Object>> outputRows) throws Exception {
        TSalesforceOutputProperties outputProps;
        outputProps = (TSalesforceOutputProperties) getComponentService()
                .getComponentProperties(TSalesforceOutputDefinition.COMPONENT_NAME);
        outputProps.connection = props.connection;
        outputProps.module = props.module;
        outputProps.setValue(outputProps.outputAction, TSalesforceOutputProperties.OutputAction.INSERT);
        runtime.output(outputProps, outputRows);
        return readAndCheckRows(runtime, props, outputRows.size());
    }

    protected void deleteRows(SalesforceRuntime runtime, List<Map<String, Object>> inputRows) throws Exception {
        List<String> ids = getDeleteIds(inputRows);
        for (String id : ids) {
            runtime.delete(id);
        }
    }

    protected void checkAndDelete(SalesforceRuntime runtime, SalesforceConnectionModuleProperties props, int count)
            throws Exception {
        List<Map<String, Object>> inputRows = readAndCheckRows(runtime, props, count);
        deleteRows(runtime, inputRows);
        readAndCheckRows(runtime, props, 0);
    }

    @Test
    public void testBulkExec() throws Throwable {
        ComponentDefinition definition = getComponentService()
                .getComponentDefinition(TSalesforceBulkExecDefinition.COMPONENT_NAME);
        TSalesforceBulkExecProperties props;
        props = (TSalesforceBulkExecProperties) getComponentService()
                .getComponentProperties(TSalesforceBulkExecDefinition.COMPONENT_NAME);
        setupProps(props.connection, DO_NOT_ADD_QUOTES);

        if (false) {
            Form f = props.module.getForm(Form.REFERENCE);
            SalesforceModuleProperties moduleProps = (SalesforceModuleProperties) f.getComponentProperties();
            moduleProps = (SalesforceModuleProperties) checkAndBeforePresent(f, "moduleName", moduleProps);
            moduleProps.setValue(moduleProps.moduleName, "Account");
            checkAndAfter(f, "moduleName", moduleProps);
            props.setValue(props.outputAction, TSalesforceOutputProperties.OutputAction.INSERT);

            ComponentTestUtils.checkSerialize(props);

            SalesforceRuntime runtime = createRuntime(definition);

            int count = 10;
            List<Map<String, Object>> outputRows = makeRows(count);
            runtime.output(props, outputRows);
            checkAndDelete(runtime, props, count);
        }
    }

    @Test
    public void testOutputInsert() throws Throwable {
        runOutputInsert(!DYNAMIC);
    }

    @Test
    public void testOutputInsertDynamic() throws Throwable {
        runOutputInsert(DYNAMIC);
    }

    protected void runOutputInsert(boolean isDynamic) throws Throwable {
        ComponentDefinition definition = getComponentService().getComponentDefinition(TSalesforceOutputDefinition.COMPONENT_NAME);
        TSalesforceOutputProperties props;
        props = (TSalesforceOutputProperties) getComponentService()
                .getComponentProperties(TSalesforceOutputDefinition.COMPONENT_NAME);
        setupProps(props.connection, DO_NOT_ADD_QUOTES);

        setupModule(props.module, "Account");
        if (isDynamic) {
            fixSchemaForDynamic();
        }
        props.setValue(props.outputAction, TSalesforceOutputProperties.OutputAction.INSERT);

        ComponentTestUtils.checkSerialize(props);

        SalesforceRuntime runtime = createRuntime(definition);

        int count = 10;
        List<Map<String, Object>> outputRows = makeRows(count);
        runtime.output(props, outputRows);
        checkAndDelete(runtime, props, count);
    }

    @Test
    public void testOutputUpsert() throws Throwable {
        ComponentDefinition definition = getComponentService().getComponentDefinition(TSalesforceOutputDefinition.COMPONENT_NAME);
        TSalesforceOutputProperties props;
        props = (TSalesforceOutputProperties) getComponentService()
                .getComponentProperties(TSalesforceOutputDefinition.COMPONENT_NAME);
        setupProps(props.connection, DO_NOT_ADD_QUOTES);

        setupModule(props.module, "Account");
        props.setValue(props.outputAction, TSalesforceOutputProperties.OutputAction.UPSERT);
        checkAndAfter(props.getForm(Form.MAIN), "outputAction", props);

        SchemaElement se = props.getProperty("upsertKeyColumn");
        System.out.println("--upsertKeyColumn - possible values");
        System.out.println(se.getPossibleValues());
        assertTrue(se.getPossibleValues().size() > 10);

        ComponentTestUtils.checkSerialize(props);

        createRuntime(definition);

        Map<String, Object> row = new HashMap<>();
        row.put("Name", "TestName");
        row.put("BillingStreet", "123 Main Street");
        row.put("BillingState", "CA");
        List<Map<String, Object>> outputRows = new ArrayList<>();
        outputRows.add(row);
        // FIXME - finish this test
    }

    @Test
    public void testGetServerTimestamp() throws Throwable {
        ComponentDefinition definition = getComponentService()
                .getComponentDefinition(TSalesforceGetServerTimestampDefinition.COMPONENT_NAME);
        TSalesforceGetServerTimestampProperties props;
        props = (TSalesforceGetServerTimestampProperties) getComponentService()
                .getComponentProperties(TSalesforceGetServerTimestampDefinition.COMPONENT_NAME);
        setupProps(props.connection, DO_NOT_ADD_QUOTES);

        SalesforceRuntime runtime = createRuntime(definition);
        runtime.inputBegin(props);

        Map<String, Object> row;
        row = runtime.inputRow();
        // TODO we need to make sure about the server and local time zone are the same.
        Calendar now = Calendar.getInstance();
        Calendar date = (Calendar) row.get("ServerTimestamp");
        long nowMillis = now.getTimeInMillis();
        long dateMillis = date.getTimeInMillis();
        System.out.println("now: " + nowMillis);
        System.out.println(dateMillis);
        long delta = nowMillis - dateMillis;
        assertTrue(Math.abs(delta) < 50000);
        assertNull(runtime.inputRow());
    }

    @Test
    public void testAlli18n() {
        ComponentTestUtils.testAlli18n(getComponentService());
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