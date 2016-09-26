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
import java.util.Date;
import java.util.TimeZone;

public class FormatterUtils {

    public static final String dateDefaultPattern = "dd-MM-yyyy";

    public static String formatDate(Date date, String pattern) {
        if (date != null) {
            DateFormat format = FastDateParser.getInstance(pattern == null ? dateDefaultPattern : pattern);
            format.setTimeZone(TimeZone.getDefault());
            return format.format(date);
        } else {
            return null;
        }
    }

    /**
     * in order to transform the number "1234567.89" to string 123,456.89
     */
    public static String formatNumber(String s, Character thousandsSeparator, Character decimalSeparator) {
        if (s == null) {
            return null;
        }
        String result = s;
        int decimalIndex = s.indexOf("."); //$NON-NLS-1$

        if (decimalIndex == -1) {
            if (thousandsSeparator != null) {
                return formatNumber(result, thousandsSeparator);
            } else {
                return result;
            }
        }

        if (thousandsSeparator != null) {
            result = formatNumber(s.substring(0, decimalIndex), thousandsSeparator);
        } else {
            result = s.substring(0, decimalIndex);
        }

        if (decimalSeparator != null) {
            result += (s.substring(decimalIndex)).replace('.', decimalSeparator);
        } else {
            result += s.substring(decimalIndex);
        }
        return result;
    }

    private static String formatNumber(String s, char thousandsSeparator) {

        StringBuilder sb = new StringBuilder(s);
        int index = sb.length();

        index = index - 3;
        while (index > 0 && sb.charAt(index - 1) != '-') {
            sb.insert(index, thousandsSeparator);
            index = index - 3;
        }

        return sb.toString();
    }

}
