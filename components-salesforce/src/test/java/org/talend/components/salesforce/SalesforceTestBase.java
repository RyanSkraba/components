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

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ErrorCollector;
import org.talend.components.api.component.runtime.BoundedReader;
import org.talend.components.api.component.runtime.Writer;
import org.talend.components.api.component.runtime.WriterResult;
import org.talend.components.api.container.DefaultComponentRuntimeContainerImpl;
import org.talend.components.api.container.RuntimeContainer;
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
import org.talend.daikon.avro.IndexedRecordAdapterFactory;
import org.talend.daikon.avro.util.AvroUtils;
import org.talend.daikon.properties.Property;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.service.PropertiesServiceTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.*;

public class SalesforceTestBase extends AbstractComponentTest {

    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();

    private ComponentService componentService;

    // Used to make sure we have our own data - the billingPostalCode is set to this value
    protected String random;

    // Test schema
    protected Schema schema;

    protected RuntimeContainer adaptor;

    public static final boolean ADD_QUOTES = true;

    public final String userId = System.getProperty("salesforce.user");

    public final String password = System.getProperty("salesforce.password");

    public final String securityKey = System.getProperty("salesforce.key");

    protected static final boolean DYNAMIC = true;

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

    protected void fixSchemaForDynamic() {
        List<Schema.Field> fields = schema.getFields();

        schema = SchemaBuilder.record("record").fields().endRecord();

        List<Schema.Field> newFields = new ArrayList();
        for (Schema.Field field : fields) {
            if (field.name().equals("ShippingState"))
                continue;
            newFields.add(new Schema.Field(field.name(), schema, field.doc(), null));
        }
        Schema.Field dynField = new Schema.Field("dynamic", schema, "dynamic", null);
        AvroUtils.setFieldDynamic(dynField);
        newFields.add(dynField);
    }

    public void addDynamicColumn(Map<String, Object> row, boolean isDynamic) {
        if (isDynamic) {
            row.put("ShippingState", "CA");
        }
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
            assertEquals("CA", row.get("ShippingState"));
            assertEquals("TestName", row.get("Name"));
            assertEquals("123 Main Street", row.get("BillingStreet"));
            assertEquals("CA", row.get("BillingState"));
            checkedRows.add(row);
        }
        assertEquals(count, checkCount);
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

    protected List<Map<String, Object>> readRows(TSalesforceInputProperties inputProps) throws IOException {
        IndexedRecordAdapterFactory<Object, ? extends IndexedRecord> factory = null;
        BoundedReader reader = createBoundedReader(inputProps);
        boolean hasRecord = reader.start();
        List<Map<String, Object>> rows = new ArrayList<>();
        while (hasRecord) {
            Map<String, Object> r = new HashMap<>();
            if (factory == null)
                factory = (IndexedRecordAdapterFactory<Object, ? extends org.apache.avro.generic.IndexedRecord>) new org.talend.daikon.avro.AvroRegistry()
                        .createAdapterFactory(reader.getCurrent().getClass());
            org.apache.avro.generic.IndexedRecord unenforced = factory.convertToAvro(reader.getCurrent());
            for (Schema.Field field : unenforced.getSchema().getFields()) {
                r.put(field.name(), unenforced.get(field.pos()));
            }
            rows.add(r);
            hasRecord = reader.advance();
        }
        return rows;
    }

    protected List<Map<String, Object>> readRows(SalesforceConnectionModuleProperties props) throws IOException {
        TSalesforceInputProperties inputProps = (TSalesforceInputProperties) new TSalesforceInputProperties("bar").init();
        inputProps.connection = props.connection;
        inputProps.module = props.module;
        inputProps.batchSize.setValue(200);
        List<Map<String, Object>> inputRows = readRows(inputProps);
        return inputRows;
    }

    protected List<Map<String, Object>> readAndCheckRows(SalesforceConnectionModuleProperties props, int count) throws Exception {
        List<Map<String, Object>> inputRows = readRows(props);
        return checkRows(inputRows, count);
    }

    protected void checkRows(List<Map<String, Object>> outputRows, SalesforceConnectionModuleProperties props) throws Exception {
        List<Map<String, Object>> inputRows = readRows(props);
        assertThat(inputRows, containsInAnyOrder(outputRows.toArray()));
    }

    protected void checkAndDelete(SalesforceConnectionModuleProperties props, int count) throws Exception {
        List<Map<String, Object>> inputRows = readAndCheckRows(props, count);
        deleteRows(inputRows, props);
        readAndCheckRows(props, 0);
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

    // Returns the rows written (having been re-read so they have their Ids)
    protected void doWriteRows(SalesforceConnectionModuleProperties props, List<Map<String, Object>> outputRows)
            throws Exception {
        SalesforceSink salesforceSink = new SalesforceSink();
        salesforceSink.initialize(adaptor, props);
        SalesforceWriteOperation writeOperation = (SalesforceWriteOperation) salesforceSink.createWriteOperation();
        Writer<WriterResult> saleforceWriter = writeOperation.createWriter(adaptor);
        writeRows(saleforceWriter, outputRows);
    }

    // Returns the rows written (having been re-read so they have their Ids)
    protected List<Map<String, Object>> writeRows(SalesforceConnectionModuleProperties props,
                                                  List<Map<String, Object>> outputRows) throws Exception {
        TSalesforceOutputProperties outputProps = new TSalesforceOutputProperties("output"); //$NON-NLS-1$
        outputProps.copyValuesFrom(props);
        outputProps.outputAction.setValue(TSalesforceOutputProperties.OutputAction.INSERT);
        doWriteRows(outputProps, outputRows);
        return readAndCheckRows(props, outputRows.size());
    }

    protected void deleteRows(List<Map<String, Object>> rows, SalesforceConnectionModuleProperties props) throws Exception {
        TSalesforceOutputProperties deleteProperties = new TSalesforceOutputProperties("delete"); //$NON-NLS-1$
        deleteProperties.copyValuesFrom(props);
        deleteProperties.outputAction.setValue(TSalesforceOutputProperties.ACTION_DELETE);
        doWriteRows(deleteProperties, rows);
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
