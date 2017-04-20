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

package org.talend.components.netsuite.avro.converter;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.avro.Schema;
import org.joda.time.DateTimeZone;
import org.joda.time.MutableDateTime;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.netsuite.NsObjectTransducer;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.converter.AvroConverter;

/**
 * Responsible for conversion of <code>XMLGregorianCalendar</code> from/to <code>milliseconds</code>.
 */
public class XMLGregorianCalendarToLongConverter implements AvroConverter<XMLGregorianCalendar, Long> {

    private DatatypeFactory datatypeFactory;

    public XMLGregorianCalendarToLongConverter(DatatypeFactory datatypeFactory) {
        this.datatypeFactory = datatypeFactory;
    }

    @Override
    public Schema getSchema() {
        return AvroUtils._logicalTimestamp();
    }

    @Override
    public Class<XMLGregorianCalendar> getDatumClass() {
        return XMLGregorianCalendar.class;
    }

    @Override
    public XMLGregorianCalendar convertToDatum(Long timestamp) {
        if (timestamp == null) {
            return null;
        }

        MutableDateTime dateTime = new MutableDateTime();
        dateTime.setMillis(timestamp);

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

    @Override
    public Long convertToAvro(XMLGregorianCalendar xts) {
        if (xts == null) {
            return null;
        }

        MutableDateTime dateTime = new MutableDateTime();
        try {
            dateTime.setYear(xts.getYear());
            dateTime.setMonthOfYear(xts.getMonth());
            dateTime.setDayOfMonth(xts.getDay());
            dateTime.setHourOfDay(xts.getHour());
            dateTime.setMinuteOfHour(xts.getMinute());
            dateTime.setSecondOfMinute(xts.getSecond());
            dateTime.setMillisOfSecond(xts.getMillisecond());

            DateTimeZone tz = DateTimeZone.forOffsetMillis(xts.getTimezone() * 60000);
            if (tz != null) {
                dateTime.setZoneRetainFields(tz);
            }

            return dateTime.getMillis();
        } catch (IllegalArgumentException e) {
            throw new ComponentException(e);
        }
    }
}
