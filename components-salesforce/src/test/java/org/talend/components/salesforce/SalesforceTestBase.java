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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.SchemaBuilder.FieldAssembler;
import org.apache.avro.generic.GenericData;
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
import org.talend.components.salesforce.runtime.SalesforceWriterTestIT;
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

import com.sforce.async.AsyncApiException;
import com.sforce.ws.ConnectionException;

@SuppressWarnings("nls")
public class SalesforceTestBase extends AbstractComponentTest {

    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();

    private ComponentService componentService;

    protected RuntimeContainer adaptor;

    public static final boolean ADD_QUOTES = true;

    static public final String userId = System.getProperty("salesforce.user");

    static public final String password = System.getProperty("salesforce.password");

    static public final String securityKey = System.getProperty("salesforce.key");

    public SalesforceTestBase() {
        adaptor = new DefaultComponentRuntimeContainerImpl();
    }

    public String createNewRandom() {
        return Integer.toString(ThreadLocalRandom.current().nextInt(1, 100000));
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

    static public SalesforceConnectionProperties setupProps(SalesforceConnectionProperties props, boolean addQuotes) {
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
    }

    public Schema getMakeRowSchema(boolean isDynamic) {
        FieldAssembler<Schema> fa = SchemaBuilder.builder().record("MakeRowRecord").fields() //
                .name("Name").type().nullable().stringType().noDefault() //
                .name("ShippingStreet").type().nullable().stringType().noDefault() //
                .name("ShippingPostalCode").type().nullable().intType().noDefault() //
                .name("BillingStreet").type().nullable().stringType().noDefault() //
                .name("BillingState").type().nullable().stringType().noDefault() //
                .name("BillingPostalCode").type().nullable().stringType().noDefault();
        if (isDynamic) {
            fa = fa.name("ShippingState").type().nullable().stringType().noDefault();
        }

        return fa.endRecord();
    }

    public List<IndexedRecord> makeRows(String random, int count, boolean isDynamic) {
        List<IndexedRecord> outputRows = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            GenericData.Record row = new GenericData.Record(getMakeRowSchema(isDynamic));
            row.put("Name", "TestName");
            row.put("ShippingStreet", TEST_KEY);
            row.put("ShippingPostalCode", Integer.toString(i));
            row.put("BillingStreet", "123 Main Street");
            row.put("BillingState", "CA");
            row.put("BillingPostalCode", random);
            if (isDynamic) {
                row.put("ShippingState", "CA");
            }
            System.out.println("Row to insert: " + row.get("Name") //
                    + " id: " + row.get("Id") //
                    + " shippingPostalCode: " + row.get("ShippingPostalCode") //
                    + " billingPostalCode: " + row.get("BillingPostalCode") //
                    + " billingStreet: " + row.get("BillingStreet"));
            outputRows.add(row);
        }
        return outputRows;
    }

    protected List<IndexedRecord> checkRows(String random, List<IndexedRecord> rows, int count) {
        List<IndexedRecord> checkedRows = new ArrayList<>();

        Schema rowSchema = null;
        int iName = 0;
        int iId = 0;
        int iBillingPostalCode = 0;
        int iBillingStreet = 0;
        int iShippingStreet = 0;
        int iShippingState = 0;
        int iBillingState = 0;

        int checkCount = 0;
        for (IndexedRecord row : rows) {
            if (rowSchema == null) {
                rowSchema = row.getSchema();
                iName = rowSchema.getField("Name").pos();
                iId = rowSchema.getField("Id").pos();
                iBillingPostalCode = rowSchema.getField("BillingPostalCode").pos();
                iBillingStreet = rowSchema.getField("BillingStreet").pos();
                iBillingState = rowSchema.getField("BillingState").pos();
                iShippingStreet = rowSchema.getField("ShippingStreet").pos();
                iShippingState = rowSchema.getField("ShippingState").pos();
            }

            System.out.println("check: " + row.get(iName) + " id: " + row.get(iId) + " post: " + row.get(iBillingPostalCode)
                    + " st: " + " post: " + row.get(iBillingStreet));
            String check = (String) row.get(iShippingStreet);
            if (check == null || !check.equals(SalesforceTestBase.TEST_KEY)) {
                continue;
            }
            check = (String) row.get(iBillingPostalCode);
            if (check == null || !check.equals(random)) {
                continue;
            }
            checkCount++;
            assertEquals("CA", row.get(iShippingState));
            assertEquals("TestName", row.get(iName));
            assertEquals("123 Main Street", row.get(iBillingStreet));
            assertEquals("CA", row.get(iBillingState));
            checkedRows.add(row);
        }
        assertEquals(count, checkCount);
        return checkedRows;
    }

