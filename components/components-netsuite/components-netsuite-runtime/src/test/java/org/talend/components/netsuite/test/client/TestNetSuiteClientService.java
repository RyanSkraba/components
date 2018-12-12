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

package org.talend.components.netsuite.test.client;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.SOAPFaultException;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.cxf.headers.Header;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.mockito.Mockito;
import org.talend.components.netsuite.NetSuiteErrorCode;
import org.talend.components.netsuite.NetSuiteRuntimeI18n;
import org.talend.components.netsuite.client.CustomMetaDataSource;
import org.talend.components.netsuite.client.EmptyCustomMetaDataSource;
import org.talend.components.netsuite.client.NetSuiteClientService;
import org.talend.components.netsuite.client.NetSuiteCredentials;
import org.talend.components.netsuite.client.NetSuiteException;
import org.talend.components.netsuite.client.NsPreferences;
import org.talend.components.netsuite.client.NsReadResponse;
import org.talend.components.netsuite.client.NsSearchPreferences;
import org.talend.components.netsuite.client.NsSearchResult;
import org.talend.components.netsuite.client.NsStatus;
import org.talend.components.netsuite.client.NsWriteResponse;
import org.talend.components.netsuite.client.model.BasicMetaData;
import org.talend.components.netsuite.client.model.TestBasicMetaDataImpl;

import com.netsuite.webservices.test.platform.InvalidCredentialsFault;
import com.netsuite.webservices.test.platform.InvalidSessionFault;
import com.netsuite.webservices.test.platform.NetSuitePortType;
import com.netsuite.webservices.test.platform.UnexpectedErrorFault;
import com.netsuite.webservices.test.platform.core.BaseRef;
import com.netsuite.webservices.test.platform.core.Passport;
import com.netsuite.webservices.test.platform.core.Record;
import com.netsuite.webservices.test.platform.core.RecordRef;
import com.netsuite.webservices.test.platform.core.SearchRecord;
import com.netsuite.webservices.test.platform.core.SearchResult;
import com.netsuite.webservices.test.platform.core.Status;
import com.netsuite.webservices.test.platform.core.StatusDetail;
import com.netsuite.webservices.test.platform.messages.AddListRequest;
import com.netsuite.webservices.test.platform.messages.AddRequest;
import com.netsuite.webservices.test.platform.messages.ApplicationInfo;
import com.netsuite.webservices.test.platform.messages.DeleteListRequest;
import com.netsuite.webservices.test.platform.messages.DeleteRequest;
import com.netsuite.webservices.test.platform.messages.GetListRequest;
import com.netsuite.webservices.test.platform.messages.GetRequest;
import com.netsuite.webservices.test.platform.messages.LoginRequest;
import com.netsuite.webservices.test.platform.messages.LoginResponse;
import com.netsuite.webservices.test.platform.messages.LogoutRequest;
import com.netsuite.webservices.test.platform.messages.Preferences;
import com.netsuite.webservices.test.platform.messages.ReadResponse;
import com.netsuite.webservices.test.platform.messages.ReadResponseList;
import com.netsuite.webservices.test.platform.messages.SearchMoreRequest;
import com.netsuite.webservices.test.platform.messages.SearchMoreWithIdRequest;
import com.netsuite.webservices.test.platform.messages.SearchNextRequest;
import com.netsuite.webservices.test.platform.messages.SearchPreferences;
import com.netsuite.webservices.test.platform.messages.SearchRequest;
import com.netsuite.webservices.test.platform.messages.SessionResponse;
import com.netsuite.webservices.test.platform.messages.UpdateListRequest;
import com.netsuite.webservices.test.platform.messages.UpdateRequest;
import com.netsuite.webservices.test.platform.messages.UpsertListRequest;
import com.netsuite.webservices.test.platform.messages.UpsertRequest;
import com.netsuite.webservices.test.platform.messages.WriteResponse;
import com.netsuite.webservices.test.platform.messages.WriteResponseList;
import com.netsuite.webservices.test.platform.ExceededRequestLimitFault;

