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

package org.talend.components.salesforce;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.junit.Test;
import org.talend.components.salesforce.runtime.SalesforceRuntime;

import com.sforce.soap.partner.Error;
import com.sforce.soap.partner.StatusCode;

/**
 *
 */
public class SalesforceRuntimeTest {

    @Test
    public void testAddLog() throws IOException {
        Error error1 = new Error();
        error1.setStatusCode(StatusCode.INVALID_STATUS);
        error1.setMessage("Error message 1");

        Error error2 = new Error();
        error2.setStatusCode(StatusCode.FIELD_NOT_UPDATABLE);
        error2.setMessage("Error message 2");
        error2.setFields(new String[]{"Id", "Created"});

        StringWriter logStringWriter = new StringWriter();
        BufferedWriter logWriter = new BufferedWriter(logStringWriter);

        StringBuilder sb = SalesforceRuntime.addLog(new Error[]{error1, error2}, "12345", logWriter);
        logWriter.flush();

        assertNotNull(sb);

        String content = sb.toString();
        assertThat(content, containsString(error1.getMessage()));
        assertThat(content, containsString(error2.getMessage()));

        String logContent = logStringWriter.toString();
        assertThat(logContent, containsString(error1.getMessage()));
        assertThat(logContent, containsString(error1.getStatusCode().toString()));
        assertThat(logContent, containsString(error2.getMessage()));
        assertThat(logContent, containsString(error2.getStatusCode().toString()));
        assertThat(logContent, containsString("12345"));
        assertThat(logContent, containsString("Id"));
        assertThat(logContent, containsString("Created"));
    }

    @Test
    public void testConvertDateToCalendar() throws IOException {
        long timestamp = System.currentTimeMillis();
        Calendar calendar1 = SalesforceRuntime.convertDateToCalendar(new Date(timestamp));
        assertNotNull(calendar1);
        assertEquals(TimeZone.getTimeZone("GMT"), calendar1.getTimeZone());

        assertNull(SalesforceRuntime.convertDateToCalendar(null));
    }
}
