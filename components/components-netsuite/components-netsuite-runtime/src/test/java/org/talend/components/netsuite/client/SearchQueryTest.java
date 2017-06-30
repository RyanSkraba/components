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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import javax.xml.datatype.DatatypeFactory;

import org.joda.time.Instant;
import org.joda.time.MutableDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.netsuite.avro.converter.XMLGregorianCalendarToDateTimeConverter;
import org.talend.components.netsuite.client.model.CustomFieldDesc;
import org.talend.components.netsuite.client.model.CustomRecordTypeInfo;
import org.talend.components.netsuite.client.model.RecordTypeInfo;
import org.talend.components.netsuite.client.search.SearchCondition;
import org.talend.components.netsuite.client.search.SearchQuery;
import org.talend.components.netsuite.test.TestUtils;
import org.talend.components.netsuite.test.client.TestNetSuiteClientService;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netsuite.webservices.test.lists.accounting.AccountSearch;
import com.netsuite.webservices.test.platform.NetSuitePortType;
import com.netsuite.webservices.test.platform.common.AccountSearchBasic;
import com.netsuite.webservices.test.platform.common.AddressSearchBasic;
import com.netsuite.webservices.test.platform.common.CustomRecordSearchBasic;
import com.netsuite.webservices.test.platform.common.TransactionSearchBasic;
import com.netsuite.webservices.test.platform.core.SearchBooleanCustomField;
import com.netsuite.webservices.test.platform.core.SearchBooleanField;
import com.netsuite.webservices.test.platform.core.SearchCustomFieldList;
import com.netsuite.webservices.test.platform.core.SearchDateCustomField;
import com.netsuite.webservices.test.platform.core.SearchDateField;
import com.netsuite.webservices.test.platform.core.SearchDoubleField;
import com.netsuite.webservices.test.platform.core.SearchEnumMultiSelectField;
import com.netsuite.webservices.test.platform.core.SearchLongCustomField;
import com.netsuite.webservices.test.platform.core.SearchMultiSelectField;
import com.netsuite.webservices.test.platform.core.SearchRecord;
import com.netsuite.webservices.test.platform.core.SearchStringCustomField;
import com.netsuite.webservices.test.platform.core.SearchStringField;
import com.netsuite.webservices.test.platform.core.types.RecordType;
import com.netsuite.webservices.test.platform.core.types.SearchDate;
import com.netsuite.webservices.test.platform.core.types.SearchDateFieldOperator;
import com.netsuite.webservices.test.platform.core.types.SearchDoubleFieldOperator;
import com.netsuite.webservices.test.platform.core.types.SearchEnumMultiSelectFieldOperator;
import com.netsuite.webservices.test.platform.core.types.SearchLongFieldOperator;
import com.netsuite.webservices.test.platform.core.types.SearchStringFieldOperator;
import com.netsuite.webservices.test.setup.customization.CustomRecordSearch;
import com.netsuite.webservices.test.transactions.sales.TransactionSearch;

/**
 *
 */
