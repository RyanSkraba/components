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

package org.talend.components.netsuite.output;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.talend.components.netsuite.NetSuiteWebServiceMockTestFixture.assertNsObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.netsuite.AbstractNetSuiteTestBase;
import org.talend.components.netsuite.NetSuiteDatasetRuntime;
import org.talend.components.netsuite.NetSuiteDatasetRuntimeImpl;
import org.talend.components.netsuite.NetSuiteMockTestBase;
import org.talend.components.netsuite.NetSuiteRuntime;
import org.talend.components.netsuite.TestNetSuiteRuntimeImpl;
import org.talend.components.netsuite.client.CustomMetaDataSource;
import org.talend.components.netsuite.client.NetSuiteClientService;
import org.talend.components.netsuite.client.model.RefType;
import org.talend.components.netsuite.client.model.TypeDesc;

import com.netsuite.webservices.test.platform.NetSuitePortType;
import com.netsuite.webservices.test.platform.core.CustomRecordRef;
import com.netsuite.webservices.test.platform.core.RecordRef;
import com.netsuite.webservices.test.platform.core.types.RecordType;
import com.netsuite.webservices.test.setup.customization.CustomRecord;
import com.netsuite.webservices.test.transactions.sales.Opportunity;

/**
 *
 */
public class NetSuiteOutputTransducerTest extends NetSuiteMockTestBase {

    protected NetSuitePortType port;
    protected NetSuiteClientService<NetSuitePortType> clientService;

    @Override
    @Before
    public void setUp() throws Exception {
        installWebServiceMockTestFixture();
        installMockTestFixture();

        super.setUp();

        port = webServiceMockTestFixture.getPortMock();
        clientService = webServiceMockTestFixture.getClientService();
    }

    @Test
    public void testBasic() throws Exception {

        NetSuiteRuntime netSuiteRuntime = new TestNetSuiteRuntimeImpl(webServiceMockTestFixture.getClientFactory());
        NetSuiteDatasetRuntime dataSetRuntime = netSuiteRuntime.getDatasetRuntime(mockTestFixture.getConnectionProperties());

        mockGetRequestResults(null);

        TypeDesc typeDesc = clientService.getMetaDataSource().getTypeInfo("Opportunity");

        Schema schema = dataSetRuntime.getSchema(typeDesc.getTypeName());

        NsObjectOutputTransducer transducer = new NsObjectOutputTransducer(
                webServiceMockTestFixture.getClientService(), typeDesc.getTypeName());

        List<IndexedRecord> indexedRecordList = makeIndexedRecords(clientService, schema,
                new AbstractNetSuiteTestBase.SimpleObjectComposer<>(Opportunity.class), 10);

        for (IndexedRecord indexedRecord : indexedRecordList) {
            Opportunity record = (Opportunity) transducer.write(indexedRecord);
            assertNsObject(typeDesc, record);
        }
    }

    @Test
    public void testNonRecordObjects() throws Exception {

        NetSuiteRuntime netSuiteRuntime = new TestNetSuiteRuntimeImpl(webServiceMockTestFixture.getClientFactory());
        NetSuiteDatasetRuntime dataSetRuntime = netSuiteRuntime.getDatasetRuntime(mockTestFixture.getConnectionProperties());

        Collection<String> typeNames = Arrays.asList(
                RefType.RECORD_REF.getTypeName(),
                RefType.CUSTOM_RECORD_REF.getTypeName()
        );

        for (String typeName : typeNames) {
            TypeDesc typeDesc = clientService.getMetaDataSource().getTypeInfo(typeName);

            Schema schema = dataSetRuntime.getSchema(typeDesc.getTypeName());

            NsObjectOutputTransducer transducer = new NsObjectOutputTransducer(webServiceMockTestFixture.getClientService(),
                    typeDesc.getTypeName());

            List<IndexedRecord> indexedRecordList = makeIndexedRecords(clientService, schema,
                    new AbstractNetSuiteTestBase.SimpleObjectComposer<>(typeDesc.getTypeClass()), 10);

            for (IndexedRecord indexedRecord : indexedRecordList) {
                Object nsObject = transducer.write(indexedRecord);
                assertNsObject(typeDesc, nsObject);
            }
        }
    }

    @Test
    public void testRecordRef() throws Exception {

        NetSuiteRuntime netSuiteRuntime = new TestNetSuiteRuntimeImpl(webServiceMockTestFixture.getClientFactory());
        NetSuiteDatasetRuntime dataSetRuntime = netSuiteRuntime.getDatasetRuntime(mockTestFixture.getConnectionProperties());

        TypeDesc typeDesc = clientService.getMetaDataSource().getTypeInfo(RefType.RECORD_REF.getTypeName());
        TypeDesc referencedTypeDesc = clientService.getMetaDataSource().getTypeInfo("Opportunity");

        Schema schema = dataSetRuntime.getSchema(typeDesc.getTypeName());

        NsObjectOutputTransducer transducer = new NsObjectOutputTransducer(webServiceMockTestFixture.getClientService(),
                referencedTypeDesc.getTypeName());
        transducer.setReference(true);

        List<IndexedRecord> indexedRecordList = makeIndexedRecords(clientService, schema,
                new AbstractNetSuiteTestBase.SimpleObjectComposer<>(typeDesc.getTypeClass()), 10);

        for (IndexedRecord indexedRecord : indexedRecordList) {
            Object nsObject = transducer.write(indexedRecord);
            assertNsObject(typeDesc, nsObject);

            RecordRef ref = (RecordRef) nsObject;
            assertEquals(RecordType.OPPORTUNITY, ref.getType());
        }
    }

