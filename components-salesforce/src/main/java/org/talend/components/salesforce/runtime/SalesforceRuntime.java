// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.salesforce.runtime;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.talend.daikon.exception.TalendRuntimeException;

import com.sforce.soap.partner.Error;
import com.sforce.ws.bind.CalendarCodec;
import com.sforce.ws.bind.DateCodec;

/**
 * Contains only runtime helper classes, mainly to do with logging.
 */
public class SalesforceRuntime {

    private static CalendarCodec calendarCodec = new CalendarCodec();

    private static DateCodec dateCodec = new DateCodec();

    private SalesforceRuntime() {
    }

    public static StringBuilder addLog(Error[] resultErrors, String row_key, BufferedWriter logWriter) {
        StringBuilder errors = new StringBuilder("");
        if (resultErrors != null) {
            for (Error error : resultErrors) {
                errors.append(error.getMessage()).append("\n");
                if (logWriter != null) {
                    try {
                        logWriter.append("\tStatus Code: ").append(error.getStatusCode().toString());
                        logWriter.newLine();
                        logWriter.newLine();
                        logWriter.append("\tRowKey/RowNo: " + row_key);
                        if (error.getFields() != null) {
                            logWriter.newLine();
                            logWriter.append("\tFields: ");
                            boolean flag = false;
                            for (String field : error.getFields()) {
                                if (flag) {
                                    logWriter.append(", ");
                                } else {
                                    flag = true;
                                }
                                logWriter.append(field);
                            }
                        }
                        logWriter.newLine();
                        logWriter.newLine();

                        logWriter.append("\tMessage: ").append(error.getMessage());

                        logWriter.newLine();

                        logWriter.append("\t--------------------------------------------------------------------------------");

                        logWriter.newLine();
                        logWriter.newLine();
                    } catch (IOException ex) {
                        TalendRuntimeException.unexpectedException(ex);
                    }
                }
            }
        }
        return errors;
    }

    public static Calendar convertDateToCalendar(Date date) {
        if (date != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeZone(TimeZone.getTimeZone("GMT"));
            cal.setTime(date);
            return cal;
        } else {
            return null;
        }
    }

}
