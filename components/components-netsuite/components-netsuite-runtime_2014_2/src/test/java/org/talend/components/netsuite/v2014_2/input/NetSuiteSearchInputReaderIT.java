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

import java.util.Arrays;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.netsuite.AbstractNetSuiteTestBase;
import org.talend.components.netsuite.NetSuiteDatasetRuntime;
import org.talend.components.netsuite.NetSuiteEndpoint;
import org.talend.components.netsuite.NetSuiteSource;
import org.talend.components.netsuite.NetSuiteWebServiceTestFixture;
import org.talend.components.netsuite.client.NetSuiteClientService;
import org.talend.components.netsuite.connection.NetSuiteConnectionProperties;
import org.talend.components.netsuite.input.NetSuiteInputProperties;
import org.talend.components.netsuite.input.NetSuiteSearchInputReader;
import org.talend.components.netsuite.v2014_2.NetSuiteRuntimeImpl;
import org.talend.components.netsuite.v2014_2.NetSuiteSourceImpl;
import org.talend.components.netsuite.v2014_2.client.NetSuiteClientFactoryImpl;

import com.netsuite.webservices.v2014_2.lists.accounting.types.AccountType;
import com.netsuite.webservices.v2014_2.platform.NetSuitePortType;

/**
 *
 */
public class NetSuiteSearchInputReaderIT extends AbstractNetSuiteTestBase {
    private static NetSuiteWebServiceTestFixture webServiceTestFixture;

    private static NetSuiteConnectionProperties connectionProperties;

    private static NetSuiteClientService<NetSuitePortType> clientService;

    private static final RuntimeContainer CONTAINER = getRuntimeContainer();

    @BeforeClass
    public static void classSetUp() throws Exception {
        webServiceTestFixture = new NetSuiteWebServiceTestFixture(
                NetSuiteClientFactoryImpl.INSTANCE, "2014_2");
        classScopedTestFixtures.add(webServiceTestFixture);
        setUpClassScopedTestFixtures();
        connectionProperties = getConnectionProperties();
        clientService = webServiceTestFixture.getClientService();
        clientService.login();
        CONTAINER.setComponentData(CONNECTION_COMPONENT_ID, NetSuiteEndpoint.CONNECTION, clientService);
    }

    @AfterClass
    public static void classTearDown() throws Exception {
        tearDownClassScopedTestFixtures();
    }

    @Test
    public void testInput() throws Exception {
        NetSuiteInputProperties properties = new NetSuiteInputProperties("test");
        properties.init();

        connectionProperties.apiVersion.setValue("2014.2");
        properties.connection.referencedComponent.componentInstanceId.setValue(CONNECTION_COMPONENT_ID);
        properties.connection.referencedComponent.setReference(connectionProperties);
        properties.module.moduleName.setValue("Account");

        NetSuiteRuntimeImpl runtime = new NetSuiteRuntimeImpl();
        NetSuiteDatasetRuntime dataSetRuntime = runtime.getDatasetRuntime(CONTAINER, properties);
        Schema schema = dataSetRuntime.getSchema(properties.module.moduleName.getValue());
        properties.module.main.schema.setValue(schema);

        properties.module.searchQuery.field.setValue(Arrays.asList("Type"));
        properties.module.searchQuery.operator.setValue(Arrays.asList("List.anyOf"));
        properties.module.searchQuery.value1.setValue(Arrays.<Object>asList("Bank"));
        properties.module.searchQuery.value2.setValue(Arrays.<Object>asList((String) null));
        NetSuiteSource source = new NetSuiteSourceImpl();
        source.initialize(CONTAINER, properties);

        NetSuiteSearchInputReader reader = (NetSuiteSearchInputReader) source.createReader(CONTAINER);

        boolean started = reader.start();
        assertTrue(started);

        IndexedRecord record = reader.getCurrent();
        assertNotNull(record);

        List<Schema.Field> fields = record.getSchema().getFields();
        for (int i = 0; i < fields.size(); i++) {
            Schema.Field typeField = getFieldByName(fields, "AcctType");
            Object value = record.get(typeField.pos());
            assertNotNull(value);
            assertEquals(AccountType.BANK.value(), value);
        }
    }

}