    public List<String> getDeleteIds(List<IndexedRecord> rows) {
        List<String> ids = new ArrayList<>();
        for (IndexedRecord row : rows) {
            System.out.println("del: " + row.get(row.getSchema().getField("Name").pos()) + " id: "
                    + row.get(row.getSchema().getField("Id").pos()) + " post: "
                    + row.get(row.getSchema().getField("BillingPostalCode").pos()) + " st: " + " post: "
                    + row.get(row.getSchema().getField("BillingStreet").pos()));
            String check = (String) row.get(row.getSchema().getField("ShippingStreet").pos());
            if (check == null || !check.equals(SalesforceTestBase.TEST_KEY)) {
                continue;
            }
            ids.add((String) row.get(row.getSchema().getField("Id").pos()));
        }
        return ids;
    }

    /**
     * @return the list of row match the TEST_KEY, and if a random values it specified it also filter row against the
     */
    public List<IndexedRecord> filterAllTestRows(String random, List<IndexedRecord> rows) {
        List<IndexedRecord> checkedRows = new ArrayList<>();

        for (IndexedRecord row : rows) {
            String check = (String) row.get(row.getSchema().getField("ShippingStreet").pos());
            if (check == null || !check.equals(TEST_KEY)) {
                continue;
            }
            if (random != null) {// check the random value if specified
                check = (String) row.get(row.getSchema().getField("BillingPostalCode").pos());
                if (check == null || !check.equals(random)) {
                    continue;
                }
            }
            System.out.println("Found match: " + row.get(row.getSchema().getField("Name").pos()) //
                    + " id: " + row.get(row.getSchema().getField("Id").pos()) //
                    + " shippingPostalCode: " + row.get(row.getSchema().getField("ShippingPostalCode").pos()) //
                    + " billingPostalCode: " + row.get(row.getSchema().getField("BillingPostalCode").pos()) //
                    + " billingStreet: " + row.get(row.getSchema().getField("BillingStreet").pos())); //
            checkedRows.add(row);
        }
        return checkedRows;
    }

    static public List<IndexedRecord> getAllTestRows(List<IndexedRecord> rows) {
        List<IndexedRecord> checkedRows = new ArrayList<>();

        for (IndexedRecord row : rows) {
            String check = (String) row.get(row.getSchema().getField("ShippingStreet").pos());
            if (check == null || !check.equals(TEST_KEY)) {
                continue;
            }
            System.out.println("Test row is: " + row.get(row.getSchema().getField("Name").pos()) + " id: "
                    + row.get(row.getSchema().getField("Id").pos()) + " post: "
                    + row.get(row.getSchema().getField("BillingPostalCode").pos()) + " st: " + " post: "
                    + row.get(row.getSchema().getField("BillingStreet").pos()));
            checkedRows.add(row);
        }
        return checkedRows;
    }

    protected List<IndexedRecord> readRows(TSalesforceInputProperties inputProps) throws IOException {
        BoundedReader<IndexedRecord> reader = createBoundedReader(inputProps);
        boolean hasRecord = reader.start();
        List<IndexedRecord> rows = new ArrayList<>();
        while (hasRecord) {
            org.apache.avro.generic.IndexedRecord unenforced = reader.getCurrent();
            rows.add(unenforced);
            hasRecord = reader.advance();
        }
        return rows;
    }

    protected List<IndexedRecord> readRows(SalesforceConnectionModuleProperties props) throws IOException {
        TSalesforceInputProperties inputProps = (TSalesforceInputProperties) new TSalesforceInputProperties("bar").init();
        inputProps.connection = props.connection;
        inputProps.module = props.module;
        inputProps.batchSize.setValue(200);
        List<IndexedRecord> inputRows = readRows(inputProps);
        return inputRows;
    }

    List<IndexedRecord> readAndCheckRows(String random, SalesforceConnectionModuleProperties props, int count) throws Exception {
        List<IndexedRecord> inputRows = readRows(props);
        return checkRows(random, inputRows, count);
    }

