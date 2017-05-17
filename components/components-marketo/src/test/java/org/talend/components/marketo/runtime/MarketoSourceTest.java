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
package org.talend.components.marketo.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.text.DateFormat;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.common.runtime.FastDateParser;
import org.talend.components.marketo.tmarketoinput.TMarketoInputProperties;
import org.talend.daikon.properties.ValidationResult;

public class MarketoSourceTest {

    MarketoSource source;

    @Before
    public void setUp() throws Exception {
        source = new MarketoSource();
    }

    @Test
    public void splitIntoBundles() throws Exception {
        assertTrue(source.splitIntoBundles(1000, null).size() > 0);
    }

    @Test
    public void getEstimatedSizeBytes() throws Exception {
        assertEquals(0, source.getEstimatedSizeBytes(null));
    }

    @Test
    public void producesSortedKeys() throws Exception {
        assertFalse(source.producesSortedKeys(null));
    }

    @Test
    public void testValidate() throws Exception {
        TMarketoInputProperties props = new TMarketoInputProperties("test");
        source.initialize(null, props);
        assertEquals(ValidationResult.Result.ERROR, source.validate(null).getStatus());
    }

    @Test
    public void testTDI38561() throws Exception {
        TMarketoInputProperties props = new TMarketoInputProperties("test");
        props.connection.setupProperties();
        props.setupProperties();
        props.connection.endpoint.setValue("htp:ttoot.com");
        props.connection.clientAccessId.setValue("user");
        props.connection.secretKey.setValue("secret");
        source.initialize(null, props);
        assertEquals(ValidationResult.Result.ERROR, source.validate(null).getStatus());
        props.connection.endpoint.setValue("https://ttoot.com");
        source.initialize(null, props);
        assertEquals(ValidationResult.Result.ERROR, source.validate(null).getStatus());
        props.connection.endpoint.setValue("https://ttoot.com/rustinpeace/rest");
        source.initialize(null, props);
        assertEquals(ValidationResult.Result.ERROR, source.validate(null).getStatus());
        props.connection.endpoint.setValue("https://ttoot.com/rest");
        source.initialize(null, props);
        ValidationResult vr = source.validate(null);
        assertEquals(ValidationResult.Result.ERROR, vr.getStatus());
        assertTrue(vr.getMessage().contains("refused"));
    }

    @Test
    public void testIsInvalidDate() throws Exception {
        assertTrue(source.isInvalidDate("20170516 112417"));
        assertTrue(source.isInvalidDate("20170516 11:24:17"));
        assertTrue(source.isInvalidDate("20170516 11:24:17 0000"));
        assertTrue(source.isInvalidDate("2017-05-16 11:24:17 0000"));
        assertTrue(source.isInvalidDate("2017-05-16 11:24:17"));
        assertTrue(source.isInvalidDate("2017-05-16'T'11:24:17 +0100"));
        DateFormat format = FastDateParser.getInstance("yyyy-MM-dd HH:mm:ss Z");
        format.setTimeZone(TimeZone.getTimeZone("Europe/England"));
        assertFalse(source.isInvalidDate(FastDateParser.getInstance("yyyy-MM-dd HH:mm:ss Z").format(new java.util.Date())));
        format.setTimeZone(TimeZone.getTimeZone("Europe/Lisbon"));
        assertFalse(source.isInvalidDate(FastDateParser.getInstance("yyyy-MM-dd HH:mm:ss Z").format(new java.util.Date())));
        assertFalse(source.isInvalidDate("2017-05-16 11:24:17 +0100"));
        assertFalse(source.isInvalidDate("2017-05-16 11:24:17 -0100"));
    }

}
