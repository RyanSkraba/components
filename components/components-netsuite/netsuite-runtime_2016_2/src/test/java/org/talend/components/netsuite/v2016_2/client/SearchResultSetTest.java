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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.talend.components.netsuite.client.NetSuiteClientService;
import org.talend.components.netsuite.client.NsSearchResult;
import org.talend.components.netsuite.client.model.RecordTypeInfo;
import org.talend.components.netsuite.client.model.SearchRecordTypeDesc;
import org.talend.components.netsuite.client.search.SearchResultSet;

import com.netsuite.webservices.v2016_2.lists.accounting.Account;
import com.netsuite.webservices.v2016_2.lists.accounting.AccountSearch;
import com.netsuite.webservices.v2016_2.lists.accounting.InventoryItem;
import com.netsuite.webservices.v2016_2.lists.accounting.ItemSearch;
import com.netsuite.webservices.v2016_2.lists.accounting.ServiceSaleItem;
import com.netsuite.webservices.v2016_2.platform.core.Record;
import com.netsuite.webservices.v2016_2.platform.core.RecordList;
import com.netsuite.webservices.v2016_2.platform.core.SearchResult;
import com.netsuite.webservices.v2016_2.platform.core.Status;
import com.netsuite.webservices.v2016_2.platform.messages.SearchMoreWithIdResponse;
import com.netsuite.webservices.v2016_2.platform.messages.SearchResponse;

/**
 *
 */
public class SearchResultSetTest {

    @Test
    public void testEmptyResult() throws Exception {
        NetSuiteClientService<?> conn = mock(NetSuiteClientService.class);

        SearchResult result1 = new SearchResult();
        Status status = new Status();
        status.setIsSuccess(true);
        result1.setStatus(status);
        result1.setSearchId("abc123");
        result1.setPageIndex(1);
        result1.setTotalRecords(0);
        result1.setTotalPages(0);

        SearchResponse response1 = new SearchResponse();
        response1.setSearchResult(result1);

        AccountSearch nsSearchRecord1 = new AccountSearch();
        NsSearchResult nsSearchResult1 = NetSuiteClientServiceImpl.toNsSearchResult(result1);

        when(conn.search(eq(nsSearchRecord1))).thenReturn(nsSearchResult1);

        NetSuiteClientService<?> clientService = new NetSuiteClientServiceImpl();
        RecordTypeInfo recordTypeInfo = clientService.getRecordType("Account");
        SearchRecordTypeDesc searchRecordTypeDesc = clientService.getSearchRecordType(
                recordTypeInfo.getRecordType().getSearchRecordType());

        SearchResultSet<Record> resultSet = new SearchResultSet<>(conn,
                recordTypeInfo.getRecordType(), searchRecordTypeDesc, nsSearchResult1);

        assertFalse(resultSet.next());
    }

    @Test
    public void testPagination() throws Exception {
        NetSuiteClientService<?> conn = mock(NetSuiteClientService.class);

        List<Record> page1 = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            page1.add(new Account());
        }

        List<Record> page2 = new ArrayList<>();
        for (int i = 0; i < 750; i++) {
            page2.add(new Account());
        }

        SearchResult result1 = new SearchResult();
        Status status = new Status();
        status.setIsSuccess(true);
        result1.setStatus(status);
        result1.setSearchId("abc123");
        result1.setPageIndex(1);
        result1.setTotalRecords(page1.size() + page2.size());
        result1.setTotalPages(2);
        result1.setRecordList(new RecordList());
        result1.getRecordList().getRecord().addAll(page1);

        SearchResult result2 = new SearchResult();
        result2.setStatus(status);
        result2.setSearchId(result1.getSearchId());
        result2.setPageIndex(2);
        result2.setTotalRecords(result1.getTotalRecords());
        result2.setTotalPages(result1.getTotalPages());
        result2.setRecordList(new RecordList());
        result2.getRecordList().getRecord().addAll(page2);

        SearchResponse response1 = new SearchResponse();
        response1.setSearchResult(result1);

