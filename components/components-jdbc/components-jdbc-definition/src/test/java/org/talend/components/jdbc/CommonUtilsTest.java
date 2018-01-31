package org.talend.components.jdbc;

import java.util.ArrayList;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.SchemaBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.talend.components.jdbc.module.JDBCConnectionModule;
import org.talend.components.jdbc.runtime.setting.AllSetting;
import org.talend.components.jdbc.tjdbcconnection.TJDBCConnectionProperties;
import org.talend.components.jdbc.tjdbcinput.TJDBCInputProperties;
import org.talend.components.jdbc.tjdbcoutput.TJDBCOutputProperties;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.properties.presentation.Form;

public class CommonUtilsTest {

    @Test
    public void testAddForm() {
        TJDBCConnectionProperties properties = new TJDBCConnectionProperties("row");
        Form main = CommonUtils.addForm(properties, Form.MAIN);
        Form advanced = CommonUtils.addForm(properties, Form.ADVANCED);

        Assert.assertTrue(main == properties.getForm(Form.MAIN));
        Assert.assertTrue(advanced == properties.getForm(Form.ADVANCED));
    }

    @Test
    public void testGetMainSchemaFromOutputConnector() {
        TJDBCInputProperties properties = new TJDBCInputProperties("input");
        properties.init();

        Schema schema = CommonUtils.getMainSchemaFromOutputConnector(properties);
        Assert.assertNotNull(schema);
    }

    @Test
    public void testGetMainSchemaFromInputConnector() {
        TJDBCOutputProperties properties = new TJDBCOutputProperties("output");
        properties.init();

        Schema schema = CommonUtils.getMainSchemaFromInputConnector(properties);
        Assert.assertNotNull(schema);
    }

    @Test
    public void testGetOutputSchema() {
        TJDBCInputProperties properties = new TJDBCInputProperties("input");
        properties.init();

        Schema schema = CommonUtils.getOutputSchema(properties);
        Assert.assertNotNull(schema);
    }

    @Test
    public void testGetRejectSchema() {
        TJDBCOutputProperties properties = new TJDBCOutputProperties("output");
        properties.init();

        Schema schema = CommonUtils.getRejectSchema(properties);
        Assert.assertNotNull(schema);
    }

    @Test
    public void testNewSchema() {
        Schema schema = SchemaBuilder.builder().record("schema").prop(SchemaConstants.INCLUDE_ALL_FIELDS, "true").fields()
                .name("ID").prop(SchemaConstants.TALEND_COLUMN_DB_LENGTH, "1").type(AvroUtils._string()).noDefault().endRecord();

        final List<Schema.Field> additionalFields = new ArrayList<Schema.Field>();

        Schema.Field field = new Schema.Field("NAME", Schema.create(Schema.Type.STRING), null, (Object) null);
        field.addProp(SchemaConstants.TALEND_COLUMN_DB_LENGTH, "2");
        additionalFields.add(field);

        field = new Schema.Field("ADDRESS", Schema.create(Schema.Type.STRING), null, (Object) null);
        field.addProp(SchemaConstants.TALEND_COLUMN_DB_LENGTH, "3");
        additionalFields.add(field);

        Schema newSchema = CommonUtils.newSchema(schema, "newName", additionalFields);

        List<Schema.Field> fields = newSchema.getFields();
        Assert.assertEquals(3, fields.size());

        Assert.assertEquals("1", fields.get(0).getProp(SchemaConstants.TALEND_COLUMN_DB_LENGTH));
        Assert.assertEquals("2", fields.get(1).getProp(SchemaConstants.TALEND_COLUMN_DB_LENGTH));
        Assert.assertEquals("3", fields.get(2).getProp(SchemaConstants.TALEND_COLUMN_DB_LENGTH));

        Assert.assertEquals("true", newSchema.getProp(SchemaConstants.INCLUDE_ALL_FIELDS));
    }

    @Test
    public void testSetCommonConnectionInfo() {
        JDBCConnectionModule module = new JDBCConnectionModule("module");
        module.jdbcUrl.setValue("url");

        AllSetting setting = new AllSetting();
        CommonUtils.setCommonConnectionInfo(setting, module);

        Assert.assertEquals("url", setting.getJdbcUrl());
    }

    @Test
    public void testMergeRuntimeSchema2DesignSchema4Dynamic() {
        Schema runtimeSchema = SchemaBuilder.builder().record("schema").prop(SchemaConstants.INCLUDE_ALL_FIELDS, "true")
                .prop(ComponentConstants.TALEND6_DYNAMIC_COLUMN_POSITION, "1").fields()
                .name("ID").prop(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME, "ID_DB").type(AvroUtils._string()).noDefault()
                .name("NAME").prop(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME, "NAME_DB").type(AvroUtils._string()).noDefault()
                .name("AGE").prop(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME, "AGE_DB").type(AvroUtils._string()).noDefault()
                .name("SCORE").prop(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME, "SCORE_DB").type(AvroUtils._string()).noDefault()
                .name("ADDRESS").prop(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME, "ADDRESS_DB").type(AvroUtils._string()).noDefault()
                .endRecord();
        
        Schema designSchema = SchemaBuilder.builder().record("schema").prop(SchemaConstants.INCLUDE_ALL_FIELDS, "true")
                .prop(ComponentConstants.TALEND6_DYNAMIC_COLUMN_POSITION, "1").fields().name("ID")
                .prop(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME, "ID_WRONG").type(AvroUtils._string()).noDefault().name("ADDRESS")
                .prop(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME, "ADDRESS_WRONG").type(AvroUtils._string()).noDefault()
                .endRecord();

        Schema result = CommonUtils.mergeRuntimeSchema2DesignSchema4Dynamic(designSchema, runtimeSchema);
        List<Field> fields = result.getFields();
        Assert.assertEquals(5,fields.size());
        
        Assert.assertEquals("ID",fields.get(0).name());
        Assert.assertEquals("ID_WRONG",fields.get(0).getProp(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME));
        
        Assert.assertEquals("NAME",fields.get(1).name());
        Assert.assertEquals("NAME_DB",fields.get(1).getProp(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME));
        
        Assert.assertEquals("AGE",fields.get(2).name());
        Assert.assertEquals("AGE_DB",fields.get(2).getProp(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME));
        
        Assert.assertEquals("SCORE",fields.get(3).name());
        Assert.assertEquals("SCORE_DB",fields.get(3).getProp(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME));
        
        Assert.assertEquals("ADDRESS",fields.get(4).name());
        Assert.assertEquals("ADDRESS_WRONG",fields.get(4).getProp(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME));
    }

}
