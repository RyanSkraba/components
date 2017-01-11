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
package org.talend.components.common.runtime;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ParserUtilsTest {

    private transient static final Logger LOG = LoggerFactory.getLogger(FileRuntimeHelperTest.class);

    @Test
    public void testParseToList() throws Exception {
        assertNull(ParserUtils.parseToList(null, ","));
        // Test for list string not surrounded by "[ ]"
        List<String> list1 = ParserUtils.parseToList("e1,e2,e3,e4", ",");
        assertEquals(1, list1.size());
        assertEquals("e1,e2,e3,e4", list1.get(0));
        // Test for list string surrounded by "[ ]"
        List<String> list2 = ParserUtils.parseToList("[e1,e2,e3,e4]", ",");
        assertEquals(4, list2.size());
        assertEquals("e1", list2.get(0));
        assertEquals("e2", list2.get(1));
        assertEquals("e3", list2.get(2));
        assertEquals("e4", list2.get(3));
        // Test for separator is null
        List<String> list3 = ParserUtils.parseToList("e1,e2,e3,e4", null);
        assertEquals(1, list3.size());
        assertEquals("e1,e2,e3,e4", list3.get(0));
    }

    @Test
    public void testParseListToString() throws Exception {
        assertNull(ParserUtils.parseListToString(null, ","));
        List<String> list = new ArrayList<String>();
        list.add("e1");
        list.add("e2");
        list.add("e3");
        list.add("e4");
        assertEquals("[e1,e2,e3,e4]", ParserUtils.parseListToString(list, ","));
    }

    @Test
    public void testParseToCharacter() throws Exception {
        assertNull(ParserUtils.parseToCharacter(null));
        assertEquals((Character) 'a', ParserUtils.parseToCharacter("a"));
        assertEquals((Character) 'b', ParserUtils.parseToCharacter("bc"));
    }

    @Test
    public void testParseToByte() throws Exception {
        assertNull(ParserUtils.parseToByte(null, true));
        assertEquals(Byte.valueOf("10"), ParserUtils.parseToByte("010", false));
        assertEquals(Byte.valueOf("8"), ParserUtils.parseToByte("010", true));
        assertEquals(Byte.valueOf("10"), ParserUtils.parseToByte("10", false));
        assertEquals(Byte.valueOf("10"), ParserUtils.parseToByte("10", true));
        try {
            ParserUtils.parseToByte("0X10", false);
            fail("Except get NumberFormatException");
        } catch (Exception e) {
            assertEquals(NumberFormatException.class, e.getClass());
            LOG.debug("Except exception:" + e.getMessage());
        }
        assertEquals(Byte.valueOf("16"), ParserUtils.parseToByte("0X10", true));
        try {
            ParserUtils.parseToByte("0X123", true);
        } catch (Exception e) {
            assertEquals(NumberFormatException.class, e.getClass());
            LOG.debug("Except exception:" + e.getMessage());
        }
    }

    @Test
    public void testParseToDouble() throws Exception {
        assertNull(ParserUtils.parseToDouble(null));
        assertNull(ParserUtils.parseToDouble(null));
        assertThat(12.54, equalTo(ParserUtils.parseToDouble("12.54")));
        assertThat(12.5111, equalTo(ParserUtils.parseToDouble("12.5111")));
    }

    @Test
    public void testParseToFloat() throws Exception {
        assertNull(ParserUtils.parseToFloat(null));
        assertNull(ParserUtils.parseToFloat(null));
        assertThat(12.54f, equalTo(ParserUtils.parseToFloat("12.54")));
        assertThat(12.5111f, equalTo(ParserUtils.parseToFloat("12.5111")));
    }

    @Test
    public void testParseToInteger() throws Exception {
        assertNull(ParserUtils.parseToInteger(null, true));
        assertThat(10, equalTo(ParserUtils.parseToInteger("010", false)));
        assertThat(8, equalTo(ParserUtils.parseToInteger("010", true)));
        assertThat(10, equalTo(ParserUtils.parseToInteger("10", false)));
        assertThat(10, equalTo(ParserUtils.parseToInteger("10", true)));
        try {
            ParserUtils.parseToInteger("0X10", false);
            fail("Except get NumberFormatException");
        } catch (Exception e) {
            assertEquals(NumberFormatException.class, e.getClass());
            LOG.debug("Except exception:" + e.getMessage());
        }
        assertThat(16, equalTo(ParserUtils.parseToInteger("0X10", true)));
        assertThat(74565, equalTo(ParserUtils.parseToInteger("0X12345", true)));

    }

    @Test
    public void testParseToShort() throws Exception {
        assertNull(ParserUtils.parseToShort(null, false));
        assertEquals(Short.valueOf("10"), ParserUtils.parseToShort("010", false));
        assertEquals(Short.valueOf("8"), ParserUtils.parseToShort("010", true));
        assertEquals(Short.valueOf("10"), ParserUtils.parseToShort("10", false));
        assertEquals(Short.valueOf("10"), ParserUtils.parseToShort("10", true));
        try {
            ParserUtils.parseToShort("0X10", false);
            fail("Except get NumberFormatException");
        } catch (Exception e) {
            assertEquals(NumberFormatException.class, e.getClass());
            LOG.debug("Except exception:" + e.getMessage());
        }
        assertEquals(Short.valueOf("16"), ParserUtils.parseToShort("0X10", true));
        try {
            ParserUtils.parseToShort("0X12345", true);
        } catch (Exception e) {
            assertEquals(NumberFormatException.class, e.getClass());
            LOG.debug("Except exception:" + e.getMessage());
        }
    }

    @Test
    public void testParseToLong() throws Exception {
        assertNull(ParserUtils.parseToLong(null, false));
        assertThat(10L, equalTo(ParserUtils.parseToLong("010", false)));
        assertThat(8L, equalTo(ParserUtils.parseToLong("010", true)));
        assertThat(10L, equalTo(ParserUtils.parseToLong("10", false)));
        assertThat(10L, equalTo(ParserUtils.parseToLong("10", true)));
        try {
            ParserUtils.parseToLong("0X10", false);
            fail("Except get NumberFormatException");
        } catch (Exception e) {
            assertEquals(NumberFormatException.class, e.getClass());
            LOG.debug("Except exception:" + e.getMessage());
        }
        assertThat(16L, equalTo(ParserUtils.parseToLong("0X10", true)));
        assertThat(74565L, equalTo(ParserUtils.parseToLong("0X12345", true)));
    }

    @Test
    public void testParseToBoolean() throws Exception {
        assertNull(ParserUtils.parseToBoolean(null));
        // "1" would return true
        assertTrue(ParserUtils.parseToBoolean("1"));
        // Others flow to the boolean parser
        assertFalse(ParserUtils.parseToBoolean("0"));
        assertTrue(ParserUtils.parseToBoolean("true"));
        assertFalse(ParserUtils.parseToBoolean("false"));
        assertTrue(ParserUtils.parseToBoolean("TRUE"));
        assertFalse(ParserUtils.parseToBoolean("FALSE"));
        assertFalse(ParserUtils.parseToBoolean("a"));
    }

    @Test
    public void testParseToDate() throws Exception {
        // Parse date lenient. The wrong month number would be ignore
        Date date1 = ParserUtils.parseToDate("1988-02-30", "yyyy-MM-dd", true);
        assertEquals("1988-03-01", FormatterUtils.formatDate(date1, "yyyy-MM-dd"));
        try {
            // Parse date strict. "1988-20" with wrong month number would throw a exception
            ParserUtils.parseToDate("1988-20", "yyyy-MM", false);
            fail("Except get exception: Unparseable date: \"1988-20\"");
        } catch (Exception e) {
            assertEquals(RuntimeException.class, e.getClass());
            LOG.debug("Except exception:" + e.getMessage());
        }
        Date date2 = ParserUtils.parseToDate("1987-06-21", "yyyy-MM-dd", true);
        assertEquals("1987-06-21", FormatterUtils.formatDate(date2, "yyyy-MM-dd"));
        Date date3 = ParserUtils.parseToDate("1987-06-21 12:21:22", "yyyy-MM-dd hh:mm:ss", true);
        assertEquals("1987-06-21 12:21:22", FormatterUtils.formatDate(date3, "yyyy-MM-dd hh:mm:ss"));
        Date date4 = ParserUtils.parseToDate("1987-06-21 12:21:22.123", "yyyy-MM-dd hh:mm:ss.SSS", true);
        assertEquals("1987-06-21 12:21:22.123", FormatterUtils.formatDate(date4, "yyyy-MM-dd hh:mm:ss.SSS"));
    }

    @Test
    public void testTransformNumberString() throws Exception {
        // Transform xx[thousandsSeparator]xxx[thousandsSeparator]xxx[decimalSeparator]xx to xxxxxxxx.xx
        assertEquals("123456789.123", ParserUtils.transformNumberString("123,456,789.123", ',', '.'));
        assertEquals("123456789.123", ParserUtils.transformNumberString("123.456.789,123", '.', ','));
    }

}
