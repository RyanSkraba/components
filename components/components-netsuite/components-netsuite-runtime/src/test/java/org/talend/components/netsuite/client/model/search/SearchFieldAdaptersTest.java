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

package org.talend.components.netsuite.client.model.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.Collections;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Test;
import org.talend.components.netsuite.avro.converter.XMLGregorianCalendarToDateTimeConverter;
import org.talend.components.netsuite.client.model.BasicMetaData;
import org.talend.components.netsuite.client.model.TestBasicMetaDataImpl;

import com.netsuite.webservices.test.platform.core.SearchBooleanCustomField;
import com.netsuite.webservices.test.platform.core.SearchBooleanField;
import com.netsuite.webservices.test.platform.core.SearchDateCustomField;
import com.netsuite.webservices.test.platform.core.SearchDateField;
import com.netsuite.webservices.test.platform.core.SearchDoubleCustomField;
import com.netsuite.webservices.test.platform.core.SearchDoubleField;
import com.netsuite.webservices.test.platform.core.SearchEnumMultiSelectCustomField;
import com.netsuite.webservices.test.platform.core.SearchEnumMultiSelectField;
import com.netsuite.webservices.test.platform.core.SearchLongCustomField;
import com.netsuite.webservices.test.platform.core.SearchLongField;
import com.netsuite.webservices.test.platform.core.SearchMultiSelectCustomField;
import com.netsuite.webservices.test.platform.core.SearchMultiSelectField;
import com.netsuite.webservices.test.platform.core.SearchStringCustomField;
import com.netsuite.webservices.test.platform.core.SearchStringField;
import com.netsuite.webservices.test.platform.core.SearchTextNumberField;
import com.netsuite.webservices.test.platform.core.types.SearchDate;
import com.netsuite.webservices.test.platform.core.types.SearchDateFieldOperator;
import com.netsuite.webservices.test.platform.core.types.SearchDoubleFieldOperator;
import com.netsuite.webservices.test.platform.core.types.SearchEnumMultiSelectFieldOperator;
import com.netsuite.webservices.test.platform.core.types.SearchLongFieldOperator;
import com.netsuite.webservices.test.platform.core.types.SearchMultiSelectFieldOperator;
import com.netsuite.webservices.test.platform.core.types.SearchStringFieldOperator;
import com.netsuite.webservices.test.platform.core.types.SearchTextNumberFieldOperator;

/**
 *
 */
public class SearchFieldAdaptersTest {

    private BasicMetaData basicMetaData = TestBasicMetaDataImpl.getInstance();

    @Test
    public void testAdapterForBooleanSearchField() {
        SearchBooleanFieldAdapter adapter1 = new SearchBooleanFieldAdapter(basicMetaData,
                SearchFieldType.BOOLEAN, SearchBooleanField.class);
        SearchBooleanField field1 = (SearchBooleanField) adapter1.populate(null,
                "Boolean", Arrays.asList("true"));
        assertNotNull(field1);
        assertEquals(Boolean.TRUE, field1.getSearchValue());

        field1 = (SearchBooleanField) adapter1.populate(null,
                "Boolean", null);
        assertNull(field1.getSearchValue());

        SearchBooleanFieldAdapter adapter2 = new SearchBooleanFieldAdapter(basicMetaData,
                SearchFieldType.CUSTOM_BOOLEAN, SearchBooleanCustomField.class);
        SearchBooleanCustomField field2 = (SearchBooleanCustomField) adapter2.populate(null,
                "Boolean", Arrays.asList("true"));
        assertNotNull(field2);
        assertEquals(Boolean.TRUE, field2.getSearchValue());
    }

