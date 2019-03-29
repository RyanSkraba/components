// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
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

import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.BoundedReader;
import org.talend.components.api.container.DefaultComponentRuntimeContainerImpl;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.test.ComponentTestUtils;
import org.talend.components.salesforce.SalesforceConnectionProperties;
import org.talend.components.salesforce.runtime.common.ConnectionHolder;
import org.talend.components.salesforce.test.SalesforceTestBase;
import org.talend.components.salesforce.tsalesforceconnection.TSalesforceConnectionDefinition;
import org.talend.components.salesforce.tsalesforceinput.TSalesforceInputDefinition;
import org.talend.components.salesforce.tsalesforceinput.TSalesforceInputProperties;
import org.talend.components.salesforce.tsalesforceoutput.TSalesforceOutputProperties;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.test.PropertiesTestUtils;

import com.sforce.async.AsyncApiException;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.fault.ExceptionCode;
import com.sforce.soap.partner.fault.LoginFault;
import com.sforce.ws.ConnectionException;

/**
 * Test Salesforce connection session
 */
public class SalesforceSessionReuseTestIT extends SalesforceTestBase {
    @ClassRule
    public static final TestRule DISABLE_IF_NEEDED = new DisableIfMissingConfig();

    private static final Logger LOGGER = LoggerFactory.getLogger(SalesforceSessionReuseTestIT.class);

    private static final String WRONG_PWD = "WRONG_PWD";

    private static String randomizedValue;

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @BeforeClass
    public static void setup() throws Throwable {
        randomizedValue = "Name_Unit_" + createNewRandom();

        List<IndexedRecord> outputRows = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            GenericData.Record row = new GenericData.Record(getSchema(false));
            row.put("Name", randomizedValue);
            row.put("ShippingStreet", "123 Main Street");
            row.put("ShippingPostalCode", Integer.toString(i));
            row.put("BillingStreet", "123 Main Street");
            row.put("BillingState", "CA");
            row.put("BillingPostalCode", createNewRandom());
            outputRows.add(row);
        }

