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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.talend.components.netsuite.v2016_2.MockTestHelper.makeIndexedRecords;
import static org.talend.components.netsuite.v2016_2.NetSuitePortTypeMockAdapterImpl.createSuccessStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.netsuite.NetSuiteDatasetRuntime;
import org.talend.components.netsuite.NetSuiteRuntime;
import org.talend.components.netsuite.NetSuiteSink;
import org.talend.components.netsuite.client.NetSuiteClientService;
import org.talend.components.netsuite.client.model.RefType;
import org.talend.components.netsuite.client.model.TypeDesc;
import org.talend.components.netsuite.output.NetSuiteOutputProperties;
import org.talend.components.netsuite.output.NetSuiteOutputWriter;
import org.talend.components.netsuite.output.NetSuiteWriteOperation;
import org.talend.components.netsuite.output.OutputAction;
import org.talend.components.netsuite.v2016_2.NetSuiteMockTestBase;
import org.talend.components.netsuite.v2016_2.NetSuiteRuntimeImpl;
import org.talend.components.netsuite.v2016_2.NetSuiteSinkImpl;
import org.talend.components.netsuite.v2016_2.client.model.RecordTypeEnum;

import com.netsuite.webservices.v2016_2.platform.NetSuitePortType;
import com.netsuite.webservices.v2016_2.platform.core.RecordRef;
import com.netsuite.webservices.v2016_2.platform.core.types.RecordType;
import com.netsuite.webservices.v2016_2.platform.messages.DeleteRequest;
import com.netsuite.webservices.v2016_2.platform.messages.DeleteResponse;
import com.netsuite.webservices.v2016_2.platform.messages.UpdateRequest;
import com.netsuite.webservices.v2016_2.platform.messages.UpdateResponse;
import com.netsuite.webservices.v2016_2.platform.messages.WriteResponse;
import com.netsuite.webservices.v2016_2.transactions.sales.Opportunity;

/**
 *
 */
public class NetSuiteOutputWriterTest extends NetSuiteMockTestBase {
    protected NetSuiteOutputProperties properties;

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