/**
 *
 */
public class TestNetSuiteClientService extends NetSuiteClientService<NetSuitePortType> {

    public static final String NS_URI_PLATFORM_MESSAGES =
            "urn:messages_2016_2.platform.webservices.netsuite.com";

    protected NetSuitePortProvider<NetSuitePortType> portProvider = new NetSuitePortMockProvider();

    public TestNetSuiteClientService() {
        super();

        portAdapter = new PortAdapterImpl();

        metaDataSource = createDefaultMetaDataSource();
    }

    public NetSuitePortProvider<NetSuitePortType> getPortProvider() {
        return portProvider;
    }

    public void setPortProvider(NetSuitePortProvider<NetSuitePortType> portProvider) {
        this.portProvider = portProvider;
    }

    @Override
    public BasicMetaData getBasicMetaData() {
        return TestBasicMetaDataImpl.getInstance();
    }

    @Override
    public CustomMetaDataSource createDefaultCustomMetaDataSource() {
        return new EmptyCustomMetaDataSource();
    }

    @Override
    protected void doLogout() throws NetSuiteException {
        try {
            LogoutRequest request = new LogoutRequest();
            port.logout(request);
        } catch (Exception e) {
            throw new NetSuiteException(e.getMessage(), e);
        }
    }

    @Override
    protected void doLogin() throws NetSuiteException {
        port = getNetSuitePort(endpointUrl, credentials.getAccount());

        setHttpClientPolicy(port);

        setLoginHeaders(port);

        PortOperation<SessionResponse, NetSuitePortType> loginOp;
        if (!credentials.isUseSsoLogin()) {
            final Passport passport = createNativePassport(credentials);
            loginOp = new PortOperation<SessionResponse, NetSuitePortType>() {
                @Override public SessionResponse execute(NetSuitePortType port) throws Exception {
                    LoginRequest request = new LoginRequest();
                    request.setPassport(passport);
                    LoginResponse response = port.login(request);
                    return response.getSessionResponse();
                }
            };
        } else {
            throw new NetSuiteException(new NetSuiteErrorCode(NetSuiteErrorCode.CLIENT_ERROR),
                    NetSuiteRuntimeI18n.MESSAGES.getMessage("error.ssoLoginNotSupported"));
        }

        Status status = null;
        SessionResponse sessionResponse;
        String exceptionMessage = null;
        for (int i = 0; i < getRetryCount(); i++) {
            try {
                sessionResponse = loginOp.execute(port);
                status = sessionResponse.getStatus();

            } catch (InvalidCredentialsFault f) {
                throw new NetSuiteException(new NetSuiteErrorCode(NetSuiteErrorCode.CLIENT_ERROR),
                        f.getFaultInfo().getMessage());
            } catch (UnexpectedErrorFault f) {
                exceptionMessage = f.getFaultInfo().getMessage();
            } catch (Exception e) {
                exceptionMessage = e.getMessage();
            }

            if (status != null) {
                break;
            }

            if (i != getRetryCount() - 1) {
                waitForRetryInterval(i);
            }
        }

        checkLoginError(toNsStatus(status), exceptionMessage);

        updateLoginHeaders(port);
    }

    @Override
    protected boolean errorCanBeWorkedAround(Throwable t) {
        if (t instanceof ExceededRequestLimitFault ||
                t instanceof InvalidSessionFault ||
                t instanceof RemoteException ||
                t instanceof SOAPFaultException ||
                t instanceof SocketException)
            return true;

        return false;
    }

    @Override
    protected boolean errorRequiresNewLogin(Throwable t) {
        if (t instanceof InvalidSessionFault || t instanceof SocketException) {
            return true;
        }
        return false;
    }

    @Override
    protected String getPlatformMessageNamespaceUri() {
        return NS_URI_PLATFORM_MESSAGES;
    }

