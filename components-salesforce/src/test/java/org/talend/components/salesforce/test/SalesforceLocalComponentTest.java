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
package org.talend.components.salesforce.test;

import java.util.*;

import junit.framework.TestCase;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.talend.components.api.NamedThing;
import org.talend.components.api.internal.SpringApp;
import org.talend.components.api.properties.*;
import org.talend.components.api.properties.presentation.Form;
import org.talend.components.api.schema.Schema;
import org.talend.components.api.schema.SchemaElement;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.test.LocalComponentTest;
import org.talend.components.api.wizard.ComponentWizard;
import org.talend.components.api.wizard.ComponentWizardDefinition;
import org.talend.components.api.wizard.WizardImageType;
import org.talend.components.salesforce.*;
import org.talend.components.salesforce.tsalesforceconnection.TSalesforceConnectionDefinition;
import org.talend.components.salesforce.tsalesforceinput.TSalesforceInputDefinition;
import org.talend.components.salesforce.tsalesforceinput.TSalesforceInputProperties;
import org.talend.components.salesforce.tsalesforceoutput.TSalesforceOutputDefinition;
import org.talend.components.salesforce.tsalesforceoutput.TSalesforceOutputProperties;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = SpringApp.class)
public class SalesforceLocalComponentTest extends TestCase {

    boolean inChina = !true;

    String userId;

    String password;

    @Autowired
    protected ComponentService componentService;

    public SalesforceLocalComponentTest() {
        if (inChina) {
            userId = "bchen2@talend.com";
            password = "talend123sfYYBBe4aZN0TcDVDV7Ylzb6Ku";
        } else {
            userId = "fupton@talend.com";
            password = "talendsal99QSCzLBQgrkEq9w9EXiOt1BSy";
        }

    }

    protected ComponentProperties checkAndBefore(Form form, String propName, ComponentProperties props) throws Throwable {
        assertTrue(form.getWidget(propName).isCallBefore());
        return componentService.beforeProperty(propName, props);
    }

    protected ComponentProperties checkAndAfter(Form form, String propName, ComponentProperties props) throws Throwable {
        assertTrue(form.getWidget(propName).isCallAfter());
        return componentService.afterProperty(propName, props);
    }

