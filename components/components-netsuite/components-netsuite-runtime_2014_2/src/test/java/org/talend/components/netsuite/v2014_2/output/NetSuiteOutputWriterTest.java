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

package org.talend.components.netsuite.v2014_2.output;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.talend.components.netsuite.v2014_2.NetSuitePortTypeMockAdapterImpl.createSuccessStatus;

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
import org.talend.components.netsuite.v2014_2.NetSuiteMockTestBase;
import org.talend.components.netsuite.v2014_2.NetSuiteRuntimeImpl;
import org.talend.components.netsuite.v2014_2.NetSuiteSinkImpl;
import org.talend.components.netsuite.v2014_2.client.model.RecordTypeEnum;

import com.netsuite.webservices.v2014_2.platform.NetSuitePortType;
import com.netsuite.webservices.v2014_2.platform.core.RecordRef;
import com.netsuite.webservices.v2014_2.platform.core.types.RecordType;
import com.netsuite.webservices.v2014_2.platform.messages.DeleteListRequest;
import com.netsuite.webservices.v2014_2.platform.messages.DeleteListResponse;
import com.netsuite.webservices.v2014_2.platform.messages.UpdateListRequest;
import com.netsuite.webservices.v2014_2.platform.messages.UpdateListResponse;
import com.netsuite.webservices.v2014_2.platform.messages.WriteResponse;
import com.netsuite.webservices.v2014_2.platform.messages.WriteResponseList;
import com.netsuite.webservices.v2014_2.transactions.sales.Opportunity;

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

        mockGetListRequestResults(null);

        final List<Opportunity> updatedRecordList = new ArrayList<>();
        when(port.updateList(any(UpdateListRequest.class))).then(new Answer<UpdateListResponse>() {
            @Override public UpdateListResponse answer(InvocationOnMock invocationOnMock) throws Throwable {
                UpdateListRequest request = (UpdateListRequest) invocationOnMock.getArguments()[0];
                assertFalse(request.getRecord().isEmpty());

                UpdateListResponse response = new UpdateListResponse();
                WriteResponseList writeResponseList = new WriteResponseList();
                writeResponseList.setStatus(createSuccessStatus());
                for (int i = 0; i < request.getRecord().size(); i++) {
                    Opportunity record = (Opportunity) request.getRecord().get(i);
                    assertNotNull(record);
                    assertNotNull(record.getInternalId());

                    RecordRef recordRef = new RecordRef();
                    recordRef.setInternalId(record.getInternalId());
                    recordRef.setType(RecordType.OPPORTUNITY);

                    updatedRecordList.add(record);

                    WriteResponse writeResponse = new WriteResponse();
                    writeResponse.setStatus(createSuccessStatus());
                    writeResponse.setBaseRef(recordRef);

                    writeResponseList.getWriteResponse().add(writeResponse);
                }
                response.setWriteResponseList(writeResponseList);
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
                new SimpleObjectComposer<>(typeDesc.getTypeClass()), 150);

        for (IndexedRecord record : indexedRecordList) {
            writer.write(record);
        }

        Result writerResult = writer.close();
        assertNotNull(writerResult);
        assertEquals(indexedRecordList.size(), writerResult.totalCount);

        verify(port, times(2)).updateList(any(UpdateListRequest.class));
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

        final List<RecordRef> deletedRecordRefList = new ArrayList<>();
        when(port.deleteList(any(DeleteListRequest.class))).then(new Answer<DeleteListResponse>() {
            @Override public DeleteListResponse answer(InvocationOnMock invocationOnMock) throws Throwable {
                DeleteListRequest request = (DeleteListRequest) invocationOnMock.getArguments()[0];
                DeleteListResponse response = new DeleteListResponse();
                WriteResponseList writeResponseList = new WriteResponseList();
                for (int i = 0; i < request.getBaseRef().size(); i++) {
                    RecordRef recordRef = (RecordRef) request.getBaseRef().get(i);
                    assertNotNull(recordRef);
                    assertNotNull(recordRef.getInternalId());
                    assertNotNull(recordRef.getType());

                    deletedRecordRefList.add(recordRef);

                    WriteResponse writeResponse = new WriteResponse();
                    writeResponse.setStatus(createSuccessStatus());
                    writeResponse.setBaseRef(recordRef);

                    writeResponseList.getWriteResponse().add(writeResponse);
                }
                response.setWriteResponseList(writeResponseList);
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
                new RecordRefComposer<>(refTypeDesc.getTypeClass()), 150);

        for (IndexedRecord record : indexedRecordList) {
            writer.write(record);
        }

        Result writerResult = writer.close();
        assertNotNull(writerResult);
        assertEquals(indexedRecordList.size(), writerResult.totalCount);

        verify(port, times(2)).deleteList(any(DeleteListRequest.class));
        assertEquals(indexedRecordList.size(), deletedRecordRefList.size());
    }
}