        properties = new NetSuiteOutputProperties("test");
        properties.init();
        properties.connection.copyValuesFrom(mockTestFixture.getConnectionProperties());
    }

    @Override @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void testUpdate() throws Exception {
        final NetSuitePortType port = webServiceMockTestFixture.getPortMock();

        final TypeDesc typeDesc = webServiceMockTestFixture.getClientService().getMetaDataSource()
                .getTypeInfo(RecordTypeEnum.OPPORTUNITY.getTypeName());

        mockGetRequestResults(null);

        final List<Opportunity> updatedRecordList = new ArrayList<>();
        when(port.update(any(UpdateRequest.class))).then(new Answer<UpdateResponse>() {
            @Override public UpdateResponse answer(InvocationOnMock invocationOnMock) throws Throwable {
                UpdateRequest request = (UpdateRequest) invocationOnMock.getArguments()[0];
                Opportunity record = (Opportunity) request.getRecord();
                assertNotNull(record);
                assertNotNull(record.getInternalId());

                RecordRef recordRef = new RecordRef();
                recordRef.setInternalId(record.getInternalId());
                recordRef.setType(RecordType.OPPORTUNITY);

                updatedRecordList.add(record);

                UpdateResponse response = new UpdateResponse();
                WriteResponse writeResponse = new WriteResponse();
                writeResponse.setStatus(createSuccessStatus());
                writeResponse.setBaseRef(recordRef);
                response.setWriteResponse(writeResponse);
                return response;
            }
        });

        properties.module.moduleName.setValue(typeDesc.getTypeName());
        properties.module.action.setValue(OutputAction.UPDATE);

        NetSuiteRuntime netSuiteRuntime = new NetSuiteRuntimeImpl();
        NetSuiteDatasetRuntime dataSetRuntime = netSuiteRuntime.getDatasetRuntime(properties.getConnectionProperties());

        Schema schema = dataSetRuntime.getSchema(properties.module.moduleName.getValue());

        properties.module.main.schema.setValue(schema);

        NetSuiteSink sink = new NetSuiteSinkImpl();
        sink.initialize(mockTestFixture.getRuntimeContainer(), properties);

        NetSuiteClientService<?> clientService = sink.getClientService();

        NetSuiteWriteOperation writeOperation = (NetSuiteWriteOperation) sink.createWriteOperation();
        NetSuiteOutputWriter writer = (NetSuiteOutputWriter) writeOperation.createWriter(
                mockTestFixture.getRuntimeContainer());
        writer.open(UUID.randomUUID().toString());

        List<IndexedRecord> indexedRecordList = makeIndexedRecords(clientService, schema,
                new SimpleObjectComposer<>(typeDesc.getTypeClass()), 100);

        for (IndexedRecord record : indexedRecordList) {
            writer.write(record);
        }

        Result writerResult = writer.close();
        assertNotNull(writerResult);
        assertEquals(indexedRecordList.size(), writerResult.totalCount);

        verify(port, times(indexedRecordList.size())).update(any(UpdateRequest.class));
        assertEquals(indexedRecordList.size(), updatedRecordList.size());
    }

    @Test
    public void testDelete() throws Exception {
        final NetSuitePortType port = webServiceMockTestFixture.getPortMock();

        final TypeDesc typeDesc = webServiceMockTestFixture.getClientService().getMetaDataSource()
                .getTypeInfo(RecordTypeEnum.OPPORTUNITY.getTypeName());
        final TypeDesc refTypeDesc = webServiceMockTestFixture.getClientService().getMetaDataSource()
                .getTypeInfo(RefType.RECORD_REF.getTypeName());

        properties.module.moduleName.setValue(typeDesc.getTypeName());
        properties.module.action.setValue(OutputAction.DELETE);

        mockGetRequestResults(null);

        final List<RecordRef> deletedRecordRefList = new ArrayList<>();
        when(port.delete(any(DeleteRequest.class))).then(new Answer<DeleteResponse>() {
            @Override public DeleteResponse answer(InvocationOnMock invocationOnMock) throws Throwable {
                DeleteRequest request = (DeleteRequest) invocationOnMock.getArguments()[0];
                RecordRef recordRef = (RecordRef) request.getBaseRef();
                assertNotNull(recordRef);
                assertNotNull(recordRef.getInternalId());
                assertNotNull(recordRef.getType());

                deletedRecordRefList.add(recordRef);

                DeleteResponse response = new DeleteResponse();
                WriteResponse writeResponse = new WriteResponse();
                writeResponse.setStatus(createSuccessStatus());
                writeResponse.setBaseRef(recordRef);
                response.setWriteResponse(writeResponse);
                return response;
            }
        });

        NetSuiteRuntime netSuiteRuntime = new NetSuiteRuntimeImpl();
        NetSuiteDatasetRuntime dataSetRuntime = netSuiteRuntime.getDatasetRuntime(properties.getConnectionProperties());

        Schema schema = dataSetRuntime.getSchema(RefType.RECORD_REF.getTypeName());

        properties.module.main.schema.setValue(schema);

        NetSuiteSink sink = new NetSuiteSinkImpl();
        sink.initialize(mockTestFixture.getRuntimeContainer(), properties);

        NetSuiteClientService<?> clientService = sink.getClientService();

        NetSuiteWriteOperation writeOperation = (NetSuiteWriteOperation) sink.createWriteOperation();
        NetSuiteOutputWriter writer = (NetSuiteOutputWriter) writeOperation.createWriter(
                mockTestFixture.getRuntimeContainer());
        writer.open(UUID.randomUUID().toString());

        List<IndexedRecord> indexedRecordList = makeIndexedRecords(clientService, schema,
                new RecordRefComposer<>(refTypeDesc.getTypeClass()), 100);

        for (IndexedRecord record : indexedRecordList) {
            writer.write(record);
        }

        Result writerResult = writer.close();
        assertNotNull(writerResult);
        assertEquals(indexedRecordList.size(), writerResult.totalCount);

        verify(port, times(indexedRecordList.size())).delete(any(DeleteRequest.class));
        assertEquals(indexedRecordList.size(), deletedRecordRefList.size());
    }
}
