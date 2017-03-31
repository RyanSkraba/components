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

import static org.talend.components.netsuite.NetSuiteWebServiceMockTestFixture.assertIndexedRecord;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.avro.Schema;
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
import org.talend.components.netsuite.client.search.SearchResultSet;
import org.talend.components.netsuite.input.NsObjectInputTransducer;
import org.talend.components.netsuite.v2014_2.NetSuiteMockTestBase;

import com.netsuite.webservices.v2014_2.platform.core.Record;
import com.netsuite.webservices.v2014_2.platform.core.types.RecordType;
import com.netsuite.webservices.v2014_2.setup.customization.types.CustomizationFieldType;
import com.netsuite.webservices.v2014_2.transactions.sales.Opportunity;

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

        final List<Opportunity> recordList = makeNsObjects(new RecordComposer<>(Opportunity.class,
                Collections.<String, CustomFieldSpec<RecordType, CustomizationFieldType>>emptyMap()), 10);

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

        Collection<String> typeNames = Arrays.asList(RefType.RECORD_REF.getTypeName());

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
    public void testDynamicSchema() throws Exception {
        NetSuiteClientService<?> connection = webServiceMockTestFixture.getClientService();
        connection.login();

        TypeDesc basicTypeDesc = connection.getMetaDataSource().getTypeInfo("Opportunity");

        final List<Opportunity> recordList = makeNsObjects(new RecordComposer<>(Opportunity.class,
                Collections.<String, CustomFieldSpec<RecordType, CustomizationFieldType>>emptyMap()), 10);
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

}
