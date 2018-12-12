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

package org.talend.components.netsuite.v2016_2.output;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.IndexedRecord;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.netsuite.AbstractNetSuiteTestBase;
import org.talend.components.netsuite.NetSuiteDatasetRuntime;
import org.talend.components.netsuite.NetSuiteEndpoint;
import org.talend.components.netsuite.NetSuiteSink;
import org.talend.components.netsuite.NetSuiteWebServiceTestFixture;
import org.talend.components.netsuite.client.NetSuiteClientFactory;
import org.talend.components.netsuite.client.NetSuiteClientService;
import org.talend.components.netsuite.client.NetSuiteException;
import org.talend.components.netsuite.client.NsReadResponse;
import org.talend.components.netsuite.client.NsSearchResult;
import org.talend.components.netsuite.client.NsWriteResponse;
import org.talend.components.netsuite.client.model.CustomFieldDesc;
import org.talend.components.netsuite.client.model.FieldDesc;
import org.talend.components.netsuite.client.model.RefType;
import org.talend.components.netsuite.client.model.TypeDesc;
import org.talend.components.netsuite.connection.NetSuiteConnectionProperties;
import org.talend.components.netsuite.input.NsObjectInputTransducer;
import org.talend.components.netsuite.output.NetSuiteOutputProperties;
import org.talend.components.netsuite.output.NetSuiteOutputWriter;
import org.talend.components.netsuite.output.NetSuiteWriteOperation;
import org.talend.components.netsuite.output.OutputAction;
import org.talend.components.netsuite.test.TestUtils;
import org.talend.components.netsuite.v2016_2.NetSuiteRuntimeImpl;
import org.talend.components.netsuite.v2016_2.NetSuiteSinkImpl;
import org.talend.components.netsuite.v2016_2.client.NetSuiteClientFactoryImpl;
import org.talend.components.netsuite.v2016_2.client.NetSuiteClientServiceImpl;
import org.talend.components.netsuite.v2016_2.client.model.RecordTypeEnum;

import com.netsuite.webservices.v2016_2.general.communication.Message;
import com.netsuite.webservices.v2016_2.lists.accounting.Account;
import com.netsuite.webservices.v2016_2.lists.accounting.Subsidiary;
import com.netsuite.webservices.v2016_2.lists.accounting.SubsidiarySearch;
import com.netsuite.webservices.v2016_2.lists.accounting.types.AccountType;
import com.netsuite.webservices.v2016_2.lists.relationships.Contact;
import com.netsuite.webservices.v2016_2.platform.NetSuitePortType;
import com.netsuite.webservices.v2016_2.platform.common.SubsidiarySearchBasic;
import com.netsuite.webservices.v2016_2.platform.core.BooleanCustomFieldRef;
import com.netsuite.webservices.v2016_2.platform.core.CustomFieldList;
import com.netsuite.webservices.v2016_2.platform.core.CustomFieldRef;
import com.netsuite.webservices.v2016_2.platform.core.CustomRecordRef;
import com.netsuite.webservices.v2016_2.platform.core.RecordRef;
import com.netsuite.webservices.v2016_2.platform.core.RecordRefList;
import com.netsuite.webservices.v2016_2.platform.core.StringCustomFieldRef;
import com.netsuite.webservices.v2016_2.platform.core.types.RecordType;
import com.netsuite.webservices.v2016_2.platform.messages.GetListRequest;
import com.netsuite.webservices.v2016_2.setup.customization.CustomRecord;

/**
 *
 */
public class NetSuiteOutputWriterIT extends AbstractNetSuiteTestBase {
    private static NetSuiteWebServiceTestFixture webServiceTestFixture;

    private static NetSuiteClientService<NetSuitePortType> clientService;

    private static final RuntimeContainer CONTAINER = getRuntimeContainer();

    private static NetSuiteConnectionProperties connectionProperties;

    private final NetSuiteClientFactory<NetSuitePortType> clientFactory = new NetSuiteClientFactoryImpl() {

        @Override
        public NetSuiteClientService<NetSuitePortType> createClient() throws NetSuiteException {
            return clientService;
        }
    };

    @BeforeClass
    public static void classSetUp() throws Exception {
        webServiceTestFixture = new NetSuiteWebServiceTestFixture(
                NetSuiteClientFactoryImpl.INSTANCE, "2016_2");
        classScopedTestFixtures.add(webServiceTestFixture);
        setUpClassScopedTestFixtures();
        connectionProperties = getConnectionProperties();
        connectionProperties.apiVersion.setValue("2016.2");
        clientService = webServiceTestFixture.getClientService();
        clientService.login();
        CONTAINER.setComponentData(CONNECTION_COMPONENT_ID, NetSuiteEndpoint.CONNECTION, clientService);
    }

