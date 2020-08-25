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
package org.talend.components.snowflake.tsnowflakeoutput;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.common.tableaction.TableAction;
import org.talend.components.snowflake.SnowflakeDbTypeProperties;
import org.talend.components.snowflake.tsnowflakeoutput.TSnowflakeOutputProperties.OutputAction;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.SchemaProperty;

/**
 * Unit tests for {@link TSnowflakeOutputProperties} class
 */
public class TSnowflakeOutputPropertiesTest {

    private static final String TALEND6_COLUMN_TALEND_TYPE = "di.column.talendType";

    TSnowflakeOutputProperties outputProperties;

    @Before
    public void reset() {
        outputProperties = new TSnowflakeOutputProperties("output");
        outputProperties.init();
    }

    @Test
    public void testLayoutOnOutputActionChange() {
        boolean isOutputActionPropertyVisible;
        boolean isUpsertKeyColumnVisible;
        boolean isUpsertKeyColumnVisibleWhenOutputActionIsUpsert;
        boolean isTableActionProperyVisible;

        Form main = outputProperties.getForm(Form.MAIN);
        Form advanced = outputProperties.getForm(Form.ADVANCED);
        isOutputActionPropertyVisible = main.getWidget(outputProperties.outputAction).isVisible();
        isUpsertKeyColumnVisible = main.getWidget(outputProperties.upsertKeyColumn).isVisible();
        isTableActionProperyVisible = main.getWidget(outputProperties.tableAction).isVisible();
        boolean usePersonalDBTypePropertyVisible = advanced.getWidget(outputProperties.usePersonalDBType).isVisible();
        boolean isDbTypePropertyVisible = advanced.getWidget(outputProperties.dbtypeTable).isVisible();

        outputProperties.outputAction.setValue(OutputAction.UPSERT);
        outputProperties.afterOutputAction();
        outputProperties.tableAction.setValue(TableAction.TableActionEnum.CREATE);
        outputProperties.afterTableAction();

        boolean usePersonalDBTypePropertyVisibleIfCreateAction = advanced.getWidget(outputProperties.usePersonalDBType).isVisible();
        boolean isDbTypePropertyVisibleIfCreateAction = advanced.getWidget(outputProperties.dbtypeTable).isVisible();

        boolean isUseSchemaKeysVisible = advanced.getWidget(outputProperties.useSchemaKeysForUpsert).isVisible();
        isUpsertKeyColumnVisibleWhenOutputActionIsUpsert = main.getWidget(outputProperties.upsertKeyColumn).isVisible();

        assertTrue(isOutputActionPropertyVisible);
        assertFalse(isUpsertKeyColumnVisible);
        assertFalse(isUpsertKeyColumnVisibleWhenOutputActionIsUpsert);
        assertTrue(isUseSchemaKeysVisible);
        assertTrue(isTableActionProperyVisible);
        assertFalse(usePersonalDBTypePropertyVisible);
        assertFalse(isDbTypePropertyVisible);
        assertTrue(usePersonalDBTypePropertyVisibleIfCreateAction);
        assertFalse(isDbTypePropertyVisibleIfCreateAction);
    }

    @Test
    public void testDefaultValue() {
        OutputAction defaultValueOutputAction;
        boolean defaultConvertColumnsAndTableToUppercase;
        TableAction.TableActionEnum tableAction;
        boolean useSchemaDatePattern;

        defaultValueOutputAction = outputProperties.outputAction.getValue();
        defaultConvertColumnsAndTableToUppercase = outputProperties.convertColumnsAndTableToUppercase.getValue();
        tableAction = outputProperties.tableAction.getValue();
        useSchemaDatePattern = outputProperties.useSchemaDatePattern.getValue();

        boolean defaultUsePersonalDBType = outputProperties.usePersonalDBType.getValue();
        List<String> defaultDBTypeColumns = outputProperties.dbtypeTable.column.getValue();
        List<String> defaultDBTypeType = outputProperties.dbtypeTable.dbtype.getValue();

        assertEquals(defaultValueOutputAction, OutputAction.INSERT);
        assertTrue(defaultConvertColumnsAndTableToUppercase);
        assertEquals(TableAction.TableActionEnum.NONE, tableAction);

        assertFalse(defaultUsePersonalDBType);
        assertEquals(Collections.emptyList(), defaultDBTypeColumns);
        assertEquals(Collections.emptyList(), defaultDBTypeType);
        assertFalse(useSchemaDatePattern);
        assertTrue(outputProperties.useSchemaKeysForUpsert.getValue());
    }

    @Test
    public void testTriggers() {
        Form main = outputProperties.getForm(Form.MAIN);
        Form advanced = outputProperties.getForm(Form.ADVANCED);
        boolean isOutputActionCalledAfter = main.getWidget(outputProperties.outputAction).isCallAfter();
        boolean isUsePersonalDBTypeAfter = advanced.getWidget(outputProperties.usePersonalDBType).isCallAfter();

        assertTrue(isOutputActionCalledAfter);
        assertTrue(isUsePersonalDBTypeAfter);
    }

    @Test
    public void testGetAllSchemaPropertiesConnectors() {
        Set<PropertyPathConnector> schemaPropertyForInputConnection;

        schemaPropertyForInputConnection = outputProperties.getAllSchemaPropertiesConnectors(true);

        assertEquals(1, schemaPropertyForInputConnection.size());

        assertTrue(schemaPropertyForInputConnection.contains(outputProperties.REJECT_CONNECTOR));
    }

