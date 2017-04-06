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
import static org.junit.Assert.assertNull;

import java.util.Arrays;

import javax.xml.datatype.DatatypeFactory;

import org.joda.time.Instant;
import org.joda.time.MutableDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.netsuite.avro.converter.XMLGregorianCalendarToLongConverter;
import org.talend.components.netsuite.client.NetSuiteClientService;
import org.talend.components.netsuite.client.search.SearchCondition;
import org.talend.components.netsuite.client.search.SearchQuery;

import com.netsuite.webservices.v2016_2.lists.accounting.AccountSearch;
import com.netsuite.webservices.v2016_2.platform.NetSuitePortType;
import com.netsuite.webservices.v2016_2.platform.common.AccountSearchBasic;
import com.netsuite.webservices.v2016_2.platform.common.AddressSearchBasic;
import com.netsuite.webservices.v2016_2.platform.common.TransactionSearchBasic;
import com.netsuite.webservices.v2016_2.platform.core.SearchBooleanCustomField;
import com.netsuite.webservices.v2016_2.platform.core.SearchBooleanField;
import com.netsuite.webservices.v2016_2.platform.core.SearchCustomFieldList;
import com.netsuite.webservices.v2016_2.platform.core.SearchDateCustomField;
import com.netsuite.webservices.v2016_2.platform.core.SearchDateField;
import com.netsuite.webservices.v2016_2.platform.core.SearchDoubleField;
import com.netsuite.webservices.v2016_2.platform.core.SearchEnumMultiSelectField;
import com.netsuite.webservices.v2016_2.platform.core.SearchLongCustomField;
import com.netsuite.webservices.v2016_2.platform.core.SearchRecord;
import com.netsuite.webservices.v2016_2.platform.core.SearchStringCustomField;
import com.netsuite.webservices.v2016_2.platform.core.SearchStringField;
import com.netsuite.webservices.v2016_2.platform.core.types.RecordType;
import com.netsuite.webservices.v2016_2.platform.core.types.SearchDate;
import com.netsuite.webservices.v2016_2.platform.core.types.SearchDateFieldOperator;
import com.netsuite.webservices.v2016_2.platform.core.types.SearchDoubleFieldOperator;
import com.netsuite.webservices.v2016_2.platform.core.types.SearchEnumMultiSelectFieldOperator;
import com.netsuite.webservices.v2016_2.platform.core.types.SearchLongFieldOperator;
import com.netsuite.webservices.v2016_2.platform.core.types.SearchStringFieldOperator;
import com.netsuite.webservices.v2016_2.transactions.sales.TransactionSearch;

/**
 *
 */
public class SearchQueryTest {

    private NetSuiteClientService<NetSuitePortType> clientService = new NetSuiteClientServiceImpl();

    private XMLGregorianCalendarToLongConverter calendarValueConverter;

    @Before
    public void setUp() throws Exception {
        calendarValueConverter = new XMLGregorianCalendarToLongConverter(DatatypeFactory.newInstance());
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
        assertEquals(AccountSearch.class, sr1.getClass());

        AccountSearch search = (AccountSearch) sr1;
        assertNotNull(search.getBasic());

        AccountSearchBasic searchBasic = search.getBasic();
        assertNotNull(searchBasic.getBalance());

        SearchEnumMultiSelectField typeField = searchBasic.getType();
        assertEquals(SearchEnumMultiSelectFieldOperator.ANY_OF, typeField.getOperator());
        assertEquals(Arrays.asList("bank"), typeField.getSearchValue());

        SearchDoubleField balanceField = searchBasic.getBalance();
        assertEquals(SearchDoubleFieldOperator.GREATER_THAN_OR_EQUAL_TO, balanceField.getOperator());
        assertEquals(Double.valueOf(10000.0), balanceField.getSearchValue());

        SearchBooleanField isInactiveField = searchBasic.getIsInactive();
        assertEquals(Boolean.TRUE, isInactiveField.getSearchValue());

        SearchStringField legalNameField = searchBasic.getLegalName();
        assertEquals(SearchStringFieldOperator.CONTAINS, legalNameField.getOperator());
        assertEquals("Acme", legalNameField.getSearchValue());

        SearchCustomFieldList customFieldList = searchBasic.getCustomFieldList();
        assertNotNull(customFieldList);
        assertNotNull(customFieldList.getCustomField());
        assertEquals(3, customFieldList.getCustomField().size());

        SearchBooleanCustomField customBooleanField1 = (SearchBooleanCustomField) customFieldList.getCustomField().get(0);
        assertEquals(Boolean.TRUE, customBooleanField1.getSearchValue());

        SearchStringCustomField customStringField1 = (SearchStringCustomField) customFieldList.getCustomField().get(1);
        assertEquals(SearchStringFieldOperator.DOES_NOT_CONTAIN, customStringField1.getOperator());
        assertEquals("Foo", customStringField1.getSearchValue());

        SearchLongCustomField customLongField1 = (SearchLongCustomField) customFieldList.getCustomField().get(2);
        assertEquals(SearchLongFieldOperator.LESS_THAN, customLongField1.getOperator());
        assertEquals(Long.valueOf(100), customLongField1.getSearchValue());
    }

