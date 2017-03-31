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
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
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
import org.talend.components.netsuite.output.NetSuiteOutputProperties;
import org.talend.components.netsuite.output.NetSuiteOutputWriter;
import org.talend.components.netsuite.output.NetSuiteWriteOperation;
import org.talend.components.netsuite.output.OutputAction;
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
import com.netsuite.webservices.v2016_2.platform.core.RecordRef;
import com.netsuite.webservices.v2016_2.platform.core.RecordRefList;
import com.netsuite.webservices.v2016_2.platform.core.types.RecordType;
import com.netsuite.webservices.v2016_2.platform.messages.GetListRequest;

/**
 *
 */
public class NetSuiteOutputWriterIT extends AbstractNetSuiteTestBase {
    private static NetSuiteWebServiceTestFixture webServiceTestFixture;

    private final NetSuiteClientFactory<NetSuitePortType> clientFactory = new NetSuiteClientFactoryImpl() {
        @Override public NetSuiteClientService<NetSuitePortType> createClient() throws NetSuiteException {
            NetSuiteClientService<NetSuitePortType> service = super.createClient();
            service.getMetaDataSource().setCustomizationEnabled(webServiceTestFixture.getClientService().getMetaDataSource().isCustomizationEnabled());
            return service;
        }
    };

    @BeforeClass
    public static void classSetUp() throws Exception {
        webServiceTestFixture = new NetSuiteWebServiceTestFixture(
                NetSuiteClientFactoryImpl.INSTANCE, "2016_2");
        classScopedTestFixtures.add(webServiceTestFixture);
        setUpClassScopedTestFixtures();
    }

    @AfterClass
    public static void classTearDown() throws Exception {
        tearDownClassScopedTestFixtures();
    }

