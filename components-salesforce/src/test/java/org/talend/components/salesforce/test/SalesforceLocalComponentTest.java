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
import org.talend.components.api.internal.SpringApp;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.presentation.Form;
import org.talend.components.api.schema.Schema;
import org.talend.components.api.schema.SchemaElement;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.test.LocalComponentTest;
import org.talend.components.api.wizard.ComponentWizard;
import org.talend.components.api.wizard.ComponentWizardDefinition;
import org.talend.components.common.UserPasswordProperties;
import org.talend.components.common.oauth.OauthProperties;
import org.talend.components.salesforce.*;
import org.talend.components.salesforce.SalesforceConnectionProperties.LoginType;
import org.talend.components.salesforce.tsalesforceconnect.TSalesforceConnectDefinition;
import org.talend.components.salesforce.tsalesforceconnect.TSalesforceConnectProperties;
import org.talend.components.salesforce.tsalesforceinput.TSalesforceInputDefinition;
import org.talend.components.salesforce.tsalesforceinput.TSalesforceInputProperties;
import org.talend.components.salesforce.tsalesforceoutput.TSalesforceOutputDefinition;
import org.talend.components.salesforce.tsalesforceoutput.TSalesforceOutputProperties;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = SpringApp.class)
public class SalesforceLocalComponentTest extends TestCase {

    @Autowired
    protected ComponentService componentService;

    public SalesforceLocalComponentTest() {
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

    @Test
    public void testWizard() {
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
                "userData");
        assertNotNull(wiz);
        assertEquals("userData", wiz.getUserData());
        assertTrue(wiz instanceof SalesforceConnectionWizard);
    }

    @Test
    public void testGetProps() {
        ComponentProperties props = componentService.getComponentProperties(TSalesforceConnectDefinition.COMPONENT_NAME);
        Form f = props.getForm(TSalesforceConnectProperties.MAIN);
        LocalComponentTest.checkSerialize(props);
        System.out.println(f);
        System.out.println(props);
    }

    @Test
    public void testAfterLoginType() throws Throwable {
        SalesforceConnectionProperties props;

        props = (SalesforceConnectionProperties) componentService
                .getComponentProperties(TSalesforceConnectDefinition.COMPONENT_NAME);
        LocalComponentTest.checkSerialize(props);
        assertEquals(SalesforceConnectionProperties.LoginType.BASIC, props.getValue(props.loginType));
        Form mainForm = props.getForm(TSalesforceConnectProperties.MAIN);
        String userPassFormName = props.userPassword.setupFormName(UserPasswordProperties.USERPASSWORD);
        assertTrue(mainForm.getWidget(userPassFormName).isVisible());
        String oauthFormName = props.oauth.setupFormName(OauthProperties.OAUTH);
        assertFalse(mainForm.getWidget(oauthFormName).isVisible());

        props.setValue(props.loginType, SalesforceConnectionProperties.LoginType.OAUTH);
        props = (SalesforceConnectionProperties) checkAndAfter(mainForm, "loginType", props);
        mainForm = props.getForm(TSalesforceConnectProperties.MAIN);
        assertTrue(mainForm.isRefreshUI());

        assertFalse(mainForm.getWidget(userPassFormName).isVisible());
        assertTrue(mainForm.getWidget(oauthFormName).isVisible());
    }

    private SalesforceConnectionProperties setupProps(SalesforceConnectionProperties props) {
        if (props == null) {
            props = (SalesforceConnectionProperties) componentService
                    .getComponentProperties(TSalesforceConnectDefinition.COMPONENT_NAME);
        }
        System.out.println("URI:" + props.getStringValue(props.url));

        boolean inChina = true;
        if (!inChina) {
            props.userPassword.setValue(props.userPassword.userId, "bchen2@talend.com");
            props.userPassword.setValue(props.userPassword.password, "talend123sfYYBBe4aZN0TcDVDV7Ylzb6Ku");
        } else {
            props.userPassword.setValue(props.userPassword.userId, "fupton@talend.com");
            props.userPassword.setValue(props.userPassword.password, "talendsal99QSCzLBQgrkEq9w9EXiOt1BSy");
        }
        return props;
    }

    @Test
    public void testLogin() throws Throwable {
        SalesforceConnectionProperties props = setupProps(null);
        Form f = props.getForm(TSalesforceConnectProperties.MAIN);
        props = (SalesforceConnectionProperties) checkAndValidate(f, "testConnection", props);
        System.out.println(props.getValidationResult());
    }

    @Test
    public void testBulkLogin() throws Throwable {
        SalesforceConnectionProperties props = setupProps(null);
        props.setValue(props.bulkConnection, true);
        Form f = props.getForm(TSalesforceConnectProperties.MAIN);
        props = (SalesforceConnectionProperties) checkAndValidate(f, "testConnection", props);
        System.out.println(props.getValidationResult());
    }

    private SalesforceConnectionProperties setupOAuthProps(SalesforceConnectionProperties props) throws Throwable {
        if (props == null) {
            props = (SalesforceConnectionProperties) componentService
                    .getComponentProperties(TSalesforceConnectDefinition.COMPONENT_NAME);
        }
        props.setValue(props.loginType, LoginType.OAUTH);
        Form mainForm = props.getForm(TSalesforceConnectProperties.MAIN);
        props = (SalesforceConnectionProperties) checkAndAfter(mainForm, "loginType", props);
        System.out.println("URI:" + props.getStringValue(props.url));
        props.oauth.setValue(props.oauth.clientId,
                "3MVG9Y6d_Btp4xp6ParHznfCCUh0d9fU3LYcvd_hCXz3G3Owp4KvaDhNuEOrXJTBd09JMoPdZeDtNYxXZM4X2");
        props.oauth.setValue(props.oauth.clientSecret, "3545101463828280342");
        props.oauth.setValue(props.oauth.callbackHost, "localhost");
        props.oauth.setValue(props.oauth.callbackPort, 8115);
        // props.oauth.tokenFile.setValue()
        return props;
    }

    @Ignore("oauth need manual operation")
    @Test
    public void testOAuthLogin() throws Throwable {
        SalesforceConnectionProperties props = setupOAuthProps(null);
        Form f = props.getForm(TSalesforceConnectProperties.MAIN);
        props = (SalesforceConnectionProperties) checkAndValidate(f, "testConnection", props);
        System.out.println(props.getValidationResult());
    }

    @Ignore("oauth need manual operation")
    @Test
    public void testOAuthBulkLogin() throws Throwable {
        SalesforceConnectionProperties props = setupOAuthProps(null);
        props.setValue(props.bulkConnection, true);
        Form f = props.getForm(TSalesforceConnectProperties.MAIN);
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
        Form f = props.module.getForm(SalesforceModuleProperties.REFERENCE);
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

        Form f = props.module.getForm(SalesforceModuleProperties.REFERENCE);
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

        Form f = props.module.getForm(SalesforceModuleProperties.REFERENCE);
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
    public void testOutput() throws Throwable {
        TSalesforceOutputProperties props = (TSalesforceOutputProperties) componentService
                .getComponentProperties(TSalesforceOutputDefinition.COMPONENT_NAME);
        setupProps(props.connection);

        Form f = props.module.getForm(SalesforceModuleProperties.REFERENCE);
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

}
