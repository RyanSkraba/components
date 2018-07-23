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
package org.talend.components.marketo.runtime.client;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.talend.components.marketo.MarketoConstants.DATETIME_PATTERN_REST;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.marketo.runtime.client.type.MarketoError;
import org.talend.components.marketo.runtime.client.type.MarketoException;
import org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties;
import org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties.APIMode;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.SchemaConstants;

import com.google.gson.internal.LinkedTreeMap;

public class MarketoBaseRESTClientTest {

    MarketoRESTClient client;

    @Before
    public void setUp() throws Exception {
        TMarketoConnectionProperties conn = new TMarketoConnectionProperties("test");
        conn.apiMode.setValue(APIMode.REST);
        conn.endpoint.setValue("https://fake.io");
        conn.clientAccessId.setValue("client");
        conn.secretKey.setValue("sekret");
        client = spy(new MarketoRESTClient(conn));
    }

    @Test
    public void testIsAccessTokenExpired() throws Exception {
        assertFalse(client.isAccessTokenExpired(null));
        MarketoError error = new MarketoException("REST", "602", "Access token expired").toMarketoError();
        assertTrue(client.isAccessTokenExpired(Arrays.asList(error)));
    }

    @Test
    public void testIsErrorRecoverable() throws Exception {
        MarketoError error = new MarketoException("REST", "602", "Access token expired").toMarketoError();
        doNothing().when(client).getToken();
        assertTrue(client.isErrorRecoverable(Arrays.asList(error)));
        for (String code : new String[] { "502", "604", "606", "608", "611", "614", "615" }) {
            error = new MarketoException("REST", code, "API Temporarily Unavailable").toMarketoError();
            assertTrue(client.isErrorRecoverable(Arrays.asList(error)));
        }
        error = new MarketoException("REST", "404", "Page not found").toMarketoError();
        assertFalse(client.isErrorRecoverable(Arrays.asList(error)));
    }

    @Test
    public void testParseRecords() throws Exception {
        Schema schema = SchemaBuilder.builder().record("test").fields() //
                .name("field1").type().nullable().stringType().noDefault() //
                .name("fields").type().nullable().stringType().noDefault() //
                .name("field2").type().nullable().intType().noDefault() //
                .name("field3").type().nullable().longType().noDefault() //
                .name("field4").type().nullable().booleanType().noDefault() //
                .name("field5").type().nullable().doubleType().noDefault() //
                .name("field6").type().nullable().floatType().noDefault() //
                .name("field7") //
                .prop(SchemaConstants.TALEND_COLUMN_PATTERN, DATETIME_PATTERN_REST)//
                .prop(SchemaConstants.JAVA_CLASS_FLAG, Date.class.getCanonicalName()) //
                .type(AvroUtils._logicalTimestamp()).noDefault()//
                .name("field8") //
                .prop(SchemaConstants.TALEND_COLUMN_PATTERN, DATETIME_PATTERN_REST)//
                .prop(SchemaConstants.JAVA_CLASS_FLAG, Date.class.getCanonicalName()) //
                .type(AvroUtils._logicalTimestamp()).noDefault()//
                .name("field9").type().nullType().noDefault() //
                .endRecord();
        //
        List<LinkedTreeMap> values = new ArrayList<>();
        LinkedTreeMap map = new LinkedTreeMap();
        map.put("field1", "value1");
        map.put("fields", "{value1: 1223}");
        map.put("field2", 12345);
        map.put("field3", 1234567890L);
        map.put("field4", true);
        map.put("field5", 123456.5);
        map.put("field6", 123456.5);
        map.put("field7", new Date());
        map.put("field8", new SimpleDateFormat(DATETIME_PATTERN_REST).format(new Date().getTime()));
        map.put("field9", "nullType");
        values.add(map);
        //
        assertEquals(Collections.emptyList(), client.parseRecords(values, null));
        assertEquals(Collections.emptyList(), client.parseRecords(null, schema));
        List<IndexedRecord> records = client.parseRecords(values, schema);
        assertNotNull(records);
        IndexedRecord record = records.get(0);
        assertNotNull(record);
        assertEquals("value1", record.get(0));
        assertEquals("\"{value1: 1223}\"", record.get(1));
        assertEquals(12345, record.get(2));
        assertEquals(1234567890L, record.get(3));
        assertEquals(true, record.get(4));
        assertEquals(123456.5, record.get(5));
        assertEquals((float) 123456.5, record.get(6));
        assertEquals(null, record.get(7));
        assertTrue(record.get(8) instanceof Long);
        assertEquals("nullType", record.get(9));
    }
}