    protected void checkRows(List<IndexedRecord> outputRows, SalesforceConnectionModuleProperties props) throws Exception {
        List<IndexedRecord> inputRows = readRows(props);
        assertThat(inputRows, containsInAnyOrder(outputRows.toArray()));
    }

    protected void checkAndDelete(String random, SalesforceConnectionModuleProperties props, int count) throws Exception {
        List<IndexedRecord> inputRows = readAndCheckRows(random, props, count);
        deleteRows(inputRows, props);
        readAndCheckRows(random, props, 0);
    }

    public <T> T writeRows(Writer<T> writer, List<IndexedRecord> outputRows) throws IOException {
        T result;
        writer.open("foo");
        try {
            for (IndexedRecord row : outputRows) {
                writer.write(row);
            }
        } finally {
            result = writer.close();
        }
        return result;
    }

    // Returns the rows written (having been re-read so they have their Ids)
    protected void doWriteRows(SalesforceConnectionModuleProperties props, List<IndexedRecord> outputRows) throws Exception {
        SalesforceSink salesforceSink = new SalesforceSink();
        salesforceSink.initialize(adaptor, props);
        SalesforceWriteOperation writeOperation = (SalesforceWriteOperation) salesforceSink.createWriteOperation();
        Writer<WriterResult> saleforceWriter = writeOperation.createWriter(adaptor);
        writeRows(saleforceWriter, outputRows);
    }

    // Returns the rows written (having been re-read so they have their Ids)
    protected List<IndexedRecord> writeRows(String random, SalesforceConnectionModuleProperties props,
            List<IndexedRecord> outputRows) throws Exception {
        TSalesforceOutputProperties outputProps = new TSalesforceOutputProperties("output"); //$NON-NLS-1$
        outputProps.copyValuesFrom(props);
        outputProps.outputAction.setValue(TSalesforceOutputProperties.OutputAction.INSERT);
        doWriteRows(outputProps, outputRows);
        return readAndCheckRows(random, props, outputRows.size());
    }

    protected void deleteRows(List<IndexedRecord> rows, SalesforceConnectionModuleProperties props) throws Exception {
        TSalesforceOutputProperties deleteProperties = new TSalesforceOutputProperties("delete"); //$NON-NLS-1$
        deleteProperties.copyValuesFrom(props);
        deleteProperties.outputAction.setValue(TSalesforceOutputProperties.ACTION_DELETE);
        System.out.println("deleting " + rows.size() + " rows");
        doWriteRows(deleteProperties, rows);
    }

    public <T> BoundedReader<T> createSalesforceInputReaderFromModule(String moduleName) {
        TSalesforceInputProperties tsip = (TSalesforceInputProperties) new TSalesforceInputProperties("foo").init(); //$NON-NLS-1$
        SalesforceConnectionProperties conProps = setupProps(tsip.connection, !ADD_QUOTES);
        tsip.module.moduleName.setValue(moduleName);
        return createBoundedReader(tsip);
    }

    public <T> BoundedReader<T> createBoundedReader(ComponentProperties tsip) {
        SalesforceSource salesforceSource = new SalesforceSource();
        salesforceSource.initialize(null, tsip);
        return salesforceSource.createReader(null);
    }

    public static void main(String[] args) throws Exception {
        deleteAllAccountTestRows();

    }

    public static void deleteAllAccountTestRows() throws ConnectionException, AsyncApiException, Exception {
        BoundedReader salesforceInputReader = new SalesforceTestBase()
                .createSalesforceInputReaderFromModule(EXISTING_MODULE_NAME);
        // getting all rows
        List<IndexedRecord> rows = new ArrayList<>();
        try {
            salesforceInputReader.start();
            while (salesforceInputReader.advance()) {
                rows.add((IndexedRecord) salesforceInputReader.getCurrent());
            }
        } finally {
            salesforceInputReader.close();
        }
        // filtering rows
        List<IndexedRecord> rowToBeDeleted = getAllTestRows(rows);
        // deleting rows
        TSalesforceOutputProperties salesforceoutputProperties = SalesforceWriterTestIT.createAccountSalesforceoutputProperties();
        setupProps(salesforceoutputProperties.connection, !ADD_QUOTES);
        new SalesforceTestBase().deleteRows(rowToBeDeleted, salesforceoutputProperties);
    }

}