    @AfterClass
    public static void classTearDown() throws Exception {
        tearDownClassScopedTestFixtures();
    }

    @Test
    public void testUpdate() throws Exception {
        clientService.getMetaDataSource().setCustomizationEnabled(false);

        NetSuiteOutputProperties properties = new NetSuiteOutputProperties("test");
        properties.init();
        properties.connection.referencedComponent.componentInstanceId.setValue(CONNECTION_COMPONENT_ID);
        properties.connection.referencedComponent.setReference(connectionProperties);

        properties.module.moduleName.setValue(RecordTypeEnum.ACCOUNT.getTypeName());
        properties.module.action.setValue(OutputAction.UPDATE);

        NetSuiteRuntimeImpl runtime = new NetSuiteRuntimeImpl();
        runtime.setClientFactory(clientFactory);

        NetSuiteDatasetRuntime dataSetRuntime = runtime.getDatasetRuntime(CONTAINER, properties);

        Schema schema = dataSetRuntime.getSchema(properties.module.moduleName.getValue());
        properties.module.main.schema.setValue(schema);
        SubsidiarySearch subsidiarySearch = new SubsidiarySearch();
        SubsidiarySearchBasic subsidiarySearchBasic = new SubsidiarySearchBasic();
        subsidiarySearch.setBasic(subsidiarySearchBasic);
        NsSearchResult<Subsidiary> searchResult = clientService.search(subsidiarySearch);
        assertTrue(searchResult.isSuccess());
        assertNotNull(searchResult.getRecordList());
        assertTrue(searchResult.getRecordList().size() > 0);

        Subsidiary subsidiary = searchResult.getRecordList().get(0);
        RecordRef subsidiaryRef = new RecordRef();
        subsidiaryRef.setType(RecordType.SUBSIDIARY);
        subsidiaryRef.setInternalId(subsidiary.getInternalId());

        List<Account> recordsToAdd = makeAccountRecords(5, subsidiaryRef);

        final List<RecordRef> refList = new ArrayList<>(recordsToAdd.size());

        // Add records

        List<NsWriteResponse<RecordRef>> writeResponseList = clientService.addList(recordsToAdd);
        for (NsWriteResponse<RecordRef> writeResponse : writeResponseList) {
            assertTrue(writeResponse.getStatus().isSuccess());
            assertNotNull(writeResponse.getRef());

            refList.add(writeResponse.getRef());
        }

        // Read added records


        List<NsReadResponse<Account>> readResponseList = clientService.execute(
        new NetSuiteClientService.PortOperation<List<NsReadResponse<Account>>, NetSuitePortType>() {
            @Override public List<NsReadResponse<Account>> execute(NetSuitePortType port) throws Exception {
                GetListRequest request = new GetListRequest();
                request.getBaseRef().addAll(refList);
                return NetSuiteClientServiceImpl.toNsReadResponseList(port.getList(request).getReadResponseList());
            }
        });
        TypeDesc typeDesc = clientService.getMetaDataSource().getTypeInfo("Account");
        NsObjectInputTransducer transducer = new NsObjectInputTransducer(clientService, schema, typeDesc.getTypeName());
        List<IndexedRecord> recordList = new ArrayList<>(refList.size());
        for (NsReadResponse<Account> readResponse : readResponseList) {
            assertTrue(readResponse.getStatus().isSuccess());

            recordList.add(transducer.read(readResponse.getRecord()));
        }

        for (IndexedRecord record : recordList) {
            // Updated fields
            int pos = schema.getField("AcctName").pos();
            record.put(pos, record.get(pos) + " (edited)");
            record.put(schema.getField("Description").pos(), "qwerty");
        }

        // Update records

        NetSuiteSink sink = new NetSuiteSinkImpl();
        sink.setClientFactory(clientFactory);
        sink.initialize(CONTAINER, properties);

        NetSuiteWriteOperation writeOperation = (NetSuiteWriteOperation) sink.createWriteOperation();
        NetSuiteOutputWriter writer = (NetSuiteOutputWriter) writeOperation.createWriter(CONTAINER);
        writer.open(UUID.randomUUID().toString());

        for (IndexedRecord indexedRecord : recordList) {
            writer.write(indexedRecord);
        }

        Result writerResult = writer.close();
        assertNotNull(writerResult);
        assertEquals(recordList.size(), writerResult.totalCount);
        assertEquals(recordList.size(), writerResult.successCount);

        // Re-read updated records

        readResponseList = clientService.execute(
                new NetSuiteClientService.PortOperation<List<NsReadResponse<Account>>, NetSuitePortType>() {
                    @Override public List<NsReadResponse<Account>> execute(NetSuitePortType port) throws Exception {
                        GetListRequest request = new GetListRequest();
                        request.getBaseRef().addAll(refList);
                        return NetSuiteClientServiceImpl.toNsReadResponseList(port.getList(request).getReadResponseList());
                    }
                });
        for (NsReadResponse<Account> readResponse : readResponseList) {
            assertTrue(readResponse.getStatus().isSuccess());

            Account record = readResponse.getRecord();
            assertEquals("qwerty", record.getDescription());
            assertTrue(record.getAcctName().endsWith("(edited)"));
        }

        clientService.deleteList(refList);
    }