    @Override
    protected Preferences createNativePreferences(NsPreferences nsPreferences) {
        Preferences preferences = new Preferences();
        try {
            BeanUtils.copyProperties(preferences, nsPreferences);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new NetSuiteException(e.getMessage(), e);
        }
        return preferences;
    }

    @Override
    protected SearchPreferences createNativeSearchPreferences(NsSearchPreferences nsSearchPreferences) {
        SearchPreferences searchPreferences = new SearchPreferences();
        try {
            BeanUtils.copyProperties(searchPreferences, nsSearchPreferences);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new NetSuiteException(e.getMessage(), e);
        }
        return searchPreferences;
    }

    @Override
    protected ApplicationInfo createNativeApplicationInfo(NetSuiteCredentials credentials) {
        ApplicationInfo applicationInfo = new ApplicationInfo();
        applicationInfo.setApplicationId(credentials.getApplicationId());
        return applicationInfo;
    }

    @Override
    protected Passport createNativePassport(NetSuiteCredentials nsCredentials) {
        RecordRef roleRecord = new RecordRef();
        roleRecord.setInternalId(nsCredentials.getRoleId());

        final Passport passport = new Passport();
        passport.setEmail(nsCredentials.getEmail());
        passport.setPassword(nsCredentials.getPassword());
        passport.setRole(roleRecord);
        passport.setAccount(nsCredentials.getAccount());

        return passport;
    }

    @Override
    protected NetSuitePortType getNetSuitePort(String defaultEndpointUrl, String account) throws NetSuiteException {
        try {
            return portProvider.getPort(new URL(defaultEndpointUrl), account);
        } catch (MalformedURLException e) {
            throw new NetSuiteException(new NetSuiteErrorCode(
                    NetSuiteErrorCode.CLIENT_ERROR, "Malformed URL: " + defaultEndpointUrl), e);
        }
    }

    @Override
    protected void setHeader(NetSuitePortType port, Header header) {
        // Not supported
    }

    @Override
    protected void removeHeader(NetSuitePortType port, QName name) {
        // Not supported
    }

    @Override
    protected void setHttpClientPolicy(NetSuitePortType port, HTTPClientPolicy httpClientPolicy) {
        // Not supported
    }

    public static <RefT> List<NsWriteResponse<RefT>> toNsWriteResponseList(WriteResponseList writeResponseList) {
        List<NsWriteResponse<RefT>> nsWriteResponses = new ArrayList<>(writeResponseList.getWriteResponse().size());
        for (WriteResponse writeResponse : writeResponseList.getWriteResponse()) {
            nsWriteResponses.add((NsWriteResponse<RefT>) toNsWriteResponse(writeResponse));
        }
        return nsWriteResponses;
    }

    public static <RecT> List<NsReadResponse<RecT>> toNsReadResponseList(ReadResponseList readResponseList) {
        List<NsReadResponse<RecT>> nsReadResponses = new ArrayList<>(readResponseList.getReadResponse().size());
        for (ReadResponse readResponse : readResponseList.getReadResponse()) {
            nsReadResponses.add((NsReadResponse<RecT>) toNsReadResponse(readResponse));
        }
        return nsReadResponses;
    }

    public static <RecT> NsSearchResult<RecT> toNsSearchResult(SearchResult result) {
        NsSearchResult nsResult = new NsSearchResult(toNsStatus(result.getStatus()));
        nsResult.setSearchId(result.getSearchId());
        nsResult.setTotalPages(result.getTotalPages());
        nsResult.setTotalRecords(result.getTotalRecords());
        nsResult.setPageIndex(result.getPageIndex());
        nsResult.setPageSize(result.getPageSize());
        if (result.getRecordList() != null) {
            List<Record> nsRecordList = new ArrayList<>(result.getRecordList().getRecord().size());
            for (Record record : result.getRecordList().getRecord()) {
                nsRecordList.add(record);
            }
            nsResult.setRecordList(nsRecordList);
        } else {
            nsResult.setRecordList(Collections.emptyList());
        }
        return nsResult;
    }