    @Test
    public void testUpdate() throws Exception {
        final NetSuiteClientService<NetSuitePortType> clientService = webServiceTestFixture.getClientService();
        clientService.getMetaDataSource().setCustomizationEnabled(false);

        RuntimeContainer container = mock(RuntimeContainer.class);

        NetSuiteOutputProperties properties = new NetSuiteOutputProperties("test");
        properties.init();
        properties.connection.endpoint.setValue(webServiceTestFixture.getEndpointUrl());
        properties.connection.email.setValue(webServiceTestFixture.getCredentials().getEmail());
        properties.connection.password.setValue(webServiceTestFixture.getCredentials().getPassword());
        properties.connection.account.setValue(webServiceTestFixture.getCredentials().getAccount());
        properties.connection.role.setValue(Integer.valueOf(webServiceTestFixture.getCredentials().getRoleId()));
        properties.connection.applicationId.setValue(webServiceTestFixture.getCredentials().getApplicationId());

        properties.module.moduleName.setValue(RecordTypeEnum.ACCOUNT.getTypeName());
        properties.module.action.setValue(OutputAction.UPDATE);

        NetSuiteRuntimeImpl runtime = new NetSuiteRuntimeImpl();
        runtime.setClientFactory(clientFactory);

        NetSuiteDatasetRuntime dataSetRuntime = runtime.getDatasetRuntime(properties.getConnectionProperties());

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

        List<Account> recordList = new ArrayList<>(refList.size());

        List<NsReadResponse<Account>> readResponseList = clientService.execute(
        new NetSuiteClientService.PortOperation<List<NsReadResponse<Account>>, NetSuitePortType>() {
            @Override public List<NsReadResponse<Account>> execute(NetSuitePortType port) throws Exception {
                GetListRequest request = new GetListRequest();
                request.getBaseRef().addAll(refList);
                return NetSuiteClientServiceImpl.toNsReadResponseList(port.getList(request).getReadResponseList());
            }
        });
        for (NsReadResponse<Account> readResponse : readResponseList) {
            assertTrue(readResponse.getStatus().isSuccess());

            recordList.add(readResponse.getRecord());
        }

        List<IndexedRecord> indexedRecordList = new ArrayList<>(refList.size());
        for (Account record : recordList) {
            GenericRecord indexedRecord = new GenericData.Record(schema);

            indexedRecord.put("InternalId", record.getInternalId());
            indexedRecord.put("AcctNumber", record.getAcctNumber());
            indexedRecord.put("AcctType", record.getAcctType().value());

            // Updated fields
            indexedRecord.put("AcctName", record.getAcctName() + " (edited)");
            indexedRecord.put("Description", "qwerty");

            indexedRecordList.add(indexedRecord);
        }

        // Update records

        NetSuiteSink sink = new NetSuiteSinkImpl();
        sink.setClientFactory(clientFactory);
        sink.initialize(container, properties);

        NetSuiteWriteOperation writeOperation = (NetSuiteWriteOperation) sink.createWriteOperation();
        NetSuiteOutputWriter writer = (NetSuiteOutputWriter) writeOperation.createWriter(container);
        writer.open(UUID.randomUUID().toString());

        for (IndexedRecord indexedRecord : indexedRecordList) {
            writer.write(indexedRecord);
        }

        Result writerResult = writer.close();
        assertNotNull(writerResult);
        assertEquals(indexedRecordList.size(), writerResult.totalCount);
        assertEquals(indexedRecordList.size(), writerResult.successCount);

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
        final NetSuiteClientService<NetSuitePortType> clientService = webServiceTestFixture.getClientService();

        RuntimeContainer container = mock(RuntimeContainer.class);

        NetSuiteOutputProperties properties = new NetSuiteOutputProperties("test");
        properties.init();
        properties.connection.endpoint.setValue(webServiceTestFixture.getEndpointUrl());
        properties.connection.email.setValue(webServiceTestFixture.getCredentials().getEmail());
        properties.connection.password.setValue(webServiceTestFixture.getCredentials().getPassword());
        properties.connection.account.setValue(webServiceTestFixture.getCredentials().getAccount());
        properties.connection.role.setValue(Integer.valueOf(webServiceTestFixture.getCredentials().getRoleId()));
        properties.connection.applicationId.setValue(webServiceTestFixture.getCredentials().getApplicationId());

        properties.module.moduleName.setValue(RecordTypeEnum.CONTACT.getTypeName());
        properties.module.action.setValue(OutputAction.UPDATE);

        NetSuiteRuntimeImpl runtime = new NetSuiteRuntimeImpl();
        runtime.setClientFactory(clientFactory);

        NetSuiteDatasetRuntime dataSetRuntime = runtime.getDatasetRuntime(properties.getConnectionProperties());

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

        List<Contact> recordList = new ArrayList<>(refList.size());

        List<NsReadResponse<Contact>> readResponseList = clientService.execute(
                new NetSuiteClientService.PortOperation<List<NsReadResponse<Contact>>, NetSuitePortType>() {
                    @Override public List<NsReadResponse<Contact>> execute(NetSuitePortType port) throws Exception {
                        GetListRequest request = new GetListRequest();
                        request.getBaseRef().addAll(refList);
                        return NetSuiteClientServiceImpl.toNsReadResponseList(port.getList(request).getReadResponseList());
                    }
                });
        for (NsReadResponse<Contact> readResponse : readResponseList) {
            assertTrue(readResponse.getStatus().isSuccess());

            recordList.add(readResponse.getRecord());
        }

        List<IndexedRecord> indexedRecordList = new ArrayList<>(refList.size());
        for (Contact record : recordList) {
            GenericRecord indexedRecord = new GenericData.Record(schema);

            indexedRecord.put("InternalId", record.getInternalId());

            // Updated fields

            indexedRecord.put("custentity_interest_bpm", Boolean.FALSE);

            indexedRecordList.add(indexedRecord);
        }

        // Update records

        NetSuiteSink sink = new NetSuiteSinkImpl();
        sink.setClientFactory(clientFactory);
        sink.initialize(container, properties);

        NetSuiteWriteOperation writeOperation = (NetSuiteWriteOperation) sink.createWriteOperation();
        NetSuiteOutputWriter writer = (NetSuiteOutputWriter) writeOperation.createWriter(container);
        writer.open(UUID.randomUUID().toString());

        for (IndexedRecord indexedRecord : indexedRecordList) {
            writer.write(indexedRecord);
        }

        Result writerResult = writer.close();
        assertNotNull(writerResult);
        assertEquals(indexedRecordList.size(), writerResult.totalCount);
        assertEquals(indexedRecordList.size(), writerResult.successCount);

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
        NetSuiteClientService<NetSuitePortType> clientService = webServiceTestFixture.getClientService();
        clientService.getMetaDataSource().setCustomizationEnabled(false);

        RuntimeContainer container = mock(RuntimeContainer.class);

        NetSuiteOutputProperties properties = new NetSuiteOutputProperties("test");
        properties.init();
        properties.connection.endpoint.setValue(webServiceTestFixture.getEndpointUrl());
        properties.connection.email.setValue(webServiceTestFixture.getCredentials().getEmail());
        properties.connection.password.setValue(webServiceTestFixture.getCredentials().getPassword());
        properties.connection.account.setValue(webServiceTestFixture.getCredentials().getAccount());
        properties.connection.role.setValue(Integer.valueOf(webServiceTestFixture.getCredentials().getRoleId()));
        properties.connection.applicationId.setValue(webServiceTestFixture.getCredentials().getApplicationId());

        properties.module.moduleName.setValue(RecordTypeEnum.MESSAGE.getTypeName());
        properties.module.action.setValue(OutputAction.DELETE);

        NetSuiteRuntimeImpl runtime = new NetSuiteRuntimeImpl();
        runtime.setClientFactory(clientFactory);

        NetSuiteDatasetRuntime dataSetRuntime = runtime.getDatasetRuntime(properties.getConnectionProperties());

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
        sink.initialize(container, properties);

        NetSuiteWriteOperation writeOperation = (NetSuiteWriteOperation) sink.createWriteOperation();
        NetSuiteOutputWriter writer = (NetSuiteOutputWriter) writeOperation.createWriter(container);
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
            record.setAcctNumber(id);
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
            customFieldRef1.setScriptId(customFieldDesc1.getRef().getScriptId());
            customFieldRef1.setInternalId(customFieldDesc1.getRef().getInternalId());
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