    @Test
    public void testGetAllSchemaPropertiesConnectorsForOutputConnection() {
        Set<PropertyPathConnector> schemaPropertyForOutputConnection;

        schemaPropertyForOutputConnection = outputProperties.getAllSchemaPropertiesConnectors(false);

        assertEquals(1, schemaPropertyForOutputConnection.size());

        // BUG THERE??? Method sets MAIN_CONNECTOR instead of FLOW_CONNECTOR
        assertTrue(schemaPropertyForOutputConnection.contains(outputProperties.FLOW_CONNECTOR));
    }

    @Test
    public void testGetFieldNames() {
        Schema runtimeSchema;
        Property<Schema> schemaProperty;
        List<String> propertyFieldNames;
        List<String> expectedPropertyFieldNames;

        runtimeSchema = SchemaBuilder.builder().record("Record").fields() //
                .name("logicalTime").type(AvroUtils._logicalTime()).noDefault() //
                .name("logicalDate").type(AvroUtils._logicalDate()).noDefault() //
                .name("logicalTimestamp").type(AvroUtils._logicalTimestamp()).noDefault() //
                .name("id").type().intType().noDefault() //
                .name("name").type().stringType().noDefault() //
                .name("age").type().intType().noDefault() //
                .name("valid").type().booleanType().noDefault() //
                .name("address").type().stringType().noDefault() //
                .name("comment").prop(SchemaConstants.TALEND_COLUMN_DB_LENGTH, "255").type().stringType().noDefault() //
                .name("createdDate").prop(TALEND6_COLUMN_TALEND_TYPE, "id_Date") //
                .prop(SchemaConstants.TALEND_COLUMN_PATTERN, "yyyy-MM-dd'T'HH:mm:ss'000Z'").type().nullable().longType() //
                .noDefault() //
                .endRecord(); //

        schemaProperty = new SchemaProperty("schema");
        schemaProperty.setValue(runtimeSchema);

        expectedPropertyFieldNames = new ArrayList<>();
        expectedPropertyFieldNames.add("logicalTime");
        expectedPropertyFieldNames.add("logicalDate");
        expectedPropertyFieldNames.add("logicalTimestamp");
        expectedPropertyFieldNames.add("id");
        expectedPropertyFieldNames.add("name");
        expectedPropertyFieldNames.add("age");
        expectedPropertyFieldNames.add("valid");
        expectedPropertyFieldNames.add("address");
        expectedPropertyFieldNames.add("comment");
        expectedPropertyFieldNames.add("createdDate");

        propertyFieldNames = outputProperties.getFieldNames(schemaProperty);

        assertEquals(propertyFieldNames, expectedPropertyFieldNames);
    }

    @Test
    public void testGetFieldsOfEmptySchema() {
        Schema emptySchema;
        Property<Schema> emptySchemaProperty;
        List<String> emptyPropertyFieldNames;


        emptySchema = SchemaBuilder.builder().record("EmptyRecord").fields().endRecord();
        emptySchemaProperty = new SchemaProperty("Empty schema");
        emptySchemaProperty.setValue(emptySchema);
        emptyPropertyFieldNames = outputProperties.getFieldNames(emptySchemaProperty);

        assertTrue(emptyPropertyFieldNames.isEmpty());
    }

    @Test
    public void testAfterTableName() throws Exception {
        Schema schema = SchemaBuilder.builder().record("Record").fields() //
                .requiredInt("id")
                .requiredString("name")
                .requiredInt("age")
                .endRecord();
        outputProperties.setupProperties();
        outputProperties.table.main.schema.setValue(schema);
        Assert.assertTrue(outputProperties.upsertKeyColumn.getPossibleValues().isEmpty());
        outputProperties.table.afterTableName();
        Assert.assertEquals(3, outputProperties.upsertKeyColumn.getPossibleValues().size());
    }

    @Test
    public void testAfterTableWithNotSetSchema() throws Exception {
        outputProperties.setupProperties();
        Assert.assertTrue(outputProperties.upsertKeyColumn.getPossibleValues().isEmpty());
        outputProperties.table.afterTableName();
        Assert.assertTrue(outputProperties.upsertKeyColumn.getPossibleValues().isEmpty());
    }

    @Test
    public void testAfterSchema() {
        Schema schema = SchemaBuilder.builder().record("Record").fields() //
                .requiredInt("id").endRecord();
        outputProperties.setupProperties();
        outputProperties.table.main.schema.setValue(schema);
        Assert.assertTrue(outputProperties.schemaReject.schema.getValue().getFields().isEmpty());
        outputProperties.table.schemaListener.afterSchema();
        Assert.assertEquals(9, outputProperties.schemaReject.schema.getValue().getFields().size());
        Assert.assertEquals(1, outputProperties.dbtypeTable.column.getPossibleValues().size());
        Assert.assertEquals("id", outputProperties.dbtypeTable.column.getPossibleValues().get(0));
    }

    @Test
    public void testAfterUseSchemaDefinedKeysForUpsert() {
        outputProperties.outputAction.setValue(OutputAction.UPSERT);
        assertFalse(outputProperties.getForm(Form.MAIN).getWidget(outputProperties.upsertKeyColumn).isVisible());
        outputProperties.useSchemaKeysForUpsert.setValue(false);
        outputProperties.afterUseSchemaKeysForUpsert();
        assertTrue(outputProperties.getForm(Form.MAIN).getWidget(outputProperties.upsertKeyColumn).isVisible());
    }

}