    protected ComponentProperties checkAndValidate(Form form, String propName, ComponentProperties props) throws Throwable {
        assertTrue(form.getWidget(propName).isCallValidate());
        return componentService.validateProperty(propName, props);
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

    @Test
    public void testWizard() throws Throwable {
        final List<RepoProps> repoProps = new ArrayList();

        Repository repo = new Repository() {

            private int locationNum;

            @Override
            public String storeComponentProperties(ComponentProperties properties, String name, String repositoryLocation,
                    Schema schema) {
                RepoProps rp = new RepoProps(properties, name, repositoryLocation, schema);
                repoProps.add(rp);
                System.out.println(rp);
                return repositoryLocation + ++locationNum;
            }
        };
        componentService.setRepository(repo);

        Set<ComponentWizardDefinition> props = componentService.getTopLevelComponentWizards();
        int count = 0;
        ComponentWizardDefinition wizardDef = null;
        for (ComponentWizardDefinition wizardDefinition : props) {
            if (wizardDefinition instanceof SalesforceConnectionWizardDefinition) {
                wizardDef = wizardDefinition;
                count++;
            }
        }
        assertEquals(1, count);
        ComponentWizard wiz = componentService.getComponentWizard(SalesforceConnectionWizardDefinition.COMPONENT_WIZARD_NAME,
                "nodeSalesforce");
        assertNotNull(wiz);
        assertEquals("nodeSalesforce", wiz.getRepositoryLocation());
        assertTrue(wiz instanceof SalesforceConnectionWizard);
        List<Form> forms = wiz.getForms();
        assertEquals("Main", forms.get(0).getName());
        assertEquals("Main", forms.get(1).getName());

        Object image = componentService.getWizardPngImage(SalesforceConnectionWizardDefinition.COMPONENT_WIZARD_NAME,
                WizardImageType.TREE_ICON_16X16);
        assertNotNull(image);
        image = componentService.getWizardPngImage(SalesforceConnectionWizardDefinition.COMPONENT_WIZARD_NAME,
                WizardImageType.WIZARD_BANNER_75X66);
        assertNotNull(image);

        // check password i18n
        Form connForm = forms.get(0);
        SalesforceConnectionProperties connProps = (SalesforceConnectionProperties) connForm.getProperties();
        assertEquals("Name", connProps.getProperty("name").getDisplayName());
        connProps.setValue(connProps.name, "connName");
        setupProps(connProps);
        Form userPassword = (Form) connForm.getChild("userPassword");
        SchemaElement passwordSe = (SchemaElement) userPassword.getChild("password");
        assertEquals("Password", passwordSe.getDisplayName());
        // check name i18n
        NamedThing nameProp = connForm.getChild("name"); //$NON-NLS-1$
        assertEquals("Name", nameProp.getDisplayName());

        Form modForm = forms.get(1);
        SalesforceModuleListProperties mlProps = (SalesforceModuleListProperties) modForm.getProperties();
        assertFalse(modForm.isCallAfterFormBack());
        assertFalse(modForm.isCallAfterFormNext());
        assertTrue(modForm.isCallAfterFormFinish());
        assertTrue(modForm.isCallBeforeFormPresent());
        mlProps = (SalesforceModuleListProperties) componentService.beforeFormPresent(modForm.getName(), mlProps);
        System.out.println(mlProps.getValue(mlProps.moduleName));
        List<NameAndLabel> all = (List<NameAndLabel>) mlProps.getValue(mlProps.moduleName);
        List<NameAndLabel> selected = new ArrayList();
        selected.add(all.get(0));
        selected.add(all.get(2));
        selected.add(all.get(3));

        mlProps.setValue(mlProps.moduleName, selected);
        componentService.afterFormFinish(modForm.getName(), mlProps);
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
    public void testGetProps() {
        ComponentProperties props = componentService.getComponentProperties(TSalesforceConnectionDefinition.COMPONENT_NAME);
        Form f = props.getForm(Form.MAIN);
        LocalComponentTest.checkSerialize(props);
        System.out.println(f);
        System.out.println(props);
        assertEquals(Form.MAIN, f.getName());
        Form af = props.getForm(Form.ADVANCED);
        assertTrue(((PresentationItem)f.getChild("advanced")).getFormtoShow() == af);
    }

    @Test
    public void testAfterLoginType() throws Throwable {
        ComponentProperties props;

        props = componentService.getComponentProperties(TSalesforceConnectionDefinition.COMPONENT_NAME);
        LocalComponentTest.checkSerialize(props);
        SchemaElement loginType = props.getProperty("loginType");
        System.out.println(loginType.getPossibleValues());
        assertEquals("Basic", loginType.getPossibleValues().get(0).toString());
        assertEquals("OAuth", loginType.getPossibleValues().get(1).toString());
        assertEquals(SalesforceConnectionProperties.LOGIN_BASIC, props.getValue(loginType));
        Form mainForm = props.getForm(Form.MAIN);
        assertEquals("Salesforce Connection Settings", mainForm.getTitle());
        assertEquals("Complete these fields in order to connect to your Salesforce account.", mainForm.getSubtitle());
        assertTrue(mainForm.getWidget("userPassword").isVisible());
        assertFalse(mainForm.getWidget("oauth").isVisible());

        props.setValue(loginType, SalesforceConnectionProperties.LOGIN_OAUTH);
        props = checkAndAfter(mainForm, "loginType", props);
        mainForm = props.getForm(Form.MAIN);
        assertTrue(mainForm.isRefreshUI());

        assertFalse(mainForm.getWidget("userPassword").isVisible());
        assertTrue(mainForm.getWidget("oauth").isVisible());
    }

    private SalesforceConnectionProperties setupProps(SalesforceConnectionProperties props) {
        if (props == null) {
            props = (SalesforceConnectionProperties) componentService
                    .getComponentProperties(TSalesforceConnectionDefinition.COMPONENT_NAME);
        }
        System.out.println("URI:" + props.getStringValue(props.url));
        ComponentProperties userPassword = (ComponentProperties) props.getProperty("userPassword");
        userPassword.setValue(userPassword.getProperty("userId"), userId);
        userPassword.setValue(userPassword.getProperty("password"), password);
        return props;
    }

    @Test
    public void testLogin() throws Throwable {
        SalesforceConnectionProperties props = setupProps(null);
        Form f = props.getForm(Form.MAIN);
        props = (SalesforceConnectionProperties) checkAndValidate(f, "testConnection", props);
        System.out.println(props.getValidationResult());
        assertEquals(ValidationResult.Result.OK, props.getValidationResult().getStatus());
    }

    @Test
    public void testLoginFail() throws Throwable {
        SalesforceConnectionProperties props = setupProps(null);
        props.userPassword.setValue(props.userPassword.userId, "blah");
        Form f = props.getForm(Form.MAIN);
        props = (SalesforceConnectionProperties) checkAndValidate(f, "testConnection", props);
        System.out.println(props.getValidationResult());
        assertEquals(ValidationResult.Result.ERROR, props.getValidationResult().getStatus());
    }

    @Test
    public void testBulkLogin() throws Throwable {
        SalesforceConnectionProperties props = setupProps(null);
        props.setValue(props.bulkConnection, true);
        Form f = props.getForm(Form.MAIN);
        props = (SalesforceConnectionProperties) checkAndValidate(f, "testConnection", props);
        System.out.println(props.getValidationResult());
    }

    private SalesforceConnectionProperties setupOAuthProps(SalesforceConnectionProperties props) throws Throwable {
        if (props == null) {
            props = (SalesforceConnectionProperties) componentService
                    .getComponentProperties(TSalesforceConnectionDefinition.COMPONENT_NAME);
        }
        props.setValue(props.loginType, SalesforceConnectionProperties.LOGIN_OAUTH);
        Form mainForm = props.getForm(Form.MAIN);
        props = (SalesforceConnectionProperties) checkAndAfter(mainForm, "loginType", props);
        System.out.println("URI:" + props.getStringValue(props.url));
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
        System.out.println(props.getValidationResult());
    }

    @Ignore("oauth need manual operation")
    @Test
    public void testOAuthBulkLogin() throws Throwable {
        SalesforceConnectionProperties props = setupOAuthProps(null);
        props.setValue(props.bulkConnection, true);
        Form f = props.getForm(Form.MAIN);
        props = (SalesforceConnectionProperties) checkAndValidate(f, "testConnection", props);
        System.out.println(props.getValidationResult());
    }

    @Test
    public void testModuleNames() throws Throwable {
        TSalesforceInputProperties props = (TSalesforceInputProperties) componentService
                .getComponentProperties(TSalesforceInputDefinition.COMPONENT_NAME);
        setupProps(props.connection);
        LocalComponentTest.checkSerialize(props);

        assertEquals(2, props.getForms().size());
        Form f = props.module.getForm(Form.REFERENCE);
        assertTrue(f.getWidget("moduleName").isCallBefore());
        // The Form is bound to a Properties object that created it. The Forms might not always be associated with the
        // properties object
        // they came from.
        ComponentProperties moduleProps = f.getProperties();
        moduleProps = checkAndBefore(f, "moduleName", moduleProps);
        SchemaElement prop = (SchemaElement) f.getChild("moduleName");
        assertTrue(prop.getPossibleValues().size() > 100);
        System.out.println(prop.getPossibleValues());
        System.out.println(moduleProps.getValidationResult());
    }

    @Test
    public void testSchema() throws Throwable {
        TSalesforceInputProperties props = (TSalesforceInputProperties) componentService
                .getComponentProperties(TSalesforceInputDefinition.COMPONENT_NAME);
        setupProps(props.connection);

        Form f = props.module.getForm(Form.REFERENCE);
        SalesforceModuleProperties moduleProps = (SalesforceModuleProperties) f.getProperties();
        moduleProps = (SalesforceModuleProperties) checkAndBefore(f, "moduleName", moduleProps);
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
    public void testInput() throws Throwable {
        TSalesforceInputProperties props = (TSalesforceInputProperties) componentService
                .getComponentProperties(TSalesforceInputDefinition.COMPONENT_NAME);
        setupProps(props.connection);

        SchemaElement returns = props.getProperty(ComponentProperties.RETURNS);
        assertEquals("NB_LINE", returns.getChildren().get(0).getName());

        Form f = props.module.getForm(Form.REFERENCE);
        SalesforceModuleProperties moduleProps = (SalesforceModuleProperties) f.getProperties();
        moduleProps = (SalesforceModuleProperties) checkAndBefore(f, "moduleName", moduleProps);
        moduleProps.setValue(moduleProps.moduleName, "Account");
        moduleProps = (SalesforceModuleProperties) checkAndAfter(f, "moduleName", moduleProps);
        Schema schema = (Schema) moduleProps.schema.getValue(moduleProps.schema.schema);

        LocalComponentTest.checkSerialize(props);
        SalesforceRuntime runtime = new SalesforceRuntime();
        runtime.connect(props.connection);

        Map<String, Object> row = new HashMap();
        List<Map<String, Object>> rows = new ArrayList();

        if (false) {
            runtime.input(props, null, rows);
        }

        System.out.println(rows);
    }

    @Test
    public void testOutputInsert() throws Throwable {
        TSalesforceOutputProperties props;
        props = (TSalesforceOutputProperties) componentService.getComponentProperties(TSalesforceOutputDefinition.COMPONENT_NAME);
        setupProps(props.connection);

        Form f = props.module.getForm(Form.REFERENCE);
        SalesforceModuleProperties moduleProps = (SalesforceModuleProperties) f.getProperties();
        moduleProps = (SalesforceModuleProperties) checkAndBefore(f, "moduleName", moduleProps);
        moduleProps.setValue(moduleProps.moduleName, "Account");
        checkAndAfter(f, "moduleName", moduleProps);
        props.setValue(props.outputAction, TSalesforceOutputProperties.OutputAction.INSERT);

        LocalComponentTest.checkSerialize(props);

        SalesforceRuntime runtime = new SalesforceRuntime();
        runtime.connect(props.connection);

        Map<String, Object> row = new HashMap();
        row.put("Name", "TestName");
        row.put("BillingStreet", "123 Main Street");
        row.put("BillingState", "CA");
        List<Map<String, Object>> rows = new ArrayList();
        rows.add(row);

        // Don't run for now, even though it works, until we can clean this stuff up
        if (!false) {
            runtime.output(props, null, rows);
        }
    }

    @Test
    public void testOutputUpsert() throws Throwable {
        TSalesforceOutputProperties props;
        props = (TSalesforceOutputProperties) componentService.getComponentProperties(TSalesforceOutputDefinition.COMPONENT_NAME);
        setupProps(props.connection);

        Form f = props.module.getForm(Form.REFERENCE);
        SalesforceModuleProperties moduleProps = (SalesforceModuleProperties) f.getProperties();
        moduleProps = (SalesforceModuleProperties) checkAndBefore(f, "moduleName", moduleProps);
        moduleProps.setValue(moduleProps.moduleName, "Account");
        checkAndAfter(f, "moduleName", moduleProps);
        props.setValue(props.outputAction, TSalesforceOutputProperties.OutputAction.UPSERT);
        checkAndAfter(props.getForm(Form.MAIN), "outputAction", props);

        SchemaElement se = props.getProperty("upsertKeyColumn");
        System.out.println("--upsertKeyColumn - possible values");
        System.out.println(se.getPossibleValues());
        assertTrue(se.getPossibleValues().size() > 10);

        LocalComponentTest.checkSerialize(props);

        SalesforceRuntime runtime = new SalesforceRuntime();
        runtime.connect(props.connection);

        Map<String, Object> row = new HashMap();
        row.put("Name", "TestName");
        row.put("BillingStreet", "123 Main Street");
        row.put("BillingState", "CA");
        List<Map<String, Object>> rows = new ArrayList();
        rows.add(row);

        // Don't run for now, even though it works, until we can clean this stuff up
        if (false) {
            runtime.output(props, null, rows);
        }
    }

    @Test
    public void testAlli18n() {
        ComponentProperties compProps = componentService.getComponentProperties(TSalesforceConnectionDefinition.COMPONENT_NAME);
        checkAllI18NProperties(compProps);
        compProps = componentService.getComponentProperties(TSalesforceInputDefinition.COMPONENT_NAME);
        checkAllI18NProperties(compProps);
        compProps = componentService.getComponentProperties(TSalesforceOutputDefinition.COMPONENT_NAME);
        checkAllI18NProperties(compProps);
        compProps = new SalesforceModuleListProperties(null, null, null);
        checkAllI18NProperties(compProps);
        compProps = new SalesforceModuleProperties(null, null);
        checkAllI18NProperties(compProps);
    }

    /**
     * DOC sgandon Comment method "checkAllI18N".
     * 
     * @param outputDef
     */
    private void checkAllI18NProperties(ComponentProperties outputDef) {
        List<SchemaElement> properties = outputDef.getProperties();
        for (SchemaElement prop : properties) {
            if (!(prop instanceof ComponentProperties)) {
                assertFalse("property [" + outputDef.getClass().getCanonicalName() + "/" + prop.getName()
                        + "] should have a translated message key [property." + prop.getName()
                        + ".displayName] in [ZE proper messages.property]", prop.getDisplayName().endsWith(".displayName"));
            } // else the nested ComponentProperties should be tested separatly
        }
    }

}
