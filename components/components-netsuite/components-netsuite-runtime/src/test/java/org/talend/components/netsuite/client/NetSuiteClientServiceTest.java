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

package org.talend.components.netsuite.client;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.talend.components.netsuite.NetSuiteMockTestBase;
import org.talend.components.netsuite.client.model.TypeDesc;
import org.talend.components.netsuite.test.AssertMatcher;
import org.talend.components.netsuite.test.NetSuitePortTypeMockAdapterImpl;
import org.talend.components.netsuite.test.client.TestNetSuiteClientService;

import com.netsuite.webservices.test.platform.InvalidCredentialsFault;
import com.netsuite.webservices.test.platform.NetSuitePortType;
import com.netsuite.webservices.test.platform.UnexpectedErrorFault;
import com.netsuite.webservices.test.platform.core.Passport;
import com.netsuite.webservices.test.platform.core.Record;
import com.netsuite.webservices.test.platform.core.RecordRef;
import com.netsuite.webservices.test.platform.core.Status;
import com.netsuite.webservices.test.platform.InvalidSessionFault;
import com.netsuite.webservices.test.platform.faults.types.FaultCodeType;
import com.netsuite.webservices.test.platform.faults.types.StatusDetailCodeType;
import com.netsuite.webservices.test.platform.messages.AddListRequest;
import com.netsuite.webservices.test.platform.messages.AddListResponse;
import com.netsuite.webservices.test.platform.messages.AddRequest;
import com.netsuite.webservices.test.platform.messages.AddResponse;
import com.netsuite.webservices.test.platform.messages.DeleteListRequest;
import com.netsuite.webservices.test.platform.messages.DeleteListResponse;
import com.netsuite.webservices.test.platform.messages.DeleteRequest;
import com.netsuite.webservices.test.platform.messages.DeleteResponse;
import com.netsuite.webservices.test.platform.messages.GetListRequest;
import com.netsuite.webservices.test.platform.messages.GetListResponse;
import com.netsuite.webservices.test.platform.messages.GetRequest;
import com.netsuite.webservices.test.platform.messages.GetResponse;
import com.netsuite.webservices.test.platform.messages.LoginRequest;
import com.netsuite.webservices.test.platform.messages.LoginResponse;
import com.netsuite.webservices.test.platform.messages.ReadResponse;
import com.netsuite.webservices.test.platform.messages.ReadResponseList;
import com.netsuite.webservices.test.platform.messages.SessionResponse;
import com.netsuite.webservices.test.platform.messages.UpdateListRequest;
import com.netsuite.webservices.test.platform.messages.UpdateListResponse;
import com.netsuite.webservices.test.platform.messages.UpdateRequest;
import com.netsuite.webservices.test.platform.messages.UpdateResponse;
import com.netsuite.webservices.test.platform.messages.UpsertListRequest;
import com.netsuite.webservices.test.platform.messages.UpsertListResponse;
import com.netsuite.webservices.test.platform.messages.UpsertRequest;
import com.netsuite.webservices.test.platform.messages.UpsertResponse;
import com.netsuite.webservices.test.platform.messages.WriteResponse;
import com.netsuite.webservices.test.platform.messages.WriteResponseList;
import com.netsuite.webservices.test.transactions.purchases.PurchaseOrder;

/**
 * Performs testing of {@link NetSuiteClientService}'s basic functionality.
 */
public class NetSuiteClientServiceTest extends NetSuiteMockTestBase {

    private NetSuitePortType port;

    private NetSuiteClientService<NetSuitePortType> clientService;

    private NetSuiteCredentials credentials;

    @Override
    @Before
    public void setUp() throws Exception {
        installWebServiceMockTestFixture();

        super.setUp();

        port = webServiceMockTestFixture.getPortMock();

        clientService = webServiceMockTestFixture.getClientService();

        credentials = new NetSuiteCredentials();
        credentials.setEmail("test@test.com");
        credentials.setPassword("secret123");
        credentials.setAccount("12345");
        credentials.setRoleId("3");
        credentials.setApplicationId("00000000-0000-0000-0000-000000000000");
        clientService.setCredentials(credentials);

        mockLoginResponse(port);
    }