public class SearchQueryTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private NetSuiteClientService<NetSuitePortType> clientService = new TestNetSuiteClientService();

    private XMLGregorianCalendarToDateTimeConverter calendarValueConverter;

    @Before
    public void setUp() throws Exception {
        calendarValueConverter = new XMLGregorianCalendarToDateTimeConverter(DatatypeFactory.newInstance());
    }

    @Test
    public void testBasics() throws Exception {

        SearchQuery s1 = clientService.newSearch();
        s1.target("Account");
        s1.condition(new SearchCondition("Type", "List.anyOf", Arrays.asList("bank")));
        s1.condition(new SearchCondition("Balance", "Double.greaterThanOrEqualTo", Arrays.asList("10000.0", "")));
        s1.condition(new SearchCondition("LegalName", "String.contains", Arrays.asList("Acme")));
        s1.condition(new SearchCondition("IsInactive", "Boolean", Arrays.asList("true")));

        s1.condition(new SearchCondition("CustomBooleanField1", "Boolean", Arrays.asList("true")));
        s1.condition(new SearchCondition("CustomStringField1", "String.doesNotContain", Arrays.asList("Foo")));
        s1.condition(new SearchCondition("CustomLongField1", "Long.lessThan", Arrays.asList("100", "")));

        SearchRecord sr1 = (SearchRecord) s1.toNativeQuery();
        assertNotNull(sr1);
        Assert.assertEquals(AccountSearch.class, sr1.getClass());

        AccountSearch search = (AccountSearch) sr1;
        assertNotNull(search.getBasic());

        AccountSearchBasic searchBasic = search.getBasic();
        assertNotNull(searchBasic.getBalance());

        SearchEnumMultiSelectField typeField = searchBasic.getType();
        Assert.assertEquals(SearchEnumMultiSelectFieldOperator.ANY_OF, typeField.getOperator());
        Assert.assertEquals(Arrays.asList("bank"), typeField.getSearchValue());

        SearchDoubleField balanceField = searchBasic.getBalance();
        Assert.assertEquals(SearchDoubleFieldOperator.GREATER_THAN_OR_EQUAL_TO, balanceField.getOperator());
        Assert.assertEquals(Double.valueOf(10000.0), balanceField.getSearchValue());

        SearchBooleanField isInactiveField = searchBasic.getIsInactive();
        Assert.assertEquals(Boolean.TRUE, isInactiveField.getSearchValue());

        SearchStringField legalNameField = searchBasic.getLegalName();
        Assert.assertEquals(SearchStringFieldOperator.CONTAINS, legalNameField.getOperator());
        Assert.assertEquals("Acme", legalNameField.getSearchValue());

        SearchCustomFieldList customFieldList = searchBasic.getCustomFieldList();
        assertNotNull(customFieldList);
        assertNotNull(customFieldList.getCustomField());
        Assert.assertEquals(3, customFieldList.getCustomField().size());

        SearchBooleanCustomField customBooleanField1 = (SearchBooleanCustomField) customFieldList.getCustomField().get(0);
        Assert.assertEquals(Boolean.TRUE, customBooleanField1.getSearchValue());

        SearchStringCustomField customStringField1 = (SearchStringCustomField) customFieldList.getCustomField().get(1);
        Assert.assertEquals(SearchStringFieldOperator.DOES_NOT_CONTAIN, customStringField1.getOperator());
        Assert.assertEquals("Foo", customStringField1.getSearchValue());

        SearchLongCustomField customLongField1 = (SearchLongCustomField) customFieldList.getCustomField().get(2);
        Assert.assertEquals(SearchLongFieldOperator.LESS_THAN, customLongField1.getOperator());
        Assert.assertEquals(Long.valueOf(100), customLongField1.getSearchValue());
    }

    @Test
    public void testSearchOperatorWithoutValues() throws Exception {
        SearchQuery s1 = clientService.newSearch();
        s1.target("Account");
        s1.condition(new SearchCondition("LegalName", "String.notEmpty", null));

        SearchRecord sr1 = (SearchRecord) s1.toNativeQuery();
        AccountSearch search = (AccountSearch) sr1;
        AccountSearchBasic searchBasic = search.getBasic();

        SearchStringField legalNameField = searchBasic.getLegalName();
        Assert.assertEquals(SearchStringFieldOperator.NOT_EMPTY, legalNameField.getOperator());
        assertNull(legalNameField.getSearchValue());
    }

    @Test
    public void testSearchForSpecialRecordTypes() throws Exception {

        SearchQuery s1 = clientService.newSearch();
        s1.target("Address");
        s1.condition(new SearchCondition("Country", "List.anyOf", Arrays.asList("Ukraine")));
        s1.condition(new SearchCondition("CustomStringField1", "String.contains", Arrays.asList("abc")));

        SearchRecord sr1 = (SearchRecord) s1.toNativeQuery();
        assertNotNull(sr1);
        Assert.assertEquals(AddressSearchBasic.class, sr1.getClass());

        AddressSearchBasic search = (AddressSearchBasic) sr1;
        assertNotNull(search.getCountry());

        SearchEnumMultiSelectField field1 = search.getCountry();
        assertNotNull(field1);
        Assert.assertEquals(SearchEnumMultiSelectFieldOperator.ANY_OF, field1.getOperator());
        assertNotNull(field1.getSearchValue());
        Assert.assertEquals(1, field1.getSearchValue().size());
        Assert.assertEquals(Arrays.asList("Ukraine"), field1.getSearchValue());

        SearchCustomFieldList customFieldList = search.getCustomFieldList();
        assertNotNull(customFieldList);
        assertNotNull(customFieldList.getCustomField());
        Assert.assertEquals(1, customFieldList.getCustomField().size());

        SearchStringCustomField customField1 = (SearchStringCustomField) customFieldList.getCustomField().get(0);
        assertNotNull(customField1.getOperator());
        Assert.assertEquals(SearchStringFieldOperator.CONTAINS, customField1.getOperator());
        assertNotNull(customField1.getSearchValue());
        Assert.assertEquals("abc", customField1.getSearchValue());
    }

    @Test
    public void testSearchDateField() throws Exception {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd");
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        DateTimeFormatter timeFormatter = DateTimeFormat.forPattern("HH:mm:ss");

        Instant now = Instant.now();
        String currentDateFormatted = dateFormatter.print(now.getMillis());

        SearchQuery s1 = clientService.newSearch();
        s1.target("Check");
        s1.condition(new SearchCondition("TranDate", "Date.onOrAfter", Arrays.asList("2017-01-01 12:00:00")));
        s1.condition(new SearchCondition("CustomDateField1", "Date.onOrAfter", Arrays.asList("14:00:00")));

        SearchRecord sr1 = (SearchRecord) s1.toNativeQuery();
        assertNotNull(sr1);
        Assert.assertEquals(TransactionSearch.class, sr1.getClass());

        TransactionSearch search = (TransactionSearch) sr1;
        assertNotNull(search.getBasic());

        TransactionSearchBasic searchBasic = search.getBasic();
        assertNotNull(searchBasic.getTranDate());

        SearchDateField field1 = searchBasic.getTranDate();
        Assert.assertEquals(SearchDateFieldOperator.ON_OR_AFTER, field1.getOperator());
        assertNotNull(field1.getSearchValue());
        assertNull(field1.getSearchValue2());
        assertEquals("2017-01-01 12:00:00",
                dateTimeFormatter.print((Long) calendarValueConverter.convertToAvro(field1.getSearchValue())));

        SearchCustomFieldList customFieldList = searchBasic.getCustomFieldList();
        assertNotNull(customFieldList);
        assertNotNull(customFieldList.getCustomField());
        Assert.assertEquals(1, customFieldList.getCustomField().size());

        MutableDateTime controlDateTime = new MutableDateTime();
        controlDateTime.setHourOfDay(14);
        controlDateTime.setMinuteOfHour(0);
        controlDateTime.setSecondOfMinute(0);
        controlDateTime.setMillisOfSecond(0);
        String controlTimeFormatted = timeFormatter.print(controlDateTime);

        SearchDateCustomField customField1 = (SearchDateCustomField) customFieldList.getCustomField().get(0);
        Assert.assertEquals(SearchDateFieldOperator.ON_OR_AFTER, customField1.getOperator());
        assertNotNull(customField1.getSearchValue());
        assertNull(customField1.getSearchValue2());
        assertEquals(currentDateFormatted + " " + controlTimeFormatted,
                dateTimeFormatter.print((Long) calendarValueConverter.convertToAvro(customField1.getSearchValue())));
    }

    @Test
    public void testSearchDateFieldWithPredefinedDate() throws Exception {

        SearchQuery s1 = clientService.newSearch();
        s1.target("Check");
        s1.condition(new SearchCondition("TranDate", "PredefinedDate.lastBusinessWeek", null));
        s1.condition(new SearchCondition("CustomDateField1", "PredefinedDate.lastBusinessWeek", null));

        SearchRecord sr1 = (SearchRecord) s1.toNativeQuery();
        assertNotNull(sr1);
        Assert.assertEquals(TransactionSearch.class, sr1.getClass());

        TransactionSearch search = (TransactionSearch) sr1;
        assertNotNull(search.getBasic());

        TransactionSearchBasic searchBasic = search.getBasic();
        assertNotNull(searchBasic.getTranDate());

        SearchEnumMultiSelectField typeField = searchBasic.getType();
        assertNotNull(typeField);
        assertNotNull(typeField.getSearchValue());
        Assert.assertEquals(1, typeField.getSearchValue().size());
        Assert.assertEquals(RecordType.CHECK.value(), typeField.getSearchValue().get(0));

        SearchDateField field1 = searchBasic.getTranDate();
        assertNull(field1.getOperator());
        assertNull(field1.getSearchValue());
        assertNull(field1.getSearchValue2());
        assertNotNull(field1.getPredefinedSearchValue());
        Assert.assertEquals(SearchDate.LAST_BUSINESS_WEEK, field1.getPredefinedSearchValue());

        SearchCustomFieldList customFieldList = searchBasic.getCustomFieldList();
        assertNotNull(customFieldList);
        assertNotNull(customFieldList.getCustomField());
        Assert.assertEquals(1, customFieldList.getCustomField().size());

        SearchDateCustomField customField1 = (SearchDateCustomField) customFieldList.getCustomField().get(0);
        assertNull(customField1.getOperator());
        assertNull(customField1.getSearchValue());
        assertNull(customField1.getSearchValue2());
        assertNotNull(customField1.getPredefinedSearchValue());
        Assert.assertEquals(SearchDate.LAST_BUSINESS_WEEK, customField1.getPredefinedSearchValue());
    }

    @Test
    public void testSearchMultiSelectField() throws Exception {
        clientService.getMetaDataSource().setCustomMetaDataSource(new TestCustomMetaDataSource());

        SearchQuery s1 = clientService.newSearch();
        s1.target("custom_record_type_1");
        s1.condition(new SearchCondition("Owner", "List.anyOf", Arrays.asList(
                "123456789"
        )));

        SearchRecord sr1 = (SearchRecord) s1.toNativeQuery();
        assertNotNull(sr1);
        Assert.assertEquals(CustomRecordSearch.class, sr1.getClass());

        CustomRecordSearch search = (CustomRecordSearch) sr1;
        assertNotNull(search.getBasic());

        CustomRecordSearchBasic searchBasic = search.getBasic();
        assertNotNull(searchBasic.getOwner());

        SearchMultiSelectField sf1 = searchBasic.getOwner();
        assertNotNull(sf1.getSearchValue());
        Assert.assertEquals(1, sf1.getSearchValue().size());
        Assert.assertEquals("123456789", sf1.getSearchValue().get(0).getInternalId());
    }

    protected class TestCustomMetaDataSource extends EmptyCustomMetaDataSource {

        @Override
        public Collection<CustomRecordTypeInfo> getCustomRecordTypes() {
            return Arrays.asList(getCustomRecordType("custom_record_type_1"));
        }

        @Override
        public CustomRecordTypeInfo getCustomRecordType(String typeName) {
            try {
                if (typeName.equals("custom_record_type_1")) {
                    JsonNode recordTypeNode = objectMapper.readTree(SearchQueryTest.class.getResource(
                            "/test-data/customRecord-1.json"));
                    CustomRecordTypeInfo customRecordTypeInfo =
                            TestUtils.readCustomRecord(clientService.getBasicMetaData(), recordTypeNode);
                    return customRecordTypeInfo;
                }
                return null;
            } catch (IOException e) {
                throw new NetSuiteException(e.getMessage(), e);
            }
        }

        @Override
        public Map<String, CustomFieldDesc> getCustomFields(RecordTypeInfo recordTypeInfo) {
            try {
                if (recordTypeInfo.getName().equals("custom_record_type_1")) {
                    JsonNode fieldListNode = objectMapper.readTree(SearchQueryTest.class.getResource(
                            "/test-data/customRecordFields-1.json"));
                    Map<String, CustomFieldDesc> customFieldDescMap =
                            TestUtils.readCustomFields(fieldListNode);
                    return customFieldDescMap;
                }
                return null;
            } catch (IOException e) {
                throw new NetSuiteException(e.getMessage(), e);
            }
        }

    }
}