    @Test
    public void testCustomFields() throws Exception {

        TestCustomMetaDataSource customMetaDataSource = new TestCustomMetaDataSource(clientService, "Opportunity");
        clientService.getMetaDataSource().setCustomMetaDataSource(customMetaDataSource);

        NetSuiteRuntime netSuiteRuntime = new TestNetSuiteRuntimeImpl(webServiceMockTestFixture.getClientFactory());
        NetSuiteDatasetRuntime dataSetRuntime = netSuiteRuntime.getDatasetRuntime(mockTestFixture.getConnectionProperties());

        mockGetRequestResults(null);

        TypeDesc basicTypeDesc = clientService.getBasicMetaData().getTypeInfo("Opportunity");
        TypeDesc typeDesc = clientService.getMetaDataSource().getTypeInfo("Opportunity");

        final List<Opportunity> recordList = makeNsObjects(
                new NsObjectComposer<Opportunity>(clientService.getMetaDataSource(), typeDesc), 10);
        mockSearchRequestResults(recordList, 100);

        Schema schema = dataSetRuntime.getSchema(typeDesc.getTypeName());

        NsObjectOutputTransducer transducer = new NsObjectOutputTransducer(
                webServiceMockTestFixture.getClientService(), typeDesc.getTypeName());

        List<IndexedRecord> indexedRecordList = makeIndexedRecords(clientService, schema,
                new NsObjectComposer<Opportunity>(clientService.getMetaDataSource(), typeDesc), 10);

        for (IndexedRecord indexedRecord : indexedRecordList) {
            Opportunity record = (Opportunity) transducer.write(indexedRecord);
            assertNsObject(basicTypeDesc, record);
        }
    }

    @Test
    public void testCustomRecord() throws Exception {

        CustomMetaDataSource customMetaDataSource = new TestCustomMetaDataSource(clientService);
        clientService.getMetaDataSource().setCustomMetaDataSource(customMetaDataSource);

        NetSuiteDatasetRuntime dataSetRuntime = new NetSuiteDatasetRuntimeImpl(clientService.getMetaDataSource());

        mockGetRequestResults(null);

        TypeDesc typeDesc = clientService.getMetaDataSource().getTypeInfo("custom_record_type_1");

        Schema schema = dataSetRuntime.getSchema(typeDesc.getTypeName());

        NsObjectOutputTransducer transducer = new NsObjectOutputTransducer(
                webServiceMockTestFixture.getClientService(), typeDesc.getTypeName());

        GenericRecord indexedRecordToAdd = new GenericData.Record(schema);

        String testId = Long.toString(System.currentTimeMillis());
        indexedRecordToAdd.put("Custom_record_field_1", "Test Project " +  testId);
        indexedRecordToAdd.put("Custom_record_field_2", Long.valueOf(System.currentTimeMillis()));

        List<IndexedRecord> indexedRecordList = new ArrayList<>();
        indexedRecordList.add(indexedRecordToAdd);

        for (IndexedRecord indexedRecord : indexedRecordList) {
            CustomRecord record = (CustomRecord) transducer.write(indexedRecord);
            assertNotNull(record.getRecType());
            assertNotNull(record.getRecType().getInternalId());
        }
    }

    @Test
    public void testCustomRecordRef() throws Exception {

        CustomMetaDataSource customMetaDataSource = new TestCustomMetaDataSource(clientService);
        clientService.getMetaDataSource().setCustomMetaDataSource(customMetaDataSource);

        NetSuiteDatasetRuntime dataSetRuntime = new NetSuiteDatasetRuntimeImpl(clientService.getMetaDataSource());

        mockGetRequestResults(null);

        TypeDesc customTypeDesc = clientService.getMetaDataSource().getTypeInfo("custom_record_type_1");

        Schema schema = dataSetRuntime.getSchemaForDelete(customTypeDesc.getTypeName());

        NsObjectOutputTransducer transducer = new NsObjectOutputTransducer(
                webServiceMockTestFixture.getClientService(), customTypeDesc.getTypeName());
        transducer.setReference(true);

        GenericRecord indexedRecordToAdd = new GenericData.Record(schema);

        indexedRecordToAdd.put("InternalId", "123456789");

        List<IndexedRecord> indexedRecordList = new ArrayList<>();
        indexedRecordList.add(indexedRecordToAdd);

        for (IndexedRecord indexedRecord : indexedRecordList) {
            CustomRecordRef recordRef = (CustomRecordRef) transducer.write(indexedRecord);
            assertNotNull(recordRef.getInternalId());
            assertNotNull(recordRef.getTypeId());
        }
    }

}