    @Test
    public void testAdapterForStringSearchField() {
        SearchStringFieldAdapter adapter1 = new SearchStringFieldAdapter(basicMetaData,
                SearchFieldType.STRING, SearchStringField.class);
        SearchStringField field1 = (SearchStringField) adapter1.populate(null,
                "String.startsWith", Arrays.asList("abc"));
        assertNotNull(field1);
        assertEquals(SearchStringFieldOperator.STARTS_WITH, field1.getOperator());
        assertEquals("abc", field1.getSearchValue());

        field1 = (SearchStringField) adapter1.populate(null,
                "String.isNot", null);
        assertEquals(SearchStringFieldOperator.IS_NOT, field1.getOperator());
        assertNull(field1.getSearchValue());

        SearchStringFieldAdapter adapter2 = new SearchStringFieldAdapter(basicMetaData,
                SearchFieldType.CUSTOM_STRING, SearchStringCustomField.class);
        SearchStringCustomField field2 = (SearchStringCustomField) adapter2.populate(null,
                "String.contains", Arrays.asList("qwerty"));
        assertNotNull(field2);
        assertEquals(SearchStringFieldOperator.CONTAINS, field2.getOperator());
        assertEquals("qwerty", field2.getSearchValue());
    }

    @Test
    public void testAdapterForLongSearchField() {
        SearchLongFieldAdapter adapter1 = new SearchLongFieldAdapter(basicMetaData,
                SearchFieldType.LONG, SearchLongField.class);
        SearchLongField field1 = (SearchLongField) adapter1.populate(null,
                "Long.greaterThanOrEqualTo", Arrays.asList("12000"));
        assertNotNull(field1);
        assertEquals(SearchLongFieldOperator.GREATER_THAN_OR_EQUAL_TO, field1.getOperator());
        assertEquals(Long.valueOf(12000), field1.getSearchValue());

        field1 = (SearchLongField) adapter1.populate(null,
                "Long.between", Arrays.asList("10000", "30000"));
        assertEquals(SearchLongFieldOperator.BETWEEN, field1.getOperator());
        assertEquals(Long.valueOf(10000), field1.getSearchValue());
        assertEquals(Long.valueOf(30000), field1.getSearchValue2());

        field1 = (SearchLongField) adapter1.populate(null,
                "Long.lessThan", null);
        assertEquals(SearchLongFieldOperator.LESS_THAN, field1.getOperator());
        assertNull(field1.getSearchValue());

        SearchLongFieldAdapter adapter2 = new SearchLongFieldAdapter(basicMetaData,
                SearchFieldType.CUSTOM_LONG, SearchLongCustomField.class);
        SearchLongCustomField field2 = (SearchLongCustomField) adapter2.populate(null,
                "Long.notEqualTo", Arrays.asList("-1"));
        assertNotNull(field2);
        assertEquals(SearchLongFieldOperator.NOT_EQUAL_TO, field2.getOperator());
        assertEquals(Long.valueOf(-1), field2.getSearchValue());
    }

    @Test
    public void testAdapterForDoubleSearchField() {
        SearchDoubleFieldAdapter adapter1 = new SearchDoubleFieldAdapter(basicMetaData,
                SearchFieldType.DOUBLE, SearchDoubleField.class);
        SearchDoubleField field1 = (SearchDoubleField) adapter1.populate(null,
                "Double.greaterThanOrEqualTo", Arrays.asList("1.25"));
        assertNotNull(field1);
        assertEquals(SearchDoubleFieldOperator.GREATER_THAN_OR_EQUAL_TO, field1.getOperator());
        assertEquals(Double.valueOf(1.25), field1.getSearchValue());

        field1 = (SearchDoubleField) adapter1.populate(null,
                "Double.between", Arrays.asList("1.05", "1.35"));
        assertEquals(SearchDoubleFieldOperator.BETWEEN, field1.getOperator());
        assertEquals(Double.valueOf(1.05), field1.getSearchValue());
        assertEquals(Double.valueOf(1.35), field1.getSearchValue2());

        field1 = (SearchDoubleField) adapter1.populate(null,
                "Double.lessThan", null);
        assertEquals(SearchDoubleFieldOperator.LESS_THAN, field1.getOperator());
        assertNull(field1.getSearchValue());

        SearchDoubleFieldAdapter adapter2 = new SearchDoubleFieldAdapter(basicMetaData,
                SearchFieldType.CUSTOM_DOUBLE, SearchDoubleCustomField.class);
        SearchDoubleCustomField field2 = (SearchDoubleCustomField) adapter2.populate(null,
                "Double.notEqualTo", Arrays.asList("0.01"));
        assertNotNull(field2);
        assertEquals(SearchDoubleFieldOperator.NOT_EQUAL_TO, field2.getOperator());
        assertEquals(Double.valueOf(0.01), field2.getSearchValue());
    }

