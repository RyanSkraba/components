// ============================================================================
//
// Copyright (C) 2006-2020 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.components.netsuite.v2019_2.input;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.junit.AfterClass;
import org.junit.Assert;
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
import org.talend.components.netsuite.v2019_2.NetSuiteRuntimeImpl;
import org.talend.components.netsuite.v2019_2.NetSuiteSourceImpl;
import org.talend.components.netsuite.v2019_2.client.NetSuiteClientFactoryImpl;

import com.netsuite.webservices.v2019_2.platform.NetSuitePortType;
import com.netsuite.webservices.v2019_2.lists.accounting.types.AccountType;

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
                NetSuiteClientFactoryImpl.INSTANCE, "2018_2");
        classScopedTestFixtures.add(webServiceTestFixture);
        setUpClassScopedTestFixtures();
        connectionProperties = getConnectionProperties();
        clientService = webServiceTestFixture.getClientService();
        CONTAINER.setComponentData(CONNECTION_COMPONENT_ID, NetSuiteEndpoint.CONNECTION, clientService);
    }

    @AfterClass
    public static void classTearDown() throws Exception {
        tearDownClassScopedTestFixtures();
    }

    @Test
    public void testSearch() throws Exception {
        NetSuiteInputProperties properties = new NetSuiteInputProperties("test");
        properties.init();
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

    @Test
    public void testSearchItemSublists() throws Exception {
        NetSuiteInputProperties properties = new NetSuiteInputProperties("test");
        properties.init();

        properties.connection.referencedComponent.componentInstanceId.setValue(CONNECTION_COMPONENT_ID);
        properties.connection.referencedComponent.setReference(connectionProperties);
        properties.module.moduleName.setValue("PurchaseOrder");

        NetSuiteRuntimeImpl runtime = new NetSuiteRuntimeImpl();
        NetSuiteDatasetRuntime dataSetRuntime = runtime.getDatasetRuntime(CONTAINER, properties);
        Schema schema = dataSetRuntime.getSchema(properties.module.moduleName.getValue());
        properties.module.main.schema.setValue(schema);

        properties.module.searchQuery.field.setValue(Arrays.asList("InternalId"));
        properties.module.searchQuery.operator.setValue(Arrays.asList("List.anyOf"));
        properties.module.searchQuery.value1.setValue(Arrays.<Object>asList("3"));
        properties.module.searchQuery.value2.setValue(Arrays.<Object>asList((String) null));

        NetSuiteSource source = new NetSuiteSourceImpl();
        source.initialize(CONTAINER, properties);

        NetSuiteSearchInputReader reader = (NetSuiteSearchInputReader) source.createReader(CONTAINER);
        boolean started = reader.start();
        assertTrue(started);

        IndexedRecord record = reader.getCurrent();
        assertNotNull(record);

        List<Schema.Field> fields = record.getSchema().getFields();
        Schema.Field typeField = getFieldByName(fields, "ItemList");
        Assert.assertNull(record.get(typeField.pos()));

        properties.bodyFieldsOnly.setValue(false);

        source.initialize(CONTAINER, properties);

        reader = (NetSuiteSearchInputReader) source.createReader(CONTAINER);
        started = reader.start();
        assertTrue(started);

        record = reader.getCurrent();
        assertNotNull(record);

        fields = record.getSchema().getFields();
        typeField = getFieldByName(fields, "ItemList");
        Assert.assertNotNull(record.get(typeField.pos()));
    }

}
