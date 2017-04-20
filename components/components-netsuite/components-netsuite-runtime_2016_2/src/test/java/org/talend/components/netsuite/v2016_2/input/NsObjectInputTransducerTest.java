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
import org.talend.components.netsuite.client.NetSuiteClientService;
import org.talend.components.netsuite.client.model.RefType;
import org.talend.components.netsuite.client.model.TypeDesc;
import org.talend.components.netsuite.client.model.customfield.CustomFieldRefType;
import org.talend.components.netsuite.client.search.SearchResultSet;
import org.talend.components.netsuite.input.NsObjectInputTransducer;
import org.talend.components.netsuite.v2016_2.NetSuiteMockTestBase;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.di.DiSchemaConstants;

import com.netsuite.webservices.v2016_2.platform.core.Record;
import com.netsuite.webservices.v2016_2.platform.core.types.RecordType;
import com.netsuite.webservices.v2016_2.setup.customization.TransactionBodyCustomField;
import com.netsuite.webservices.v2016_2.setup.customization.types.CustomizationFieldType;
import com.netsuite.webservices.v2016_2.transactions.bank.Check;
import com.netsuite.webservices.v2016_2.transactions.sales.Opportunity;

/**
 *
 */
public class NsObjectInputTransducerTest extends NetSuiteMockTestBase {

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
    }

    @Override @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void testBasic() throws Exception {
        NetSuiteClientService<?> connection = webServiceMockTestFixture.getClientService();
        connection.login();

        TypeDesc typeDesc = connection.getMetaDataSource().getTypeInfo("Opportunity");

        final Map<String, CustomFieldSpec<RecordType, CustomizationFieldType>> customFieldSpecs =
                createCustomFieldSpecs();
        mockCustomizationRequestResults(customFieldSpecs);

        final List<Opportunity> recordList = makeNsObjects(
                new RecordComposer<>(Opportunity.class, customFieldSpecs), 10);

        Schema schema = NetSuiteDatasetRuntimeImpl.inferSchemaForType(typeDesc.getTypeName(), typeDesc.getFields());

        NsObjectInputTransducer transducer = new NsObjectInputTransducer(connection, schema, typeDesc.getTypeName());

        for (Record record : recordList) {
            IndexedRecord indexedRecord = transducer.read(record);
            assertIndexedRecord(typeDesc, indexedRecord);
        }
    }

    @Test
    public void testNonRecordObjects() throws Exception {
        NetSuiteClientService<?> connection = webServiceMockTestFixture.getClientService();
        connection.login();

        Collection<String> typeNames = Arrays.asList(
                RefType.RECORD_REF.getTypeName(),
                RefType.CUSTOMIZATION_REF.getTypeName()
        );

        for (String typeName : typeNames) {
            TypeDesc typeDesc = connection.getMetaDataSource().getTypeInfo(typeName);

            final List<?> nsObjects = makeNsObjects(new SimpleObjectComposer<>(typeDesc.getTypeClass()), 10);

            Schema schema = NetSuiteDatasetRuntimeImpl.inferSchemaForType(typeDesc.getTypeName(), typeDesc.getFields());

            NsObjectInputTransducer transducer = new NsObjectInputTransducer(connection, schema, typeDesc.getTypeName());

            for (Object record : nsObjects) {
                IndexedRecord indexedRecord = transducer.read(record);
                assertIndexedRecord(typeDesc, indexedRecord);
            }
        }
    }

    @Test
    public void testDynamicSchemaWithCustomFields() throws Exception {
        NetSuiteClientService<?> connection = webServiceMockTestFixture.getClientService();
        connection.login();

        TypeDesc basicTypeDesc = connection.getMetaDataSource().getTypeInfo("Opportunity");

        final Map<String, CustomFieldSpec<RecordType, CustomizationFieldType>> customFieldSpecs =
                createCustomFieldSpecs();
        mockCustomizationRequestResults(customFieldSpecs);

        final List<Opportunity> recordList = makeNsObjects(
                new RecordComposer<>(Opportunity.class, customFieldSpecs), 10);
        mockSearchRequestResults(recordList, 100);

        TypeDesc typeDesc = connection.getMetaDataSource().getTypeInfo(basicTypeDesc.getTypeName());

        Schema schema = getDynamicSchema();

        NsObjectInputTransducer transducer = new NsObjectInputTransducer(connection, schema, typeDesc.getTypeName());

        SearchResultSet<Record> rs = connection.newSearch()
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
        NetSuiteClientService<?> connection = webServiceMockTestFixture.getClientService();
        connection.login();

        TypeDesc basicTypeDesc = connection.getMetaDataSource().getTypeInfo("Check");

        final List<Check> recordList = makeNsObjects(
                new SimpleObjectComposer<>(Check.class), 10);
        mockSearchRequestResults(recordList, 100);

        TypeDesc typeDesc = connection.getMetaDataSource().getTypeInfo(basicTypeDesc.getTypeName());

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

        NsObjectInputTransducer transducer = new NsObjectInputTransducer(connection, schema, typeDesc.getTypeName());

        SearchResultSet<Record> rs = connection.newSearch()
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
        NetSuiteClientService<?> connection = webServiceMockTestFixture.getClientService();
        connection.login();

        TypeDesc basicTypeDesc = connection.getMetaDataSource().getTypeInfo("Check");

        final List<Check> recordList = makeNsObjects(
                new SimpleObjectComposer<>(Check.class), 10);
        mockSearchRequestResults(recordList, 100);

        TypeDesc typeDesc = connection.getMetaDataSource().getTypeInfo(basicTypeDesc.getTypeName());

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

        NsObjectInputTransducer transducer = new NsObjectInputTransducer(connection, schema, typeDesc.getTypeName());

        SearchResultSet<Record> rs = connection.newSearch()
                .target(basicTypeDesc.getTypeName())
                .search();

        while (rs.next()) {
            Record record = rs.get();

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

}
