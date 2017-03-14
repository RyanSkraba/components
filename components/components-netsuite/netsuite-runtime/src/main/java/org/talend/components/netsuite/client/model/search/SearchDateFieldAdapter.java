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

import static org.talend.components.netsuite.client.model.beans.Beans.setProperty;
import static org.talend.components.netsuite.client.model.beans.Beans.setSimpleProperty;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.lang3.StringUtils;
import org.talend.components.netsuite.client.NetSuiteException;
import org.talend.components.netsuite.client.model.BasicMetaData;

/**
 *
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
                Calendar calValue = Calendar.getInstance();

                String formatPattern = dateFormatPattern + " " + timeFormatPattern;

                if (values.get(0).length() == dateFormatPattern.length()) {
                    formatPattern = dateFormatPattern;
                }

                if (values.get(0).length() == timeFormatPattern.length()) {
                    values.set(0, new SimpleDateFormat(dateFormatPattern)
                            .format(calValue.getTime()) + " " + values.get(0));
                }

                DateFormat df = new SimpleDateFormat(formatPattern);

                try {
                    calValue.setTime(df.parse(values.get(0)));
                } catch (ParseException e) {
                    throw new IllegalArgumentException("Invalid date/time format: " + values.get(0));
                }

                XMLGregorianCalendar xts = datatypeFactory.newXMLGregorianCalendar();
                xts.setYear(calValue.get(Calendar.YEAR));
                xts.setMonth(calValue.get(Calendar.MONTH) + 1);
                xts.setDay(calValue.get(Calendar.DAY_OF_MONTH));
                xts.setHour(calValue.get(Calendar.HOUR_OF_DAY));
                xts.setMinute(calValue.get(Calendar.MINUTE));
                xts.setSecond(calValue.get(Calendar.SECOND));
                xts.setMillisecond(calValue.get(Calendar.MILLISECOND));
                xts.setTimezone(calValue.get(Calendar.ZONE_OFFSET) / 60000);

                setProperty(nsObject,"searchValue", xts);

                if (values.size() > 1 && StringUtils.isNotEmpty(values.get(1))) {
                    try {
                        calValue.setTime(df.parse(values.get(1)));
                    } catch (ParseException e) {
                        throw new IllegalArgumentException("Invalid date/time format: " + values.get(1));
                    }

                    XMLGregorianCalendar xts2 = datatypeFactory.newXMLGregorianCalendar();
                    xts2.setYear(calValue.get(Calendar.YEAR));
                    xts2.setMonth(calValue.get(Calendar.MONTH) + 1);
                    xts2.setDay(calValue.get(Calendar.DAY_OF_MONTH));
                    xts2.setHour(calValue.get(Calendar.HOUR_OF_DAY));
                    xts2.setMinute(calValue.get(Calendar.MINUTE));
                    xts2.setSecond(calValue.get(Calendar.SECOND));
                    xts2.setMillisecond(calValue.get(Calendar.MILLISECOND));
                    xts2.setTimezone(calValue.get(Calendar.ZONE_OFFSET) / 60000);

                    setSimpleProperty(nsObject, "searchValue2", xts2);
                }
            }

            setSimpleProperty(nsObject, "operator",
                    metaData.getSearchFieldOperatorByName(fieldType.getFieldTypeName(), operatorName));
        }

        return nsObject;
    }
}