    @Test
    public void testSearchForSpecialRecordTypes() throws Exception {

        SearchQuery s1 = clientService.newSearch();
        s1.target("Address");
        s1.condition(new SearchCondition("Country", "List.anyOf", Arrays.asList("Ukraine")));
        s1.condition(new SearchCondition("CustomStringField1", "String.contains", Arrays.asList("abc")));

        SearchRecord sr1 = (SearchRecord) s1.toNativeQuery();
        assertNotNull(sr1);
        assertEquals(AddressSearchBasic.class, sr1.getClass());

        AddressSearchBasic search = (AddressSearchBasic) sr1;
        assertNotNull(search.getCountry());

        SearchEnumMultiSelectField field1 = search.getCountry();
        assertNotNull(field1);
        assertEquals(SearchEnumMultiSelectFieldOperator.ANY_OF, field1.getOperator());
        assertNotNull(field1.getSearchValue());
        assertEquals(1, field1.getSearchValue().size());
        assertEquals(Arrays.asList("Ukraine"), field1.getSearchValue());

        SearchCustomFieldList customFieldList = search.getCustomFieldList();
        assertNotNull(customFieldList);
        assertNotNull(customFieldList.getCustomField());
        assertEquals(1, customFieldList.getCustomField().size());

        SearchStringCustomField customField1 = (SearchStringCustomField) customFieldList.getCustomField().get(0);
        assertNotNull(customField1.getOperator());
        assertEquals(SearchStringFieldOperator.CONTAINS, customField1.getOperator());
        assertNotNull(customField1.getSearchValue());
        assertEquals("abc", customField1.getSearchValue());
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
        assertEquals(TransactionSearch.class, sr1.getClass());

        TransactionSearch search = (TransactionSearch) sr1;
        assertNotNull(search.getBasic());

        TransactionSearchBasic searchBasic = search.getBasic();
        assertNotNull(searchBasic.getTranDate());

        SearchDateField field1 = searchBasic.getTranDate();
        assertEquals(SearchDateFieldOperator.ON_OR_AFTER, field1.getOperator());
        assertNotNull(field1.getSearchValue());
        assertNull(field1.getSearchValue2());
        assertEquals("2017-01-01 12:00:00",
                dateTimeFormatter.print(calendarValueConverter.convertToAvro(field1.getSearchValue())));

        SearchCustomFieldList customFieldList = searchBasic.getCustomFieldList();
        assertNotNull(customFieldList);
        assertNotNull(customFieldList.getCustomField());
        assertEquals(1, customFieldList.getCustomField().size());

        MutableDateTime controlDateTime = new MutableDateTime();
        controlDateTime.setHourOfDay(14);
        controlDateTime.setMinuteOfHour(0);
        controlDateTime.setSecondOfMinute(0);
        controlDateTime.setMillisOfSecond(0);
        String controlTimeFormatted = timeFormatter.print(controlDateTime);

        SearchDateCustomField customField1 = (SearchDateCustomField) customFieldList.getCustomField().get(0);
        assertEquals(SearchDateFieldOperator.ON_OR_AFTER, customField1.getOperator());
        assertNotNull(customField1.getSearchValue());
        assertNull(customField1.getSearchValue2());
        assertEquals(currentDateFormatted + " " + controlTimeFormatted,
                dateTimeFormatter.print(calendarValueConverter.convertToAvro(customField1.getSearchValue())));
    }

    @Test
    public void testSearchDateFieldWithPredefinedDate() throws Exception {

        SearchQuery s1 = clientService.newSearch();
        s1.target("Check");
        s1.condition(new SearchCondition("TranDate", "PredefinedDate.lastBusinessWeek", null));
        s1.condition(new SearchCondition("CustomDateField1", "PredefinedDate.lastBusinessWeek", null));

        SearchRecord sr1 = (SearchRecord) s1.toNativeQuery();
        assertNotNull(sr1);
        assertEquals(TransactionSearch.class, sr1.getClass());

        TransactionSearch search = (TransactionSearch) sr1;
        assertNotNull(search.getBasic());

        TransactionSearchBasic searchBasic = search.getBasic();
        assertNotNull(searchBasic.getTranDate());

        SearchEnumMultiSelectField typeField = searchBasic.getType();
        assertNotNull(typeField);
        assertNotNull(typeField.getSearchValue());
        assertEquals(1, typeField.getSearchValue().size());
        assertEquals(RecordType.CHECK.value(), typeField.getSearchValue().get(0));

        SearchDateField field1 = searchBasic.getTranDate();
        assertNull(field1.getOperator());
        assertNull(field1.getSearchValue());
        assertNull(field1.getSearchValue2());
        assertNotNull(field1.getPredefinedSearchValue());
        assertEquals(SearchDate.LAST_BUSINESS_WEEK, field1.getPredefinedSearchValue());

        SearchCustomFieldList customFieldList = searchBasic.getCustomFieldList();
        assertNotNull(customFieldList);
        assertNotNull(customFieldList.getCustomField());
        assertEquals(1, customFieldList.getCustomField().size());

        SearchDateCustomField customField1 = (SearchDateCustomField) customFieldList.getCustomField().get(0);
        assertNull(customField1.getOperator());
        assertNull(customField1.getSearchValue());
        assertNull(customField1.getSearchValue2());
        assertNotNull(customField1.getPredefinedSearchValue());
        assertEquals(SearchDate.LAST_BUSINESS_WEEK, customField1.getPredefinedSearchValue());
    }
}