    @Test
    public void testUpdateWithCustomFields() throws Exception {
        clientService.getMetaDataSource().setCustomizationEnabled(true);
        NetSuiteOutputProperties properties = new NetSuiteOutputProperties("test");
        properties.init();
        properties.connection.referencedComponent.componentInstanceId.setValue(CONNECTION_COMPONENT_ID);
        properties.connection.referencedComponent.setReference(connectionProperties);

        properties.module.moduleName.setValue(RecordTypeEnum.CONTACT.getTypeName());
        properties.module.action.setValue(OutputAction.UPDATE);

        NetSuiteRuntimeImpl runtime = new NetSuiteRuntimeImpl();
        runtime.setClientFactory(clientFactory);

        NetSuiteDatasetRuntime dataSetRuntime = runtime.getDatasetRuntime(CONTAINER, properties);

        Schema schema = dataSetRuntime.getSchema(properties.module.moduleName.getValue());
        properties.module.main.schema.setValue(schema);
        SubsidiarySearch subsidiarySearch = new SubsidiarySearch();
        SubsidiarySearchBasic subsidiarySearchBasic = new SubsidiarySearchBasic();
        subsidiarySearch.setBasic(subsidiarySearchBasic);
        NsSearchResult<Subsidiary> searchResult = clientService.search(subsidiarySearch);
        assertTrue(searchResult.isSuccess());
        assertNotNull(searchResult.getRecordList());
        assertTrue(searchResult.getRecordList().size() > 0);

        Subsidiary subsidiary = searchResult.getRecordList().get(0);
        RecordRef subsidiaryRef = new RecordRef();
        subsidiaryRef.setType(RecordType.SUBSIDIARY);
        subsidiaryRef.setInternalId(subsidiary.getInternalId());

        TypeDesc typeDesc = clientService.getMetaDataSource().getTypeInfo("Contact");
        assertNotNull(typeDesc.getField("custentity_interest_bpm"));

        List<Contact> recordsToAdd = makeContactRecords(5, subsidiaryRef, typeDesc.getFieldMap());

        final List<RecordRef> refList = new ArrayList<>(recordsToAdd.size());

        // Add records

        List<NsWriteResponse<RecordRef>> writeResponseList = clientService.addList(recordsToAdd);
        for (NsWriteResponse<RecordRef> writeResponse : writeResponseList) {
            assertTrue("Add: " + writeResponse.getStatus(), writeResponse.getStatus().isSuccess());
            assertNotNull(writeResponse.getRef());

            refList.add(writeResponse.getRef());
        }

        // Read added records
        List<NsReadResponse<Contact>> readResponseList = clientService.execute(
                new NetSuiteClientService.PortOperation<List<NsReadResponse<Contact>>, NetSuitePortType>() {
                    @Override public List<NsReadResponse<Contact>> execute(NetSuitePortType port) throws Exception {
                        GetListRequest request = new GetListRequest();
                        request.getBaseRef().addAll(refList);
                        return NetSuiteClientServiceImpl.toNsReadResponseList(port.getList(request).getReadResponseList());
                    }
                });
        List<IndexedRecord> recordList = new ArrayList<>(refList.size());

        NsObjectInputTransducer transducer = new NsObjectInputTransducer(clientService, schema, typeDesc.getTypeName());

        for (NsReadResponse<Contact> readResponse : readResponseList) {
            assertTrue(readResponse.getStatus().isSuccess());

            recordList.add(transducer.read(readResponse.getRecord()));
        }

        for (IndexedRecord record : recordList) {
            record.put(schema.getField("Custentity_interest_bpm").pos(), Boolean.FALSE);
        }

        // Update records

        NetSuiteSink sink = new NetSuiteSinkImpl();
        sink.setClientFactory(clientFactory);
        sink.initialize(CONTAINER, properties);

        NetSuiteWriteOperation writeOperation = (NetSuiteWriteOperation) sink.createWriteOperation();
        NetSuiteOutputWriter writer = (NetSuiteOutputWriter) writeOperation.createWriter(CONTAINER);
        writer.open(UUID.randomUUID().toString());

        for (IndexedRecord indexedRecord : recordList) {
            writer.write(indexedRecord);
        }

        Result writerResult = writer.close();
        assertNotNull(writerResult);
        assertEquals(recordList.size(), writerResult.totalCount);
        assertEquals(recordList.size(), writerResult.successCount);

        // Re-read updated records

        readResponseList = clientService.execute(
                new NetSuiteClientService.PortOperation<List<NsReadResponse<Contact>>, NetSuitePortType>() {
                    @Override public List<NsReadResponse<Contact>> execute(NetSuitePortType port) throws Exception {
                        GetListRequest request = new GetListRequest();
                        request.getBaseRef().addAll(refList);
                        return NetSuiteClientServiceImpl.toNsReadResponseList(port.getList(request).getReadResponseList());
                    }
                });

        for (NsReadResponse<Contact> readResponse : readResponseList) {
            assertTrue(readResponse.getStatus().isSuccess());

            Contact record = readResponse.getRecord();

            CustomFieldList customFieldList = record.getCustomFieldList();
            assertNotNull(customFieldList);

            Map<String, CustomFieldRef> customFieldRefMap = new HashMap<>();
            for (CustomFieldRef fieldRef : customFieldList.getCustomField()) {
                customFieldRefMap.put(fieldRef.getScriptId(), fieldRef);
            }

            BooleanCustomFieldRef customFieldRef1 =
                    (BooleanCustomFieldRef) customFieldRefMap.get("custentity_interest_bpm");
            assertNotNull(customFieldRef1);
            assertEquals(Boolean.FALSE, customFieldRef1.getValue());
        }

        clientService.deleteList(refList);
    }

