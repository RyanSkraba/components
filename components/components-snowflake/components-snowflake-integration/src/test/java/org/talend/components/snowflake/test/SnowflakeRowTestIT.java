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
package org.talend.components.snowflake.test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Collections;
import java.util.Date;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.talend.components.api.component.runtime.Reader;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.component.runtime.WriterWithFeedback;
import org.talend.components.api.service.common.DefinitionRegistry;
import org.talend.components.snowflake.SnowflakeConnectionProperties;
import org.talend.components.snowflake.runtime.SnowflakeRowSink;
import org.talend.components.snowflake.runtime.SnowflakeRowSource;
import org.talend.components.snowflake.runtime.SnowflakeRowStandalone;
import org.talend.components.snowflake.runtime.SnowflakeRowWriter;
import org.talend.components.snowflake.runtime.SnowflakeRuntime;
import org.talend.components.snowflake.runtime.utils.DriverManagerUtils;
import org.talend.components.snowflake.tsnowflakerow.TSnowflakeRowDefinition;
import org.talend.components.snowflake.tsnowflakerow.TSnowflakeRowProperties;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.definition.service.DefinitionRegistryService;

public class SnowflakeRowTestIT extends SnowflakeTestIT {

    private static final String TABLE_NAME = "SNOWFLAKE_ROW_IT_TEST_" + testNumber;

    private static final String REF_ID = "connection_1";

    private static Connection connection;

    private static TestRuntimeContainer container;

    private static SnowflakeConnectionProperties connectionProperties;

    private static SnowflakeRowStandalone rowStandalone;

    private static TSnowflakeRowProperties standaloneProperties;

    private static final Schema DEFAULT_SCHEMA = SchemaBuilder.record("Record").fields().name("ID")
            .prop(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME, "ID").type(AvroUtils._decimal()).noDefault().name("AGE")
            .prop(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME, "AGE").type(AvroUtils._decimal()).noDefault().name("NAME")
            .prop(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME, "NAME").type().nullable().stringType().noDefault()
            .name("CREATED_DATE").prop(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME, "CREATED_DATE")
            .type(AvroUtils._logicalTimestamp()).noDefault().endRecord();

    @BeforeClass
    public static void prepareTests() throws Exception {
        container = new TestRuntimeContainer();
        connectionProperties = new SnowflakeConnectionProperties("connection");
        connectionProperties.userPassword.userId.setStoredValue(USER);
        connectionProperties.userPassword.password.setStoredValue(PASSWORD);
        connectionProperties.account.setStoredValue(ACCOUNT_STR);
        connectionProperties.region.setStoredValue(SNOWFLAKE_REGION);
        connectionProperties.warehouse.setStoredValue(WAREHOUSE);
        connectionProperties.db.setStoredValue(DB);
        connectionProperties.schemaName.setStoredValue(SCHEMA);
        connectionProperties.loginTimeout.setStoredValue(1);
        container.setComponentData(REF_ID, SnowflakeRuntime.KEY_CONNECTION,
                DriverManagerUtils.getConnection(connectionProperties));

        rowStandalone = new SnowflakeRowStandalone();
        standaloneProperties = new TSnowflakeRowProperties("standalone");
        standaloneProperties.connection.referencedComponent.componentInstanceId.setValue(REF_ID);
        standaloneProperties.connection.referencedComponent.setReference(connectionProperties);
        standaloneProperties.query.setStoredValue("alter user " + USER + " set mins_to_unlock=0");
        rowStandalone.initialize(container, standaloneProperties);
        rowStandalone.runAtDriver(container);
        standaloneProperties.query.setStoredValue("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(" + "ID INT," + "AGE INT,"
                + "NAME VARCHAR(20)," + "CREATED_DATE TIMESTAMP" + ");");
        rowStandalone.runAtDriver(container);

    }

    @AfterClass
    public static void cleanTests() throws Exception {
        standaloneProperties.query.setStoredValue("DROP TABLE IF EXISTS " + TABLE_NAME);
        rowStandalone.runAtDriver(container);

        if (connection != null) {
            connection.close();
        }
    }

    @Test
    public void testDynamicSchemaReadValues() throws Exception {
        writeRows(10);
        TSnowflakeRowProperties properties = createDefaultProperties("input");
        Schema dynamicSchema = SchemaBuilder.record("Dynamic").prop(SchemaConstants.INCLUDE_ALL_FIELDS, "true").fields()
                .requiredString("Dyna").endRecord();
        properties.table.main.schema.setValue(dynamicSchema);
        properties.query.setStoredValue("SELECT ID, NAME, CREATED_DATE FROM " + TABLE_NAME);
        Reader<IndexedRecord> reader = createReader(properties);
        int i = 1;
        if (reader.start()) {
            do {
                IndexedRecord record = reader.getCurrent();
                Assert.assertTrue(new BigDecimal(i).equals(record.get(0)));
                Assert.assertTrue(("foo_" + i).equals(record.get(1)));
                i++;
            } while (reader.advance());
        }
        Assert.assertEquals(11, i);
    }

    private void writeRows(int count) throws Exception {
        TSnowflakeRowProperties outputProperties = createDefaultProperties("output");
        outputProperties.table.main.schema.setValue(DEFAULT_SCHEMA);
        WriterWithFeedback<?, ?, ?> writer = createWriter(outputProperties);
        writer.open("");
        for (int i = 1; i <= count; i++) {
            IndexedRecord row = makeRow(i);
            outputProperties.query.setValue("INSERT INTO " + TABLE_NAME + "(ID, AGE, NAME, CREATED_DATE) VALUES(" + row.get(0)
                    + ", " + row.get(1) + ", '" + row.get(2) + "', to_timestamp(" + row.get(3) + "))");
            writer.write(row);
        }
    }

    private IndexedRecord makeRow(int i) {
        GenericData.Record row = new GenericData.Record(DEFAULT_SCHEMA);

        row.put("ID", i);
        row.put("AGE", 27);
        row.put("NAME", "foo_" + i);
        row.put("CREATED_DATE", new Date().getTime() / 1000);
        return row;
    }

    private TSnowflakeRowProperties createDefaultProperties(String name) {
        TSnowflakeRowProperties properties = new TSnowflakeRowProperties(name);
        properties.connection.referencedComponent.componentInstanceId.setValue(REF_ID);
        properties.connection.referencedComponent.setReference(connectionProperties);
        properties.setupProperties();
        return properties;
    }

    private Reader<IndexedRecord> createReader(TSnowflakeRowProperties properties) {
        SnowflakeRowSource rowSource = new SnowflakeRowSource();

        rowSource.initialize(container, properties);
        return rowSource.createReader(container);
    }

    private WriterWithFeedback<Result, IndexedRecord, IndexedRecord> createWriter(TSnowflakeRowProperties properties) {
        SnowflakeRowSink rowSink = new SnowflakeRowSink();
        rowSink.initialize(container, properties);
        return (SnowflakeRowWriter) rowSink.createWriteOperation().createWriter(container);
    }

    @Override
    public DefinitionRegistryService getDefinitionService() {
        DefinitionRegistry definitionRegistry = new DefinitionRegistry();
        definitionRegistry.registerDefinition(Collections.singleton(new TSnowflakeRowDefinition()));
        return definitionRegistry;
    }
}