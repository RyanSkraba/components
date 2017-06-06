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

package org.talend.components.netsuite.v2016_2.input;

import static org.talend.components.netsuite.NetSuiteWebServiceMockTestFixture.assertIndexedRecord;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.IndexedRecord;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.talend.components.netsuite.CustomFieldSpec;
import org.talend.components.netsuite.NetSuiteDatasetRuntimeImpl;
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
import org.talend.components.netsuite.client.search.SearchResultSet;
import org.talend.components.netsuite.input.NsObjectInputTransducer;
import org.talend.components.netsuite.test.TestUtils;
import org.talend.components.netsuite.v2016_2.NetSuiteMockTestBase;
import org.talend.components.netsuite.v2016_2.output.NetSuiteOutputTransducerTest;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.di.DiSchemaConstants;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netsuite.webservices.v2016_2.platform.NetSuitePortType;
import com.netsuite.webservices.v2016_2.platform.core.Record;
import com.netsuite.webservices.v2016_2.platform.core.types.RecordType;
import com.netsuite.webservices.v2016_2.setup.customization.CustomRecord;
import com.netsuite.webservices.v2016_2.setup.customization.TransactionBodyCustomField;
import com.netsuite.webservices.v2016_2.setup.customization.types.CustomizationFieldType;
import com.netsuite.webservices.v2016_2.transactions.bank.Check;
import com.netsuite.webservices.v2016_2.transactions.sales.Opportunity;

/**
 *
 */
public class NsObjectInputTransducerTest extends NetSuiteMockTestBase {

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

        TypeDesc typeDesc = clientService.getMetaDataSource().getTypeInfo("Opportunity");

        final Map<String, CustomFieldSpec<RecordType, CustomizationFieldType>> customFieldSpecs =
                createCustomFieldSpecs();
        mockCustomizationRequestResults(customFieldSpecs);

        final List<Opportunity> recordList = makeNsObjects(
                new RecordComposer<>(Opportunity.class, customFieldSpecs), 10);

        Schema schema = NetSuiteDatasetRuntimeImpl.inferSchemaForType(typeDesc.getTypeName(), typeDesc.getFields());

        NsObjectInputTransducer transducer = new NsObjectInputTransducer(clientService, schema, typeDesc.getTypeName());

