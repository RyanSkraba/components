// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.BoundedReader;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.component.runtime.Writer;
import org.talend.components.api.test.ComponentTestUtils;
import org.talend.components.common.oauth.OAuth2FlowType;
import org.talend.components.salesforce.SalesforceBulkProperties;
import org.talend.components.salesforce.SalesforceBulkProperties.Concurrency;
import org.talend.components.salesforce.SalesforceConnectionModuleProperties;
import org.talend.components.salesforce.SalesforceConnectionProperties;
import org.talend.components.salesforce.SalesforceOutputProperties;
import org.talend.components.salesforce.integration.SalesforceTestBase;
import org.talend.components.salesforce.tsalesforcebulkexec.TSalesforceBulkExecDefinition;
import org.talend.components.salesforce.tsalesforcebulkexec.TSalesforceBulkExecProperties;
import org.talend.components.salesforce.tsalesforceinput.TSalesforceInputProperties;
import org.talend.components.salesforce.tsalesforceoutputbulk.TSalesforceOutputBulkProperties;
import org.talend.components.salesforce.tsalesforceoutputbulkexec.TSalesforceOutputBulkExecProperties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.runtime.RuntimeInfo;
import org.talend.daikon.runtime.RuntimeUtil;
import org.talend.daikon.sandbox.SandboxedInstance;

/**
 * Created by jzhao on 2016-03-09.
 */
