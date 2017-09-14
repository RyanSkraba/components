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
package org.talend.components.marketo;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.text.ParseException;
import java.util.Collections;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;
import org.apache.avro.Schema.Field.Order;
import org.junit.Test;
import org.talend.daikon.avro.AvroUtils;

public class MarketoUtilsTest {

    @Test
    public void testParseDateString() throws Exception {
        try {
            MarketoUtils.parseDateString("20170516 112417");
            fail("Should not be here");
        } catch (ParseException pe) {
        }
        try {
            MarketoUtils.parseDateString("20170516 11:24:17");
            fail("Should not be here");
        } catch (ParseException pe) {
        }
        try {
            MarketoUtils.parseDateString("20170516 11:24:17 0000");
            fail("Should not be here");
        } catch (ParseException pe) {
        }
        try {
            MarketoUtils.parseDateString("2017-05-16 11:24:17 0000");
            fail("Should not be here");
        } catch (ParseException pe) {
        }
        try {
            MarketoUtils.parseDateString("2017-05-16 11:24:17");
            fail("Should not be here");
        } catch (ParseException pe) {
        }
        try {
            MarketoUtils.parseDateString("2017-05-16'T'11:24:17 +0100");
            fail("Should not be here");
        } catch (ParseException pe) {
        }
        assertNotNull(MarketoUtils.parseDateString("2017-05-16 11:24:17 +0100"));
        assertNotNull(MarketoUtils.parseDateString("2017-05-16 11:24:17 -0100"));
        assertNotNull(MarketoUtils.parseDateString("2017-05-16 11:24:17+0000"));
        assertNotNull(MarketoUtils.parseDateString("2017-05-16 11:24:17-0000"));
        assertNotNull(MarketoUtils.parseDateString("2017-05-16 11:24:17+0100"));
        assertNotNull(MarketoUtils.parseDateString("2017-05-16 11:24:17-0100"));
        assertNotNull(MarketoUtils.parseDateString("2017-07-10 13:53:26Z"));
        assertNotNull(MarketoUtils.parseDateString("2017-05-16T11:24:17+0100"));
        assertNotNull(MarketoUtils.parseDateString("2017-05-16T11:24:17.000Z"));
    }

    @Test
    public void testGenerateNewField() throws Exception {
        Field in = new Schema.Field("email", AvroUtils._string(), "doc", null, Order.ASCENDING);
        in.addProp("test", "testvalue");
        Field out = MarketoUtils.generateNewField(in);
        assertEquals("email", out.name());
        assertEquals("string", out.schema().getType().getName());
        assertEquals("doc", out.doc());
        assertEquals(Order.ASCENDING, out.order());
        assertNotNull(out.getProp("test"));
        assertEquals("testvalue", out.getProp("test"));
    }

    @Test
    public void testModifySchemaFields() {
        Schema s = MarketoConstants.getRESTSchemaForGetLeadActivity();
        Field f = new Schema.Field("id", AvroUtils._string(), "", (Object) null);
        Schema r = MarketoUtils.modifySchemaFields(s, Collections.singletonList(f));
        assertThat(s.getFields().size(), equalTo(r.getFields().size()));
        assertThat(f, equalTo(r.getField("id")));
    }

    public Field generateFieldType(Type type) {
        return new Schema.Field("generated", Schema.create(type), "", (Object) null);
    }

    @Test
    public void testGetFieldType() throws Exception {
        assertEquals(Type.STRING, MarketoUtils.getFieldType(generateFieldType(Type.STRING)));
        assertEquals(Type.INT, MarketoUtils.getFieldType(generateFieldType(Type.INT)));
        assertEquals(Type.LONG, MarketoUtils.getFieldType(generateFieldType(Type.LONG)));
    }

}
