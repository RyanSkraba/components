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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.talend.components.ComponentTestUtils;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.Repository;
import org.talend.components.api.properties.presentation.Form;
import org.talend.components.api.runtime.ComponentDynamicHolder;
import org.talend.components.api.runtime.DefaultComponentRuntimeContainerImpl;
import org.talend.components.api.schema.Schema;
import org.talend.components.api.schema.SchemaElement;
import org.talend.components.api.schema.SchemaFactory;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.service.internal.ComponentServiceImpl;
import org.talend.components.common.oauth.OauthProperties;
import org.talend.components.salesforce.tsalesforceconnection.TSalesforceConnectionDefinition;
import org.talend.components.salesforce.tsalesforceinput.TSalesforceInputDefinition;
import org.talend.components.salesforce.tsalesforceinput.TSalesforceInputProperties;
import org.talend.components.salesforce.tsalesforceoutput.TSalesforceOutputDefinition;
import org.talend.components.salesforce.tsalesforceoutput.TSalesforceOutputProperties;

public class SalesforceLocalComponentTest {

    // Test schema
    Schema schema;

    ComponentDynamicHolder dynamic;

    // SalesforceRuntime runtime;

    // Used to make sure we have our own data
    String random;

    public static final String TEST_KEY = "Address2 456";

    ComponentService componentService = new ComponentServiceImpl(null);

    @BeforeClass
    public static void init() {
        ComponentTestUtils.setupGlobalContext();
    }

    @AfterClass
    public static void unset() {
        ComponentTestUtils.unsetGlobalContext();
    }

    public SalesforceLocalComponentTest() {
        random = Integer.toString(ThreadLocalRandom.current().nextInt(1, 100000));
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
        SchemaElement loginType = props.getProperty("loginType");
        System.out.println(loginType.getPossibleValues());
        assertEquals("Basic", loginType.getPossibleValues().get(0).toString());
        assertEquals("OAuth", loginType.getPossibleValues().get(1).toString());
        assertEquals(SalesforceConnectionProperties.LOGIN_BASIC, props.getValue(loginType));
        Form mainForm = props.getForm(Form.MAIN);
        assertEquals("Salesforce Connection Settings", mainForm.getTitle());
        assertTrue(mainForm.getWidget(SalesforceUserPasswordProperties.class).isVisible());
        assertFalse(mainForm.getWidget(OauthProperties.class).isVisible());

        props.setValue(loginType, SalesforceConnectionProperties.LOGIN_OAUTH);
        props = checkAndAfter(mainForm, "loginType", props);
        mainForm = props.getForm(Form.MAIN);
        assertTrue(mainForm.isRefreshUI());

        assertFalse(mainForm.getWidget(SalesforceUserPasswordProperties.class).isVisible());
        assertTrue(mainForm.getWidget(OauthProperties.class).isVisible());
    }

    private SalesforceConnectionProperties setupProps(SalesforceConnectionProperties props, String user, String password,
            String key) {
        if (props == null) {
            props = (SalesforceConnectionProperties) componentService
                    .getComponentProperties(TSalesforceConnectionDefinition.COMPONENT_NAME);
        }
        ComponentProperties userPassword = (ComponentProperties) props.getProperty(SalesforceUserPasswordProperties.class);
        userPassword.setValue(userPassword.getProperty("userId"), user);
        userPassword.setValue(userPassword.getProperty("password"), password);
        userPassword.setValue(userPassword.getProperty("securityKey"), key);
        return props;
    }

    private SalesforceConnectionProperties setupOAuthProps(SalesforceConnectionProperties props) throws Throwable {
        if (props == null) {
            props = (SalesforceConnectionProperties) componentService
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

    @Test
    public void testInputProps() throws Throwable {
        TSalesforceInputProperties props = (TSalesforceInputProperties) new TSalesforceInputDefinition().createProperties();
        setupProps(props.connection, null, null, null);

        assertEquals(2, props.queryMode.getPossibleValues().size());

        SchemaElement returns = props.getProperty(ComponentProperties.RETURNS);
        assertEquals("NB_LINE", returns.getChildren().get(0).getName());
    }

    protected void setupModule(SalesforceModuleProperties moduleProps, String module) throws Throwable {
        Form f = moduleProps.getForm(Form.REFERENCE);
        moduleProps = (SalesforceModuleProperties) checkAndBefore(f, "moduleName", moduleProps);
        moduleProps.setValue(moduleProps.moduleName, module);
        moduleProps = (SalesforceModuleProperties) checkAndAfter(f, "moduleName", moduleProps);
        schema = (Schema) moduleProps.schema.getValue(moduleProps.schema.schema);
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

    protected void checkRows(List<Map<String, Object>> rows, int count) {
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
        }
        assertEquals(count, checkCount);
        if (dynamic != null) {
            assertEquals(count, checkDynamicCount);
            System.out.println("Check dynamic rows: " + checkDynamicCount);
        }
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
        TSalesforceInputProperties inputProps = (TSalesforceInputProperties) componentService
                .getComponentProperties(TSalesforceInputDefinition.COMPONENT_NAME);
        inputProps.connection = props.connection;
        inputProps.module = props.module;
        inputProps.setValue(inputProps.batchSize, 200);
        runtime.input(inputProps, inputRows);
        checkRows(inputRows, count);
        return inputRows;
    }

    protected void writeRows(SalesforceRuntime runtime, SalesforceConnectionModuleProperties props,
            List<Map<String, Object>> outputRows) throws Exception {
        TSalesforceOutputProperties outputProps;
        outputProps = (TSalesforceOutputProperties) componentService
                .getComponentProperties(TSalesforceOutputDefinition.COMPONENT_NAME);
        outputProps.connection = props.connection;
        outputProps.module = props.module;
        outputProps.setValue(outputProps.outputAction, TSalesforceOutputProperties.OutputAction.INSERT);
        runtime.output(outputProps, outputRows);
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

}
