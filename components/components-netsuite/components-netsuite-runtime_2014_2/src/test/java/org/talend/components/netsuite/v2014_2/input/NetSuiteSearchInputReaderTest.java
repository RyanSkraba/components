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

package org.talend.components.netsuite.v2014_2.input;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.talend.components.netsuite.NetSuiteWebServiceMockTestFixture.assertIndexedRecord;

import java.util.List;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.netsuite.NetSuiteDatasetRuntime;
import org.talend.components.netsuite.NetSuiteRuntime;
import org.talend.components.netsuite.NetSuiteSource;
import org.talend.components.netsuite.client.NetSuiteClientService;
import org.talend.components.netsuite.client.model.TypeDesc;
import org.talend.components.netsuite.input.NetSuiteInputProperties;
import org.talend.components.netsuite.input.NetSuiteSearchInputReader;
import org.talend.components.netsuite.v2014_2.NetSuiteMockTestBase;
import org.talend.components.netsuite.v2014_2.NetSuiteRuntimeImpl;
import org.talend.components.netsuite.v2014_2.NetSuiteSourceImpl;

import com.netsuite.webservices.v2014_2.lists.accounting.Account;

/**
 *
 */
public class NetSuiteSearchInputReaderTest extends NetSuiteMockTestBase {
    protected NetSuiteInputProperties properties;

    @BeforeClass
    public static void classSetUp() throws Exception {
        installWebServiceTestFixture();
        setUpClassScopedTestFixtures();
    }

    @AfterClass
    public static void classTearDown() throws Exception {
        tearDownClassScopedTestFixtures();
    }

    @Override @Before
    public void setUp() throws Exception {
        installMockTestFixture();

        super.setUp();

        properties = new NetSuiteInputProperties("test");
        properties.init();
        properties.connection.copyValuesFrom(mockTestFixture.getConnectionProperties());
    }

    @Override @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void testBasic() throws Exception {
        properties.module.moduleName.setValue("Account");

        NetSuiteRuntime netSuiteRuntime = new NetSuiteRuntimeImpl();
        NetSuiteDatasetRuntime dataSetRuntime = netSuiteRuntime.getDatasetRuntime(properties.getConnectionProperties());

        Schema schema = dataSetRuntime.getSchema(properties.module.moduleName.getValue());
        properties.module.main.schema.setValue(schema);

        NetSuiteSource source = new NetSuiteSourceImpl();
        source.initialize(mockTestFixture.getRuntimeContainer(), properties);

        List<Account> recordList = makeNsObjects(new SimpleObjectComposer<>(Account.class), 150);
        mockSearchRequestResults(recordList, 100);

        NetSuiteClientService<?> clientService = source.getClientService(null);
        TypeDesc typeDesc = clientService.getMetaDataSource().getTypeInfo(Account.class);

        NetSuiteSearchInputReader reader = (NetSuiteSearchInputReader) source.createReader(
                mockTestFixture.getRuntimeContainer());

        boolean started = reader.start();
        assertTrue(started);

        IndexedRecord record = reader.getCurrent();
        assertNotNull(record);

        while (reader.advance()) {
            record = reader.getCurrent();

            assertIndexedRecord(typeDesc, record);
        }

        Map<String, Object> readerResult = reader.getReturnValues();
        assertNotNull(readerResult);

        assertEquals(150, readerResult.get(ComponentDefinition.RETURN_TOTAL_RECORD_COUNT));
    }

}
