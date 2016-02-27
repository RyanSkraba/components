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

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.talend.components.api.adaptor.DefaultComponentRuntimeContainerImpl;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.test.ComponentTestUtils;
import org.talend.components.salesforce.SalesforceModuleProperties;
import org.talend.components.salesforce.SalesforceRuntime;
import org.talend.components.salesforce.SalesforceTestBase;
import org.talend.components.salesforce.tsalesforcebulkexec.TSalesforceBulkExecDefinition;
import org.talend.components.salesforce.tsalesforcebulkexec.TSalesforceBulkExecProperties;
import org.talend.components.salesforce.tsalesforcegetservertimestamp.TSalesforceGetServerTimestampDefinition;
import org.talend.components.salesforce.tsalesforcegetservertimestamp.TSalesforceGetServerTimestampProperties;
import org.talend.components.salesforce.tsalesforceinput.TSalesforceInputDefinition;
import org.talend.components.salesforce.tsalesforceinput.TSalesforceInputProperties;
import org.talend.components.salesforce.tsalesforceoutput.TSalesforceOutputDefinition;
import org.talend.components.salesforce.tsalesforceoutput.TSalesforceOutputProperties;
import org.talend.daikon.properties.Property;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.service.PropertiesServiceTest;
import org.talend.daikon.schema.SchemaElement;

public class SalesforceRuntimeTestIT extends SalesforceTestBase {

    public SalesforceRuntimeTestIT() {
        super();
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
        setupProps(props.connection, !ADD_QUOTES);

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


    @Test
    public void testBulkExec() throws Throwable {
        ComponentDefinition definition = getComponentService()
                .getComponentDefinition(TSalesforceBulkExecDefinition.COMPONENT_NAME);
        TSalesforceBulkExecProperties props;
        props = (TSalesforceBulkExecProperties) getComponentService()
                .getComponentProperties(TSalesforceBulkExecDefinition.COMPONENT_NAME);
        setupProps(props.connection,!ADD_QUOTES);

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
        setupProps(props.connection, !ADD_QUOTES);

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
        setupProps(props.connection, !ADD_QUOTES);

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
        setupProps(props.connection, !ADD_QUOTES);

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