        SearchMoreWithIdResponse response2 = new SearchMoreWithIdResponse();
        response2.setSearchResult(result2);

        AccountSearch nsSearchRecord1 = new AccountSearch();
        NsSearchResult nsSearchResult1 = NetSuiteClientServiceImpl.toNsSearchResult(result1);
        NsSearchResult nsSearchResult2 = NetSuiteClientServiceImpl.toNsSearchResult(result2);

        when(conn.search(eq(nsSearchRecord1))).thenReturn(nsSearchResult1);
        when(conn.searchMoreWithId(eq("abc123"), eq(2))).thenReturn(nsSearchResult2);

        NetSuiteClientService<?> clientService = new NetSuiteClientServiceImpl();
        RecordTypeInfo recordTypeInfo = clientService.getRecordType("Account");
        SearchRecordTypeDesc searchRecordTypeDesc = clientService.getSearchRecordType(
                recordTypeInfo.getRecordType().getSearchRecordType());

        SearchResultSet<Record> resultSet = new SearchResultSet<>(conn,
                recordTypeInfo.getRecordType(), searchRecordTypeDesc, nsSearchResult1);

        List<Object> recordList = new ArrayList<>();
        while (resultSet.next()) {
            Object record = resultSet.get();
            assertNotNull(record);
            recordList.add(record);
        }

        assertEquals(page1.size() + page2.size(), recordList.size());
    }

    @Test
    public void testRecordFiltering() throws Exception {
        NetSuiteClientService<?> conn = mock(NetSuiteClientService.class);

        List<Record> page1 = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            page1.add(new InventoryItem());
        }

        List<Record> page2 = new ArrayList<>();
        for (int i = 0; i < 750; i++) {
            page2.add(new ServiceSaleItem());
        }

        SearchResult result1 = new SearchResult();
        Status status = new Status();
        status.setIsSuccess(true);
        result1.setStatus(status);
        result1.setSearchId("abc123");
        result1.setPageIndex(1);
        result1.setTotalRecords(page1.size() + page2.size());
        result1.setTotalPages(2);
        result1.setRecordList(new RecordList());
        result1.getRecordList().getRecord().addAll(page1);

        SearchResult result2 = new SearchResult();
        result2.setStatus(status);
        result2.setSearchId(result1.getSearchId());
        result2.setPageIndex(2);
        result2.setTotalRecords(result1.getTotalRecords());
        result2.setTotalPages(result1.getTotalPages());
        result2.setRecordList(new RecordList());
        result2.getRecordList().getRecord().addAll(page2);

        SearchResponse response1 = new SearchResponse();
        response1.setSearchResult(result1);

        SearchMoreWithIdResponse response2 = new SearchMoreWithIdResponse();
        response2.setSearchResult(result2);

        ItemSearch nsSearchRecord1 = new ItemSearch();
        NsSearchResult nsSearchResult1 = NetSuiteClientServiceImpl.toNsSearchResult(result1);
        NsSearchResult nsSearchResult2 = NetSuiteClientServiceImpl.toNsSearchResult(result2);

        when(conn.search(eq(nsSearchRecord1))).thenReturn(nsSearchResult1);
        when(conn.searchMoreWithId(eq("abc123"), eq(2))).thenReturn(nsSearchResult2);

        NetSuiteClientService<?> clientService = new NetSuiteClientServiceImpl();
        RecordTypeInfo recordTypeInfo = clientService.getRecordType("InventoryItem");
        SearchRecordTypeDesc searchRecordTypeDesc = clientService.getSearchRecordType(
                recordTypeInfo.getRecordType().getSearchRecordType());

        SearchResultSet<Record> resultSet = new SearchResultSet<>(conn,
                recordTypeInfo.getRecordType(), searchRecordTypeDesc, nsSearchResult1);

        List<Object> recordList = new ArrayList<>();
        while (resultSet.next()) {
            Object record = resultSet.get();
            assertNotNull(record);
            recordList.add(record);
        }

        assertEquals(page1.size(), recordList.size());
    }

}
