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

package org.talend.components.netsuite.client.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import org.junit.Test;
import org.talend.components.netsuite.client.NetSuiteException;
import org.talend.components.netsuite.client.model.customfield.CustomFieldRefType;
import org.talend.components.netsuite.client.model.search.SearchFieldAdapter;
import org.talend.components.netsuite.client.model.search.SearchFieldOperatorName;
import org.talend.components.netsuite.client.model.search.SearchFieldType;
import org.talend.components.netsuite.test.client.model.TestRecordTypeEnum;
import org.talend.components.netsuite.test.client.model.TestSearchRecordTypeEnum;

import com.netsuite.webservices.test.lists.accounting.Account;
import com.netsuite.webservices.test.platform.core.CustomFieldList;
import com.netsuite.webservices.test.platform.core.NullField;
import com.netsuite.webservices.test.platform.core.RecordRef;
import com.netsuite.webservices.test.platform.core.StringCustomFieldRef;
import com.netsuite.webservices.test.platform.core.types.SearchRecordType;
import com.netsuite.webservices.test.setup.customization.CustomRecord;
import com.netsuite.webservices.test.setup.customization.EntityCustomField;
import com.netsuite.webservices.test.setup.customization.types.CustomizationFieldType;

/**
 *
 */
public class BasicMetaDataTest {

    private BasicMetaData basicMetaData = TestBasicMetaDataImpl.getInstance();

    @Test
    public void testBasicRecordType() {
        Collection<String> typeNames = Arrays.asList(
                "crmCustomField",
                "entityCustomField",
                "itemCustomField",
                "itemNumberCustomField",
                "itemOptionCustomField",
                "otherCustomField",
                "transactionBodyCustomField",
                "transactionColumnCustomField",
                "customRecordCustomField",
                "transaction",
                "item",
                "customList",
                "customRecord",
                "customRecordType",
                "customTransaction",
                "customTransactionType"
        );

        for (String typeName : typeNames) {
            BasicRecordType value = BasicRecordType.getByType(typeName);
            assertNotNull(value);
            if (value.getSearchType() != null) {
                assertNotNull(SearchRecordType.fromValue(value.getSearchType()));
            }
        }
    }

    @Test
    public void testGetTypeClass() {
        assertEquals(Account.class, basicMetaData.getTypeClass(TestRecordTypeEnum.ACCOUNT.getTypeName()));
        assertEquals(CustomRecord.class, basicMetaData.getTypeClass(TestRecordTypeEnum.CUSTOM_RECORD.getTypeName()));
        assertEquals(RecordRef.class, basicMetaData.getTypeClass(RefType.RECORD_REF.getTypeName()));
        assertEquals(StringCustomFieldRef.class, basicMetaData.getTypeClass(CustomFieldRefType.STRING.getTypeName()));
        assertEquals(CustomFieldList.class, basicMetaData.getTypeClass("CustomFieldList"));
        assertEquals(NullField.class, basicMetaData.getTypeClass("NullField"));
        assertNull(basicMetaData.getTypeClass("Unknown"));
    }

    @Test
    public void testGetTypeInfo() {
        TypeDesc typeDesc1 = basicMetaData.getTypeInfo(basicMetaData.getTypeClass(TestRecordTypeEnum.ACCOUNT.getTypeName()));
        assertNotNull(typeDesc1);
        assertFalse(typeDesc1.getFields().isEmpty());
        assertNull(typeDesc1.getField("class"));

        assertNotNull(typeDesc1.getField("internalId"));
        assertNotNull(typeDesc1.getField("internalId").asSimple().getPropertyName());
        assertTrue(typeDesc1.getField("internalId").isKey());

        assertNotNull(typeDesc1.getField("externalId"));
        assertNotNull(typeDesc1.getField("externalId").asSimple().getPropertyName());
        assertTrue(typeDesc1.getField("externalId").isKey());

        TypeDesc typeDesc2 = basicMetaData.getTypeInfo(basicMetaData.getTypeClass(TestRecordTypeEnum.CUSTOM_RECORD.getTypeName()));

        assertNotNull(typeDesc2.getField("scriptId"));
        assertNotNull(typeDesc2.getField("scriptId").asSimple().getPropertyName());
        assertTrue(typeDesc2.getField("scriptId").isKey());
    }

    @Test
    public void testGetSearchRecordType() {
        SearchRecordTypeDesc searchRecordTypeDesc = basicMetaData.getSearchRecordType(TestRecordTypeEnum.ACCOUNT);
        assertEquals(TestSearchRecordTypeEnum.ACCOUNT, searchRecordTypeDesc);
    }

    @Test
    public void testGetSearchFieldType() {
        for (SearchFieldType searchFieldType : SearchFieldType.values()) {
            Class<?> clazz = basicMetaData.getSearchFieldClass(searchFieldType.getFieldTypeName());
            assertNotNull(clazz);
        }
    }

