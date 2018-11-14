// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.snowflake.runtime;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * the class help format the date type to string, the inner SimpleDateFormat object is thread local, then avoid the thread
 * share issue
 *
 * @author wangwei
 *
 */
public class Formatter {

    private static final ThreadLocal<SimpleDateFormat> DATEFORMATTER_LOCAL = new ThreadLocal<SimpleDateFormat>() {

        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd");
        }

    };

    private SimpleDateFormat dateFormatter = DATEFORMATTER_LOCAL.get();

    private static final ThreadLocal<SimpleDateFormat> TIMEFORMATTER_LOCAL = new ThreadLocal<SimpleDateFormat>() {

        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("HH:mm:ss.SSS");
        }

    };

    private SimpleDateFormat timeFormatter = TIMEFORMATTER_LOCAL.get();

    {
        // Time in milliseconds would mean time from midnight. It shouldn't be influenced by timezone differences.
        // That's why we have to use GMT.
        timeFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    private static final ThreadLocal<SimpleDateFormat> TIMESTAMPFORMATTER_LOCAL = new ThreadLocal<SimpleDateFormat>() {

        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSXXX");
        }

    };

    private SimpleDateFormat timestampFormatter = TIMESTAMPFORMATTER_LOCAL.get();

    /**
     * format timestamp by this pattern : yyyy-MM-dd HH:mm:ss.SSSXXX
     *
     * @param inputValue
     * @return
     */
    Object formatTimestampMillis(Object inputValue) {
        if (inputValue instanceof Date) {
            return getTimestampFormatter().format(inputValue);
        } else if (inputValue instanceof Long) {
            return getTimestampFormatter().format(new Date((Long) inputValue));
        } else {
            return inputValue;
        }
    }

    /**
     * format date by this pattern : yyyy-MM-dd
     *
     * @param inputValue
     * @return
     */
    String formatDate(Object inputValue) {
        Date date = null;
        if (inputValue instanceof Date) {
            // Sometimes it can be sent as a Date object. We need to process it like a common date then.
            date = (Date) inputValue;
        } else if (inputValue instanceof Integer) {
            // If the date is int, it represents amount of days from 1970(no timezone). So if the date is
            // 14.01.2017 it shouldn't be influenced by timezones time differences. It should be the same date
            // in any timezone.
            Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            c.setTimeInMillis(0);
            c.add(Calendar.DATE, (Integer) inputValue);
            c.setTimeZone(TimeZone.getDefault());
            long timeInMillis = c.getTime().getTime();
            date = new Date(timeInMillis - c.getTimeZone().getOffset(timeInMillis));
        } else {
            // long is just a common timestamp value.
            date = new Date((Long) inputValue);
        }
        return getDateFormatter().format(date);
    }

    /**
     * format time by this pattern : HH:mm:ss.SSS
     *
     * @param inputValue
     * @return
     */
    String formatTimeMillis(Object inputValue) {
        final Date date;
        if (inputValue instanceof Date) {
            date = (Date)inputValue;
        } else {//support the old int type input for time type though it's wrong before
            date = new Date((int) inputValue);
        }
        
        return getTimeFormatter().format(date);
    }

    public SimpleDateFormat getDateFormatter() {
        return dateFormatter;
    }

    public SimpleDateFormat getTimeFormatter() {
        return timeFormatter;
    }

    public SimpleDateFormat getTimestampFormatter() {
        return timestampFormatter;
    }

}
