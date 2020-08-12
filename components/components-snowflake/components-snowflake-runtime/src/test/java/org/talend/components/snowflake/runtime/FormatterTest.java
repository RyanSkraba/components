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

import org.junit.Assert;
import org.junit.Test;

import javax.validation.constraints.AssertTrue;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class FormatterTest {

    @Test
    public void testFormatter4SafeThread() {
        final Formatter f1 = new Formatter();
        new Thread(new Runnable() {

            @Override
            public void run() {
                Formatter f2 = new Formatter();
                compare(f1,f2);
            }

        }).start();
    }

    public void compare(Formatter f1, Formatter f2) {
        Assert.assertNotEquals(f1.getDateFormatter(), f2.getDateFormatter());
        Assert.assertNotEquals(f1.getTimeFormatter(), f2.getTimeFormatter());
        Assert.assertNotEquals(f1.getTimestampFormatter(), f2.getTimestampFormatter());
    }

    @Test
    public void testTimeMillis() throws ParseException {
        String dateTime = "2017-10-15 12:12:12";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Date actualDate = sdf.parse(dateTime);
        Formatter formatter = new Formatter();

        Date expectedDate = sdf.parse("1970-01-01 12:12:12");

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss.SSS");
        timeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        String expected = timeFormat.format(expectedDate);
        String actual = formatter.formatTimeMillis(actualDate);

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testFormatDateFromInteger() {
        Formatter formatter = new Formatter();

        // Sydney daylight saving time ended 2017-10-01
        Assert.assertEquals("2017-09-30", formatter.formatDate(17439));
        Assert.assertEquals("2017-10-01", formatter.formatDate(17440));

        // Seoul daylight saving time started 1988-05-08
        Assert.assertEquals("1988-05-07", formatter.formatDate(6701));
        Assert.assertEquals("1988-05-08", formatter.formatDate(6702));
    }

    @Test
    public void testFormatDateWithPattern(){
        Formatter formatter = new Formatter();
        Assert.assertEquals("2017|30|09", formatter.formatDateWithPattern("yyyy|dd|MM", 17439));
        Assert.assertEquals("2017|01|10", formatter.formatDateWithPattern("yyyy|dd|MM", 17440));
        Assert.assertEquals("1988|07|05 00,00,00", formatter.formatDateWithPattern("yyyy|dd|MM HH,mm,ss", 6701));
        Assert.assertEquals("1988|08|05 00,00,00", formatter.formatDateWithPattern("yyyy|dd|MM HH,mm,ss", 6702));

        Calendar c = Calendar.getInstance(Locale.ENGLISH);
        c.set(Calendar.YEAR, 2010);
        c.set(Calendar.MONTH, 5);
        c.set(Calendar.DAY_OF_MONTH, 10);
        c.set(Calendar.HOUR_OF_DAY, 10);
        c.set(Calendar.MINUTE, 25);
        c.set(Calendar.SECOND, 30);
        Assert.assertEquals("2010|10|06 10,25,30", formatter.formatDateWithPattern("yyyy|dd|MM hh,mm,ss", c.getTime()));
    }
}