    @Test
    public void testDelete() throws Exception {
        clientService.getMetaDataSource().setCustomizationEnabled(false);
        NetSuiteOutputProperties properties = new NetSuiteOutputProperties("test");
        properties.init();
        properties.connection.referencedComponent.componentInstanceId.setValue(CONNECTION_COMPONENT_ID);
        properties.connection.referencedComponent.setReference(connectionProperties);

        properties.module.moduleName.setValue(RecordTypeEnum.MESSAGE.getTypeName());
        properties.module.action.setValue(OutputAction.DELETE);

        NetSuiteRuntimeImpl runtime = new NetSuiteRuntimeImpl();
        runtime.setClientFactory(clientFactory);

        NetSuiteDatasetRuntime dataSetRuntime = runtime.getDatasetRuntime(CONTAINER, properties);

        Schema schema = dataSetRuntime.getSchema(RefType.RECORD_REF.getTypeName());
        properties.module.main.schema.setValue(schema);
        List<Message> recordsToAdd = makeMessageRecords(5);

        final List<RecordRef> refList = new ArrayList<>(recordsToAdd.size());

        List<NsWriteResponse<RecordRef>> writeResponseList = clientService.addList(recordsToAdd);
        for (NsWriteResponse<RecordRef> writeResponse : writeResponseList) {
            assertTrue(writeResponse.getStatus().isSuccess());
            assertNotNull(writeResponse.getRef());
            refList.add(writeResponse.getRef());
        }

        List<IndexedRecord> indexedRecordList = makeRecordRefIndexedRecords(schema, refList);

        NetSuiteSink sink = new NetSuiteSinkImpl();
        sink.setClientFactory(new NetSuiteClientFactoryImpl() {
            @Override public NetSuiteClientService<NetSuitePortType> createClient() throws NetSuiteException {
                NetSuiteClientService<NetSuitePortType> service = super.createClient();
                service.getMetaDataSource().setCustomizationEnabled(webServiceTestFixture.getClientService().getMetaDataSource().isCustomizationEnabled());
                return service;
            }
        });
        sink.initialize(CONTAINER, properties);

        NetSuiteWriteOperation writeOperation = (NetSuiteWriteOperation) sink.createWriteOperation();
        NetSuiteOutputWriter writer = (NetSuiteOutputWriter) writeOperation.createWriter(CONTAINER);
        writer.open(UUID.randomUUID().toString());

        for (IndexedRecord indexedRecord : indexedRecordList) {
            writer.write(indexedRecord);
        }

        Result writerResult = writer.close();
        assertNotNull(writerResult);
        assertEquals(indexedRecordList.size(), writerResult.totalCount);
        assertEquals(indexedRecordList.size(), writerResult.successCount);

        List<NsReadResponse<Message>> readResponseList = clientService.execute(
                new NetSuiteClientService.PortOperation<List<NsReadResponse<Message>>, NetSuitePortType>() {
            @Override public List<NsReadResponse<Message>> execute(NetSuitePortType port) throws Exception {
                GetListRequest request = new GetListRequest();
                request.getBaseRef().addAll(refList);
                return NetSuiteClientServiceImpl.toNsReadResponseList(port.getList(request).getReadResponseList());
            }
        });

        for (NsReadResponse<Message> readResponse : readResponseList) {
            // success=false means that NetSuite Record was not found because it was deleted
            assertFalse(readResponse.getStatus().isSuccess());
        }
    }

