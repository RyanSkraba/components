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
package org.talend.components.jdbc;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.talend.components.api.component.runtime.Reader;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.jdbc.common.DBTestUtils;
import org.talend.components.jdbc.module.SPParameterTable;
import org.talend.components.jdbc.runtime.JDBCSPSink;
import org.talend.components.jdbc.runtime.JDBCSPSource;
import org.talend.components.jdbc.runtime.JDBCSPSourceOrSink;
import org.talend.components.jdbc.runtime.setting.AllSetting;
import org.talend.components.jdbc.runtime.writer.JDBCSPWriter;
import org.talend.components.jdbc.tjdbcsp.TJDBCSPDefinition;
import org.talend.components.jdbc.tjdbcsp.TJDBCSPProperties;
import org.talend.daikon.properties.ValidationResult;

public class JDBCSPTestIT {

    public static AllSetting allSetting;

    @BeforeClass
    public static void beforeClass() throws Exception {
        allSetting = DBTestUtils.createAllSetting();

        DBTestUtils.createTable(allSetting);
    }

    @AfterClass
    public static void afterClass() throws ClassNotFoundException, SQLException {
        DBTestUtils.releaseResource(allSetting);
    }

    @Before
    public void before() throws Exception {
        DBTestUtils.truncateTableAndLoadData(allSetting);
    }

    @Test
    public void test_basic_no_connector() throws Exception {
        TJDBCSPDefinition definition = new TJDBCSPDefinition();
        TJDBCSPProperties properties = DBTestUtils.createCommonJDBCSPProperties(allSetting, definition);
        
        properties.spName.setValue("SYSCS_UTIL.SYSCS_EMPTY_STATEMENT_CACHE");

        JDBCSPSourceOrSink sourceOrSink = new JDBCSPSourceOrSink();

        sourceOrSink.initialize(null, properties);
        ValidationResult result = sourceOrSink.validate(null);
        Assert.assertTrue(result.getStatus() == ValidationResult.Result.OK);
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void test_basic_as_input() throws Exception {
        TJDBCSPDefinition definition = new TJDBCSPDefinition();
        TJDBCSPProperties properties = DBTestUtils.createCommonJDBCSPProperties(allSetting, definition);

        properties.isFunction.setValue(true);
        properties.returnResultIn.setValue("PARAMETER");
        properties.spName.setValue("SYSCS_UTIL.SYSCS_GET_DATABASE_NAME");
        Schema schema = DBTestUtils.createSPSchema2();
        properties.main.schema.setValue(schema);
        properties.schemaFlow.schema.setValue(schema);

        JDBCSPSource source = new JDBCSPSource();

        source.initialize(null, properties);
        ValidationResult result = source.validate(null);
        Assert.assertTrue(result.getStatus() == ValidationResult.Result.OK);

        Reader reader = source.createReader(null);
        try {
            reader.start();
            IndexedRecord row = (IndexedRecord) reader.getCurrent();
            String v1 = (String) row.get(0);
            Assert.assertEquals("memory:myDB", v1);

            Assert.assertFalse(reader.advance());// only output one row when it works as a input component

            reader.close();
        } finally {
            reader.close();
        }
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void test_basic_as_output_and_no_input() throws Exception {
        TJDBCSPDefinition definition = new TJDBCSPDefinition();
        TJDBCSPProperties properties = DBTestUtils.createCommonJDBCSPProperties(allSetting, definition);

        properties.spName.setValue("SYSCS_UTIL.SYSCS_DISABLE_LOG_ARCHIVE_MODE");
        properties.main.schema.setValue(DBTestUtils.createSPSchema1());
        properties.spParameterTable.parameterTypes.setValue(Arrays.asList(SPParameterTable.ParameterType.IN.name()));
        properties.spParameterTable.schemaColumns.setValue(Arrays.asList("PARAMETER"));

        JDBCSPSink sink = new JDBCSPSink();

        sink.initialize(null, properties);
        ValidationResult result = sink.validate(null);
        Assert.assertTrue(result.getStatus() == ValidationResult.Result.OK);

        WriteOperation operation = sink.createWriteOperation();
        JDBCSPWriter writer = (JDBCSPWriter) operation.createWriter(null);

        try {
            writer.open("wid");

            IndexedRecord r1 = new GenericData.Record(properties.main.schema.getValue());
            r1.put(0, 0);
            
            writer.write(r1);
        } finally {
            writer.close();
        }
    }
    
    @SuppressWarnings("rawtypes")
    @Test
    public void test_basic_as_output_and_input() throws Exception {
        TJDBCSPDefinition definition = new TJDBCSPDefinition();
        TJDBCSPProperties properties = DBTestUtils.createCommonJDBCSPProperties(allSetting, definition);

        properties.spName.setValue("SYSCS_UTIL.SYSCS_DISABLE_LOG_ARCHIVE_MODE");
        Schema schema = DBTestUtils.createSPSchema3();
        properties.main.schema.setValue(schema);
        properties.schemaFlow.schema.setValue(schema);
        properties.spParameterTable.parameterTypes.setValue(Arrays.asList(SPParameterTable.ParameterType.IN.name()));
        properties.spParameterTable.schemaColumns.setValue(Arrays.asList("PARAMETER1"));

        JDBCSPSink sink = new JDBCSPSink();

        sink.initialize(null, properties);
        ValidationResult result = sink.validate(null);
        Assert.assertTrue(result.getStatus() == ValidationResult.Result.OK);

        WriteOperation operation = sink.createWriteOperation();
        JDBCSPWriter writer = (JDBCSPWriter) operation.createWriter(null);

        try {
            writer.open("wid");

            IndexedRecord r1 = new GenericData.Record(properties.main.schema.getValue());
            r1.put(0, 0);
            r1.put(1, "wangwei");
            
            writer.write(r1);
            
            List<IndexedRecord> writeResult = writer.getSuccessfulWrites();
            Assert.assertEquals(1, writeResult.size());
            
            IndexedRecord record = writeResult.get(0);
            Assert.assertEquals(Integer.valueOf(0), record.get(0));
            Assert.assertEquals("wangwei", record.get(1));
            
            writer.cleanWrites();
        } finally {
            writer.close();
        }
    }

}