    @Test
    public void testLogin() throws Exception {
        clientService.login();

        verify(port, times(1)).login(argThat(new AssertMatcher<LoginRequest>() {

            @Override
            protected void doAssert(LoginRequest request) throws Exception {
                assertNotNull(request);

                Passport passport = request.getPassport();
                assertNotNull(passport);
            }
        }));

        // Verify that logging in not performed for already logged in client

        TypeDesc typeDesc = clientService.getMetaDataSource().getTypeInfo("RecordRef");

        RecordRef recordRef = new NsObjectComposer<RecordRef>(clientService.getMetaDataSource(), typeDesc)
                .composeObject();

        DeleteResponse response = new DeleteResponse();
        response.setWriteResponse(createSuccessWriteResponse());
        when(port.delete(notNull(DeleteRequest.class))).thenReturn(response);

        clientService.delete(recordRef);

        verify(port, times(1)).login(any(LoginRequest.class));
    }

    @Test(expected = NetSuiteException.class)
    public void testLoginInvalidCredentials() throws Exception {
        SessionResponse sessionResponse = new SessionResponse();
        sessionResponse.setStatus(NetSuitePortTypeMockAdapterImpl.createErrorStatus(
                StatusDetailCodeType.INVALID_LOGIN_CREDENTIALS, "Invalid credentials"));
        LoginResponse response = new LoginResponse();
        response.setSessionResponse(sessionResponse);

        when(port.login(any(LoginRequest.class))).thenReturn(response);

        clientService.login();
    }

    @Test(expected = NetSuiteException.class)
    public void testLoginInvalidCredentialsFault() throws Exception {
        com.netsuite.webservices.test.platform.faults.InvalidCredentialsFault faultInfo =
                new com.netsuite.webservices.test.platform.faults.InvalidCredentialsFault();
        faultInfo.setCode(FaultCodeType.ACCT_TEMP_UNAVAILABLE);
        faultInfo.setMessage("Account temporarily unavailable");
        InvalidCredentialsFault fault = new InvalidCredentialsFault(faultInfo.getMessage(), faultInfo);

        when(port.login(any(LoginRequest.class))).thenThrow(fault);

        clientService.login();
    }

    @Test(expected = NetSuiteException.class)
    public void testLoginUnexpectedFault() throws Exception {
        com.netsuite.webservices.test.platform.faults.UnexpectedErrorFault faultInfo =
                new com.netsuite.webservices.test.platform.faults.UnexpectedErrorFault();
        faultInfo.setMessage("Internal error");
        UnexpectedErrorFault fault = new UnexpectedErrorFault(faultInfo.getMessage(), faultInfo);

        when(port.login(any(LoginRequest.class))).thenThrow(fault);

        clientService.login();
    }

    @Test
    public void testGet() throws Exception {

        TypeDesc typeDesc = clientService.getMetaDataSource().getTypeInfo("PurchaseOrder");
        TypeDesc refTypeDesc = clientService.getMetaDataSource().getTypeInfo("RecordRef");

        PurchaseOrder record = new NsObjectComposer<PurchaseOrder>(clientService.getMetaDataSource(), typeDesc)
                .composeObject();

        RecordRef recordRef = new NsObjectComposer<RecordRef>(clientService.getMetaDataSource(), refTypeDesc)
                .composeObject();

        GetResponse response = new GetResponse();
        response.setReadResponse(createSuccessReadResponse(record));
        when(port.get(notNull(GetRequest.class))).thenReturn(response);

        clientService.get(recordRef);

        verify(port, times(1)).login(notNull(LoginRequest.class));
        verify(port, times(1)).get(notNull(GetRequest.class));

        NsReadResponse readResponse = clientService.get(null);
        assertNull(readResponse.getStatus());
        assertNull(readResponse.getRecord());
    }