    @Test
    public void testGetSearchFieldOperatorByName() {
        for (SearchFieldType searchFieldType : SearchFieldType.values()) {
            if (searchFieldType == SearchFieldType.BOOLEAN) {
                Object operatorByName = basicMetaData.getSearchFieldOperatorByName(
                        searchFieldType.getFieldTypeName(), "Boolean");
                assertNotNull(operatorByName);
            } else if (searchFieldType == SearchFieldType.STRING || searchFieldType == SearchFieldType.CUSTOM_STRING) {
                Object operatorByName = basicMetaData.getSearchFieldOperatorByName(
                        searchFieldType.getFieldTypeName(), "String.startsWith");
                assertNotNull(operatorByName);
            } else if (searchFieldType == SearchFieldType.LONG || searchFieldType == SearchFieldType.CUSTOM_LONG) {
                Object operatorByName = basicMetaData.getSearchFieldOperatorByName(
                        searchFieldType.getFieldTypeName(), "Long.greaterThan");
                assertNotNull(operatorByName);
            } else if (searchFieldType == SearchFieldType.DOUBLE || searchFieldType == SearchFieldType.CUSTOM_DOUBLE) {
                Object operatorByName = basicMetaData.getSearchFieldOperatorByName(
                        searchFieldType.getFieldTypeName(), "Double.greaterThanOrEqualTo");
                assertNotNull(operatorByName);
            } else if (searchFieldType == SearchFieldType.DATE || searchFieldType == SearchFieldType.CUSTOM_DATE) {
                Object operatorByName = basicMetaData.getSearchFieldOperatorByName(
                        searchFieldType.getFieldTypeName(), "Date.after");
                assertNotNull(operatorByName);
            } else if (searchFieldType == SearchFieldType.TEXT_NUMBER) {
                Object operatorByName = basicMetaData.getSearchFieldOperatorByName(
                        searchFieldType.getFieldTypeName(), "TextNumber.lessThan");
                assertNotNull(operatorByName);
            } else if (searchFieldType == SearchFieldType.SELECT || searchFieldType == SearchFieldType.CUSTOM_SELECT ||
                    searchFieldType == SearchFieldType.MULTI_SELECT || searchFieldType == SearchFieldType.CUSTOM_MULTI_SELECT) {
                Object operatorByName = basicMetaData.getSearchFieldOperatorByName(
                        searchFieldType.getFieldTypeName(), "List.anyOf");
                assertNotNull(operatorByName);
            }
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetSearchFieldOperatorInvalid() {
        basicMetaData.getSearchFieldOperatorByName(SearchFieldType.BOOLEAN.getFieldTypeName(), "Boolean.is");
    }

    @Test
    public void testGetSearchOperatorNames() {
        Collection<SearchFieldOperatorName> operatorNames = basicMetaData.getSearchOperatorNames();
        Collection<String> dataTypes = new HashSet<>();
        for (SearchFieldOperatorName operatorName : operatorNames) {
            dataTypes.add(operatorName.getDataType());
        }
        assertThat(dataTypes, containsInAnyOrder(
                "Boolean", "String", "Long", "Double", "TextNumber", "Date", "PredefinedDate", "List"));
    }


    @Test
    public void testGetSearchFieldAdapter() {
        for (SearchFieldType searchFieldType : SearchFieldType.values()) {
            SearchFieldAdapter<?> searchFieldAdapter = basicMetaData.getSearchFieldAdapter(searchFieldType);
            assertNotNull(searchFieldAdapter);
            assertEquals(searchFieldType, searchFieldAdapter.getFieldType());
        }
    }

    @Test
    public void testGetCustomFieldRefType() {

        EntityCustomField entityCustomField1 = new EntityCustomField();
        entityCustomField1.setFieldType(CustomizationFieldType.FREE_FORM_TEXT);
        entityCustomField1.setAppliesToContact(true);
        entityCustomField1.setAppliesToVendor(true);

        CustomFieldRefType customFieldRefType = basicMetaData.getCustomFieldRefType(
                TestRecordTypeEnum.CONTACT.getType(), BasicRecordType.ENTITY_CUSTOM_FIELD, entityCustomField1);
        assertEquals(CustomFieldRefType.STRING, customFieldRefType);

        customFieldRefType = basicMetaData.getCustomFieldRefType(
                TestRecordTypeEnum.VENDOR.getType(), BasicRecordType.ENTITY_CUSTOM_FIELD, entityCustomField1);
        assertEquals(CustomFieldRefType.STRING, customFieldRefType);

        customFieldRefType = basicMetaData.getCustomFieldRefType(
                TestRecordTypeEnum.PARTNER.getType(), BasicRecordType.ENTITY_CUSTOM_FIELD, entityCustomField1);
        assertNull(customFieldRefType);
    }

    @Test
    public void testCreateInstance() {
        Object instance = basicMetaData.createInstance(TestRecordTypeEnum.ACCOUNT.getTypeName());
        assertNotNull(instance);
        assertThat(instance, instanceOf(Account.class));
    }

    @Test(expected = NetSuiteException.class)
    public void testCreateInstanceForUnknownType() {
        basicMetaData.createInstance("Unknown");
    }
}