    @Test
    public void testAdapterForDateSearchField() throws Exception {
        XMLGregorianCalendarToDateTimeConverter calendarValueConverter = new XMLGregorianCalendarToDateTimeConverter(
                DatatypeFactory.newInstance());

        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd");

        String dateString1 = "2017-01-01";
        String dateString2 = "2017-02-01";
        XMLGregorianCalendar xmlGregorianCalendar1 = calendarValueConverter.convertToDatum(
                dateFormatter.parseDateTime(dateString1).getMillis());
        XMLGregorianCalendar xmlGregorianCalendar2 = calendarValueConverter.convertToDatum(
                dateFormatter.parseDateTime(dateString2).getMillis());

        SearchDateFieldAdapter adapter1 = new SearchDateFieldAdapter(basicMetaData,
                SearchFieldType.DATE, SearchDateField.class);
        SearchDateField field1 = (SearchDateField) adapter1.populate(null,
                "Date.onOrAfter", Arrays.asList(dateString1));
        assertNotNull(field1);
        assertEquals(SearchDateFieldOperator.ON_OR_AFTER, field1.getOperator());
        assertEquals(xmlGregorianCalendar1, field1.getSearchValue());

        field1 = (SearchDateField) adapter1.populate(null,
                "Date.within", Arrays.asList(dateString1, dateString2));
        assertEquals(SearchDateFieldOperator.WITHIN, field1.getOperator());
        assertEquals(xmlGregorianCalendar1, field1.getSearchValue());
        assertEquals(xmlGregorianCalendar2, field1.getSearchValue2());

        field1 = (SearchDateField) adapter1.populate(null,
                "Date.before", null);
        assertEquals(SearchDateFieldOperator.BEFORE, field1.getOperator());
        assertNull(field1.getSearchValue());

        SearchDateFieldAdapter adapter2 = new SearchDateFieldAdapter(basicMetaData,
                SearchFieldType.CUSTOM_DATE, SearchDateCustomField.class);
        SearchDateCustomField field2 = (SearchDateCustomField) adapter2.populate(null,
                "Date.notBefore", Arrays.asList(dateString1));
        assertNotNull(field2);
        assertEquals(SearchDateFieldOperator.NOT_BEFORE, field2.getOperator());
        assertEquals(xmlGregorianCalendar1, field2.getSearchValue());
        assertNull(field2.getSearchValue2());

        field2 = (SearchDateCustomField) adapter2.populate(null,
                "PredefinedDate.lastFiscalQuarter", null);
        assertNotNull(field2);
        assertEquals(SearchDate.LAST_FISCAL_QUARTER, field2.getPredefinedSearchValue());
        assertNull(field2.getSearchValue());
        assertNull(field2.getSearchValue2());
    }

    @Test
    public void testAdapterForTextNumberSearchField() {
        SearchTextNumberFieldAdapter adapter1 = new SearchTextNumberFieldAdapter(basicMetaData,
                SearchFieldType.LONG, SearchTextNumberField.class);
        SearchTextNumberField field1 = (SearchTextNumberField) adapter1.populate(null,
                "TextNumber.greaterThanOrEqualTo", Arrays.asList("12000"));
        assertNotNull(field1);
        assertEquals(SearchTextNumberFieldOperator.GREATER_THAN_OR_EQUAL_TO, field1.getOperator());
        assertEquals("12000", field1.getSearchValue());

        field1 = (SearchTextNumberField) adapter1.populate(null,
                "TextNumber.between", Arrays.asList("10000", "30000"));
        assertEquals(SearchTextNumberFieldOperator.BETWEEN, field1.getOperator());
        assertEquals("10000", field1.getSearchValue());
        assertEquals("30000", field1.getSearchValue2());

        field1 = (SearchTextNumberField) adapter1.populate(null,
                "TextNumber.lessThan", null);
        assertEquals(SearchTextNumberFieldOperator.LESS_THAN, field1.getOperator());
        assertNull(field1.getSearchValue());
    }

