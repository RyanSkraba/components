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

import static org.junit.Assert.*;

import java.util.Calendar;

import org.junit.Test;
import org.talend.components.api.component.runtime.BoundedReader;
import org.talend.components.salesforce.SalesforceTestBase;
import org.talend.components.salesforce.tsalesforcegetservertimestamp.TSalesforceGetServerTimestampProperties;

public class SalesforceServerTimeStampReaderTestIT extends SalesforceTestBase {

    @Test
    public void testGetServerTimestamp() throws Throwable {
        TSalesforceGetServerTimestampProperties props = (TSalesforceGetServerTimestampProperties) new TSalesforceGetServerTimestampProperties(
                "foo").init();
        setupProps(props.connection, !ADD_QUOTES);
        BoundedReader bounderReader = createBoundedReader(props);
        try {
            assertTrue(bounderReader.start());
            assertFalse(bounderReader.advance());
            Object row = bounderReader.getCurrent();
            assertNotNull(row);
            // TODO we need to make sure about the server and local time zone are the same.
            Calendar now = Calendar.getInstance();
            Calendar date = (Calendar) row;
            long nowMillis = now.getTimeInMillis();
            long dateMillis = date.getTimeInMillis();
            System.out.println("now: " + nowMillis);
            System.out.println(dateMillis);
            long delta = nowMillis - dateMillis;
            assertTrue(Math.abs(delta) < 50000);

        } finally {
            bounderReader.close();
        }

    }

}
