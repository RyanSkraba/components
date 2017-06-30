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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.netsuite.AbstractNetSuiteTestBase;
import org.talend.components.netsuite.client.model.RecordTypeDesc;
import org.talend.components.netsuite.client.model.RecordTypeInfo;
import org.talend.components.netsuite.client.model.SearchRecordTypeDesc;
import org.talend.components.netsuite.client.model.TypeDesc;
import org.talend.components.netsuite.test.client.TestNetSuiteClientService;
import org.talend.components.netsuite.test.client.model.TestRecordTypeEnum;
import org.talend.daikon.NamedThing;

import com.netsuite.webservices.test.lists.accounting.Account;

/**
 *
 */
public class MetaDataSourceTest extends AbstractNetSuiteTestBase {

    private NetSuiteClientService<?> clientService = new TestNetSuiteClientService();

    private DefaultMetaDataSource metaDataSource;

    private TestCustomMetaDataSource customMetaDataSource;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        metaDataSource = new DefaultMetaDataSource(clientService);
        metaDataSource.setCustomizationEnabled(true);

        customMetaDataSource = new TestCustomMetaDataSource(clientService);
        metaDataSource.setCustomMetaDataSource(customMetaDataSource);
    }

    @Test
    public void testGetTypeInfoByClass() {
        TypeDesc typeDesc = metaDataSource.getTypeInfo(Account.class);
        assertNotNull(typeDesc);
    }

    @Test
    public void testGetRecordTypes() {
        Collection<RecordTypeInfo> recordTypeInfos = metaDataSource.getRecordTypes();
        assertNotNull(recordTypeInfos);
        assertFalse(recordTypeInfos.isEmpty());

        Set<String> recordTypeNames = new HashSet<>();
        for (RecordTypeInfo recordTypeInfo : recordTypeInfos) {
            assertNotNull(recordTypeInfo);
            recordTypeNames.add(recordTypeInfo.getName());
        }

        for (RecordTypeDesc recordTypeDesc : TestRecordTypeEnum.values()) {
            assertTrue(recordTypeNames.contains(recordTypeDesc.getTypeName()));
        }

        for (RecordTypeInfo recordTypeInfo : customMetaDataSource.getCustomRecordTypes()) {
            assertTrue(recordTypeNames.contains(recordTypeInfo.getName()));
        }
    }

    @Test
    public void testGetSearchableTypes() {
        Collection<NamedThing> searchableTypeNamedThings = metaDataSource.getSearchableTypes();
        assertNotNull(searchableTypeNamedThings);
        assertFalse(searchableTypeNamedThings.isEmpty());

        Set<String> searchableTypeNames = new HashSet<>();
        for (NamedThing namedThing : searchableTypeNamedThings) {
            assertNotNull(namedThing);
            searchableTypeNames.add(namedThing.getName());
        }

        for (RecordTypeDesc recordTypeDesc : TestRecordTypeEnum.values()) {
            if (recordTypeDesc.getSearchRecordType() != null) {
                assertTrue(recordTypeDesc.getTypeName(), searchableTypeNames.contains(recordTypeDesc.getTypeName()));
            }
        }

        for (RecordTypeInfo recordTypeInfo : customMetaDataSource.getCustomRecordTypes()) {
            assertTrue(recordTypeInfo.getName(), searchableTypeNames.contains(recordTypeInfo.getName()));
        }
    }

    @Test
    public void testGetSearchRecordType() {
        SearchRecordTypeDesc searchRecordTypeDesc = metaDataSource.getSearchRecordType(TestRecordTypeEnum.ACCOUNT.getTypeName());
        assertNotNull(searchRecordTypeDesc);
        searchRecordTypeDesc = metaDataSource.getSearchRecordType(TestRecordTypeEnum.ACCOUNT.getTypeName());
        assertNotNull(searchRecordTypeDesc);

        searchRecordTypeDesc = metaDataSource.getSearchRecordType(TestRecordTypeEnum.CUSTOM_RECORD.getType());
        assertNotNull(searchRecordTypeDesc);
        searchRecordTypeDesc = metaDataSource.getSearchRecordType(TestRecordTypeEnum.CUSTOM_RECORD);
        assertNotNull(searchRecordTypeDesc);

        searchRecordTypeDesc = metaDataSource.getSearchRecordType(TestRecordTypeEnum.CUSTOM_TRANSACTION.getType());
        assertNotNull(searchRecordTypeDesc);
        searchRecordTypeDesc = metaDataSource.getSearchRecordType(TestRecordTypeEnum.CUSTOM_TRANSACTION);
        assertNotNull(searchRecordTypeDesc);

        assertNull(metaDataSource.getSearchRecordType(TestRecordTypeEnum.ENTITY_CUSTOM_FIELD.getTypeName()));
    }
}
