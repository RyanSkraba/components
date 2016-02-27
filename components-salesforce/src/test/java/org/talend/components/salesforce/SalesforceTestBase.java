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

import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ErrorCollector;
import org.talend.components.api.adaptor.Adaptor;
import org.talend.components.api.adaptor.ComponentDynamicHolder;
import org.talend.components.api.adaptor.DefaultComponentRuntimeContainerImpl;
import org.talend.components.api.component.runtime.BoundedReader;
import org.talend.components.api.component.runtime.Writer;
import org.talend.components.api.component.runtime.WriterResult;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.service.AbstractComponentTest;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.test.SimpleComponentRegistry;
import org.talend.components.api.test.SimpleComponentService;
import org.talend.components.salesforce.runtime.SalesforceSink;
import org.talend.components.salesforce.runtime.SalesforceSource;
import org.talend.components.salesforce.runtime.SalesforceWriteOperation;
import org.talend.components.salesforce.tsalesforcebulkexec.TSalesforceBulkExecDefinition;
import org.talend.components.salesforce.tsalesforceconnection.TSalesforceConnectionDefinition;
import org.talend.components.salesforce.tsalesforcegetdeleted.TSalesforceGetDeletedDefinition;
import org.talend.components.salesforce.tsalesforcegetservertimestamp.TSalesforceGetServerTimestampDefinition;
import org.talend.components.salesforce.tsalesforcegetupdated.TSalesforceGetUpdatedDefinition;
import org.talend.components.salesforce.tsalesforceinput.TSalesforceInputDefinition;
import org.talend.components.salesforce.tsalesforceinput.TSalesforceInputProperties;
import org.talend.components.salesforce.tsalesforceoutput.TSalesforceOutputDefinition;
import org.talend.components.salesforce.tsalesforceoutput.TSalesforceOutputProperties;
import org.talend.components.salesforce.tsalesforceoutputbulk.TSalesforceOutputBulkDefinition;
import org.talend.components.salesforce.tsalesforcewavebulkexec.TSalesforceWaveBulkExecDefinition;
import org.talend.components.salesforce.tsalesforcewaveoutputbulkexec.TSalesforceWaveOutputBulkExecDefinition;
import org.talend.daikon.properties.Property;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.service.PropertiesServiceTest;
import org.talend.daikon.schema.Schema;
import org.talend.daikon.schema.SchemaElement;
import org.talend.daikon.schema.SchemaFactory;

public class SalesforceTestBase extends AbstractComponentTest {

    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();

    private ComponentService componentService;

    // Used to make sure we have our own data - the billingPostalCode is set to this value
    protected String random;

    // Test schema
    protected Schema schema;

    protected ComponentDynamicHolder dynamic;

    protected Adaptor adaptor;

    public static final boolean ADD_QUOTES = true;

    public final String userId = System.getProperty("salesforce.user");

    public final String password = System.getProperty("salesforce.password");

    public final String securityKey = System.getProperty("salesforce.key");

    public SalesforceTestBase() {
        random = Integer.toString(ThreadLocalRandom.current().nextInt(1, 100000));
        adaptor = new DefaultComponentRuntimeContainerImpl();
    }

