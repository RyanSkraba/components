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
package org.talend.components.salesforce.runtime;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.talend.components.api.adaptor.ComponentDynamicHolder;
import org.talend.components.api.adaptor.DefaultComponentRuntimeContainerImpl;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.runtime.ComponentRuntimeContainer;
import org.talend.components.api.service.AbstractComponentTest;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.test.ComponentTestUtils;
import org.talend.components.api.test.SimpleComponentRegistry;
import org.talend.components.api.test.SimpleComponentService;
import org.talend.components.api.wizard.ComponentWizard;
import org.talend.components.api.wizard.ComponentWizardDefinition;
import org.talend.components.api.wizard.WizardImageType;
import org.talend.components.common.oauth.OauthProperties;
import org.talend.components.salesforce.SalesforceConnectionEditWizardDefinition;
import org.talend.components.salesforce.SalesforceConnectionModuleProperties;
import org.talend.components.salesforce.SalesforceConnectionProperties;
import org.talend.components.salesforce.SalesforceConnectionWizard;
import org.talend.components.salesforce.SalesforceConnectionWizardDefinition;
import org.talend.components.salesforce.SalesforceModuleListProperties;
import org.talend.components.salesforce.SalesforceModuleProperties;
import org.talend.components.salesforce.SalesforceModuleWizardDefinition;
import org.talend.components.salesforce.SalesforceRuntime;
import org.talend.components.salesforce.SalesforceTestHelper;
import org.talend.components.salesforce.SalesforceUserPasswordProperties;
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
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.PresentationItem;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.Property;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.service.PropertiesServiceTest;
import org.talend.daikon.properties.service.Repository;
import org.talend.daikon.properties.test.PropertiesTestUtils;
import org.talend.daikon.schema.Schema;
import org.talend.daikon.schema.SchemaElement;
import org.talend.daikon.schema.SchemaFactory;

import com.sforce.async.AsyncApiException;
import com.sforce.ws.ConnectionException;

public class SalesforceRuntimeTestIT extends AbstractComponentTest {

    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();

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
        }
        return componentService;
    }

    static final boolean ADD_QUOTES = true;

    static final boolean DO_NOT_ADD_QUOTES = false;

    // Test schema
    Schema schema;

    ComponentDynamicHolder dynamic;

    // SalesforceRuntime runtime;

    // Used to make sure we have our own data
    String random;

    public SalesforceRuntimeTestIT() {
        random = Integer.toString(ThreadLocalRandom.current().nextInt(1, 100000));
    }

    protected ComponentProperties checkAndAfter(Form form, String propName, ComponentProperties props) throws Throwable {
        assertTrue(form.getWidget(propName).isCallAfter());
        return getComponentService().afterProperty(propName, props);
    }


    protected SalesforceRuntime createRuntime(ComponentDefinition definition) {
        SalesforceRuntime runtime = (SalesforceRuntime) definition.createRuntime();
        runtime.setContainer(new TestRuntimeContainer());
        return runtime;
    }

    class TestRuntimeContainer extends DefaultComponentRuntimeContainerImpl {

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
        SalesforceTestHelper.setupProps(props.connection, DO_NOT_ADD_QUOTES);

        setupModule(props.module, "Account");
        if (isDynamic) {
            fixSchemaForDynamic();
        }

        ComponentTestUtils.checkSerialize(props, errorCollector);
        SalesforceRuntime runtime = createRuntime(definition);

        Map<String, Object> row = new HashMap<>();

        int count = 10;
        // store rows in SF to retreive them afterward to test the input.
        List<Map<String, Object>> outputRows = makeRows(count);
        outputRows = writeRows(runtime, props, outputRows);
        checkRows(outputRows, count);
        try {// retreive the row and make sure they are correct
            List<Map<String, Object>> rows = new ArrayList<>();
            runtime.input(props, rows);
            checkRows(rows, count);
        } finally {// make sure everything is clear.
            deleteRows(runtime, outputRows);
        }
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



    @Test
    public void testBulkExec() throws Throwable {
        ComponentDefinition definition = getComponentService()
                .getComponentDefinition(TSalesforceBulkExecDefinition.COMPONENT_NAME);
        TSalesforceBulkExecProperties props;
        props = (TSalesforceBulkExecProperties) getComponentService()
                .getComponentProperties(TSalesforceBulkExecDefinition.COMPONENT_NAME);
        SalesforceTestHelper.setupProps(props.connection, DO_NOT_ADD_QUOTES);

        if (false) {
            Form f = props.module.getForm(Form.REFERENCE);
            SalesforceModuleProperties moduleProps = (SalesforceModuleProperties) f.getProperties();
            moduleProps = (SalesforceModuleProperties) PropertiesServiceTest.checkAndBeforePresent(getComponentService(), f,
                    "moduleName", moduleProps);
            moduleProps.moduleName.setValue("Account");
            checkAndAfter(f, "moduleName", moduleProps);
            props.outputAction.setValue(TSalesforceOutputProperties.OutputAction.INSERT);

            ComponentTestUtils.checkSerialize(props, errorCollector);

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
        SalesforceTestHelper.setupProps(props.connection, DO_NOT_ADD_QUOTES);

        setupModule(props.module, "Account");
        if (isDynamic) {
            fixSchemaForDynamic();
        }
        props.outputAction.setValue(TSalesforceOutputProperties.OutputAction.INSERT);

        ComponentTestUtils.checkSerialize(props, errorCollector);

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
        SalesforceTestHelper.setupProps(props.connection, DO_NOT_ADD_QUOTES);

        setupModule(props.module, "Account");
        props.outputAction.setValue(TSalesforceOutputProperties.OutputAction.UPSERT);
        checkAndAfter(props.getForm(Form.MAIN), "outputAction", props);

        SchemaElement se = (Property) props.getProperty("upsertKeyColumn");
        System.out.println("--upsertKeyColumn - possible values");
        System.out.println(se.getPossibleValues());
        assertTrue(se.getPossibleValues().size() > 10);

        ComponentTestUtils.checkSerialize(props, errorCollector);

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
        SalesforceTestHelper.setupProps(props.connection, DO_NOT_ADD_QUOTES);

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

}