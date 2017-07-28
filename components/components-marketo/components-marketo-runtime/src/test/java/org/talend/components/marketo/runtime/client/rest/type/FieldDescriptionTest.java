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
package org.talend.components.marketo.runtime.client.rest.type;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.talend.components.marketo.MarketoConstants.DATETIME_PATTERN_REST;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.marketo.runtime.client.rest.type.FieldDescription.ApiFieldName;
import org.talend.daikon.avro.SchemaConstants;

public class FieldDescriptionTest {

    FieldDescription fd;

    @Before
    public void setUp() throws Exception {
        fd = new FieldDescription();
    }

    @Test
    public void testSettersAndGetters() throws Exception {
        fd.setDataType("STRING");
        assertEquals("STRING", fd.getDataType());
        fd.setDisplayName("A full string");
        assertEquals("A full string", fd.getDisplayName());
        fd.setId(666);
        assertEquals("666", fd.getId().toString());
        fd.setLength(255);
        assertEquals("255", fd.getLength().toString());
        fd.setName("Nome");
        fd.setUpdateable(true);
        assertTrue(fd.getUpdateable());
        assertEquals("Nome", fd.getName());
        fd.setSoap(fd.new ApiFieldName("SOAPField", true));
        assertEquals("SOAPField", fd.getSoap().getName());
        assertTrue(fd.getSoap().getReadOnly());
        fd.setRest(fd.new ApiFieldName("RESTField", true));
        assertEquals("RESTField", fd.getRest().getName());
        assertTrue(fd.getRest().getReadOnly());
        ApiFieldName apifn = fd.new ApiFieldName();
        apifn.setName("RESTF");
        apifn.setReadOnly(false);
        assertEquals("RESTF", apifn.getName());
        assertFalse(apifn.getReadOnly());
        assertEquals("ApiFieldName{name='RESTF', readOnly=false}", apifn.toString());
    }

    @Test(expected = NullPointerException.class)
    public void testGetNameFail() throws Exception {
        fd.getName();
        fail("Should't be here");
    }

    @Test
    public void testGetName() throws Exception {
        fd.setName("Nome");
        assertEquals("Nome", fd.getName());
        fd.setName(null);
        ApiFieldName rest = fd.new ApiFieldName("Name", true);
        fd.setRest(rest);
        assertEquals("Name", fd.getName());
    }

    @Test
    public void testToString() throws Exception {
        assertEquals("FieldDescription{id=null, displayName='null', dataType='null', length=null, rest=null, soap=null}",
                fd.toString());
    }

    @Test
    public void testToAvroField() throws Exception {
        fd.setName("avrof");
        fd.setId(123456);
        fd.setDataType("phone");
        fd.setDisplayName("My phone number");
        fd.setLength(25);
        fd.setUpdateable(true);
        Field af = fd.toAvroField();
        assertNotNull(af);
        assertEquals("avrof", af.name());
        assertEquals("123456", af.getProp("mktoId"));
        assertEquals("STRING", af.schema().getType().toString());
        assertEquals("25", af.getProp(SchemaConstants.TALEND_COLUMN_DB_LENGTH));
        for (String s : new String[] { "string", "text", "phone", "email", "url", "lead_function", "reference" }) {
            fd.setDataType(s);
            af = fd.toAvroField();
            assertEquals("STRING", af.schema().getType().toString());
        }
        fd.setUpdateable(false);
        fd.setDataType("integer");
        af = fd.toAvroField();
        assertEquals("INT", af.schema().getType().toString());
        assertEquals("true", af.getProp(SchemaConstants.TALEND_IS_LOCKED));
        fd.setUpdateable(null);
        af = fd.toAvroField();
        assertNull(af.getProp(SchemaConstants.TALEND_IS_LOCKED));
        fd.setDataType("boolean");
        af = fd.toAvroField();
        assertEquals("BOOLEAN", af.schema().getType().toString());
        fd.setDataType("float");
        af = fd.toAvroField();
        assertEquals("FLOAT", af.schema().getType().toString());
        fd.setDataType("currency");
        af = fd.toAvroField();
        assertEquals("FLOAT", af.schema().getType().toString());
        fd.setDataType("date");
        af = fd.toAvroField();
        assertEquals("LONG", af.schema().getType().toString());
        assertEquals(DATETIME_PATTERN_REST, af.getProp(SchemaConstants.TALEND_COLUMN_PATTERN));
        assertEquals("java.util.Date", af.getProp(SchemaConstants.JAVA_CLASS_FLAG));
        fd.setDataType("datetime");
        af = fd.toAvroField();
        assertEquals("LONG", af.schema().getType().toString());
        assertEquals(DATETIME_PATTERN_REST, af.getProp(SchemaConstants.TALEND_COLUMN_PATTERN));
        assertEquals("java.util.Date", af.getProp(SchemaConstants.JAVA_CLASS_FLAG));
        fd.setId(null);
        af = fd.toAvroField();
        assertNull(af.getProp("mktoId"));
        fd.setLength(null);
        af = fd.toAvroField();
        assertNull(af.getProp(SchemaConstants.TALEND_COLUMN_DB_LENGTH));
        fd.setDataType("unknown");
        af = fd.toAvroField();
        assertEquals("STRING", af.schema().getType().toString());
    }

    @Test
    public void testGetSchemaFromJson() throws Exception {
        String s = "schemaTest";
        String k = "";
        String f = "";
        Schema r = FieldDescription.getSchemaFromJson(s, f, k);
        assertNull(r);
        k = "[\"model\"]";
        f = "[{\"displayName\":\"Created At\",\"dataType\":\"datetime\",\"name\":\"createdAt\",\"updateable\":false},"
                + "{\"displayName\":\"Marketo GUID\",\"dataType\":\"string\",\"length\":36,\"name\":\"marketoGUID\","
                + "\"updateable\":false},{\"displayName\":\"Updated At\",\"dataType\":\"datetime\",\"name\":\"updatedAt\",\"updateable\":false},{\"displayName\":\"Acquired at\",\"dataType\":\"date\",\"name\":\"acquiredAt\",\"updateable\":true},{\"displayName\":\"Brand\",\"dataType\":\"string\",\"length\":255,\"name\":\"brand\",\"updateable\":true},{\"displayName\":\"Customer Id\",\"dataType\":\"integer\",\"name\":\"customerId\",\"updateable\":true},{\"displayName\":\"Model\",\"dataType\":\"string\",\"length\":255,\"name\":\"model\",\"updateable\":true}]";
        r = FieldDescription.getSchemaFromJson(s, f, k);
        assertNotNull(r);
        assertEquals("createdAt", r.getFields().get(0).name());
        assertEquals("true", r.getField("model").getProp(SchemaConstants.TALEND_COLUMN_IS_KEY));
        assertEquals("STRING", r.getField("marketoGUID").schema().getType().toString());
        assertEquals("STRING", r.getField("brand").schema().getType().toString());
        assertEquals("LONG", r.getField("createdAt").schema().getType().toString());
        assertEquals("java.util.Date", r.getField("createdAt").getProp(SchemaConstants.JAVA_CLASS_FLAG));
        assertEquals(DATETIME_PATTERN_REST, r.getField("createdAt").getProp(SchemaConstants.TALEND_COLUMN_PATTERN));
        assertEquals("LONG", r.getField("updatedAt").schema().getType().toString());
        assertEquals("java.util.Date", r.getField("updatedAt").getProp(SchemaConstants.JAVA_CLASS_FLAG));
        assertEquals(DATETIME_PATTERN_REST, r.getField("updatedAt").getProp(SchemaConstants.TALEND_COLUMN_PATTERN));
    }

}
