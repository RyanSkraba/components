package org.talend.components.jdbc.runtime.setting;

import java.util.Arrays;
import java.util.List;

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

    List<JDBCSQLBuilder.Column> basicColumnList = null;

    List<JDBCSQLBuilder.Column> advancedColumnList = null;

    {
        basicColumnList = JDBCSQLBuilder.getInstance().createColumnList(new AllSetting(), schema);

        AllSetting setting = new AllSetting();
        setting.setEnableFieldOptions(true);
        setting.setSchemaColumns4FieldOption(Arrays.asList("ID1", "ID2", "NAME", "ADDRESS"));
        setting.setUpdateKey4FieldOption(Arrays.asList(false, true, false, false));
        setting.setDeletionKey4FieldOption(Arrays.asList(true, false, false, false));
        setting.setInsertable4FieldOption(Arrays.asList(false, false, true, true));
        setting.setUpdatable4FieldOption(Arrays.asList(false, false, true, true));

        setting.setReferenceColumns4AdditionalParameters(Arrays.asList("ID2", "ID2", "NAME", "NAME", "ADDRESS", "ADDRESS"));
        setting.setPositions4AdditionalParameters(Arrays.asList("BEFORE", "AFTER", "REPLACE", "REPLACE", "BEFORE", "AFTER"));
        setting.setNewDBColumnNames4AdditionalParameters(Arrays.asList("ID3", "ID4", "NAME1", "NAME2", "ADDRESS1", "ADDRESS2"));
        setting.setSqlExpressions4AdditionalParameters(Arrays.asList("now1()", "now2()", "now3()", "now4()", "now5()", "now6()"));
        
        advancedColumnList = JDBCSQLBuilder.getInstance().createColumnList(setting, schema);
    }

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
    public void testGenerateSQL4Insert1() {
        Assert.assertEquals("INSERT INTO TEST (ID1,ID2,NAME,ADDRESS) VALUES (?,?,?,?)",
                JDBCSQLBuilder.getInstance().generateSQL4Insert("TEST", schema));
    }

    @Test
    public void testGenerateSQL4Insert2() {
        Assert.assertEquals("INSERT INTO TEST (ID1,ID2,NAME,ADDRESS) VALUES (?,?,?,?)",
                JDBCSQLBuilder.getInstance().generateSQL4Insert("TEST", basicColumnList));
    }

    @Test
    public void testGenerateSQL4InsertAndFieldOptions() {
        Assert.assertEquals("INSERT INTO TEST (ID3,ID4,NAME1,NAME2,ADDRESS1,ADDRESS,ADDRESS2) VALUES (now1(),now2(),now3(),now4(),now5(),?,now6())",
                JDBCSQLBuilder.getInstance().generateSQL4Insert("TEST", advancedColumnList));
    }

    @Test
    public void testGenerateSQL4Delete() {
        Assert.assertEquals("DELETE FROM TEST WHERE ID1 = ? AND ID2 = ?",
                JDBCSQLBuilder.getInstance().generateSQL4Delete("TEST", basicColumnList));
    }

    @Test
    public void testGenerateSQL4DeleteAndFieldOptions() {
        Assert.assertEquals("DELETE FROM TEST WHERE ID1 = ?",
                JDBCSQLBuilder.getInstance().generateSQL4Delete("TEST", advancedColumnList));
    }

    @Test
    public void testGenerateSQL4Update() {
        Assert.assertEquals("UPDATE TEST SET NAME = ?,ADDRESS = ? WHERE ID1 = ? AND ID2 = ?",
                JDBCSQLBuilder.getInstance().generateSQL4Update("TEST", basicColumnList));
    }

    @Test
    public void testGenerateSQL4UpdateAndFieldOptions() {
        Assert.assertEquals("UPDATE TEST SET ID3 = now1(),ID4 = now2(),NAME1 = now3(),NAME2 = now4(),ADDRESS1 = now5(),ADDRESS = ?,ADDRESS2 = now6() WHERE ID2 = ?",
                JDBCSQLBuilder.getInstance().generateSQL4Update("TEST", advancedColumnList));
    }

    @Test
    public void testGenerateQuerySQL4InsertOrUpdate() {
        Assert.assertEquals("SELECT COUNT(1) FROM TEST WHERE ID1 = ? AND ID2 = ?",
                JDBCSQLBuilder.getInstance().generateQuerySQL4InsertOrUpdate("TEST", basicColumnList));
    }

    @Test
    public void testGenerateQuerySQL4InsertOrUpdateAndFieldOptions() {
        Assert.assertEquals("SELECT COUNT(1) FROM TEST WHERE ID2 = ?",
                JDBCSQLBuilder.getInstance().generateQuerySQL4InsertOrUpdate("TEST", advancedColumnList));
    }

}
