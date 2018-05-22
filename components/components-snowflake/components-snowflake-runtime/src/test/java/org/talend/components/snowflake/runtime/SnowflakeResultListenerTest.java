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
package org.talend.components.snowflake.runtime;

import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.IndexedRecord;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.talend.components.snowflake.tsnowflakeoutput.TSnowflakeOutputProperties;

import net.snowflake.client.loader.LoadingError;
import net.snowflake.client.loader.Operation;

/**
 * Unit tests for {@link SnowflakeResultListener} class
 */
public class SnowflakeResultListenerTest {

    private SnowflakeResultListener listener;
    private TSnowflakeOutputProperties properties;

    @Before
    public void setup() {

        properties = new TSnowflakeOutputProperties("ouput");
        properties.init();

        listener = new SnowflakeResultListener(properties);
    }

    @Test
    public void testAddErrors() {
        Schema schemaReject = SchemaBuilder.record("record").fields().requiredString(TSnowflakeOutputProperties.FIELD_COLUMN_NAME)
                .requiredString(TSnowflakeOutputProperties.FIELD_ROW_NUMBER)
                .requiredString(TSnowflakeOutputProperties.FIELD_CATEGORY)
                .requiredString(TSnowflakeOutputProperties.FIELD_CHARACTER)
                .requiredString(TSnowflakeOutputProperties.FIELD_ERROR_MESSAGE)
                .requiredString(TSnowflakeOutputProperties.FIELD_BYTE_OFFSET)
                .requiredString(TSnowflakeOutputProperties.FIELD_LINE).requiredString(TSnowflakeOutputProperties.FIELD_SQL_STATE)
                .requiredString(TSnowflakeOutputProperties.FIELD_CODE).endRecord();
        properties.schemaReject.schema.setValue(schemaReject);

        LoadingError error = Mockito.mock(LoadingError.class);
        Mockito.when(error.getProperty(LoadingError.ErrorProperty.COLUMN_NAME)).thenReturn("Column 1");
        Mockito.when(error.getProperty(LoadingError.ErrorProperty.ROW_NUMBER)).thenReturn("Row 1");
        Mockito.when(error.getProperty(LoadingError.ErrorProperty.CATEGORY)).thenReturn("Category");
        Mockito.when(error.getProperty(LoadingError.ErrorProperty.CHARACTER)).thenReturn("Character");
        Mockito.when(error.getProperty(LoadingError.ErrorProperty.ERROR)).thenReturn("Error");
        Mockito.when(error.getProperty(LoadingError.ErrorProperty.BYTE_OFFSET)).thenReturn("Offset 1");
        Mockito.when(error.getProperty(LoadingError.ErrorProperty.LINE)).thenReturn("Line 1");
        Mockito.when(error.getProperty(LoadingError.ErrorProperty.SQL_STATE)).thenReturn("Sql state");
        Mockito.when(error.getProperty(LoadingError.ErrorProperty.CODE)).thenReturn("Code 1");

        listener.addError(error);
        List<IndexedRecord> resultErrorList = listener.getErrors();
        Assert.assertEquals(1, resultErrorList.size());
        Assert.assertEquals("Column 1", resultErrorList.get(0).get(schemaReject.getField(TSnowflakeOutputProperties.FIELD_COLUMN_NAME).pos()));
    }

    @Test
    public void testNeedErrors() {
        Assert.assertTrue(listener.needErrors());
    }

    @Test
    public void testNeedSuccessRecords() {
        Assert.assertFalse(listener.needSuccessRecords());
    }

    @Test
    public void testThrowOnError() {
        Assert.assertFalse(listener.throwOnError());
    }

    @Test
    public void testSetAndGetLastObjects() {
        Object[] record = {"String", 1, 10L, 1.1};
        listener.recordProvided(Operation.INSERT, record);

        Assert.assertArrayEquals(record, listener.getLastRecord());
    }

    @Test
    public void testModifyUpdatedCounter() {
        listener.addOperationRecordCount(Operation.MODIFY, 2);
        Assert.assertEquals(2, listener.updated.get());
        Assert.assertEquals(2, listener.counter.get());
    }

    @Test
    public void testModifyDeletedCounter() {
        listener.addOperationRecordCount(Operation.DELETE, 2);
        Assert.assertEquals(2, listener.deleted.get());
    }

    @Test
    public void testErrorCounter() {
        listener.addErrorCount(10);
        Assert.assertEquals(10, listener.getErrorCount());
        listener.resetErrorCount();
        Assert.assertEquals(0, listener.getErrorCount());
    }

    @Test
    public void testErrorRecordCounter() {
        listener.addErrorRecordCount(10);
        Assert.assertEquals(10, listener.getErrorRecordCount());
        listener.resetErrorRecordCount();
        Assert.assertEquals(0, listener.getErrorRecordCount());
    }

    @Test
    public void testSubmittedRowCounter() {
        listener.addSubmittedRowCount(10);
        Assert.assertEquals(10, listener.getSubmittedRowCount());
        listener.resetSubmittedRowCount();
        Assert.assertEquals(0, listener.getSubmittedRowCount());
    }

    @Test
    public void testProcessedRowCount() {
        listener.addProcessedRecordCount(Operation.MODIFY, 5);
        Assert.assertEquals(5, listener.processed.get());
    }
}