    @Test
    public void testGetList() throws Exception {

        TypeDesc typeDesc = clientService.getMetaDataSource().getTypeInfo("PurchaseOrder");
        TypeDesc refTypeDesc = clientService.getMetaDataSource().getTypeInfo("RecordRef");

        List<PurchaseOrder> recordList = makeNsObjects(
                new NsObjectComposer<PurchaseOrder>(clientService.getMetaDataSource(), typeDesc), 10);

        List<RecordRef> recordRefList = makeNsObjects(
                new NsObjectComposer<RecordRef>(clientService.getMetaDataSource(), refTypeDesc), 10);

        GetListResponse response = new GetListResponse();
        response.setReadResponseList(createSuccessReadResponseList(recordList));
        when(port.getList(notNull(GetListRequest.class))).thenReturn(response);

        clientService.getList(recordRefList);

        verify(port, times(1)).login(notNull(LoginRequest.class));
        verify(port, times(1)).getList(notNull(GetListRequest.class));

        List<NsReadResponse<Record>> readResponses = clientService.getList(null);
        assertTrue(readResponses.isEmpty());
    }

    @Test
    public void testAdd() throws Exception {

        TypeDesc typeDesc = clientService.getMetaDataSource().getTypeInfo("PurchaseOrder");

        PurchaseOrder record = new NsObjectComposer<PurchaseOrder>(clientService.getMetaDataSource(), typeDesc)
                .composeObject();

        AddResponse response = new AddResponse();
        response.setWriteResponse(createSuccessWriteResponse());
        when(port.add(notNull(AddRequest.class))).thenReturn(response);

        clientService.add(record);

        verify(port, times(1)).login(notNull(LoginRequest.class));
        verify(port, times(1)).add(notNull(AddRequest.class));

        NsWriteResponse writeResponse = clientService.add(null);
        assertNull(writeResponse.getStatus());
        assertNull(writeResponse.getRef());
    }

    @Test
    public void testAddList() throws Exception {

        TypeDesc typeDesc = clientService.getMetaDataSource().getTypeInfo("PurchaseOrder");

        List<PurchaseOrder> recordList = makeNsObjects(
                new NsObjectComposer<PurchaseOrder>(clientService.getMetaDataSource(), typeDesc), 10);

        AddListResponse response = new AddListResponse();
        response.setWriteResponseList(createSuccessWriteResponseList(recordList.size()));
        when(port.addList(notNull(AddListRequest.class))).thenReturn(response);

        clientService.addList(recordList);

        verify(port, times(1)).login(notNull(LoginRequest.class));
        verify(port, times(1)).addList(notNull(AddListRequest.class));

        List<NsWriteResponse<RecordRef>> writeResponses = clientService.addList(null);
        assertTrue(writeResponses.isEmpty());
    }

    @Test
    public void testUpdate() throws Exception {

        TypeDesc typeDesc = clientService.getMetaDataSource().getTypeInfo("PurchaseOrder");

        PurchaseOrder record = new NsObjectComposer<PurchaseOrder>(clientService.getMetaDataSource(), typeDesc)
                .composeObject();

        UpdateResponse response = new UpdateResponse();
        response.setWriteResponse(createSuccessWriteResponse());
        when(port.update(notNull(UpdateRequest.class))).thenReturn(response);

        clientService.update(record);

        verify(port, times(1)).login(notNull(LoginRequest.class));
        verify(port, times(1)).update(notNull(UpdateRequest.class));

        NsWriteResponse writeResponse = clientService.update(null);
        assertNull(writeResponse.getStatus());
        assertNull(writeResponse.getRef());
    }