        writeRows(outputRows);
    }

    @AfterClass
    public static void cleanup() throws Throwable {
        deleteAllAccountTestRows(randomizedValue);
    }

    /*
    * If the logic changes for this test please specify appropriate timeout.
    * The average execution time for this test in range 1 - 3 sec.
    */
    @Test(timeout = 30_000)
    public void testBasicLogin() throws Throwable {
        File sessionFolder = new File(tempFolder.getRoot().getPath() + "/tsalesforceconnection/");
        assertEquals(0, sessionFolder.getTotalSpace());
        LOGGER.debug("session folder: " + sessionFolder.getAbsolutePath());
        SalesforceConnectionProperties props = setupProps(null, !ADD_QUOTES);
        // setup session function
        props.reuseSession.setValue(true);
        props.sessionDirectory.setValue(sessionFolder.getAbsolutePath());

        // Init session
        assertEquals(ValidationResult.Result.OK, testConnection(props).getStatus());
        assertNotEquals(0, sessionFolder.getTotalSpace());

        // Set wrong pwd to test reuse session from session folder
        props.userPassword.password.setValue(WRONG_PWD);
        assertEquals(ValidationResult.Result.OK, testConnection(props).getStatus());

    }

    @Ignore("Need to solve test failed randomly")
    @Test(timeout = 30_000)
    public void testUseExistingConnection() throws Throwable {
        File sessionFolder = new File(tempFolder.getRoot().getPath() + "/tsalesforceconnection_1/");
        assertEquals(0, sessionFolder.getTotalSpace());
        LOGGER.debug("session folder: " + sessionFolder.getAbsolutePath());
        SalesforceConnectionProperties connProps = (SalesforceConnectionProperties) getComponentService()
                .getComponentProperties(TSalesforceConnectionDefinition.COMPONENT_NAME);
        setupProps(connProps, !ADD_QUOTES);
        // setup session function
        connProps.reuseSession.setValue(true);
        connProps.sessionDirectory.setValue(sessionFolder.getAbsolutePath());

        final String currentComponentName = TSalesforceConnectionDefinition.COMPONENT_NAME + "_1";
        final java.util.Map<String, Object> globalMap = new java.util.HashMap<String, Object>();
        RuntimeContainer connContainer = new DefaultComponentRuntimeContainerImpl() {

            @Override
            public String getCurrentComponentId() {
                return currentComponentName;
            }

            @Override
            public Object getComponentData(String componentId, String key) {
                return globalMap.get(componentId + "_" + key);
            }

            @Override
            public void setComponentData(String componentId, String key, Object data) {
                globalMap.put(componentId + "_" + key, data);
            }

            @Override
            public Object getGlobalData(String key) {
                return globalMap.get(key);
            }
        };
        // 1. salesforce connection would save the session to a session file
        SalesforceSourceOrSink salesforceSourceOrSink = new SalesforceSourceOrSink();
        salesforceSourceOrSink.initialize(connContainer, connProps);
        assertEquals(ValidationResult.Result.OK, salesforceSourceOrSink.validate(connContainer).getStatus());

        // 2. set a wrong pwd to connection properties
        connProps.userPassword.password.setValue(WRONG_PWD);

        // Input component get connection from the tSalesforceConnection
        TSalesforceInputProperties inProps = (TSalesforceInputProperties) getComponentService()
                .getComponentProperties(TSalesforceInputDefinition.COMPONENT_NAME);
        inProps.connection.referencedComponent.componentInstanceId.setValue(currentComponentName);
        inProps.connection.referencedComponent.setReference(connProps);
        checkAndAfter(inProps.connection.referencedComponent.getReference().getForm(Form.REFERENCE), "referencedComponent",
                inProps.connection);

        ComponentTestUtils.checkSerialize(inProps, errorCollector);

        assertEquals(2, inProps.getForms().size());
        Form f = inProps.module.getForm(Form.REFERENCE);
        assertTrue(f.getWidget("moduleName").isCallBeforeActivate());
        ComponentProperties moduleProps = (ComponentProperties) f.getProperties();

        try {
            // 3. input components would be get connection from connection session file
            moduleProps = (ComponentProperties) PropertiesTestUtils.checkAndBeforeActivate(getComponentService(), f, "moduleName",
                    moduleProps);

            Object connection = connContainer.getComponentData(currentComponentName, SalesforceSourceOrSink.KEY_CONNECTION);
            assertNotNull(connection);
            ((PartnerConnection) connection).getConfig().setPassword(WRONG_PWD);
            // Check whether the session disable by other test.
            ((PartnerConnection) connection).getUserInfo();

            Property<?> prop = (Property<?>) f.getWidget("moduleName").getContent();
            assertTrue(prop.getPossibleValues().size() > 100);
            LOGGER.debug(moduleProps.getValidationResult().toString());
            assertEquals(ValidationResult.Result.OK, moduleProps.getValidationResult().getStatus());

            invalidSession(connProps, null);
            // This means that the session is disabled by current test

            // 4. invalid the session, then the session should be renew based on reference connection information(wrong pwd)
            // connect would be fail

            moduleProps = (ComponentProperties) PropertiesTestUtils.checkAndBeforeActivate(getComponentService(), f, "moduleName",
                    moduleProps);
            assertEquals(ValidationResult.Result.ERROR, moduleProps.getValidationResult().getStatus());
            LOGGER.debug(moduleProps.getValidationResult().toString());
        } catch (ConnectionException e) {
            // Maybe get exception when same account run in parallel
            // The session maybe disabled by other test
            assertThat(e, instanceOf(LoginFault.class));
            assertEquals(ExceptionCode.INVALID_LOGIN, ((LoginFault) e).getExceptionCode());
            LOGGER.debug("session is disabled by other test!");
        }

        // 5.set correct pwd back
        setupProps(connProps, !ADD_QUOTES);
        moduleProps = (ComponentProperties) PropertiesTestUtils.checkAndBeforeActivate(getComponentService(), f, "moduleName",
                moduleProps);
        assertEquals(ValidationResult.Result.OK, moduleProps.getValidationResult().getStatus());
        LOGGER.debug(moduleProps.getValidationResult().toString());

    }

    /*
    * If the logic changes for this test please specify appropriate timeout.
    * The average execution time for this test 3.2-4.7 sec.
    */
    @Test(timeout = 40_000)
    public void testInputReuseSession() throws Throwable {
        File sessionFolder = new File(tempFolder.getRoot().getPath() + "/tsalesforceinput/");
        assertEquals(0, sessionFolder.getTotalSpace());
        LOGGER.debug("session folder: " + sessionFolder.getAbsolutePath());

        TSalesforceInputProperties props = (TSalesforceInputProperties) new TSalesforceInputProperties("foo").init(); //$NON-NLS-1$
        props.module.moduleName.setValue(EXISTING_MODULE_NAME);
        props.module.main.schema.setValue(getMakeRowSchema(false));
        props.connection = setupProps(null, !ADD_QUOTES);

        // setup session function
        props.connection.reuseSession.setValue(true);
        props.connection.sessionDirectory.setValue(sessionFolder.getAbsolutePath());

        // Init session
        assertEquals(ValidationResult.Result.OK, testConnection(props).getStatus());
        assertNotEquals(0, sessionFolder.getTotalSpace());

        // Invalid session, test whether it can be renew the session
        invalidSession(props.connection, null);

        List<IndexedRecord> records = readRows(props);
        assertNotNull(records);
        LOGGER.debug("current records number in module " + EXISTING_MODULE_NAME + ": " + records.size());
        assertNotEquals(0, records.size());

        // Set wrong pwd to test reuse session from session folder
        props.connection.userPassword.password.setValue(WRONG_PWD);

        try {
            testConnection(props);
            records = readRows(props);
            assertNotNull(records);
            LOGGER.debug("current records number in module " + EXISTING_MODULE_NAME + ": " + records.size());
            assertNotEquals(0, records.size());
            // Test reuse session fails with wrong pwd
            invalidSession(props.connection, null);
            // This means that the session is disabled by current test
        } catch (IOException e) {
            Throwable caused = e.getCause();
            // Maybe get exception when same account run in parallel
            // The session maybe disabled by other test
            assertThat(caused, instanceOf(LoginFault.class));
            assertEquals(ExceptionCode.INVALID_LOGIN, ((LoginFault) caused).getExceptionCode());
            LOGGER.debug("session is disabled by other test!");
        }

        try {
            readRows(props);
        } catch (IOException e) {
            Throwable caused = e.getCause();
            // Should login fails with wrong pwd
            assertThat(caused, instanceOf(LoginFault.class));
            assertEquals(ExceptionCode.INVALID_LOGIN, ((LoginFault) caused).getExceptionCode());
            LOGGER.debug("expect login fails: " + e.getMessage());
        }

        // Disable reuse session function
        props.connection.reuseSession.setValue(false);
        LOGGER.debug("expect login fails:");
        assertEquals(ValidationResult.Result.ERROR, testConnection(props).getStatus());

    }

    @Test
    public void testBulkSessionRenew() throws Exception {

        TSalesforceInputProperties props = (TSalesforceInputProperties) new TSalesforceInputProperties("foo").init(); //$NON-NLS-1$
        props.module.moduleName.setValue(EXISTING_MODULE_NAME);
        props.module.main.schema.setValue(getMakeRowSchema(false));
        props.connection = setupProps(null, !ADD_QUOTES);

        // setup session function
        props.connection.bulkConnection.setValue(true);
        props.queryMode.setValue(TSalesforceInputProperties.QueryMode.Bulk);
        props.condition.setValue("Name = '"+ randomizedValue + "'");
        // Init session
        assertEquals(ValidationResult.Result.OK, testConnection(props).getStatus());

        BoundedReader<?> reader = createBoundedReader(props);
        assertThat(reader, instanceOf(SalesforceBulkQueryInputReader.class));

        reader.start();
        // Invalid the session by session id
        String sessionIdBeforeRenew = ((SalesforceBulkQueryInputReader) reader).bulkRuntime.getBulkConnection().getConfig()
                .getSessionId();
        reader.close();

        Thread.sleep(1000);

        invalidSession(props.connection, sessionIdBeforeRenew);
        // Test renew session for bulk connections
        try {
            reader.start();
        } catch (IOException io) {
            if (io.getCause() instanceof AsyncApiException
                    && io.getMessage().contains("Unable to find any data to create batch")) {
                // This kind of error shows that connection is established well, but some issue with test data.
                // It happens in SalesforceBulkRuntime inside of method createBatchFromStream, after job was created.
                LOGGER.warn("Issue with getting prepared test data. Error msg - {}", io.getMessage());
            } else {
                throw io;
            }
        }
        // Check the renew session
        String sessionIdAfterRenew = ((SalesforceBulkQueryInputReader) reader).bulkRuntime.getBulkConnection().getConfig()
                .getSessionId();
        reader.close();
        assertNotEquals(sessionIdBeforeRenew, sessionIdAfterRenew);

    }

    @Ignore("Need to solve test failed randomly")
    @Test(timeout = 30_000)
    public void testOutputReuseSession() throws Throwable {
        File sessionFolder = new File(tempFolder.getRoot().getPath() + "/tsalesforceoutput/");
        assertEquals(0, sessionFolder.getTotalSpace());
        LOGGER.debug("session folder: " + sessionFolder.getAbsolutePath());

        TSalesforceOutputProperties props = (TSalesforceOutputProperties) new TSalesforceOutputProperties("foo").init(); //$NON-NLS-1$
        props.module.moduleName.setValue(EXISTING_MODULE_NAME);
        props.module.main.schema.setValue(getMakeRowSchema(false));
        props.connection = setupProps(null, !ADD_QUOTES);

        // setup session function
        props.connection.reuseSession.setValue(true);
        props.connection.sessionDirectory.setValue(sessionFolder.getAbsolutePath());

        // Init session
        assertEquals(ValidationResult.Result.OK, testConnection(props).getStatus());
        assertNotEquals(0, sessionFolder.getTotalSpace());

        // Invalid session, test whether it can be renew the session
        invalidSession(props.connection, null);
        // length=260
        String invalidName = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
                + "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
                + "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
        List<IndexedRecord> records = new ArrayList<>();
        IndexedRecord r1 = new GenericData.Record(props.module.main.schema.getValue());
        r1.put(0, invalidName);
        records.add(r1);
        try {
            doWriteRows(props, records);
        } catch (IOException e) {
            // means exception caused by "Name" too long
            assertTrue(e.getMessage().contains(invalidName));
            LOGGER.debug("expect exception: " + e.getMessage());
        }
        // Set wrong pwd to test reuse session from session folder
        props.connection.userPassword.password.setValue(WRONG_PWD);
        try {
            doWriteRows(props, records);
        } catch (IOException e) {
            Throwable caused = e.getCause();
            if (caused != null && caused instanceof LoginFault) {
                // Maybe throw exception when same account run in parallel
                // The session maybe disabled by other test
                assertEquals(ExceptionCode.INVALID_LOGIN, ((LoginFault) caused).getExceptionCode());
                LOGGER.debug("session is disabled by other test!");
            } else {
                // means exception caused by "Name" too long
                assertTrue(e.getMessage().contains(invalidName));
                LOGGER.debug("expect exception: " + e.getMessage());
                // This means that the session not disabled by other test

                invalidSession(props.connection, null);
                // This means that the session is disabled by current test
            }
        }

        // Test reuse session fails with wrong pwd
        try {
            doWriteRows(props, records);
        } catch (IOException e) {
            Throwable caused = e.getCause();
            // Should login fails with wrong pwd
            assertThat(caused, instanceOf(LoginFault.class));
            assertEquals(ExceptionCode.INVALID_LOGIN, ((LoginFault) caused).getExceptionCode());
            LOGGER.debug("expect login fails: " + e.getMessage());
        }

        // Disable reuse session function
        props.connection.reuseSession.setValue(false);
        LOGGER.debug("expect login fails:");
        assertEquals(ValidationResult.Result.ERROR, testConnection(props).getStatus());

    }

    protected ValidationResult testConnection(ComponentProperties props) {
        SalesforceSourceOrSink sourceOrSink = new SalesforceSourceOrSink();
        sourceOrSink.initialize(null, props);
        ValidationResult result = sourceOrSink.validate(null);
        return result;
    }

    protected void invalidSession(SalesforceConnectionProperties props, String sessionId) throws Exception {
        SalesforceSourceOrSink sourceOrSink = new SalesforceSourceOrSink();
        sourceOrSink.initialize(null, props);
        ConnectionHolder connectionHolder = sourceOrSink.connect(null);
        assertNotNull(connectionHolder.connection);
        if (sessionId != null) {
            connectionHolder.connection.invalidateSessions(new String[] { sessionId });
        }
        connectionHolder.connection.logout();
        LOGGER.debug("session \"" + connectionHolder.connection.getConfig().getSessionId() + "\" invalided!");
    }

}