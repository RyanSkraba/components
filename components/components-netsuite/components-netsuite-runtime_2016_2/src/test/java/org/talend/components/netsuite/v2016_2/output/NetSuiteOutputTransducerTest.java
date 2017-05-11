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
import static org.junit.Assert.assertNotNull;
import static org.talend.components.netsuite.NetSuiteWebServiceMockTestFixture.assertNsObject;
import static org.talend.components.netsuite.v2016_2.MockTestHelper.makeIndexedRecords;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.IndexedRecord;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.talend.components.netsuite.CustomFieldSpec;
import org.talend.components.netsuite.NetSuiteDatasetRuntime;
import org.talend.components.netsuite.NetSuiteDatasetRuntimeImpl;
import org.talend.components.netsuite.NetSuiteRuntime;
import org.talend.components.netsuite.client.CustomMetaDataSource;
import org.talend.components.netsuite.client.EmptyCustomMetaDataSource;
import org.talend.components.netsuite.client.NetSuiteClientService;
import org.talend.components.netsuite.client.NetSuiteException;
import org.talend.components.netsuite.client.model.CustomFieldDesc;
import org.talend.components.netsuite.client.model.CustomRecordTypeInfo;
import org.talend.components.netsuite.client.model.RecordTypeInfo;
import org.talend.components.netsuite.client.model.RefType;
import org.talend.components.netsuite.client.model.TypeDesc;
import org.talend.components.netsuite.client.model.customfield.CustomFieldRefType;
import org.talend.components.netsuite.output.NsObjectOutputTransducer;
import org.talend.components.netsuite.test.TestUtils;
import org.talend.components.netsuite.v2016_2.NetSuiteMockTestBase;
import org.talend.components.netsuite.v2016_2.NetSuiteRuntimeImpl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netsuite.webservices.v2016_2.platform.NetSuitePortType;
import com.netsuite.webservices.v2016_2.platform.core.CustomRecordRef;
import com.netsuite.webservices.v2016_2.platform.core.RecordRef;
import com.netsuite.webservices.v2016_2.platform.core.types.RecordType;
import com.netsuite.webservices.v2016_2.setup.customization.CustomRecord;
import com.netsuite.webservices.v2016_2.setup.customization.TransactionBodyCustomField;
import com.netsuite.webservices.v2016_2.setup.customization.types.CustomizationFieldType;
import com.netsuite.webservices.v2016_2.transactions.sales.Opportunity;

/**
 *
 */
public class NetSuiteOutputTransducerTest extends NetSuiteMockTestBase {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    protected NetSuitePortType port;
    protected NetSuiteClientService<NetSuitePortType> clientService;

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