    @Test
    public void testAdapterForMultiSelectSearchField() {
        SearchMultiSelectFieldAdapter adapter1 = new SearchMultiSelectFieldAdapter(basicMetaData,
                SearchFieldType.MULTI_SELECT, SearchMultiSelectField.class);
        SearchMultiSelectField field1 = (SearchMultiSelectField) adapter1.populate(null,
                "List.anyOf", Arrays.asList("abc"));
        assertNotNull(field1);
        assertEquals(SearchMultiSelectFieldOperator.ANY_OF, field1.getOperator());
        assertNotNull(field1.getSearchValue());
        assertEquals(1, field1.getSearchValue().size());
        assertEquals("abc", field1.getSearchValue().get(0).getInternalId());

        field1 = (SearchMultiSelectField) adapter1.populate(null,
                "List.noneOf", Collections.emptyList());
        assertEquals(SearchMultiSelectFieldOperator.NONE_OF, field1.getOperator());
        assertNotNull(field1.getSearchValue());
        assertEquals(0, field1.getSearchValue().size());

        SearchMultiSelectFieldAdapter adapter2 = new SearchMultiSelectFieldAdapter(basicMetaData,
                SearchFieldType.CUSTOM_MULTI_SELECT, SearchMultiSelectCustomField.class);
        SearchMultiSelectCustomField field2 = (SearchMultiSelectCustomField) adapter2.populate(null,
                "List.anyOf", Arrays.asList("qwerty"));
        assertNotNull(field2);
        assertEquals(SearchMultiSelectFieldOperator.ANY_OF, field2.getOperator());
        assertEquals(1, field2.getSearchValue().size());
        assertEquals("qwerty", field2.getSearchValue().get(0).getInternalId());
    }

    @Test
    public void testAdapterForEnumMultiSelectSearchField() {
        SearchEnumMultiSelectFieldAdapter adapter1 = new SearchEnumMultiSelectFieldAdapter(basicMetaData,
                SearchFieldType.SELECT, SearchEnumMultiSelectField.class);
        SearchEnumMultiSelectField field1 = (SearchEnumMultiSelectField) adapter1.populate(null,
                "List.anyOf", Arrays.asList("abc"));
        assertNotNull(field1);
        assertEquals(SearchEnumMultiSelectFieldOperator.ANY_OF, field1.getOperator());
        assertNotNull(field1.getSearchValue());
        assertEquals(1, field1.getSearchValue().size());
        assertEquals("abc", field1.getSearchValue().get(0));

        field1 = (SearchEnumMultiSelectField) adapter1.populate(null,
                "List.noneOf", Collections.emptyList());
        assertEquals(SearchEnumMultiSelectFieldOperator.NONE_OF, field1.getOperator());
        assertNotNull(field1.getSearchValue());
        assertEquals(0, field1.getSearchValue().size());

        SearchEnumMultiSelectFieldAdapter adapter2 = new SearchEnumMultiSelectFieldAdapter(basicMetaData,
                SearchFieldType.CUSTOM_SELECT, SearchEnumMultiSelectCustomField.class);
        SearchEnumMultiSelectCustomField field2 = (SearchEnumMultiSelectCustomField) adapter2.populate(null,
                "List.anyOf", Arrays.asList("qwerty"));
        assertNotNull(field2);
        assertEquals(SearchEnumMultiSelectFieldOperator.ANY_OF, field2.getOperator());
        assertEquals(1, field2.getSearchValue().size());
        assertEquals("qwerty", field2.getSearchValue().get(0));
    }
}