    public static <RefT> NsWriteResponse<RefT> toNsWriteResponse(WriteResponse writeResponse) {
        NsWriteResponse<RefT> nsWriteResponse = new NsWriteResponse(
                toNsStatus(writeResponse.getStatus()),
                writeResponse.getBaseRef());
        return nsWriteResponse;
    }

    public static <RecT> NsReadResponse<RecT> toNsReadResponse(ReadResponse readResponse) {
        NsReadResponse<RecT> nsReadResponse = new NsReadResponse(
                toNsStatus(readResponse.getStatus()),
                readResponse.getRecord());
        return nsReadResponse;
    }

    public static <RecT> List<Record> toRecordList(List<RecT> nsRecordList) {
        List<Record> recordList = new ArrayList<>(nsRecordList.size());
        for (RecT nsRecord : nsRecordList) {
            Record r = (Record) nsRecord;
            recordList.add(r);
        }
        return recordList;
    }

    public static <RefT> List<BaseRef> toBaseRefList(List<RefT> nsRefList) {
        List<BaseRef> baseRefList = new ArrayList<>(nsRefList.size());
        for (RefT nsRef : nsRefList) {
            BaseRef r = (BaseRef) nsRef;
            baseRefList.add(r);
        }
        return baseRefList;
    }

    public static NsStatus toNsStatus(Status status) {
        if (status == null) {
            return null;
        }
        NsStatus nsStatus = new NsStatus();
        nsStatus.setSuccess(status.getIsSuccess());
        for (StatusDetail detail : status.getStatusDetail()) {
            nsStatus.getDetails().add(toNsStatusDetail(detail));
        }
        return nsStatus;
    }

    public static NsStatus.Detail toNsStatusDetail(StatusDetail detail) {
        NsStatus.Detail nsDetail = new NsStatus.Detail();
        nsDetail.setType(NsStatus.Type.valueOf(detail.getType().value()));
        nsDetail.setCode(detail.getCode().value());
        nsDetail.setMessage(detail.getMessage());
        return nsDetail;
    }

    public interface NetSuitePortProvider<T> {

        T getPort(URL endpointUrl, String account);
    }

    public static class NetSuitePortMockProvider implements NetSuitePortProvider<NetSuitePortType> {

        @Override
        public NetSuitePortType getPort(URL endpointUrl, String account) {
            return Mockito.mock(NetSuitePortType.class, Mockito.withSettings().extraInterfaces(BindingProvider.class));
        }
    }

    protected class PortAdapterImpl implements PortAdapter<NetSuitePortType> {

        @Override
        public <RecT, SearchT> NsSearchResult<RecT> search(final NetSuitePortType port, final SearchT searchRecord) throws Exception {
            SearchRequest request = new SearchRequest();
            SearchRecord sr = (SearchRecord) searchRecord;
            request.setSearchRecord(sr);

            SearchResult result = port.search(request).getSearchResult();
            return toNsSearchResult(result);
        }

        @Override
        public <RecT> NsSearchResult<RecT> searchMore(final NetSuitePortType port, final int pageIndex) throws Exception {
            SearchMoreRequest request = new SearchMoreRequest();
            request.setPageIndex(pageIndex);

            SearchResult result = port.searchMore(request).getSearchResult();
            return toNsSearchResult(result);
        }

        @Override
        public <RecT> NsSearchResult<RecT> searchMoreWithId(final NetSuitePortType port,
                final String searchId, final int pageIndex) throws Exception {
            SearchMoreWithIdRequest request = new SearchMoreWithIdRequest();
            request.setSearchId(searchId);
            request.setPageIndex(pageIndex);

            SearchResult result = port.searchMoreWithId(request).getSearchResult();
            return toNsSearchResult(result);
        }

        @Override
        public <RecT> NsSearchResult<RecT> searchNext(final NetSuitePortType port) throws Exception {
            SearchNextRequest request = new SearchNextRequest();
            SearchResult result = port.searchNext(request).getSearchResult();
            return toNsSearchResult(result);
        }

