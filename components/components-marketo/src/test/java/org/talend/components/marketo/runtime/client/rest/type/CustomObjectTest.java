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
import static org.junit.Assert.assertNotNull;

import java.util.Date;

import org.apache.avro.generic.IndexedRecord;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.marketo.runtime.client.rest.type.CustomObject.ObjectRelation;
import org.talend.components.marketo.runtime.client.rest.type.CustomObject.RelatedObject;

public class CustomObjectTest {

    CustomObject co;

    Date now;

    @Before
    public void setUp() throws Exception {
        co = new CustomObject();
        now = new Date(117, 3, 21);
    }

    public CustomObject feedCO() {
        CustomObject cust = new CustomObject();
        FieldDescription fd = new FieldDescription();
        fd.setName("avrof");
        fd.setId(123456);
        fd.setDataType("phone");
        fd.setDisplayName("My phone number");
        fd.setLength(25);
        fd.setUpdateable(true);
        FieldDescription[] fields = { fd, fd, fd };
        String[][] search = new String[][] { { "one", "two", "three" } };
        ObjectRelation relation = cust.new ObjectRelation();
        relation.setField("related");
        relation.setType("string");
        RelatedObject related = cust.new RelatedObject();
        related.setField("rel");
        related.setName("Related");
        relation.setRelatedTo(related);
        ObjectRelation[] relations = { relation, relation };
        cust.setName("custom");
        cust.setDisplayName("Custom");
        cust.setDescription("a custom object");
        cust.setIdField("model");
        cust.setCreatedAt(now);
        cust.setUpdatedAt(now);
        cust.setDedupeFields(new String[] { "brand", "model" });
        cust.setFields(fields);
        cust.setRelationships(relations);
        cust.setSearchableFields(search);
        return cust;
    }

    @Test
    public void testSettersAndGetters() throws Exception {
        co = feedCO();
        //
        assertEquals("custom", co.getName());
        assertEquals("Custom", co.getDisplayName());
        assertEquals("a custom object", co.getDescription());
        assertEquals("model", co.getIdField());
        assertEquals(now, co.getCreatedAt());
        assertEquals(now, co.getUpdatedAt());
        assertEquals("brand", co.getDedupeFields()[0]);
        assertEquals(3, co.getFields().length);
        assertEquals("one", co.getSearchableFields()[0][0]);
        assertEquals("two", co.getSearchableFields()[0][1]);
        assertEquals("three", co.getSearchableFields()[0][2]);
        assertEquals("related", co.getRelationships()[0].getField());
        assertEquals("string", co.getRelationships()[0].getType());
        assertEquals("Related", co.getRelationships()[0].getRelatedTo().getName());
        assertEquals("rel", co.getRelationships()[0].getRelatedTo().getField());
    }

    @Test
    public void testToString() throws Exception {
        String s = "CustomObject{name='null', displayName='null', description='null', createdAt=null, updatedAt=null,"
                + " idField='null', dedupeFields=null, searchableFields=null, fields=null, relationships=null}";
        assertEquals(s, co.toString());
        co = feedCO();
        co.setCreatedAt(null);
        co.setUpdatedAt(null);
        s = "CustomObject{name='custom', displayName='Custom', description='a custom object', createdAt=null, "
                + "updatedAt=null, idField='model', dedupeFields=[brand, model], searchableFields=[[one, two, three]], "
                + "fields=[FieldDescription{id=123456, displayName='My phone number', dataType='phone', length=25, rest=null, soap=null}, FieldDescription{id=123456, displayName='My phone number', dataType='phone', length=25, rest=null, soap=null}, FieldDescription{id=123456, displayName='My phone number', dataType='phone', length=25, rest=null, soap=null}], relationships=[ObjectRelation{field='related', relatedTo=RelatedObject{name='Related', field='rel'}, type='string'}, ObjectRelation{field='related', relatedTo=RelatedObject{name='Related', field='rel'}, type='string'}]}";
        assertEquals(s, co.toString());
        co.setCreatedAt(new Date());
        assertNotNull(co.toString());
        co.setUpdatedAt(co.getCreatedAt());
        assertNotNull(co.toString());
    }

    @Test
    public void testToIndexedRecord() throws Exception {
        IndexedRecord r = co.toIndexedRecord();
        assertNotNull(r);
        co = feedCO();
        r = co.toIndexedRecord();
        assertNotNull(r);
        assertEquals(co.getName(), r.get(0));
        assertEquals(co.getDisplayName(), r.get(1));
        assertEquals(co.getDescription(), r.get(2));
        assertEquals(co.getCreatedAt(), r.get(3));
        assertEquals(co.getUpdatedAt(), r.get(4));
        assertEquals(co.getIdField(), r.get(5));
        assertEquals("[\"brand\",\"model\"]", r.get(6));
        assertEquals("[[\"one\",\"two\",\"three\"]]", r.get(7));
        assertEquals(
                "[{\"id\":123456,\"displayName\":\"My phone number\",\"dataType\":\"phone\",\"length\":25,\"name\":\"avrof\",\"updateable\":true},{\"id\":123456,\"displayName\":\"My phone number\",\"dataType\":\"phone\",\"length\":25,\"name\":\"avrof\",\"updateable\":true},{\"id\":123456,\"displayName\":\"My phone number\",\"dataType\":\"phone\",\"length\":25,\"name\":\"avrof\",\"updateable\":true}]",
                r.get(8));
        assertEquals(
                "[{\"field\":\"related\",\"relatedTo\":{\"name\":\"Related\",\"field\":\"rel\"},\"type\":\"string\"},{\"field\":\"related\",\"relatedTo\":{\"name\":\"Related\",\"field\":\"rel\"},\"type\":\"string\"}]",
                r.get(9));
    }

}