        for (Record record : recordList) {
            IndexedRecord indexedRecord = transducer.read(record);
            assertIndexedRecord(typeDesc, indexedRecord);
        }
    }

    @Test
    public void testNonRecordObjects() throws Exception {

        Collection<String> typeNames = Arrays.asList(
                RefType.RECORD_REF.getTypeName(),
                RefType.CUSTOMIZATION_REF.getTypeName()
        );

        for (String typeName : typeNames) {
            TypeDesc typeDesc = clientService.getMetaDataSource().getTypeInfo(typeName);

            final List<?> nsObjects = makeNsObjects(new SimpleObjectComposer<>(typeDesc.getTypeClass()), 10);

            Schema schema = NetSuiteDatasetRuntimeImpl.inferSchemaForType(typeDesc.getTypeName(), typeDesc.getFields());

            NsObjectInputTransducer transducer = new NsObjectInputTransducer(clientService, schema, typeDesc.getTypeName());

            for (Object record : nsObjects) {
                IndexedRecord indexedRecord = transducer.read(record);
                assertIndexedRecord(typeDesc, indexedRecord);
            }
        }
    }

    @Test
    public void testDynamicSchemaWithCustomFields() throws Exception {

        TypeDesc basicTypeDesc = clientService.getMetaDataSource().getTypeInfo("Opportunity");

        final Map<String, CustomFieldSpec<RecordType, CustomizationFieldType>> customFieldSpecs =
                createCustomFieldSpecs();
        mockCustomizationRequestResults(customFieldSpecs);

        final List<Opportunity> recordList = makeNsObjects(
                new RecordComposer<>(Opportunity.class, customFieldSpecs), 10);
        mockSearchRequestResults(recordList, 100);

        TypeDesc typeDesc = clientService.getMetaDataSource().getTypeInfo(basicTypeDesc.getTypeName());

        Schema schema = getDynamicSchema();

        NsObjectInputTransducer transducer = new NsObjectInputTransducer(clientService, schema, typeDesc.getTypeName());

        SearchResultSet<Record> rs = clientService.newSearch()
                .target(basicTypeDesc.getTypeName())
                .search();

        while (rs.next()) {
            Record record = rs.get();

            IndexedRecord indexedRecord = transducer.read(record);
            assertIndexedRecord(typeDesc, indexedRecord);
        }
    }

    @Test
    public void testDynamicSchemaWithDynamicColumnMiddle() throws Exception {

        TypeDesc basicTypeDesc = clientService.getMetaDataSource().getTypeInfo("Check");

        final List<Check> recordList = makeNsObjects(
                new SimpleObjectComposer<>(Check.class), 10);
        mockSearchRequestResults(recordList, 100);

        TypeDesc typeDesc = clientService.getMetaDataSource().getTypeInfo(basicTypeDesc.getTypeName());

        Schema designSchema = SchemaBuilder.record(typeDesc.getTypeName())
                .fields()
                // Field 1
                .name("InternalId")
                .type(NetSuiteDatasetRuntimeImpl.inferSchemaForField(typeDesc.getField("internalId")))
                .noDefault()
                // Field 2
                .name("TranId")
                .type(NetSuiteDatasetRuntimeImpl.inferSchemaForField(typeDesc.getField("tranId")))
                .noDefault()
                // Field 3
                .name("LastModifiedDate")
                .type(NetSuiteDatasetRuntimeImpl.inferSchemaForField(typeDesc.getField("lastModifiedDate")))
                .noDefault()
                //
                .endRecord();
        designSchema.addProp(DiSchemaConstants.TALEND6_DYNAMIC_COLUMN_POSITION, "1");
        designSchema.addProp(DiSchemaConstants.TALEND6_DYNAMIC_COLUMN_ID, "dynamic");

        Schema schema = AvroUtils.setIncludeAllFields(designSchema, true);

        NsObjectInputTransducer transducer = new NsObjectInputTransducer(clientService, schema, typeDesc.getTypeName());

        SearchResultSet<Record> rs = clientService.newSearch()
                .target(basicTypeDesc.getTypeName())
                .search();

        while (rs.next()) {
            Record record = rs.get();

            IndexedRecord indexedRecord = transducer.read(record);
            assertIndexedRecord(typeDesc, indexedRecord);
        }
    }

    @Test
    public void testDynamicSchemaWithDynamicColumnLast() throws Exception {

        TypeDesc basicTypeDesc = clientService.getMetaDataSource().getTypeInfo("Check");

        final List<Check> recordList = makeNsObjects(
                new SimpleObjectComposer<>(Check.class), 10);
        mockSearchRequestResults(recordList, 100);

        TypeDesc typeDesc = clientService.getMetaDataSource().getTypeInfo(basicTypeDesc.getTypeName());

        Schema designSchema = SchemaBuilder.record(typeDesc.getTypeName())
                .fields()
                // Field 1
                .name("InternalId")
                .type(NetSuiteDatasetRuntimeImpl.inferSchemaForField(typeDesc.getField("internalId")))
                .noDefault()
                // Field 2
                .name("TranId")
                .type(NetSuiteDatasetRuntimeImpl.inferSchemaForField(typeDesc.getField("tranId")))
                .noDefault()
                // Field 3
                .name("LastModifiedDate")
                .type(NetSuiteDatasetRuntimeImpl.inferSchemaForField(typeDesc.getField("lastModifiedDate")))
                .noDefault()
                //
                .endRecord();
        designSchema.addProp(DiSchemaConstants.TALEND6_DYNAMIC_COLUMN_POSITION, "3");
        designSchema.addProp(DiSchemaConstants.TALEND6_DYNAMIC_COLUMN_ID, "dynamic");

        Schema schema = AvroUtils.setIncludeAllFields(designSchema, true);

        NsObjectInputTransducer transducer = new NsObjectInputTransducer(clientService, schema, typeDesc.getTypeName());

        SearchResultSet<Record> rs = clientService.newSearch()
                .target(basicTypeDesc.getTypeName())
                .search();

        while (rs.next()) {
            Record record = rs.get();

            IndexedRecord indexedRecord = transducer.read(record);
            assertIndexedRecord(typeDesc, indexedRecord);
        }
    }

    @Test
    public void testDynamicSchemaWithCustomRecordType() throws Exception {
        final CustomMetaDataSource customMetaDataSource = new TestCustomMetaDataSource();

        clientService.getMetaDataSource().setCustomMetaDataSource(customMetaDataSource);

        TypeDesc typeDesc = clientService.getMetaDataSource().getTypeInfo("custom_record_type_1");

        final List<CustomRecord> recordList = makeNsObjects(
                new SimpleRecordComposer<CustomRecord>(clientService.getMetaDataSource(), typeDesc), 10);

        mockSearchRequestResults(recordList, 100);

        Schema schema = getDynamicSchema();

        NsObjectInputTransducer transducer = new NsObjectInputTransducer(clientService, schema, typeDesc.getTypeName());

        SearchResultSet<CustomRecord> rs = clientService.newSearch()
                .target(typeDesc.getTypeName())
                .search();

        while (rs.next()) {
            CustomRecord record = rs.get();

            IndexedRecord indexedRecord = transducer.read(record);
            assertIndexedRecord(typeDesc, indexedRecord);
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
