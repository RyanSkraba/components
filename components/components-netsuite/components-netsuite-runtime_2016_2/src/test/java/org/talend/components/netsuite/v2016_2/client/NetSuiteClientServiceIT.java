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
import static org.junit.Assert.assertTrue;
import static org.talend.components.netsuite.client.model.beans.Beans.getProperty;

import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.talend.components.netsuite.AbstractNetSuiteTestBase;
import org.talend.components.netsuite.NetSuiteWebServiceTestFixture;
import org.talend.components.netsuite.client.NetSuiteClientService;
import org.talend.components.netsuite.client.model.RecordTypeDesc;
import org.talend.components.netsuite.client.model.RecordTypeInfo;
import org.talend.components.netsuite.client.model.SearchRecordTypeDesc;
import org.talend.components.netsuite.client.model.TypeDesc;
import org.talend.components.netsuite.client.search.SearchCondition;
import org.talend.components.netsuite.client.search.SearchQuery;
import org.talend.components.netsuite.client.search.SearchResultSet;
import org.talend.components.netsuite.v2016_2.client.model.RecordTypeEnum;
import org.talend.daikon.NamedThing;

import com.netsuite.webservices.v2016_2.lists.accounting.types.AccountType;
import com.netsuite.webservices.v2016_2.platform.core.Record;

/**
 *
 */
public class NetSuiteClientServiceIT extends AbstractNetSuiteTestBase {
    protected static NetSuiteWebServiceTestFixture webServiceTestFixture;

    @BeforeClass
    public static void classSetUp() throws Exception {
        webServiceTestFixture = new NetSuiteWebServiceTestFixture(
                NetSuiteClientFactoryImpl.INSTANCE, "2016_2");
        classScopedTestFixtures.add(webServiceTestFixture);
        setUpClassScopedTestFixtures();
    }

    @AfterClass
    public static void classTearDown() throws Exception {
        tearDownClassScopedTestFixtures();
    }

    @Test
    public void testConnectAndLogin() throws Exception {
        NetSuiteClientService<?> connection = webServiceTestFixture.getClientService();
        connection.login();

        SearchResultSet<Record> rs = connection.newSearch()
                .target("Account")
                .condition(new SearchCondition("Type", "List.anyOf", Arrays.asList("Bank")))
                .search();

        int count = 10;
        int retrievedCount = 0;
        while (rs.next() && count-- > 0) {
            Record record = rs.get();

            assertEquals(AccountType.BANK, getProperty(record, "acctType"));

            retrievedCount++;
        }
        assertTrue(retrievedCount > 0);
    }

    @Test
    public void testSearchCustomRecord() throws Exception {
        NetSuiteClientService<?> connection = webServiceTestFixture.getClientService();
        connection.login();

        Collection<RecordTypeInfo> recordTypes = connection.getMetaDataSource().getRecordTypes();
        RecordTypeInfo recordType = getCustomRecordType(recordTypes, "customrecord_campaign_revenue");

        SearchQuery searchQuery = connection.newSearch();
        searchQuery.target(recordType.getName());

        SearchResultSet<Record> rs = searchQuery.search();
        int count = 10;
        while (rs.next() && count-- > 0) {
            Record record = rs.get();
            assertNotNull(record);
        }
    }

    @Test
    public void testGetSearchableTypes() throws Exception {
        NetSuiteClientService<?> connection = webServiceTestFixture.getClientService();
        connection.login();

        Collection<NamedThing> searches = connection.getMetaDataSource().getSearchableTypes();

        for (NamedThing search : searches) {
            assertNotNull(search);
            assertNotNull(search.getName());
            assertNotNull(search.getDisplayName());

            SearchRecordTypeDesc searchRecordInfo = connection.getMetaDataSource()
                    .getSearchRecordType(search.getName());
            assertNotNull("Search record def found: " + search.getName(), searchRecordInfo);
        }
    }

    @Test
    public void testRetrieveCustomRecordTypes() throws Exception {
        NetSuiteClientService<?> connection = webServiceTestFixture.getClientService();
        connection.login();

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        Collection<RecordTypeInfo> recordTypes = connection.getMetaDataSource().getRecordTypes();
        stopWatch.stop();

        for (RecordTypeInfo recordType : recordTypes) {
            logger.debug("Record type info: {}", recordType);
        }
    }

    @Test
    public void testRetrieveCustomRecordCustomFields() throws Exception {
        NetSuiteClientService<?> connection = webServiceTestFixture.getClientService();
        connection.login();

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        RecordTypeInfo recordType = connection.getMetaDataSource()
                .getRecordType("customrecord_campaign_revenue");

        TypeDesc typeDesc = connection.getMetaDataSource().getTypeInfo(recordType.getName());
        logger.debug("Record type desc: {}", typeDesc.getTypeName());

        stopWatch.stop();
    }

    @Test
    public void testRetrieveAllCustomizations() throws Exception {
        NetSuiteClientService<?> connection = webServiceTestFixture.getClientService();
        connection.login();

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        for (RecordTypeDesc recordType : Arrays.asList(RecordTypeEnum.OPPORTUNITY, RecordTypeEnum.CALENDAR_EVENT)) {
            TypeDesc typeDesc = connection.getMetaDataSource().getTypeInfo(recordType.getTypeName());
            logger.debug("Record type desc: {}", typeDesc.getTypeName());
        }
        stopWatch.stop();
    }

    protected RecordTypeInfo getCustomRecordType(Collection<RecordTypeInfo> recordTypes, String name) {
        for (RecordTypeInfo recordType : recordTypes) {
            if (recordType.getName().equals(name)) {
                return recordType;
            }
        }
        return null;
    }
}