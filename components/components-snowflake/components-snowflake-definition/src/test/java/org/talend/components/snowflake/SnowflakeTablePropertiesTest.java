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
package org.talend.components.snowflake;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.talend.components.snowflake.SnowflakeTestBase.MockRuntimeSourceOrSinkTestFixture;
import org.talend.daikon.NamedThing;
import org.talend.daikon.SimpleNamedThing;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.presentation.Form;

/**
 * Unit tests for {@link SnowflakeTableProperties} class
 */
public class SnowflakeTablePropertiesTest {

    SnowflakeTableProperties tableProperties = new SnowflakeTableProperties("tableProperties");

    @Test
    public void testSetupLayout() {
        Assert.assertNull(tableProperties.getForm(Form.MAIN));
        Assert.assertNull(tableProperties.getForm(Form.REFERENCE));
        tableProperties.main.addForm(Form.create(tableProperties.main, Form.REFERENCE));
        tableProperties.setupLayout();
        Assert.assertNotNull(tableProperties.getForm(Form.MAIN));
        Assert.assertNotNull(tableProperties.getForm(Form.REFERENCE));
        Assert.assertNotNull(tableProperties.getForm(Form.MAIN).getWidget(tableProperties.tableName.getName()));
        Assert.assertNotNull(tableProperties.getForm(Form.REFERENCE).getWidget(tableProperties.tableName.getName()));
    }

    @Test
    public void testBeforeTableName() throws Exception {
        List<NamedThing> tableNames = new ArrayList<>();
        tableNames.add(new SimpleNamedThing());

        try (MockRuntimeSourceOrSinkTestFixture testFixture = new MockRuntimeSourceOrSinkTestFixture()) {
            testFixture.setUp();

            Mockito.when(testFixture.runtimeSourceOrSink.getSchemaNames(null)).thenReturn(tableNames);

            Assert.assertEquals(ValidationResult.Result.OK, tableProperties.beforeTableName().getStatus());
            Assert.assertEquals(tableNames.size(), tableProperties.tableName.getPossibleValues().size());
        }
    }

    @Test
    public void testBeforeTableNameFailedGetTableNames() throws Exception {
        try (MockRuntimeSourceOrSinkTestFixture testFixture = new MockRuntimeSourceOrSinkTestFixture()) {
            testFixture.setUp();

            Mockito.when(testFixture.runtimeSourceOrSink.getSchemaNames(null))
                    .thenThrow(new IOException("Failed get TableNames from Snowflake"));

            Assert.assertEquals(ValidationResult.Result.ERROR, tableProperties.beforeTableName().getStatus());
        }
    }

    @Test
    public void testAfterTableName() throws Exception {
        String tableName = "table1";
        Schema schema = SchemaBuilder.record("record").fields().endRecord();
        tableProperties.tableName.setValue(tableName);

        try (MockRuntimeSourceOrSinkTestFixture testFixture = new MockRuntimeSourceOrSinkTestFixture()) {
            testFixture.setUp();

            Mockito.when(testFixture.runtimeSourceOrSink.getEndpointSchema(null, tableName))
                    .thenReturn(schema);

            Assert.assertEquals(ValidationResult.Result.OK, tableProperties.afterTableName().getStatus());
            Assert.assertEquals(schema, tableProperties.main.schema.getValue());
        }
    }

    @Test
    public void testAfterTableNameFailedGetSchema() throws Exception {
        String tableName = "table1";
        tableProperties.tableName.setValue(tableName);

        try (MockRuntimeSourceOrSinkTestFixture testFixture = new MockRuntimeSourceOrSinkTestFixture()) {
            testFixture.setUp();

            Mockito.when(testFixture.runtimeSourceOrSink.getEndpointSchema(null, tableName))
                    .thenThrow(new IOException("Failed get schema from Snowflake"));

            Assert.assertEquals(ValidationResult.Result.ERROR, tableProperties.afterTableName().getStatus());
        }
    }
}