        port = webServiceMockTestFixture.getPortMock();
        clientService = webServiceMockTestFixture.getClientService();
    }

    @Override @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void testBasic() throws Exception {

        NetSuiteRuntime netSuiteRuntime = new NetSuiteRuntimeImpl();
        NetSuiteDatasetRuntime dataSetRuntime = netSuiteRuntime.getDatasetRuntime(mockTestFixture.getConnectionProperties());

        mockGetRequestResults(null);

        TypeDesc typeDesc = clientService.getMetaDataSource().getTypeInfo("Opportunity");

        Schema schema = dataSetRuntime.getSchema(typeDesc.getTypeName());

        NsObjectOutputTransducer transducer = new NsObjectOutputTransducer(
                webServiceMockTestFixture.getClientService(), typeDesc.getTypeName());

        List<IndexedRecord> indexedRecordList = makeIndexedRecords(clientService, schema,
                new SimpleObjectComposer<>(Opportunity.class), 10);

        for (IndexedRecord indexedRecord : indexedRecordList) {
            Opportunity record = (Opportunity) transducer.write(indexedRecord);
            assertNsObject(typeDesc, record);
        }
    }

    @Test
    public void testNonRecordObjects() throws Exception {

        NetSuiteRuntime netSuiteRuntime = new NetSuiteRuntimeImpl();
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
                    new SimpleObjectComposer<>(typeDesc.getTypeClass()), 10);

            for (IndexedRecord indexedRecord : indexedRecordList) {
                Object nsObject = transducer.write(indexedRecord);
                assertNsObject(typeDesc, nsObject);
            }
        }
    }

    @Test
    public void testRecordRef() throws Exception {

        NetSuiteRuntime netSuiteRuntime = new NetSuiteRuntimeImpl();
        NetSuiteDatasetRuntime dataSetRuntime = netSuiteRuntime.getDatasetRuntime(mockTestFixture.getConnectionProperties());

        TypeDesc typeDesc = clientService.getMetaDataSource().getTypeInfo(RefType.RECORD_REF.getTypeName());
        TypeDesc referencedTypeDesc = clientService.getMetaDataSource().getTypeInfo("Opportunity");

        Schema schema = dataSetRuntime.getSchema(typeDesc.getTypeName());

        NsObjectOutputTransducer transducer = new NsObjectOutputTransducer(webServiceMockTestFixture.getClientService(),
                referencedTypeDesc.getTypeName());
        transducer.setReference(true);

        List<IndexedRecord> indexedRecordList = makeIndexedRecords(clientService, schema,
                new SimpleObjectComposer<>(typeDesc.getTypeClass()), 10);

        for (IndexedRecord indexedRecord : indexedRecordList) {
            Object nsObject = transducer.write(indexedRecord);
            assertNsObject(typeDesc, nsObject);

            RecordRef ref = (RecordRef) nsObject;
            assertEquals(RecordType.OPPORTUNITY, ref.getType());
        }
    }

    @Test
    public void testCustomFields() throws Exception {

        NetSuiteRuntime netSuiteRuntime = new NetSuiteRuntimeImpl();
        NetSuiteDatasetRuntime dataSetRuntime = netSuiteRuntime.getDatasetRuntime(mockTestFixture.getConnectionProperties());

        mockGetRequestResults(null);

        TypeDesc basicTypeDesc = clientService.getBasicMetaData().getTypeInfo("Opportunity");

        final Map<String, CustomFieldSpec<RecordType, CustomizationFieldType>> customFieldSpecs =
                createCustomFieldSpecs();
        mockCustomizationRequestResults(customFieldSpecs);

        final List<Opportunity> recordList = makeNsObjects(
                new RecordComposer<>(Opportunity.class, customFieldSpecs), 10);
        mockSearchRequestResults(recordList, 100);

        TypeDesc customizedTypeDesc = clientService.getMetaDataSource().getTypeInfo(basicTypeDesc.getTypeName());

        Schema schema = dataSetRuntime.getSchema(customizedTypeDesc.getTypeName());

        NsObjectOutputTransducer transducer = new NsObjectOutputTransducer(
                webServiceMockTestFixture.getClientService(), basicTypeDesc.getTypeName());

        List<IndexedRecord> indexedRecordList = makeIndexedRecords(clientService, schema,
                new RecordComposer<>(Opportunity.class, customFieldSpecs), 10);

        for (IndexedRecord indexedRecord : indexedRecordList) {
            Opportunity record = (Opportunity) transducer.write(indexedRecord);
            assertNsObject(basicTypeDesc, record);
        }
    }

    @Test
    public void testCustomRecord() throws Exception {
        final CustomMetaDataSource customMetaDataSource = new TestCustomMetaDataSource();

        clientService.getMetaDataSource().setCustomMetaDataSource(customMetaDataSource);

        NetSuiteDatasetRuntime dataSetRuntime = new NetSuiteDatasetRuntimeImpl(clientService.getMetaDataSource());

        mockGetRequestResults(null);

        TypeDesc customTypeDesc = clientService.getMetaDataSource().getTypeInfo("custom_record_type_1");

        Schema schema = dataSetRuntime.getSchema(customTypeDesc.getTypeName());

        NsObjectOutputTransducer transducer = new NsObjectOutputTransducer(
                webServiceMockTestFixture.getClientService(), customTypeDesc.getTypeName());

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
        final CustomMetaDataSource customMetaDataSource = new TestCustomMetaDataSource();

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

    protected Map<String, CustomFieldSpec<RecordType, CustomizationFieldType>> createCustomFieldSpecs() {
        CustomFieldSpec<RecordType, CustomizationFieldType> customBodyField1 = new CustomFieldSpec(
                "custbody_field1", "1001",
                RecordType.TRANSACTION_BODY_CUSTOM_FIELD, TransactionBodyCustomField.class,
                CustomizationFieldType.CHECK_BOX, CustomFieldRefType.BOOLEAN,
                Arrays.asList("bodyOpportunity")
        );

        CustomFieldSpec<RecordType, CustomizationFieldType> customBodyField2 = new CustomFieldSpec(
                "custbody_field2", "1002",
                RecordType.TRANSACTION_BODY_CUSTOM_FIELD, TransactionBodyCustomField.class,
                CustomizationFieldType.FREE_FORM_TEXT, CustomFieldRefType.STRING,
                Arrays.asList("bodyOpportunity")
        );

        CustomFieldSpec<RecordType, CustomizationFieldType> customBodyField3 = new CustomFieldSpec(
                "custbody_field3", "1003",
                RecordType.TRANSACTION_BODY_CUSTOM_FIELD, TransactionBodyCustomField.class,
                CustomizationFieldType.DATETIME, CustomFieldRefType.DATE,
                Arrays.asList("bodyOpportunity")
        );

        Collection<CustomFieldSpec<RecordType, CustomizationFieldType>> specs = Arrays.asList(
                customBodyField1, customBodyField2, customBodyField3
        );
        Map<String, CustomFieldSpec<RecordType, CustomizationFieldType>> specMap = new HashMap<>();
        for (CustomFieldSpec<RecordType, CustomizationFieldType> spec : specs) {
            specMap.put(spec.getScriptId(), spec);
        }
        return specMap;
    }

    protected class TestCustomMetaDataSource extends EmptyCustomMetaDataSource {

        @Override
        public Collection<CustomRecordTypeInfo> getCustomRecordTypes() {
            return Arrays.asList(getCustomRecordType("custom_record_type_1"));
        }

        @Override
        public CustomRecordTypeInfo getCustomRecordType(String typeName) {
            try {
                if (typeName.equals("custom_record_type_1")) {
                    JsonNode recordTypeNode = objectMapper.readTree(NetSuiteOutputTransducerTest.class.getResource(
                            "/test-data/customRecord-1.json"));
                    CustomRecordTypeInfo customRecordTypeInfo =
                            TestUtils.readCustomRecord(clientService.getBasicMetaData(), recordTypeNode);
                    return customRecordTypeInfo;
                }
                return null;
            } catch (IOException e) {
                throw new NetSuiteException(e.getMessage(), e);
            }
        }

        @Override
        public Map<String, CustomFieldDesc> getCustomFields(RecordTypeInfo recordTypeInfo) {
            try {
                if (recordTypeInfo.getName().equals("custom_record_type_1")) {
                    JsonNode fieldListNode = objectMapper.readTree(NetSuiteOutputTransducerTest.class.getResource(
                            "/test-data/customRecordFields-1.json"));
                    Map<String, CustomFieldDesc> customFieldDescMap =
                            TestUtils.readCustomFields(fieldListNode);
                    return customFieldDescMap;
                }
                return null;
            } catch (IOException e) {
                throw new NetSuiteException(e.getMessage(), e);
            }
        }

    }
}