    @Test
    public void testAddCustomRecord() throws Exception {
        clientService.getMetaDataSource().setCustomizationEnabled(true);
        NetSuiteOutputProperties properties = new NetSuiteOutputProperties("test");
        properties.init();
        properties.connection.referencedComponent.componentInstanceId.setValue(CONNECTION_COMPONENT_ID);
        properties.connection.referencedComponent.setReference(connectionProperties);

        properties.module.moduleName.setValue("customrecordqacomp_custom_recordtype");
        properties.module.action.setValue(OutputAction.ADD);

        NetSuiteRuntimeImpl runtime = new NetSuiteRuntimeImpl();
        runtime.setClientFactory(clientFactory);

        NetSuiteDatasetRuntime dataSetRuntime = runtime.getDatasetRuntime(CONTAINER, properties);

        Schema schema = dataSetRuntime.getSchema(properties.module.moduleName.getValue());
        Schema targetSchema = TestUtils.makeRecordSchema(schema, Arrays.asList(
                "name", "custrecord79", "custrecord80"
        ));
        properties.module.main.schema.setValue(targetSchema);

        Schema targetFlowSchema = dataSetRuntime.getSchemaForUpdateFlow(
                properties.module.moduleName.getValue(), targetSchema);
        properties.module.flowSchema.schema.setValue(targetFlowSchema);

        Schema targetRejectSchema = dataSetRuntime.getSchemaForUpdateReject(
                properties.module.moduleName.getValue(), targetSchema);
        properties.module.rejectSchema.schema.setValue(targetRejectSchema);
        GenericRecord indexedRecordToAdd = new GenericData.Record(targetSchema);

        String testId = Long.toString(System.currentTimeMillis());
        indexedRecordToAdd.put("Name", "Test Project " + testId);
        indexedRecordToAdd.put("Custrecord79", "Test Project " +  testId);
        indexedRecordToAdd.put("Custrecord80", "0.1.0");

        List<IndexedRecord> indexedRecordList = new ArrayList<>();
        indexedRecordList.add(indexedRecordToAdd);

        // Add records

        NetSuiteSink sink = new NetSuiteSinkImpl();
        sink.setClientFactory(clientFactory);
        sink.initialize(CONTAINER, properties);

        NetSuiteWriteOperation writeOperation = (NetSuiteWriteOperation) sink.createWriteOperation();
        NetSuiteOutputWriter<?, CustomRecordRef> writer = (NetSuiteOutputWriter) writeOperation.createWriter(CONTAINER);
        writer.open(UUID.randomUUID().toString());

        for (IndexedRecord indexedRecord : indexedRecordList) {
            writer.write(indexedRecord);
        }

        Result writerResult = writer.close();
        assertNotNull(writerResult);
        assertEquals(indexedRecordList.size(), writerResult.totalCount);
        assertEquals(indexedRecordList.size(), writerResult.successCount);

        final List<CustomRecordRef> refList = new ArrayList<>(indexedRecordList.size());

        for (NsWriteResponse<CustomRecordRef> response : writer.getWriteResponses()) {
            CustomRecordRef recordRef = response.getRef();
            refList.add(recordRef);
        }

        // Re-read updated records

        List<NsReadResponse<CustomRecord>> readResponseList = clientService.execute(
                new NetSuiteClientService.PortOperation<List<NsReadResponse<CustomRecord>>, NetSuitePortType>() {
                    @Override public List<NsReadResponse<CustomRecord>> execute(NetSuitePortType port) throws Exception {
                        GetListRequest request = new GetListRequest();
                        request.getBaseRef().addAll(refList);
                        return NetSuiteClientServiceImpl.toNsReadResponseList(port.getList(request).getReadResponseList());
                    }
                });
        int index = 0;
        for (NsReadResponse<CustomRecord> readResponse : readResponseList) {
            assertTrue(readResponse.getStatus().isSuccess());

            GenericRecord inputRecord = (GenericRecord) indexedRecordList.get(index);

            CustomRecord record = readResponse.getRecord();

            CustomFieldList customFieldList = record.getCustomFieldList();
            assertNotNull(customFieldList);

            Map<String, CustomFieldRef> customFieldRefMap = new HashMap<>();
            for (CustomFieldRef fieldRef : customFieldList.getCustomField()) {
                customFieldRefMap.put(fieldRef.getScriptId(), fieldRef);
            }

            StringCustomFieldRef customFieldRef1 =
                    (StringCustomFieldRef) customFieldRefMap.get("custrecord79");
            assertNotNull(customFieldRef1);
            assertEquals(inputRecord.get("Custrecord79"), customFieldRef1.getValue());

            StringCustomFieldRef customFieldRef2 =
                    (StringCustomFieldRef) customFieldRefMap.get("custrecord80");
            assertNotNull(customFieldRef2);
            assertEquals(inputRecord.get("Custrecord80"), customFieldRef2.getValue());
        }

        clientService.deleteList(refList);
    }