    @Test
    public void testUpdateList() throws Exception {

        TypeDesc typeDesc = clientService.getMetaDataSource().getTypeInfo("PurchaseOrder");

        List<PurchaseOrder> recordList = makeNsObjects(
                new NsObjectComposer<PurchaseOrder>(clientService.getMetaDataSource(), typeDesc), 10);

        UpdateListResponse response = new UpdateListResponse();
        response.setWriteResponseList(createSuccessWriteResponseList(recordList.size()));
        when(port.updateList(notNull(UpdateListRequest.class))).thenReturn(response);

        clientService.updateList(recordList);

        verify(port, times(1)).login(notNull(LoginRequest.class));
        verify(port, times(1)).updateList(notNull(UpdateListRequest.class));

        List<NsWriteResponse<RecordRef>> writeResponses = clientService.updateList(null);
        assertTrue(writeResponses.isEmpty());
    }

    @Test
    public void testUpsert() throws Exception {

        TypeDesc typeDesc = clientService.getMetaDataSource().getTypeInfo("PurchaseOrder");

        PurchaseOrder record = new NsObjectComposer<PurchaseOrder>(clientService.getMetaDataSource(), typeDesc)
                .composeObject();

        UpsertResponse response = new UpsertResponse();
        response.setWriteResponse(createSuccessWriteResponse());
        when(port.upsert(notNull(UpsertRequest.class))).thenReturn(response);

        clientService.upsert(record);

        verify(port, times(1)).login(notNull(LoginRequest.class));
        verify(port, times(1)).upsert(notNull(UpsertRequest.class));

        NsWriteResponse writeResponse = clientService.upsert(null);
        assertNull(writeResponse.getStatus());
        assertNull(writeResponse.getRef());
    }

    @Test
    public void testUpsertList() throws Exception {

        TypeDesc typeDesc = clientService.getMetaDataSource().getTypeInfo("PurchaseOrder");

        List<PurchaseOrder> recordList = makeNsObjects(
                new NsObjectComposer<PurchaseOrder>(clientService.getMetaDataSource(), typeDesc), 10);

        UpsertListResponse response = new UpsertListResponse();
        response.setWriteResponseList(createSuccessWriteResponseList(recordList.size()));
        when(port.upsertList(notNull(UpsertListRequest.class))).thenReturn(response);

        clientService.upsertList(recordList);

        verify(port, times(1)).login(notNull(LoginRequest.class));
        verify(port, times(1)).upsertList(notNull(UpsertListRequest.class));

        List<NsWriteResponse<RecordRef>> writeResponses = clientService.upsertList(null);
        assertTrue(writeResponses.isEmpty());
    }

    @Test
    public void testDelete() throws Exception {

        TypeDesc typeDesc = clientService.getMetaDataSource().getTypeInfo("RecordRef");

        RecordRef recordRef = new NsObjectComposer<RecordRef>(clientService.getMetaDataSource(), typeDesc)
                .composeObject();

        DeleteResponse response = new DeleteResponse();
        response.setWriteResponse(createSuccessWriteResponse());
        when(port.delete(notNull(DeleteRequest.class))).thenReturn(response);

        clientService.delete(recordRef);

        verify(port, times(1)).login(notNull(LoginRequest.class));
        verify(port, times(1)).delete(notNull(DeleteRequest.class));

        NsWriteResponse writeResponse = clientService.delete(null);
        assertNull(writeResponse.getStatus());
        assertNull(writeResponse.getRef());
    }

    @Test
    public void testDeleteList() throws Exception {

        TypeDesc typeDesc = clientService.getMetaDataSource().getTypeInfo("RecordRef");

        List<RecordRef> recordRefList = makeNsObjects(
                new NsObjectComposer<RecordRef>(clientService.getMetaDataSource(), typeDesc), 10);

        DeleteListResponse response = new DeleteListResponse();
        response.setWriteResponseList(createSuccessWriteResponseList(recordRefList.size()));
        when(port.deleteList(notNull(DeleteListRequest.class))).thenReturn(response);

        clientService.deleteList(recordRefList);

        verify(port, times(1)).login(notNull(LoginRequest.class));
        verify(port, times(1)).deleteList(notNull(DeleteListRequest.class));

        List<NsWriteResponse<RecordRef>> writeResponses = clientService.deleteList(null);
        assertTrue(writeResponses.isEmpty());
    }