public class SalesforceBulkExecReaderTestIT extends SalesforceTestBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(SalesforceBulkExecReaderTestIT.class);

    static public final String issuer = System.getProperty("salesforce.jwt.issuer");

    static public final String subject = System.getProperty("salesforce.jwt.subject");

    static public final String keyStore = System.getProperty("salesforce.jwt.keyStore");

    static public final String keyStorePwd = System.getProperty("salesforce.jwt.keyStorePwd");

    static public final String certAlias = System.getProperty("salesforce.jwt.certAlias");

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    protected static void delete(TSalesforceOutputBulkProperties outputBulkProperties) {
        File file = new File(outputBulkProperties.bulkFilePath.getStringValue());

        assertTrue(file.exists());
        assertTrue(file.delete());
        assertFalse(file.exists());
    }

    @Test
    @Ignore("because of 5M data storage limitation")
    public void testBulkLimitationV1() throws Throwable {
        testOutputBulkExec(5055,false);
    }

    @Test
    public void testOutputBulkExecV1() throws Throwable {
        testOutputBulkExec(10,false);
    }

    @Test
    @Ignore("because of 5M data storage limitation")
    public void testBulkLimitationV2() throws Throwable {
        testOutputBulkExec(5055,true);
    }

    @Test
    @Ignore("need manual operation for oauth login")
    public void testOutputBulkExecV2() throws Throwable {
        testOutputBulkExec(10,true);
    }

    /**
     *
     * Test when bulk file is empty
     *
     */
    @Test
    public void testOutputBulkExecWithEmptyFile() throws Throwable {
        testOutputBulkExec(0,false);
    }

    /**
     * This test for tSalesforceOutputBulk and tSalesforceBulkExec The runtime of tSalesforceOutputBulkExec should be
     * work like this.
     *
     */
    private void testOutputBulkExec(int count, boolean isBulkV2) throws Throwable {

        String random = createNewRandom();

        List<IndexedRecord> rows = makeRows(random, count, false);

        TSalesforceOutputBulkExecProperties outputBulkExecProperties =null ;

        if(isBulkV2){
            outputBulkExecProperties =
                    createBulkV2Properties();
        }else{
            outputBulkExecProperties =
                    createAccountSalesforceOutputBulkExecProperties();
        }

        // Prepare the bulk file
        TSalesforceOutputBulkProperties outputBulkProperties =
                (TSalesforceOutputBulkProperties) outputBulkExecProperties.getInputComponentProperties();
        generateBulkFile(outputBulkProperties, rows);

        // Test append
        outputBulkProperties.append.setValue(true);
        generateBulkFile(outputBulkProperties, rows);

        // Execute the bulk action
        TSalesforceBulkExecProperties bulkExecProperties =
                (TSalesforceBulkExecProperties) outputBulkExecProperties.getOutputComponentProperties();

        try {
            executeBulkInsert(bulkExecProperties, random, count * 2);
        } finally {
            // Delete the generated bulk file
            delete(outputBulkProperties);

            List<IndexedRecord> inputRows = readRows(bulkExecProperties);
            List<IndexedRecord> allReadTestRows = filterAllTestRows(random, inputRows);
            deleteRows(allReadTestRows, bulkExecProperties);
            inputRows = readRows(bulkExecProperties);
            assertEquals(0, filterAllTestRows(random, inputRows).size());
        }
    }

    /**
     * Test runtime of tSalesforceOutputBulk
     */
    private void executeBulkInsert(TSalesforceBulkExecProperties bulkExecProperties, String random, int count)
            throws Throwable {

        TSalesforceBulkExecDefinition definition = (TSalesforceBulkExecDefinition) getComponentService()
                .getComponentDefinition(TSalesforceBulkExecDefinition.COMPONENT_NAME);
        RuntimeInfo runtimeInfo =
                definition.getRuntimeInfo(ExecutionEngine.DI, bulkExecProperties, ConnectorTopology.OUTGOING);
        try (SandboxedInstance sandboxedInstance =
                RuntimeUtil.createRuntimeClass(runtimeInfo, definition.getClass().getClassLoader())) {
            SalesforceSource boundedSource = (SalesforceSource) sandboxedInstance.getInstance();
            boundedSource.initialize(null, bulkExecProperties);
            BoundedReader boundedReader = boundedSource.createReader(null);

            try {
                boolean hasRecord = boundedReader.start();
                List<IndexedRecord> rows = new ArrayList<>();
                while (hasRecord) {
                    rows.add((IndexedRecord) boundedReader.getCurrent());
                    hasRecord = boundedReader.advance();
                }
                checkRows(random, rows, count);
            } finally {
                boundedReader.close();
            }
        }
    }

    /**
     * Test runtime of tSalesforceBulkExec
     */
    public void generateBulkFile(TSalesforceOutputBulkProperties outputBulkProperties, List<IndexedRecord> rows)
            throws Throwable {

        SalesforceBulkFileSink bfSink = new SalesforceBulkFileSink();
        bfSink.initialize(null, outputBulkProperties);

        SalesforceBulkFileWriteOperation writeOperation =
                (SalesforceBulkFileWriteOperation) bfSink.createWriteOperation();
        Writer<Result> saleforceWriter = writeOperation.createWriter(null);

        Result result = writeRows(saleforceWriter, rows);
        Map<String, Object> resultMap = getConsolidatedResults(result, saleforceWriter);
        Assert.assertEquals(rows.size(), resultMap.get(ComponentDefinition.RETURN_TOTAL_RECORD_COUNT));
    }

    /**
     * The configuration of tSalesforceOutputBulkExec
     */
    protected TSalesforceOutputBulkExecProperties createAccountSalesforceOutputBulkExecProperties() throws Throwable {
        TSalesforceOutputBulkExecProperties props =
                (TSalesforceOutputBulkExecProperties) new TSalesforceOutputBulkExecProperties("foo").init();

        props.connection.timeout.setValue(1200000);
        props.connection.bulkConnection.setValue(true);
        props.outputAction.setValue(SalesforceOutputProperties.OutputAction.INSERT);
        String bulkFilePath = this.getClass().getResource("").getPath() + "/test_outputbulk_1.csv";
        System.out.println("Bulk file path: " + bulkFilePath);
        props.bulkFilePath.setValue(bulkFilePath);
        props.bulkProperties.bytesToCommit.setValue(10 * 1024 * 1024);
        props.bulkProperties.rowsToCommit.setValue(10000);
        props.bulkProperties.concurrencyMode.setValue(Concurrency.Parallel);
        props.bulkProperties.waitTimeCheckBatchState.setValue(10000);

        props.module.main.schema.setValue(getMakeRowSchema(false));
        props.schemaFlow.schema.setValue(getMakeRowSchema(false));

        setupProps(props.connection, !ADD_QUOTES);
        props.module.moduleName.setValue(EXISTING_MODULE_NAME);
        ComponentTestUtils.checkSerialize(props, errorCollector);
        return props;
    }

    /**
     * The configuration of tSalesforceOutputBulkExec
     */
    protected TSalesforceOutputBulkExecProperties createBulkV2Properties() throws Throwable {
        TSalesforceOutputBulkExecProperties props =
                (TSalesforceOutputBulkExecProperties) new TSalesforceOutputBulkExecProperties("foo").init();

        props.connection.timeout.setValue(1200000);
        props.connection.bulkConnection.setValue(true);

        //setup oauth login information
        setupOauthConfig(props.connection);

        props.outputAction.setValue(SalesforceOutputProperties.OutputAction.INSERT);
        String bulkFilePath = this.getClass().getResource("").getPath() + "/test_outputbulk_v2.csv";
        LOGGER.info("Bulk file path: " + bulkFilePath);
        props.bulkFilePath.setValue(bulkFilePath);

        props.bulkProperties.bulkApiV2.setValue(true);
        props.bulkProperties.columnDelimiter.setValue(SalesforceBulkProperties.ColumnDelimiter.COMMA);
        props.bulkProperties.lineEnding.setValue(SalesforceBulkProperties.LineEnding.CRLF);
        props.bulkProperties.waitTimeCheckBatchState.setValue(10000);

        props.module.main.schema.setValue(getMakeRowSchema(false));
        props.schemaFlow.schema.setValue(getMakeRowSchema(false));

        setupProps(props.connection, !ADD_QUOTES);
        props.module.moduleName.setValue(EXISTING_MODULE_NAME);
        ComponentTestUtils.checkSerialize(props, errorCollector);
        return props;
    }

    /**
     * This set the jwt flow configuration, need config in also in our test env
     */
    private void setupOauthConfig(SalesforceConnectionProperties props) throws Throwable {
        if (props == null) {
            Assert.fail("Connection properties not initialized");
        }
        props.loginType.setValue(SalesforceConnectionProperties.LoginType.OAuth);
        props.oauth2FlowType.setValue(OAuth2FlowType.JWT_Flow);
        Form mainForm = props.getForm(Form.MAIN);
        props = (SalesforceConnectionProperties) checkAndAfter(mainForm, "loginType", props);
        props.oauth2JwtFlow.issuer.setValue(issuer);
        props.oauth2JwtFlow.subject.setValue(subject);
        props.oauth2JwtFlow.expirationTime.setValue(12000000);
        props.oauth2JwtFlow.keyStore.setValue(keyStore);
        props.oauth2JwtFlow.keyStorePassword.setValue(keyStorePwd);
        props.oauth2JwtFlow.certificateAlias.setValue(certAlias);
        
    }

    /**
     * Query all fields is not supported in Bulk Query
     */
    @Override
    protected List<IndexedRecord> readRows(SalesforceConnectionModuleProperties props) throws IOException {
        TSalesforceInputProperties inputProps =
                (TSalesforceInputProperties) new TSalesforceInputProperties("bar").init();
        inputProps.connection = props.connection;
        inputProps.module = props.module;
        inputProps.batchSize.setValue(200);
        inputProps.queryMode.setValue(TSalesforceInputProperties.QueryMode.Query);

        inputProps.manualQuery.setValue(true);
        inputProps.query.setValue(
                "select Id,Name,ShippingStreet,ShippingPostalCode,BillingStreet,BillingState,BillingPostalCode from Account");

        inputProps.module.moduleName.setValue(EXISTING_MODULE_NAME);
        inputProps.module.main.schema.setValue(getMakeRowSchema(false));

        List<IndexedRecord> inputRows = readRows(inputProps);
        return inputRows;
    }

    @Override
    public Schema getMakeRowSchema(boolean isDynamic) {
        SchemaBuilder.FieldAssembler<Schema> fa = SchemaBuilder.builder().record("MakeRowRecord").fields() //
                .name("Id").type().nullable().stringType().noDefault() //
                .name("Name").type().nullable().stringType().noDefault() //
                .name("ShippingStreet").type().nullable().stringType().noDefault() //
                .name("ShippingPostalCode").type().nullable().intType().noDefault() //
                .name("BillingStreet").type().nullable().stringType().noDefault() //
                .name("BillingState").type().nullable().stringType().noDefault() //
                .name("BillingPostalCode").type().nullable().stringType().noDefault() //
                .name("BillingCity").type().nullable().stringType().noDefault();
        if (isDynamic) {
            fa = fa.name("ShippingState").type().nullable().stringType().noDefault();
        }

        return fa.endRecord();
    }

}
