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

package org.talend.components.netsuite.v2016_2.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.talend.components.netsuite.client.model.beans.Beans.toInitialUpper;
import static org.talend.components.netsuite.v2016_2.NetSuitePortTypeMockAdapterImpl.createSuccessStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;

import org.apache.cxf.headers.Header;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.talend.components.netsuite.NetSuiteWebServiceMockTestFixture;
import org.talend.components.netsuite.client.NetSuiteClientService;
import org.talend.components.netsuite.client.NetSuiteCredentials;
import org.talend.components.netsuite.client.model.RecordTypeInfo;
import org.talend.components.netsuite.client.model.SearchRecordTypeDesc;
import org.talend.components.netsuite.client.model.search.SearchFieldOperatorName;
import org.talend.components.netsuite.client.model.search.SearchFieldOperatorType;
import org.talend.components.netsuite.test.AssertMatcher;
import org.talend.components.netsuite.test.MessageContextHolder;
import org.talend.components.netsuite.v2016_2.NetSuiteMockTestBase;

import com.netsuite.webservices.v2016_2.platform.NetSuitePortType;
import com.netsuite.webservices.v2016_2.platform.core.types.RecordType;
import com.netsuite.webservices.v2016_2.platform.core.types.SearchRecordType;
import com.netsuite.webservices.v2016_2.platform.messages.LoginRequest;
import com.netsuite.webservices.v2016_2.platform.messages.LoginResponse;
import com.netsuite.webservices.v2016_2.platform.messages.SessionResponse;

/**
 *
 */
public class NetSuiteClientServiceTest extends NetSuiteMockTestBase {

    @BeforeClass
    public static void classSetUp() throws Exception {
        installWebServiceTestFixture();
        setUpClassScopedTestFixtures();
    }

    @AfterClass
    public static void classTearDown() throws Exception {
        tearDownClassScopedTestFixtures();
    }

    /**
     *
     */
    @Test
    public void testConnectAndLogin() throws Exception {
        final NetSuiteCredentials credentials = webServiceMockTestFixture.getCredentials();
        final NetSuitePortType port = webServiceMockTestFixture.getPortMock();

        SessionResponse sessionResponse = new SessionResponse();
        sessionResponse.setStatus(createSuccessStatus());
        LoginResponse response = new LoginResponse();
        response.setSessionResponse(sessionResponse);

        when(port.login(argThat(new AssertMatcher<LoginRequest>() {

            @Override protected void doAssert(LoginRequest target) throws Exception {
                assertEquals(credentials.getEmail(), target.getPassport().getEmail());
                assertEquals(credentials.getPassword(), target.getPassport().getPassword());
                assertEquals(credentials.getRoleId(), target.getPassport().getRole().getInternalId());
                assertEquals(credentials.getAccount(), target.getPassport().getAccount());

                MessageContext messageContext = MessageContextHolder.get();
                assertNotNull(messageContext);
                List<Header> headers = (List<Header>) messageContext.get(Header.HEADER_LIST);
                assertNotNull(headers);
                Header appInfoHeader = NetSuiteWebServiceMockTestFixture.getHeader(headers, new QName(
                        NetSuiteClientServiceImpl.NS_URI_PLATFORM_MESSAGES, "applicationInfo"));
                assertNotNull(appInfoHeader);
            }
        }))).thenReturn(response);

        NetSuiteClientService<?> clientService = webServiceMockTestFixture.getClientService();

        clientService.login();

        verify(port, times(1)).login(any(LoginRequest.class));
    }

    @Test
    public void testBasicMetaData() throws Exception {
        NetSuiteClientService<?> clientService = webServiceMockTestFixture.getClientService();

        Set<SearchRecordType> searchRecordTypeSet = new HashSet<>(Arrays.asList(SearchRecordType.values()));

        Set<String> searchRecordTypeNameSet = new HashSet<>();
        for (SearchRecordType searchRecordType : searchRecordTypeSet) {
            searchRecordTypeNameSet.add(toInitialUpper(searchRecordType.value()));
        }
        searchRecordTypeNameSet.add("Address");
        searchRecordTypeNameSet.add("InventoryDetail");
        searchRecordTypeNameSet.add("TimeEntry");

        for (String searchRecordType : searchRecordTypeNameSet) {
            try {
                SearchRecordTypeDesc searchRecordInfo = clientService.getMetaDataSource()
                        .getSearchRecordType(searchRecordType);
                assertNotNull("Search record def found: " + searchRecordType, searchRecordInfo);
            } catch (Exception e) {
                throw new AssertionError("Search record type: " + searchRecordType, e);
            }
        }

        Set<RecordType> recordTypeSet = new HashSet<>(Arrays.asList(RecordType.values()));
        recordTypeSet.remove(RecordType.CUSTOM_TRANSACTION_TYPE);

        Set<String> recordTypeNameSet = new HashSet<>();
        for (RecordType recordType : recordTypeSet) {
            recordTypeNameSet.add(toInitialUpper(recordType.value()));
        }
        recordTypeNameSet.add("Address");
        recordTypeNameSet.add("InventoryDetail");
        recordTypeNameSet.add("TimeEntry");

        for (String recordType : recordTypeNameSet) {
            RecordTypeInfo recordTypeInfo = clientService.getMetaDataSource().getRecordType(recordType);
            assertNotNull("Record type def found: " + recordType, recordTypeInfo);
        }

        Collection<SearchFieldOperatorName> searchOperatorNames =
                clientService.getBasicMetaData().getSearchOperatorNames();
        List<SearchFieldOperatorName> searchFieldOperatorNameList = new ArrayList<>(searchOperatorNames);
        Collections.sort(searchFieldOperatorNameList, new Comparator<SearchFieldOperatorName>() {
            @Override public int compare(SearchFieldOperatorName o1, SearchFieldOperatorName o2) {
                return o1.getQualifiedName().compareTo(o2.getQualifiedName());
            }
        });
        for (SearchFieldOperatorName operatorName : searchFieldOperatorNameList) {
            assertNotNull(operatorName.getDataType());
            if (!SearchFieldOperatorType.BOOLEAN.dataTypeEquals(operatorName.getDataType())) {
                assertNotNull(operatorName.getName());
            }
        }
    }

}