    @Test
    public void testRetrying() throws Exception {

        clientService.setRetryCount(3);
        clientService.setRetryInterval(1);

        clientService.login();

        TypeDesc typeDesc = clientService.getMetaDataSource().getTypeInfo("RecordRef");

        RecordRef recordRef = new NsObjectComposer<RecordRef>(clientService.getMetaDataSource(), typeDesc)
                .composeObject();

        final DeleteResponse response = new DeleteResponse();
        response.setWriteResponse(createSuccessWriteResponse());

        final AtomicInteger counter = new AtomicInteger(3);

        when(port.delete(notNull(DeleteRequest.class))).thenAnswer(new Answer<DeleteResponse>() {

            @Override
            public DeleteResponse answer(InvocationOnMock invocation) throws Throwable {
                if (counter.decrementAndGet() > 0) {
                    com.netsuite.webservices.test.platform.faults.InvalidSessionFault faultInfo =
                            new com.netsuite.webservices.test.platform.faults.InvalidSessionFault();
                    faultInfo.setCode(FaultCodeType.SESSION_TIMED_OUT);
                    faultInfo.setMessage("Session timed out");
                    InvalidSessionFault fault = new InvalidSessionFault(faultInfo.getMessage(), faultInfo);
                    throw fault;
                }

                return response;
            }
        });

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        clientService.delete(recordRef);

        stopWatch.stop();

        verify(port, times(3)).login(notNull(LoginRequest.class));
        verify(port, times(3)).delete(notNull(DeleteRequest.class));

        assertTrue(stopWatch.getTime() >= 3 * clientService.getRetryInterval() * 1000);
    }

    @Test(expected = NetSuiteException.class)
    public void testCheckError() throws Exception {
        Status status = NetSuitePortTypeMockAdapterImpl.createErrorStatus(
                StatusDetailCodeType.FILE_REQD, "Field(s) required: recType");
        NsStatus nsStatus = TestNetSuiteClientService.toNsStatus(status);

        NetSuiteClientService.checkError(nsStatus);
    }

    @Test
    public void testCheckErrorStatusOk() throws Exception {
        NsStatus nsStatus = new NsStatus();
        nsStatus.setSuccess(true);

        NetSuiteClientService.checkError(nsStatus);
    }

    private ReadResponseList createSuccessReadResponseList(List<? extends Record> recordList) {
        ReadResponseList readResponseList = new ReadResponseList();
        readResponseList.setStatus(NetSuitePortTypeMockAdapterImpl.createSuccessStatus());
        for (int i = 0; i < recordList.size(); i++) {
            Record record = recordList.get(i);
            ReadResponse readResponse = createSuccessReadResponse(record);
            readResponseList.getReadResponse().add(readResponse);
        }
        return readResponseList;
    }

    private ReadResponse createSuccessReadResponse(Record record) {
        ReadResponse readResponse = new ReadResponse();
        readResponse.setStatus(NetSuitePortTypeMockAdapterImpl.createSuccessStatus());
        readResponse.setRecord(record);
        return readResponse;
    }

    private WriteResponseList createSuccessWriteResponseList(int recordCount) {
        WriteResponseList writeResponseList = new WriteResponseList();
        writeResponseList.setStatus(NetSuitePortTypeMockAdapterImpl.createSuccessStatus());
        for (int i = 0; i < recordCount; i++) {
            WriteResponse writeResponse = createSuccessWriteResponse();
            writeResponseList.getWriteResponse().add(writeResponse);
        }
        return writeResponseList;
    }

    private WriteResponse createSuccessWriteResponse() {
        WriteResponse writeResponse = new WriteResponse();
        writeResponse.setStatus(NetSuitePortTypeMockAdapterImpl.createSuccessStatus());
        RecordRef recordRef = new RecordRef();
        recordRef.setInternalId(Integer.valueOf(10000 + rnd.nextInt()).toString());
        writeResponse.setBaseRef(recordRef);
        return writeResponse;
    }
}