    @Before
    public void initializeComponentRegistryAndService() {
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

    protected ComponentProperties checkAndAfter(Form form, String propName, ComponentProperties props) throws Throwable {
        assertTrue(form.getWidget(propName).isCallAfter());
        return getComponentService().afterProperty(propName, props);
    }

    public SalesforceConnectionProperties setupProps(SalesforceConnectionProperties props, boolean addQuotes) {
        if (props == null) {
            props = (SalesforceConnectionProperties) new SalesforceConnectionProperties("foo").init();
        }
        ComponentProperties userPassword = (ComponentProperties) props.getProperty("userPassword");
        ((Property) userPassword.getProperty("userId")).setValue(addQuotes ? "\"" + userId + "\"" : userId);
        ((Property) userPassword.getProperty("password")).setValue(addQuotes ? "\"" + password + "\"" : password);
        ((Property) userPassword.getProperty("securityKey")).setValue(addQuotes ? "\"" + securityKey + "\"" : securityKey);
        return props;
    }

    public static final String EXISTING_MODULE_NAME = "Account";

    public static final String NOT_EXISTING_MODULE_NAME = "foobar";

    public static final String TEST_KEY = "Address2 456";

    protected void setupModule(SalesforceModuleProperties moduleProps, String module) throws Throwable {
        Form f = moduleProps.getForm(Form.REFERENCE);
        moduleProps = (SalesforceModuleProperties) PropertiesServiceTest.checkAndBeforeActivate(getComponentService(), f,
                "moduleName", moduleProps);
        moduleProps.moduleName.setValue(module);
        moduleProps = (SalesforceModuleProperties) checkAndAfter(f, "moduleName", moduleProps);
        schema = (Schema) moduleProps.schema.schema.getValue();
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
                    dynamic = adaptor.createDynamicHolder();
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

    public void fixSchemaForDynamic(SchemaElement schemaElement) {
        SchemaElement dynElement = SchemaFactory.newSchemaElement(SchemaElement.Type.DYNAMIC, "dynamic");
        schemaElement.addChild(dynElement);
        Iterator<SchemaElement> it = schemaElement.getChildren().iterator();
        while (it.hasNext()) {
            SchemaElement se = it.next();
            if (se.getName().equals("ShippingState")) {
                it.remove();
                break;
            }
        }
    }

    public void addDynamicColumn(Map<String, Object> row, boolean isDynamic) {
        if (isDynamic) {
            setupDynamic();
            dynamic.addFieldValue("ShippingState", "CA");
            row.put("dynamic", dynamic);
        }
    }

    public <T> T writeRows(Writer<T> writer, List<Map<String, Object>> outputRows) throws IOException {
        T result;
        writer.open("foo");
        try {
            for (Map<String, Object> row : outputRows) {
                writer.write(row);
            }
        } finally {
            result = writer.close();
        }
        return result;
    }

    public List<Map<String, Object>> makeRows(int count) {
        return makeRows(count, false);
    }

    public List<Map<String, Object>> makeRows(int count, boolean isDynamic) {
        List<Map<String, Object>> outputRows = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Map<String, Object> row = new HashMap<>();
            row.put("Name", "TestName");
            row.put("ShippingStreet", TEST_KEY);
            row.put("ShippingPostalCode", Integer.toString(i));
            row.put("BillingStreet", "123 Main Street");
            row.put("BillingState", "CA");
            row.put("BillingPostalCode", random);
            addDynamicColumn(row, isDynamic);
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
            if (check == null || !check.equals(SalesforceTestBase.TEST_KEY)) {
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

    public List<String> getDeleteIds(List<Map<String, Object>> rows) {
        List<String> ids = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            System.out.println("del: " + row.get("Name") + " id: " + row.get("Id") + " post: " + row.get("BillingPostalCode")
                    + " st: " + " post: " + row.get("BillingStreet"));
            String check = (String) row.get("ShippingStreet");
            if (check == null || !check.equals(SalesforceTestBase.TEST_KEY)) {
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
        inputProps.batchSize.setValue(200);
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
        outputProps.outputAction.setValue(TSalesforceOutputProperties.OutputAction.INSERT);
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

    public void deleteAllAccountTestRows() throws Exception {
        SalesforceRuntime runtime = new SalesforceRuntime(new DefaultComponentRuntimeContainerImpl());
        SalesforceComponentTestIT salesforceComponentTestIT = new SalesforceComponentTestIT();
        TSalesforceInputProperties props = (TSalesforceInputProperties) new TSalesforceInputProperties("foo").init();
        setupProps(props.connection, !ADD_QUOTES);
        props.batchSize.setValue(200);
        props.module.moduleName.setValue("Account");
        // connecting
        runtime.connect(props.connection);
        // getting schema
        props.module.schema.schema.setValue(runtime.getSchema("Account"));
        // getting all rows
        List<Map<String, Object>> rows = new ArrayList<>();
        runtime.input(props, rows);
        // filtering rows
        List<Map<String, Object>> rowToBeDeleted = getAllTestRows(rows);
        // deleting rows
        List<String> ids = salesforceComponentTestIT.getDeleteIds(rowToBeDeleted);
        for (String id : ids) {
            runtime.delete(id);
        }
    }

    /**
     * @return the list of row match the TEST_KEY, and if a random values it specified it also filter row against the
     */
    public List<Map<String, Object>> filterAllTestRows(List<Map<String, Object>> rows, String randomValue) {
        List<Map<String, Object>> checkedRows = new ArrayList<>();

        for (Map<String, Object> row : rows) {
            String check = (String) row.get("ShippingStreet");
            if (check == null || !check.equals(TEST_KEY)) {
                continue;
            }
            if (randomValue != null) {// check the random value if specified
                check = (String) row.get("BillingPostalCode");
                if (check == null || !check.equals(randomValue)) {
                    continue;
                }
            }
            System.out.println("Test row is: " + row.get("Name") + " id: " + row.get("Id") + " post: "
                    + row.get("BillingPostalCode") + " st: " + " post: " + row.get("BillingStreet"));
            checkedRows.add(row);
        }
        return checkedRows;
    }

    public List<Map<String, Object>> getAllTestRows(List<Map<String, Object>> rows) {
        List<Map<String, Object>> checkedRows = new ArrayList<>();

        for (Map<String, Object> row : rows) {
            String check = (String) row.get("ShippingStreet");
            if (check == null || !check.equals(TEST_KEY)) {
                continue;
            }
            System.out.println("Test row is: " + row.get("Name") + " id: " + row.get("Id") + " post: "
                    + row.get("BillingPostalCode") + " st: " + " post: " + row.get("BillingStreet"));
            checkedRows.add(row);
        }
        return checkedRows;
    }

    protected void checkRows(List<Map<String, Object>> outputRows, SalesforceConnectionModuleProperties props) throws Exception {
        TSalesforceInputProperties inputProps = (TSalesforceInputProperties) new TSalesforceInputProperties("bar").init();
        inputProps.connection = props.connection;
        inputProps.module = props.module;
        inputProps.batchSize.setValue(200);
        List<Map<String, Object>> inputRows = readRows(inputProps);
        assertThat(inputRows, containsInAnyOrder(outputRows.toArray()));

    }

    protected List<Map<String, Object>> readRows(TSalesforceInputProperties inputProps) throws IOException {
        BoundedReader reader = createBoundedReader(inputProps);
        boolean hasRecord = reader.start();
        List<Map<String, Object>> rows = new ArrayList<>();
        while (hasRecord) {
            rows.add((Map<String, Object>) reader.getCurrent());
            hasRecord = reader.advance();
        }
        return rows;
    }

    protected void deleteRows(List<Map<String, Object>> rows, TSalesforceOutputProperties props) throws Exception {
        // copy the props cause we are changing it
        TSalesforceOutputProperties deleteProperties = new TSalesforceOutputProperties("delete"); //$NON-NLS-1$
        deleteProperties.copyValuesFrom(props);
        deleteProperties.outputAction.setValue(TSalesforceOutputProperties.ACTION_DELETE);

        SalesforceSink salesforceSink = new SalesforceSink();
        salesforceSink.initialize(new DefaultComponentRuntimeContainerImpl(), deleteProperties);
        SalesforceWriteOperation writeOperation = (SalesforceWriteOperation) salesforceSink.createWriteOperation();
        Writer<WriterResult> saleforceWriter = writeOperation.createWriter(new DefaultComponentRuntimeContainerImpl());
        writeRows(saleforceWriter, rows);
    }

    public BoundedReader createSalesforceInputReaderFromAccount(String moduleName) {
        TSalesforceInputProperties tsip = (TSalesforceInputProperties) new TSalesforceInputProperties("foo").init(); //$NON-NLS-1$
        SalesforceConnectionProperties conProps = setupProps(tsip.connection, !ADD_QUOTES);
        tsip.module.moduleName.setValue(moduleName);
        return createBoundedReader(tsip);
    }

    public BoundedReader createBoundedReader(ComponentProperties tsip) {
        SalesforceSource salesforceSource = new SalesforceSource();
        salesforceSource.initialize(null, tsip);
        return salesforceSource.createReader(null);
    }

}
