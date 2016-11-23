// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
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

import java.text.DateFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/*
 * This is a refer to routines.system.ParserUtils
 */
public class ParserUtils {

    /**
     * the source should be a string wrapped in chars[ ] which stands for it is a collection
     * 
     * @param listStr
     * @param fieldSep
     * @return
     */
    public static List<String> parseToList(final String listStr, String fieldSep) {
        if (listStr == null) {
            return null;
        }
        List<String> list = new ArrayList<String>();

        // the source string is wrap in [] which means it is a collection
        if ((fieldSep == null || "".equals(fieldSep)) || !(listStr.startsWith("[") && listStr.endsWith("]"))) {
            list.add(listStr);
            return list;
        }
        // remove the [ ]
        String strTemp = listStr.substring(1, listStr.length() - 1);
        for (String str : strTemp.split(fieldSep, -1)) {
            list.add(str);
        }
        return list;
    }

    /**
     * Parse String list object to String with specify separator
     */
    public static String parseListToString(final List<String> strList, String fieldSep) {
        if (strList == null) {
            return null;
        }
        StringBuffer result = new StringBuffer();
        result.append("[");
        for (int i = 0; i < strList.size(); i++) {
            if (i != 0) {
                result.append(fieldSep);
            }
            result.append(strList.get(i));
        }
        result.append("]");

        return result.toString();
    }

    /**
     * Parse String type to character
     */
    public static Character parseToCharacter(String charStr) {
        if (charStr == null) {
            return null;
        }
        return charStr.charAt(0);
    }

    /**
     * Parse String type to byte with parameter whether decode string
     */
    public static Byte parseToByte(String byteStr, boolean isDecode) {
        if (byteStr == null) {
            return null;
        }
        if (isDecode) {
            return Byte.decode(byteStr).byteValue();
        } else {
            return Byte.parseByte(byteStr);
        }
    }

    /**
     * Parse String type to Double type
     */
    public static Double parseToDouble(String doubleStr) {
        if (doubleStr == null) {
            return null;
        }
        return Double.parseDouble(doubleStr);
    }

    /**
     * Parse String type to Float type
     */
    public static Float parseToFloat(String floatStr) {
        if (floatStr == null) {
            return null;
        }
        return Float.parseFloat(floatStr);
    }

    /**
     * Parse String type to Integer with parameter whether decode string
     */
    public static Integer parseToInteger(String intStr, boolean isDecode) {
        if (intStr == null) {
            return null;
        }
        if (isDecode) {
            return Integer.decode(intStr).intValue();
        } else {
            return Integer.parseInt(intStr);
        }
    }

    /**
     * Parse String type to Short with parameter whether decode string
     */
    public static Short parseToShort(String s, boolean isDecode) {
        if (s == null) {
            return null;
        }
        if (isDecode) {
            return Short.decode(s).shortValue();
        } else {
            return Short.parseShort(s);
        }
    }

    /**
     * Parse String type to Long with parameter whether decode string
     */
    public static Long parseToLong(String longStr, boolean isDecode) {
        if (longStr == null) {
            return null;
        }
        if (isDecode) {
            return Long.decode(longStr).longValue();
        } else {
            return Long.parseLong(longStr);
        }
    }

    /**
     * Parse String type to Boolean type
     */
    public static Boolean parseToBoolean(String booleanStr) {
        if (booleanStr == null) {
            return null;
        }
        if (booleanStr.equals("1")) {
            return true;
        }
        return Boolean.parseBoolean(booleanStr);
    }

    /**
     * Parse Date string to a Date type using the given pattern
     *
     * @param pattern the pattern to parse.
     * @param stringDate Date string which to be parsed.
     * @param isLenient judge DateFormat parse the date Lenient or not.
     * @return Date object parsed from the string.
     *
     */
    public synchronized static Date parseToDate(String stringDate, String pattern, boolean isLenient) {
        // check the parameter for supporting " ","2007-09-13"," 2007-09-13 "
        if (stringDate != null) {
            stringDate = stringDate.trim();
        }
        if (stringDate == null || stringDate.length() == 0) {
            return null;
        }
        if (pattern == null) {
            pattern = FormatterUtils.dateDefaultPattern;
        }
        Date date = null;
        if (pattern.equals("yyyy-MM-dd'T'HH:mm:ss'000Z'")) {
            if (!stringDate.endsWith("000Z")) {
                throw new RuntimeException("Unparseable date: \"" + stringDate + "\"");
            }
            pattern = "yyyy-MM-dd'T'HH:mm:ss";
            stringDate = stringDate.substring(0, stringDate.lastIndexOf("000Z"));
        }
        DateFormat format = FastDateParser.getInstance(pattern, isLenient);
        ParsePosition pp = new ParsePosition(0);
        pp.setIndex(0);

        date = format.parse(stringDate, pp);
        if (pp.getIndex() != stringDate.length() || date == null) {
            throw new RuntimeException("Unparseable date: \"" + stringDate + "\"");
        }

        return date;
    }

    /**
     * In order to transform the string "1.234.567,89" to number 1234567.89
     */
    public static String transformNumberString(String numberString, Character thousandsSeparator, Character decimalSeparator) {
        if (numberString == null) {
            return null;
        }
        String result = numberString;
        if (thousandsSeparator != null) {
            result = StringUtils.replace(numberString, thousandsSeparator.toString(), "");
        }
        if (decimalSeparator != null) {
            result = result.replace(decimalSeparator, '.');
        }
        return result;
    }

}