    private static List<Message> makeMessageRecords(int count) {
        List<Message> messageList = new ArrayList<>(count);
        for (int i = 1; i <= count; i++) {
            Message record = new Message();
            record.setSubject("Test subject " + i);
            record.setMessage("Test body text " + i);
            record.setAuthorEmail("doug@acme.com");
            record.setRecipientEmail("bob@acme.com");
            record.setIncoming(false);
            record.setEmailed(false);

            messageList.add(record);
        }
        return messageList;
    }

    private static List<Account> makeAccountRecords(int count, RecordRef subsidiary) {
        List<Account> recordList = new ArrayList<>(count);
        for (int i = 1; i <= count; i++) {
            Account record = new Account();
            String id = Long.toString(System.currentTimeMillis());
            record.setAcctName("Test account " + id);
            record.setAcctType(AccountType.OTHER_ASSET);
            record.setDescription("Test description " + i);

            RecordRefList subsidiaries = new RecordRefList();
            subsidiaries.getRecordRef().add(subsidiary);
            record.setSubsidiaryList(subsidiaries);

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {

            }

            recordList.add(record);
        }
        return recordList;
    }

    private static List<Contact> makeContactRecords(int count, RecordRef subsidiary, Map<String, FieldDesc> fieldDescMap) {
        CustomFieldDesc customFieldDesc1 = fieldDescMap.get("custentity_interest_bpm").asCustom();

        List<Contact> recordList = new ArrayList<>(count);
        for (int i = 1; i <= count; i++) {
            Contact record = new Contact();
            String id = Long.toString(System.currentTimeMillis());

            record.setFirstName("AAA " + id);
            record.setLastName("BBB " + id);
            record.setPhone(id);
            record.setEntityId(id);
            record.setSubsidiary(subsidiary);

            record.setCustomFieldList(new CustomFieldList());

            BooleanCustomFieldRef customFieldRef1 = new BooleanCustomFieldRef();
            customFieldRef1.setScriptId(customFieldDesc1.getCustomizationRef().getScriptId());
            customFieldRef1.setInternalId(customFieldDesc1.getCustomizationRef().getInternalId());
            customFieldRef1.setValue(true);

            record.getCustomFieldList().getCustomField().add(customFieldRef1);

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {

            }

            recordList.add(record);
        }
        return recordList;
    }

    private static List<IndexedRecord> makeRecordRefIndexedRecords(Schema schema, List<RecordRef> refList) {
        List<IndexedRecord> indexedRecordList = new ArrayList<>(refList.size());
        for (RecordRef ref : refList) {
            GenericRecord indexedRecord = new GenericData.Record(schema);
            indexedRecord.put("InternalId", ref.getInternalId());
            indexedRecordList.add(indexedRecord);
        }
        return indexedRecordList;
    }
}
