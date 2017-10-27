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

package org.talend.components.netsuite.input;

import static org.talend.components.netsuite.NetSuiteWebServiceMockTestFixture.assertIndexedRecord;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.IndexedRecord;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertNull;
import org.talend.components.netsuite.NetSuiteDatasetRuntimeImpl;
import org.talend.components.netsuite.NetSuiteMockTestBase;
import org.talend.components.netsuite.client.CustomMetaDataSource;
import org.talend.components.netsuite.client.NetSuiteClientService;
import org.talend.components.netsuite.client.model.RefType;
import org.talend.components.netsuite.client.model.TypeDesc;
import org.talend.components.netsuite.client.search.SearchResultSet;
import org.talend.daikon.avro.AvroUtils;
import org.talend.components.netsuite.NetSuiteSchemaConstants;

import com.netsuite.webservices.test.platform.NetSuitePortType;
import com.netsuite.webservices.test.platform.core.Record;
import com.netsuite.webservices.test.setup.customization.CustomRecord;
import com.netsuite.webservices.test.transactions.bank.Check;
import com.netsuite.webservices.test.transactions.sales.Opportunity;

/**
 *
 */
public class NsObjectInputTransducerTest extends NetSuiteMockTestBase {

    protected NetSuitePortType port;
    protected NetSuiteClientService<NetSuitePortType> clientService;

    @Override
    @Before
    public void setUp() throws Exception {
        installWebServiceMockTestFixture();

        super.setUp();

        port = webServiceMockTestFixture.getPortMock();
        clientService = webServiceMockTestFixture.getClientService();

        mockLoginResponse(port);
    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void testBasic() throws Exception {

        TypeDesc typeDesc = clientService.getMetaDataSource().getTypeInfo("Opportunity");

        final List<Opportunity> recordList = makeNsObjects(
                new NsObjectComposer<Opportunity>(clientService.getMetaDataSource(), typeDesc), 10);
        mockSearchRequestResults(recordList, 100);

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

            final List<?> nsObjects = makeNsObjects(
                    new NsObjectComposer<>(clientService.getMetaDataSource(), typeDesc), 10);

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

        TestCustomMetaDataSource customMetaDataSource = new TestCustomMetaDataSource(clientService, "Opportunity");
        clientService.getMetaDataSource().setCustomMetaDataSource(customMetaDataSource);

        TypeDesc basicTypeDesc = clientService.getMetaDataSource().getTypeInfo("Opportunity");

        final List<Opportunity> recordList = makeNsObjects(
                new NsObjectComposer<Opportunity>(clientService.getMetaDataSource(), basicTypeDesc), 10);
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
                new NsObjectComposer<Check>(clientService.getMetaDataSource(), basicTypeDesc), 10);
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
        designSchema.addProp(NetSuiteSchemaConstants.TALEND6_DYNAMIC_COLUMN_POSITION, "1");
        designSchema.addProp(NetSuiteSchemaConstants.TALEND6_DYNAMIC_COLUMN_ID, "dynamic");

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
                new NsObjectComposer<Check>(clientService.getMetaDataSource(), basicTypeDesc), 10);
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
        designSchema.addProp(NetSuiteSchemaConstants.TALEND6_DYNAMIC_COLUMN_POSITION, "3");
        designSchema.addProp(NetSuiteSchemaConstants.TALEND6_DYNAMIC_COLUMN_ID, "dynamic");

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

        CustomMetaDataSource customMetaDataSource = new TestCustomMetaDataSource(clientService);
        clientService.getMetaDataSource().setCustomMetaDataSource(customMetaDataSource);

        TypeDesc typeDesc = clientService.getMetaDataSource().getTypeInfo("custom_record_type_1");

        final List<CustomRecord> recordList = makeNsObjects(
                new NsObjectComposer<CustomRecord>(clientService.getMetaDataSource(), typeDesc), 10);
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
    
    @Test
    public void testGetApiVersion() throws Exception {
        
        TypeDesc typeDesc = clientService.getMetaDataSource().getTypeInfo("Opportunity");

        Schema schema = NetSuiteDatasetRuntimeImpl.inferSchemaForType(typeDesc.getTypeName(), typeDesc.getFields());

        NsObjectInputTransducer transducer = new NsObjectInputTransducer(clientService, schema, typeDesc.getTypeName());
        
        transducer.setApiVersion("2016.2");
        assertNull(transducer.getPicklistClass());
        
       
        
    }
}
