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

import static org.talend.components.netsuite.client.model.beans.Beans.setSimpleProperty;

import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.talend.components.netsuite.NetSuiteErrorCode;
import org.talend.components.netsuite.NetSuiteRuntimeI18n;
import org.talend.components.netsuite.client.NetSuiteException;
import org.talend.components.netsuite.client.model.BasicMetaData;

/**
 * Search field adapter for {@code SearchDateField} and {@code SearchDateCustomField}.
 */
public class SearchDateFieldAdapter<T> extends SearchFieldAdapter<T> {

    private DatatypeFactory datatypeFactory;
    private String dateFormatPattern = "yyyy-MM-dd";
    private String timeFormatPattern = "HH:mm:ss";

    public SearchDateFieldAdapter(BasicMetaData metaData, SearchFieldType fieldType, Class<T> fieldClass) {
        super(metaData, fieldType, fieldClass);

        try {
            datatypeFactory = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw new NetSuiteException("Failed to create XML data type factory", e);
        }
    }

    @Override
    public T populate(T fieldObject, String internalId, String operatorName, List<String> values) {
        T nsObject = fieldObject != null ? fieldObject : createField(internalId);

        SearchFieldOperatorName operatorQName =
                new SearchFieldOperatorName(operatorName);

        if (SearchFieldOperatorType.PREDEFINED_DATE.dataTypeEquals(operatorQName.getDataType())) {
            setSimpleProperty(nsObject, "predefinedSearchValue",
                    metaData.getSearchFieldOperatorByName(fieldType.getFieldTypeName(), operatorName));
        } else {
            if (values != null && values.size() != 0) {
                setSimpleProperty(nsObject,"searchValue", convertDateTime(values.get(0)));

                if (values.size() > 1 && StringUtils.isNotEmpty(values.get(1))) {
                    setSimpleProperty(nsObject, "searchValue2", convertDateTime(values.get(1)));
                }
            }

            setSimpleProperty(nsObject, "operator",
                    metaData.getSearchFieldOperatorByName(fieldType.getFieldTypeName(), operatorName));
        }

        return nsObject;
    }

    protected XMLGregorianCalendar convertDateTime(String input) {
        String valueToParse = input;
        String dateTimeFormatPattern = dateFormatPattern + " " + timeFormatPattern;
        if (input.length() == dateFormatPattern.length()) {
            dateTimeFormatPattern = dateFormatPattern;
        } else if (input.length() == timeFormatPattern.length()) {
            DateTime dateTime = new DateTime();
            DateTimeFormatter dateFormatter = DateTimeFormat.forPattern(dateFormatPattern);
            valueToParse = dateFormatter.print(dateTime) + " " + input;
        }

        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(dateTimeFormatPattern);

        DateTime dateTime;
        try {
            dateTime = dateTimeFormatter.parseDateTime(valueToParse);
        } catch (IllegalArgumentException e) {
            throw new NetSuiteException(new NetSuiteErrorCode(NetSuiteErrorCode.CLIENT_ERROR),
                    NetSuiteRuntimeI18n.MESSAGES.getMessage("error.searchDateField.invalidDateTimeFormat",
                            valueToParse));
        }

        XMLGregorianCalendar xts = datatypeFactory.newXMLGregorianCalendar();
        xts.setYear(dateTime.getYear());
        xts.setMonth(dateTime.getMonthOfYear());
        xts.setDay(dateTime.getDayOfMonth());
        xts.setHour(dateTime.getHourOfDay());
        xts.setMinute(dateTime.getMinuteOfHour());
        xts.setSecond(dateTime.getSecondOfMinute());
        xts.setMillisecond(dateTime.getMillisOfSecond());
        xts.setTimezone(dateTime.getZone().toTimeZone().getOffset(dateTime.getMillis()) / 60000);

        return xts;
    }
}
