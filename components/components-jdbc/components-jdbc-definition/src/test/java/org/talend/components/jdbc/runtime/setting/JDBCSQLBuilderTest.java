package org.talend.components.jdbc.runtime.setting;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.SchemaConstants;

public class JDBCSQLBuilderTest {

    Schema schema = SchemaBuilder.builder().record("schema").fields().name("ID1")
            .prop(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME, "ID1").prop(SchemaConstants.TALEND_COLUMN_IS_KEY, "true")
            .type(AvroUtils._string()).noDefault().name("ID2").prop(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME, "ID2")
            .prop(SchemaConstants.TALEND_COLUMN_IS_KEY, "true").type(AvroUtils._string()).noDefault().name("NAME")
            .prop(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME, "NAME").type(AvroUtils._string()).noDefault().name("ADDRESS")
            .prop(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME, "ADDRESS").type(AvroUtils._string()).noDefault().endRecord();

    @Test
    public void testGetInstance() {
        Assert.assertNotNull(JDBCSQLBuilder.getInstance());
    }

    @Test
    public void testGetProtectedChar() {
        Assert.assertEquals("", JDBCSQLBuilder.getInstance().getProtectedChar());
    }

    @Test
    public void testGenerateSQL4SelectTable() {
        Assert.assertEquals("SELECT TEST.ID1, TEST.ID2, TEST.NAME, TEST.ADDRESS FROM TEST",
                JDBCSQLBuilder.getInstance().generateSQL4SelectTable("TEST", schema));
    }

    @Test
    public void testGenerateSQL4DeleteTable() {
        Assert.assertEquals("DELETE FROM TEST", JDBCSQLBuilder.getInstance().generateSQL4DeleteTable("TEST"));
    }

    @Test
    public void testGenerateSQL4Insert() {
        Assert.assertEquals("INSERT INTO TEST (ID1,ID2,NAME,ADDRESS) VALUES (?,?,?,?)",
                JDBCSQLBuilder.getInstance().generateSQL4Insert("TEST", schema));
    }

    @Test
    public void testGenerateSQL4Delete() {
        Assert.assertEquals("DELETE FROM TEST WHERE ID1 = ? AND ID2 = ?",
                JDBCSQLBuilder.getInstance().generateSQL4Delete("TEST", schema));
    }

    @Test
    public void testGenerateSQL4Update() {
        Assert.assertEquals("UPDATE TEST SET NAME = ?,ADDRESS = ? WHERE ID1 = ? AND ID2 = ?",
                JDBCSQLBuilder.getInstance().generateSQL4Update("TEST", schema));
    }

    @Test
    public void testGenerateQuerySQL4InsertOrUpdate() {
        Assert.assertEquals("SELECT COUNT(1) FROM TEST WHERE ID1 = ? AND ID2 = ?",
                JDBCSQLBuilder.getInstance().generateQuerySQL4InsertOrUpdate("TEST", schema));
    }

}