        @Override
        public <RecT, RefT> NsReadResponse<RecT> get(final NetSuitePortType port, final RefT ref) throws Exception {
            GetRequest request = new GetRequest();
            request.setBaseRef((BaseRef) ref);

            ReadResponse response = port.get(request).getReadResponse();
            return toNsReadResponse(response);
        }

        @Override
        public <RecT, RefT> List<NsReadResponse<RecT>> getList(final NetSuitePortType port, final List<RefT> refs) throws Exception {
            GetListRequest request = new GetListRequest();
            for (RefT ref : refs) {
                request.getBaseRef().add((BaseRef) ref);
            }

            ReadResponseList response = port.getList(request).getReadResponseList();
            return toNsReadResponseList(response);
        }

        @Override
        public <RecT, RefT> NsWriteResponse<RefT> add(final NetSuitePortType port, final RecT record) throws Exception {
            AddRequest request = new AddRequest();
            request.setRecord((Record) record);

            WriteResponse response = port.add(request).getWriteResponse();
            return toNsWriteResponse(response);
        }

        @Override
        public <RecT, RefT> List<NsWriteResponse<RefT>> addList(final NetSuitePortType port,
                final List<RecT> records) throws Exception {
            AddListRequest request = new AddListRequest();
            request.getRecord().addAll(toRecordList(records));

            WriteResponseList writeResponseList = port.addList(request).getWriteResponseList();
            return toNsWriteResponseList(writeResponseList);
        }

        @Override
        public <RecT, RefT> NsWriteResponse<RefT> update(final NetSuitePortType port, final RecT record) throws Exception {
            UpdateRequest request = new UpdateRequest();
            request.setRecord((Record) record);

            WriteResponse response = port.update(request).getWriteResponse();
            return toNsWriteResponse(response);
        }

        @Override
        public <RecT, RefT> List<NsWriteResponse<RefT>> updateList(final NetSuitePortType port, final List<RecT> records) throws Exception {
            UpdateListRequest request = new UpdateListRequest();
            request.getRecord().addAll(toRecordList(records));

            WriteResponseList writeResponseList = port.updateList(request).getWriteResponseList();
            return toNsWriteResponseList(writeResponseList);
        }

        @Override
        public <RecT, RefT> NsWriteResponse<RefT> upsert(final NetSuitePortType port, final RecT record) throws Exception {
            UpsertRequest request = new UpsertRequest();
            request.setRecord((Record) record);

            WriteResponse response = port.upsert(request).getWriteResponse();
            return toNsWriteResponse(response);
        }

        @Override
        public <RecT, RefT> List<NsWriteResponse<RefT>> upsertList(final NetSuitePortType port, final List<RecT> records) throws Exception {
            UpsertListRequest request = new UpsertListRequest();
            request.getRecord().addAll(toRecordList(records));

            WriteResponseList writeResponseList = port.upsertList(request).getWriteResponseList();
            return toNsWriteResponseList(writeResponseList);
        }

        @Override
        public <RefT> NsWriteResponse<RefT> delete(final NetSuitePortType port, final RefT ref) throws Exception {
            DeleteRequest request = new DeleteRequest();
            BaseRef baseRef = (BaseRef) ref;
            request.setBaseRef(baseRef);

            WriteResponse writeResponse = port.delete(request).getWriteResponse();
            return toNsWriteResponse(writeResponse);
        }

        @Override
        public <RefT> List<NsWriteResponse<RefT>> deleteList(final NetSuitePortType port, final List<RefT> refs) throws Exception {
            DeleteListRequest request = new DeleteListRequest();
            request.getBaseRef().addAll(toBaseRefList(refs));

            WriteResponseList writeResponseList = port.deleteList(request).getWriteResponseList();
            return toNsWriteResponseList(writeResponseList);
        }

    }
}
